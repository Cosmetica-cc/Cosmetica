package com.eyezah.cosmetics;

import com.eyezah.cosmetics.utils.Response;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cosmetics implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LOGGER.info("<Eyezah> Enjoy the new cosmetics!");
		LOGGER.info("<Valoeghese> Also try celestine client!");
	}

	public static void onShutdownClient() {
		try {
			LOOKUP_THREAD.shutdownNow();
		} catch (RuntimeException e) { // Just in case.
			e.printStackTrace();
		}
	}

	// =================
	//       IMPL
	// =================

	public static void runAsyncLookup(Runnable runnable) {
		LOOKUP_THREAD.execute(runnable);
	}

	@Nullable
	public static String getPlayerLore(UUID uuid) {
		return CosmeticsAPI.lookUp(uuid, lookingUpLore, loreCache, Cosmetics::lookUpLore);
	}

	public static void onRenderNameTag(EntityRenderDispatcher entityRenderDispatcher, Entity entity, PoseStack matrixStack, MultiBufferSource multiBufferSource, Font font, int i) {
		if (entity instanceof RemotePlayer player && CosmeticsAPI.isPlayerLoreEnabled()) {
			UUID lookupId = player.getUUID();

			if (lookupId != null) {
				double d = entityRenderDispatcher.distanceToSqr(entity);

				if (!(d > 4096.0D)) {
					String lore = Cosmetics.getPlayerLore(lookupId);

					if (!lore.isEmpty()) {
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

	private static String lookUpLore(UUID uuid) {
		try {
			Response response = Response.request("http://development.eyezah.com:9999/isaiah/comedy-uuids/get.php?uuid=" + uuid);
			OptionalInt error = response.getError();

			if (error.isPresent()) {
				return FabricLoader.getInstance().isDevelopmentEnvironment() ? "Error " + error.getAsInt() + " " + error.getAsInt() : "";
			} else {
				return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			return FabricLoader.getInstance().isDevelopmentEnvironment() ? e.toString() : "";
		}
	}

	private static Map<UUID, String> loreCache = new HashMap<>();
	private static Set<UUID> lookingUpLore = new HashSet<>();

	public static final Logger LOGGER = LogManager.getLogger("Cosmetics");
	private static final ExecutorService LOOKUP_THREAD = Executors.newSingleThreadExecutor();
}
