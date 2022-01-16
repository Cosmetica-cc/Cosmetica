package com.eyezah.cosmetics;

import com.eyezah.cosmetics.api.PlayerData;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.eyezah.cosmetics.utils.NamedSingleThreadFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static com.eyezah.cosmetics.Authentication.getToken;
import static com.eyezah.cosmetics.Authentication.runAuthenticationCheckThread;

@Environment(EnvType.CLIENT)
public class Cosmetics implements ClientModInitializer {
	public static final String authServerHost = "auth.cosmetics.eyezah.com";
	public static final int authServerPort = 25596;

	protected static boolean regionSpecificEffects = false;
	protected static boolean doShoulderBuddies = true;
	protected static boolean doHats = true; // todo setting this value
	public static boolean doRegionSpecificEffects() {return regionSpecificEffects;}

	@Override
	public void onInitializeClient() {
		LOGGER.info("<Eyezah> Enjoy the new cosmetics!");
		//LOGGER.info("<Valoeghese> Also try celestine client!"); uncomment this when celestine is released
		runAuthenticationCheckThread();
	}

	public static void onShutdownClient() {
		try {
			LOOKUP_THREAD.shutdownNow();
		} catch (RuntimeException e) { // Just in case.
			e.printStackTrace();
		}
	}

	// used for screens
	public static ConnectScreen connectScreen;
	public static Screen screenStorage;
	public static Options optionsStorage;

	public static void updateParentScreen(Screen screen, Options options) {
		screenStorage = screen;
		optionsStorage = options;
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

	public static boolean doShoulderBuddies() {
		return doShoulderBuddies;
	}

	public static boolean doHats() {
		return doHats;
	}

	public static PlayerData getPlayerData(Player player) {
		return getPlayerData(player.getUUID(), player.getName().getString());
	}

	public static PlayerData getPlayerData(UUID uuid, String username) {
		synchronized (playerDataCache) {
			return playerDataCache.computeIfAbsent(uuid, uid -> {
				if (!lookingUp.contains(uuid)) { // if not already looking up, mark as looking up.
					lookingUp.add(uuid);

					Cosmetics.runOffthread(() -> {
						try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
							System.out.println("https://eyezah.com/cosmetics/api/get/info?username=" + username + "&uuid=" + uuid.toString() + "&token=" + getToken());
							final HttpGet httpGet = new HttpGet("https://eyezah.com/cosmetics/api/get/info?username=" + username + "&uuid=" + uuid.toString() + "&token=" + getToken());
							try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
								String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8).trim();
								JsonParser parser = new JsonParser();
								JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
								JsonObject hat = jsonObject.get("hat").getAsJsonObject();

								synchronized (playerDataCache) { // update the information with what we have gotten.
									playerDataCache.put(uuid, new PlayerData(
											jsonObject.get("lore").getAsString(),
											jsonObject.get("upside-down").getAsBoolean(),
											jsonObject.get("prefix").getAsString(),
											jsonObject.get("suffix").getAsString(),
											jsonObject.get("shoulder-buddy").getAsString(),
											Models.getBakableModel(hat.get("id").getAsString(), () -> jsonObject.get("model").getAsString().getBytes(StandardCharsets.UTF_8), () -> jsonObject.get("texture").getAsString())));
									lookingUp.remove(uuid);
								}
							}
						} catch (IOException | ParseException e) {
							e.printStackTrace();
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

	public static void reloadCosmetics() {
		playerDataCache = new HashMap<>();
	}

	private static Map<UUID, PlayerData> playerDataCache = new HashMap<>();
	private static Set<UUID> lookingUp = new HashSet<>();

	public static final Logger LOGGER = LogManager.getLogger("Cosmetics");
	private static final ExecutorService LOOKUP_THREAD = Executors.newSingleThreadExecutor(new NamedSingleThreadFactory("Cosmetics Lookup Thread"));
}
