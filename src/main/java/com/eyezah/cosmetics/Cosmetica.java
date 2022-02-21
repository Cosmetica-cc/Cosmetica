package com.eyezah.cosmetics;

import com.eyezah.cosmetics.api.PlayerData;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.NamedThreadFactory;
import com.eyezah.cosmetics.utils.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
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

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.eyezah.cosmetics.Authentication.getToken;
import static com.eyezah.cosmetics.Authentication.runAuthenticationCheckThread;

@Environment(EnvType.CLIENT)
public class Cosmetica implements ClientModInitializer {
	// used for screens
	public static ConnectScreen connectScreen;

	public static String authServerHost;
	public static int authServerPort;
	public static String apiServerHost;
	public static String displayNext;
	public static String websiteHost;

	private static Map<UUID, PlayerData> playerDataCache = new HashMap<>();
	private static Set<UUID> lookingUp = new HashSet<>();

	public static final Logger LOGGER = LogManager.getLogger("Cosmetica");
	private static final ExecutorService LOOKUP_THREAD = Executors.newFixedThreadPool(
			Integer.parseInt(System.getProperty("cosmetica.lookupThreads", "3")),
			new NamedThreadFactory("Cosmetics Lookup Thread"));

	@Override
	public void onInitializeClient() {
		// delete debug dump images
		Debug.clearImages();

		// API Url Getter
		runOffthread(() -> {
			File minecraftDir = findDefaultInstallDir("minecraft").toFile();
			File file = new File(minecraftDir, "cosmetica_website_host_cache.txt");

			String apiGetHost = null;

			try (Response response = Response.request("https://raw.githubusercontent.com/EyezahMC/Cosmetica/master/api_provider_host.json?timestamp=" + System.currentTimeMillis())) {
				if (response.getError().isEmpty()) {
					Debug.info("Received response from Github CDN. We do not require a fallback (hopefully)!");

					apiGetHost = response.getAsJson().get("current_host").getAsString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			apiGetHost = loadOrCache(file, apiGetHost);
			if (apiGetHost == null) apiGetHost = "https://cosmetica.cc/getapi"; // fallback

			Debug.info("Finished retrieving GetAPI Url. Conclusion: we can get an API server from " + apiGetHost);

			file = new File(minecraftDir, "cosmetica_get_api_cache.json");

			String apiGetData = null;

			try (Response apiGetResponse = Response.request(apiGetHost)) {
				apiGetData = apiGetResponse.getAsString();

				Debug.info("Received API Url from Server");
			} catch (Exception e) {
				LOGGER.warn("Connection error to API GET. Trying to retrieve from local cache...");
			}

			apiGetData = loadOrCache(file, apiGetData);

			if (apiGetData == null) {
				LOGGER.error("Could not receive Cosmetica API host. Mod functionality will be disabled!");
			} else {
				try {
					JsonObject data = new JsonParser().parse(apiGetData).getAsJsonObject();
					Cosmetica.apiServerHost = data.get("api").getAsString();
					Cosmetica.websiteHost = data.get("website").getAsString();
					JsonObject auth = data.get("auth-server").getAsJsonObject();
					Cosmetica.authServerHost = auth.get("hostname").getAsString();
					Cosmetica.authServerPort = auth.get("port").getAsInt();

					LOGGER.info(data.get("message"));
					Debug.info("Finished retrieving API Url. Conclusion: the API should be contacted at " + Cosmetica.apiServerHost);

					Authentication.runAuthentication(new TitleScreen(), 1);

					String versionCheck = Cosmetica.apiServerHost + "/get/versioncheck?modversion="
							+ urlEncode(FabricLoader.getInstance().getModContainer("cosmetica").get().getMetadata().getVersion().getFriendlyString())
							+ "&mcversion=" + SharedConstants.getCurrentVersion().getId();

					Debug.info(versionCheck, "always_print_urls");

					try (Response response = Response.request(versionCheck)) {
						String s = response.getAsString();

						if (!s.isEmpty()) {
							displayNext = s;
						}
					} catch (IOException e) {
						LOGGER.error("Error checking version:");
						e.printStackTrace();
					}
				} catch (Exception e) {
					LOGGER.error("Error reading JSON data for API Url. Mod functionality will be disabled!");
					e.printStackTrace();
				}
			}
		});

		ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((atlasTexture, registry) -> {
			// register all reserved textures
			for (int i = 0; i < 128; ++i) {
				registry.register(new ResourceLocation("cosmetica", "generated/reserved_" + i));
			}
		});

		// make sure it clears relevant caches on resource reload
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation("cosmetica", "cache_clearer");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				Models.resetTextureBasedCaches(); // reset only the caches that need to be reset after a resource reload
			}
		});

		runAuthenticationCheckThread();
	}

	private static String loadOrCache(File file, @Nullable String value) {
		try {
			if (value != null) {
				file.createNewFile();

				try (FileWriter writer = new FileWriter(file)) {
					writer.write(value);
				}
			} else if (file.isFile()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					value = reader.readLine().trim();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	/*
	 * Adapted from code at https://github.com/FabricMC/fabric-installer
	 * Original license has been preserved for this method.
	 *
	 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	private static Path findDefaultInstallDir(String application) {
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		Path dir;

		if (os.contains("win") && System.getenv("APPDATA") != null) {
			dir = Paths.get(System.getenv("APPDATA")).resolve("." + application);
		} else {
			String home = System.getProperty("user.home", ".");
			Path homeDir = Paths.get(home);

			if (os.contains("mac")) {
				dir = homeDir.resolve("Library").resolve("Application Support").resolve(application);
			} else {
				dir = homeDir.resolve("." + application);
			}
		}

		return dir.toAbsolutePath().normalize();
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

	public static void clearPlayerData(UUID uuid) {
		playerDataCache.remove(uuid);
	}

	public static int getCacheSize() {
		return playerDataCache.size();
	}

	public static String urlEncode(String value) {
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

					Cosmetica.runOffthread(() -> {
						String target = Cosmetica.apiServerHost + "/get/info?username=" + urlEncode(username)
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
		if (entity instanceof Player player) {
			UUID lookupId = player.getUUID();

			if (lookupId != null) {
				double d = entityRenderDispatcher.distanceToSqr(entity);

				if (!(d > 4096.0D)) {
					String lore = getPlayerData(lookupId, player.getName().getString()).lore();

					if (!lore.equals("")) {
						Component component = new TextComponent(lore);

						boolean bl = !entity.isDiscrete();
						float height = entity.getBbHeight() + 0.25F;

						matrixStack.translate(0, 0.1, 0);

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
		Debug.info("Clearing all Cosmetica Caches");
		playerDataCache = new HashMap<>();
		Models.resetCaches();
	}
}
