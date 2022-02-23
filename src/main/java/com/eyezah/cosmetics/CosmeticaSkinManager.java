package com.eyezah.cosmetics;

import com.eyezah.cosmetics.mixin.textures.MixinNativeImageAccessor;
import com.eyezah.cosmetics.mixin.textures.MixinPlayerInfoAccessor;
import com.eyezah.cosmetics.mixin.textures.MixinYggdrasilAuthenticationServiceInvoker;
import com.eyezah.cosmetics.utils.Debug;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.system.MemoryUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CosmeticaSkinManager {
	// caches
	/**
	 * A cache of profiles that have been stored this session of caches-not-being-cleared. Associates UUIDS to cosmetica's GameProfiles. A value may be null if it's being computed or is unretrievable.
	 */
	private static Map<UUID, GameProfile> cosmeticaProfileCache = new HashMap<>();
	/**
	 * A cache of profiles to update once computations are done. If a game profile has been marked as unretrievable or has an associated game profile, this should have no associated value for the UUID. If it did, it would cause a memory leak.
	 */
	private static Map<UUID, List<PlayerInfo>> toUpdateProfiles = new HashMap<>();
	/**
	 * Cache of uuids that were unable to have associated profiles retrieved properly from cosmetica. This will probably just have NPC uuids. Perhaps an optimisation can be made for this in the future.
	 */
	private static Set<UUID> unretrievable = new HashSet<>();

	public static void clearCaches() {
		synchronized (cosmeticaProfileCache) {
			// letting the garbage collector do the work
			cosmeticaProfileCache = new HashMap<>();
			toUpdateProfiles = new HashMap<>();
			unretrievable = new HashSet<>();
		}
	}

	public static void removePlayer(UUID uuid) {
		synchronized (cosmeticaProfileCache) {
			// letting the garbage collector do the work
			cosmeticaProfileCache.remove(uuid);
			toUpdateProfiles.remove(uuid);
			unretrievable.remove(uuid);
		}
	}

	public static String[] getCacheData() {
		synchronized (cosmeticaProfileCache) {
			return new String[] {"Cached:" + cosmeticaProfileCache.size(), "ToUpdate:" + toUpdateProfiles.size(), "Unretrievable:" + unretrievable.size()};
		}
	}

	public static String[] getCacheData(UUID uuid) {
		synchronized (cosmeticaProfileCache) {
			if (cosmeticaProfileCache.containsKey(uuid)) {
				if (unretrievable.contains(uuid)) {
					return new String[] { "Unretrievable." };
				}

				var unupdatedInfos = toUpdateProfiles.get(uuid);
				return new String[]{"Cached:" + cosmeticaProfileCache.get(uuid), "ToUpdate:" + (unupdatedInfos == null ? 0 : unupdatedInfos.size())};
			} else {
				return new String[] {"Not Stored."};
			}
		}
	}

	public static int getCacheSize() {
		synchronized (cosmeticaProfileCache) {
			return cosmeticaProfileCache.size();
		}
	}

	public static void modifyServerGameProfiles(PlayerInfo info, GameProfile existing, YggdrasilMinecraftSessionService ygg) {
		UUID uuid = existing.getId();

		if (Cosmetica.isProbablyNPC(uuid)) return;

		synchronized (cosmeticaProfileCache) {
			if (cosmeticaProfileCache.containsKey(uuid)) { // if the uuid is already stored...
				GameProfile profile = cosmeticaProfileCache.get(uuid);

				if (profile == null) { // if it's still calculating or unretrievable, we don't want to update the profile
					if (!unretrievable.contains(uuid)) {
						toUpdateProfiles.computeIfAbsent(uuid, $ -> new ArrayList<>(4)).add(info); // add it to the update queue for when lookup is done
					}
				} else {
					updateProfile(info, profile);
				}

				return; // don't calculate again
			}

			cosmeticaProfileCache.put(existing.getId(), null); // dummy.
		}

		MixinYggdrasilAuthenticationServiceInvoker yggi = (MixinYggdrasilAuthenticationServiceInvoker) ygg.getAuthenticationService();

		// run this on the skin thread because it destroys your server connection to keep it on the network thread
		Cosmetica.runOffthread(() -> {
			URL url = CosmeticaSkinManager.getCosmeticaURL(null, existing, false);

			if (url != null) {
				try {
					Debug.checkedInfo(url.toString(), "always_print_urls");
					CosmeticaSkinManager.CosmeticaProfilePropertiesResponse cmaResponse = yggi.invokeMakeRequest(url, null, CosmeticaSkinManager.CosmeticaProfilePropertiesResponse.class);
					String mojangSkin = cmaResponse.getOriginalSkin();

					if (mojangSkin != null) {
						// A ton of nonsense to not use our textures if the server is replacing the skin.
						// starting with this code to get the original skin
						for (Property property : existing.getProperties().get("textures")) {
							String decoded = new String(Base64.decodeBase64(property.getValue()), StandardCharsets.UTF_8).trim();

							JsonObject jo = JsonParser.parseString(decoded).getAsJsonObject();
							String originalSkinURL = jo.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();

							// and finally checking it against our "actual skin" requirement

							if (mojangSkin.equals(originalSkinURL)) {
								Minecraft.getInstance().tell(() -> {
									// now we can replace with our game profile.
									GameProfile cosmeticaProfile = new GameProfile(cmaResponse.getId(), cmaResponse.getName());
									cosmeticaProfile.getProperties().putAll(cmaResponse.getProperties());
									// set our one
									Debug.info("Force Re-Registering Textures for {}", cosmeticaProfile.getName());

									updateProfile(info, cosmeticaProfile);

									// update those that were queued
									synchronized (cosmeticaProfileCache) {
										cosmeticaProfileCache.put(uuid, cosmeticaProfile);
										List<PlayerInfo> toUpdate = toUpdateProfiles.remove(uuid);

										if (toUpdate != null) {
											Debug.info("Force Re-Registering Textures for other player info of {}", cosmeticaProfile.getName());

											for (PlayerInfo updating : toUpdate) {
												updateProfile(updating, cosmeticaProfile);
											}
										}
									}
								});

								return; // don't remove it in the memory leak preventer
							}

							break; // only need one?
						}
					} else Debug.info("Mojang skin is null for {}", existing.getName());
				} catch (Exception e) {
					Cosmetica.LOGGER.error("Error adding cosmetica skin service to multiplayer skins", e);
				}
			}

			// stop memory leak
			synchronized (cosmeticaProfileCache) {
				//Debug.info("Discarded uuid of version {}", uuid.version());
				toUpdateProfiles.remove(uuid);
				unretrievable.add(uuid);
			}
		}, ThreadPool.SKIN_THREADS);
	}

	private static void updateProfile(PlayerInfo info, GameProfile profile) {
		MixinPlayerInfoAccessor info_ = (MixinPlayerInfoAccessor) info;
		info_.setProfile(profile);
		info_.setPendingTextures(false);
		info_.getTextureLocations().clear(); // it doesn't clear it already, so clear it
		info_.invokeRegisterTextures();
	}

	public static class CosmeticaProfilePropertiesResponse extends MinecraftProfilePropertiesResponse {
		private String originalSkin; // part of the cosmetica response.

		public String getOriginalSkin() {
			return this.originalSkin;
		}
	}

	public static URL getCosmeticaURL(URL fallback, final GameProfile profile, final boolean requireSecure) {
		String requestEndpoint = Cosmetica.apiServerHost + "/get/textures?";

		if (profile.isComplete()) {
			requestEndpoint += "uuid=" + profile.getId().toString() + "&username=" + profile.getName();
		} else if (profile.getId() == null) {
			requestEndpoint += "username=" + profile.getName();
		} else {
			requestEndpoint += "uuid=" + profile.getId();
		}

		try {
			requestEndpoint += "&token=" + Authentication.getToken() + "&timestamp=" + System.currentTimeMillis();
			return new URL(requestEndpoint);
		} catch (MalformedURLException e) {
			Cosmetica.LOGGER.error("Malformed URL on redirecting skin server to cosmetica???", e);
			return fallback;
		}
	}

	public static NativeImage processBadCapes(NativeImage image) {
		int srcWidth = image.getWidth();
		int srcHeight = image.getHeight();

		if (srcWidth != srcHeight * 2) {
			// very bad format from a very bad server
			int height = 32;
			int scaleFactor = 1;

			// get the right dimensions
			while (height < srcHeight) {
				height *= 2;
				scaleFactor *= 2;
			}

			// copy and dump dest
			NativeImage result = new NativeImage(height * 2, height, true);
			copyImage(image, result);

			// ancient cape format
			if (17 * srcWidth == 22 * srcHeight) {
				// time to make an elytra
				copyRect(result, 1, 1, 35, 4, 11, 16, scaleFactor);
				// on right
				clearRect(result, 44, 4, 2, 3, scaleFactor);
				clearRect(result, 45, 7, 1, 4, scaleFactor);
				// on left
				clearRect(result, 35, 16, 2, 4, scaleFactor);
				clearRect(result, 35, 11, 1, 5, scaleFactor);
				clearRect(result, 37, 19, 1, 1, scaleFactor);
				// expand below
				copyRect(result, 38, 19, 38, 20, 8, 1, scaleFactor);
				copyRect(result, 39, 19, 39, 21, 7, 1, scaleFactor);
				// expand above
				copyRect(result, 35, 4, 35, 3, 8, 1, scaleFactor);
				copyRect(result, 35, 4, 35, 2, 7, 1, scaleFactor);
				// the top bit thing
				copyRect(result, 12, 0, 31, 0 , 9, 1, scaleFactor);
				copyRect(result, 32, 0, 32, 1 , 2, 1, scaleFactor);
				copyRect(result, 35, 2, 34, 2, 2, 2, scaleFactor);
				// the edge bit
				copyRect(result, 45, 11, 22, 11 , 1, 11, scaleFactor);
			}

			// debug stuff
			String imgid = "cape_" + System.currentTimeMillis();
			Debug.dumpImages(imgid + "_src", true, image);
			Debug.dumpImages(imgid + "_formatted", true, result);

			image.close();
			return result;
		} else {
			return image;
		}
	}

	// fixed version of mojank code
	private static void copyImage(NativeImage src, NativeImage dest) {
		int width = Math.min(dest.getWidth(), src.getWidth());
		int height = Math.min(dest.getHeight(), src.getHeight());
		int bytesPerPixel = src.format().components();

		for(int l = 0; l < height; ++l) {
			int m = l * src.getWidth() * bytesPerPixel;
			int n = l * dest.getWidth() * bytesPerPixel;
			MemoryUtil.memCopy(
					((MixinNativeImageAccessor) (Object) src).getPixels() + (long)m,
					((MixinNativeImageAccessor) (Object) dest).getPixels() + (long)n,
					width * bytesPerPixel);
		}
	}

	private static void copyRect(NativeImage img, int srcX, int srcY, int destX, int destY, int width, int height, int scale) {
		srcX *= scale;
		srcY *= scale;

		destX *= scale;
		destY *= scale;

		width *= scale;
		height *= scale;

		for(int dx = 0; dx < width; ++dx) {
			for(int dy = 0; dy < height; ++dy) {
				int colour = img.getPixelRGBA(srcX + dx, srcY + dy);
				img.setPixelRGBA(destX + dx, destY + dy, colour);
			}
		}
	}

	private static void clearRect(NativeImage img, int x, int y, int width, int height, int scale) {
		x *= scale;
		y *= scale;

		width *= scale;
		height *= scale;

		for(int dx = 0; dx < width; ++dx) {
			for(int dy = 0; dy < height; ++dy) {
				img.setPixelRGBA(x + dx, y + dy, 0);
			}
		}
	}

	private record YggdrasilGameProfile(GameProfile gp, YggdrasilMinecraftSessionService ss) {
	}
}
