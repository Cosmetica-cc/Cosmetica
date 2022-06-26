package com.eyezah.cosmetics;

import cc.cosmetica.api.Cape;
import com.eyezah.cosmetics.utils.Base64Texture;
import com.eyezah.cosmetics.utils.Debug;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CosmeticaSkinManager {
	private static Map<ResourceLocation, AbstractTexture> textures = new HashMap<>();
	/**
	 * Stores capes that have been both loaded and uploaded.
	 */
	private static Set<ResourceLocation> uploaded = new HashSet<>();

	public static void clearCaches() {
		Debug.info("Clearing cosmetica skin caches");
		textures = new HashMap<>();
		uploaded = new HashSet<>();
	}

	public static boolean isUploaded(ResourceLocation id) {
		synchronized(uploaded) {
			return uploaded.contains(id);
		}
	}

	private static String pathify(String id) {
		StringBuilder result = new StringBuilder();

		for (char c : id.toCharArray()) {
			if (c == '+') {
				result.append(".");
			}
			else if (Character.isUpperCase(c)) {
				result.append("_").append(Character.toLowerCase(c));
			}
			else {
				result.append(c);
			}
		}

		return result.toString();
	}

	public static ResourceLocation processCape(Cape cloak) {
		return saveTexture(new ResourceLocation("cosmetica", "cape/" + pathify(cloak.getId())), cloak.getImage(), cloak.getFrameDelay());
	}

	public static ResourceLocation processSkin(String base64Skin, UUID uuid) {
		return saveTexture(new ResourceLocation("cosmetica", "skin/" + uuid.toString().toLowerCase(Locale.ROOT)), base64Skin, 0);
	}

	private static ResourceLocation saveTexture(ResourceLocation id, String texture, int mspf) {
		if (!textures.containsKey(id)) {
			try {
				String type = id.getPath().split("\\/")[0];
				AbstractTexture tex = type.equals("cape") ? Debug.testCape.orElseGet(() -> {
					try {
						return Base64Texture.cape(id, texture.substring(22), mspf);
					} catch (IOException e) {
						Cosmetica.LOGGER.error("Error loading cape texture", e);
						return null;
					}
				}) : Base64Texture.skin(id, texture.substring(22));

				RenderSystem.recordRenderCall(() -> {
					Minecraft.getInstance().getTextureManager().register(id, tex);
					synchronized(uploaded) { uploaded.add(id); }
				});

				Debug.info("Registering " + type + " texture for {}", id);
				textures.put(id, tex);
			} catch (IOException e) {
				Cosmetica.LOGGER.error("Error loading texture", e);
				return null;
			}
		}

		return id;
	}
}
