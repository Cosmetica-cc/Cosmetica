package com.eyezah.cosmetics;

import com.eyezah.cosmetics.api.PlayerData;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.NamedThreadFactory;
import com.eyezah.cosmetics.utils.Response;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.eyezah.cosmetics.Authentication.getToken;
import static com.eyezah.cosmetics.Authentication.runAuthenticationCheckThread;

@Environment(EnvType.CLIENT)
public class Cosmetics implements ClientModInitializer {
	public static final String authServerHost = "auth.cosmetics.eyezah.com";
	public static final int authServerPort = 25596;

	// used for screens
	public static ConnectScreen connectScreen;

	private static Map<UUID, PlayerData> playerDataCache = new HashMap<>();
	private static Set<UUID> lookingUp = new HashSet<>();

	public static final Logger LOGGER = LogManager.getLogger("Cosmetics");
	private static final ExecutorService LOOKUP_THREAD = Executors.newFixedThreadPool(
			Integer.parseInt(System.getProperty("cosmetics.lookupThreads", "3")),
			new NamedThreadFactory("Cosmetics Lookup Thread"));

	@Override
	public void onInitializeClient() {
		LOGGER.info("<Eyezah> Enjoy the new cosmetics!");

		// delete debug dump images
		Debug.clearImages();

		// only print "also try celestine client" once it's out
		// will simplify the code to just the log once celestine client is out since this is only necessary for when cosmetics is out before celestine
		runOffthread(() -> {
			try (Response response = Response.request("https://raw.githubusercontent.com/BenzeneStudios/Celestine-Installer/master/release_data.json?timestamp=" + System.currentTimeMillis())) {
				if (response.getError().isEmpty()) {
					JsonObject object = response.getAsJson();

					if (!object.get("latest_version").getAsString().isEmpty()) {
						LOGGER.info("<Valoeghese> Also try celestine client!");
					}
				}
			} catch (IOException e) {
				if (Debug.DEBUG_MODE) e.printStackTrace();
			}
		});

		ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((atlasTexture, registry) -> {
			// register all reserved textures
			for (int i = 0; i < 128; ++i) {
				registry.register(new ResourceLocation("extravagant_cosmetics", "generated/reserved_" + i));
			}
		});

		// make sure it clears relevant caches on resource reload
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation("extravagant_cosmetics", "cache_clearer");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				Models.resetTextureBasedCaches(); // reset only the caches that need to be reset after a resource reload
			}
		});

		runAuthenticationCheckThread();
	}

	public static void onShutdownClient() {
		try {
			LOOKUP_THREAD.shutdownNow();
		} catch (RuntimeException e) { // Just in case.
			e.printStackTrace();
		}
	}

	// example fabristation connection
	public static String getFabriStationActivity() {
		System.out.println("running activity check");
		if (FabricLoader.getInstance().isModLoaded("fabristation")) {
			System.out.println("Station is loaded");
			//return "connected";
			return FabriStationConnector.getFormatted();
		} else {
			System.out.println("Station isn't loaded");
			return "not connected";
		}
	}

	// =================
	//       IMPL
	// =================

	public static void runOffthread(Runnable runnable) {
		LOOKUP_THREAD.execute(runnable);
	}

	public static boolean shouldRenderUpsideDown(Player player) {
		return getPlayerData(player).upsideDown();
	}

	public static PlayerData getPlayerData(Player player) {
		return getPlayerData(player.getUUID(), player.getName().getString());
	}

	public static int getCacheSize() {
		return playerDataCache.size();
	}

	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	public static PlayerData getPlayerData(UUID uuid, String username) {
		synchronized (playerDataCache) {
			return playerDataCache.computeIfAbsent(uuid, uid -> {
				if (!lookingUp.contains(uuid)) { // if not already looking up, mark as looking up and look up.
					lookingUp.add(uuid);

					Cosmetics.runOffthread(() -> {
						String target = "https://eyezah.com/cosmetics/api/get/info?username=" + urlEncode(username)
								+ "&uuid=" + uuid.toString() + "&token=" + getToken();
						Debug.info(target, "always_print_urls");

						try (Response response = Response.request(target)) {
							JsonObject jsonObject = response.getAsJson();
							JsonObject hat = jsonObject.has("hat") ? jsonObject.get("hat").getAsJsonObject() : null;
							JsonObject shoulderBuddy = jsonObject.has("shoulder-buddy") ? jsonObject.get("shoulder-buddy").getAsJsonObject() : null;

							synchronized (playerDataCache) { // update the information with what we have gotten.
								playerDataCache.put(uuid, new PlayerData(
										jsonObject.get("lore").getAsString(),
										jsonObject.get("upside-down").getAsBoolean(),
										jsonObject.get("prefix").getAsString(),
										jsonObject.get("suffix").getAsString(),
										hat == null ? null : Models.createBakableModel(hat),
										shoulderBuddy == null ? null : Models.createBakableModel(shoulderBuddy)
								));

								lookingUp.remove(uuid);
							}
						} catch (IOException | ParseException e) {;
							new RuntimeException("Error connecting to " + target, e).printStackTrace();
						}
					});
				}

				return new PlayerData(); // temporary name: blank.
			});
		}
	}

	public static void onRenderNameTag(EntityRenderDispatcher entityRenderDispatcher, Entity entity, PoseStack matrixStack, MultiBufferSource multiBufferSource, Font font, int i) {
		//if (entity instanceof RemotePlayer && CosmeticsAPI.isPlayerLoreEnabled()) {
		if (entity instanceof Player player) {
			//RemotePlayer player = (RemotePlayer) entity;
			UUID lookupId = player.getUUID();

			if (lookupId != null) {
				double d = entityRenderDispatcher.distanceToSqr(entity);

				if (!(d > 4096.0D)) {
					String lore = getPlayerData(lookupId, player.getName().getString()).lore();

					if (!lore.equals("")) {
						Component component = new TextComponent(lore);

						boolean bl = !entity.isDiscrete();
						float height = entity.getBbHeight() + 0.25F;

						matrixStack.pushPose();
						matrixStack.translate(0.0D, height, 0.0D);
						matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
						matrixStack.scale(-0.025F, -0.025F, 0.025F);
						matrixStack.scale(0.75F, 0.75F, 0.75F);
						Matrix4f model = matrixStack.last().pose();

						@SuppressWarnings("resource")
						float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
						int alphaARGB = (int)(backgroundOpacity * 255.0F) << 24;

						float xOffset = (float)(-font.width(component) / 2);
						font.drawInBatch(component, xOffset, 0, 553648127, false, model, multiBufferSource, bl, alphaARGB, i);

						if (bl) {
							font.drawInBatch(component, xOffset, 0, -1, false, model, multiBufferSource, false, 0, i);
						}

						matrixStack.popPose();
					}
				}
			}
		}
	}

	public static void clearAllCaches() {
		Debug.info("Clearing all Cosmetics Caches");
		playerDataCache = new HashMap<>();
		Models.resetCaches();
	}
}
