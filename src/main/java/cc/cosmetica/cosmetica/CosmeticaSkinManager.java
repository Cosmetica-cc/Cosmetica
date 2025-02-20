/*
 * Copyright 2022, 2023 EyezahMC
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

package cc.cosmetica.cosmetica;

import cc.cosmetica.api.Cape;
import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.utils.textures.AnimatedTexture;
import cc.cosmetica.cosmetica.utils.textures.Base64Texture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private static final MessageDigest SHA1;

	static {
		try {
			SHA1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-1 Hashing not supported by the current Java Configuration.", e);
		}
	}

	public static void clearCaches() {
		DebugMode.log("Clearing cosmetica skin caches");
		textures = new HashMap<>();
		uploaded = new HashSet<>();
	}

	public static boolean isUploaded(ResourceLocation id) {
		synchronized(uploaded) {
			return uploaded.contains(id);
		}
	}

	public static ResourceLocation testId(String id) {
		return ResourceLocation.fromNamespaceAndPath("cosmetica", "test/" + id);
	}

	public static ResourceLocation textureId(String type, String id) {
		return ResourceLocation.fromNamespaceAndPath("cosmetica", type + "/" + pathify(id));
	}

	public static void setTestUploaded(String testId) {
		synchronized (uploaded) {
			uploaded.add(testId(testId));
		}
	}

	public static String pathify(String id) {
		StringBuilder result = new StringBuilder();

		for (char c : id.toCharArray()) {
			if (c == '+') {
				result.append(".");
			}
			else if (c == '=') {
				result.append("__");
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

	public static ResourceLocation processIcon(String base64Texture) {
		return saveTexture(textureId("icon", Base64.encodeBase64String(SHA1.digest(base64Texture.getBytes()))), base64Texture, 50 * 2);
	}

	public static ResourceLocation processModel(Model model) {
		return saveTexture(textureId(model.getType().getUrlString(), model.getId()), model.getTexture(), 50 * ((model.flags() >> 4) & 0x1F));
	}

	public static ResourceLocation processCape(Cape cloak) {
		return saveTexture(textureId("cape", cloak.getId()), cloak.getImage(), cloak.getFrameDelay());
	}

	public static ResourceLocation processSkin(@Nullable String base64Skin, UUID uuid) {
		if (base64Skin == null) {
			return DefaultPlayerSkin.get(uuid).texture();
		}

		return saveTexture(ResourceLocation.fromNamespaceAndPath("cosmetica", "skin/" + uuid.toString().toLowerCase(Locale.ROOT)), base64Skin, 0);
	}

	private static ResourceLocation saveTexture(ResourceLocation id, String texture, int mspf) {
		if (!textures.containsKey(id)) {
			try {
				String type = id.getPath().split("\\/")[0];
				AnimatedTexture tex = createTexture(type, id, texture, mspf);

				if (RenderSystem.isOnRenderThreadOrInit()) {
					Minecraft.getInstance().getTextureManager().register(id, tex);
					synchronized(uploaded) { uploaded.add(id); }
				}
				else {
					RenderSystem.recordRenderCall(() -> {
						Minecraft.getInstance().getTextureManager().register(id, tex);
						synchronized (uploaded) {
							uploaded.add(id);
						}
					});
				}

				DebugMode.log(
						"Registering " + type + " texture for {}. Raw Size: {}x{}, Interpreted Size: {}x{} with {} frames.",
						id,
						tex.getRawImage().getWidth(), tex.getRawImage().getHeight(),
						tex.getRawImage().getWidth(), tex.getFrameHeight(), tex.getFrameCount());
				textures.put(id, tex);
			} catch (IOException e) {
				Cosmetica.LOGGER.error("Error loading texture", e);
				return null;
			}
		}

		return id;
	}

	/**
	 * Creates a potentially animated texture based on the type, raw texture data, id, and milliseconds per frame. Does
	 * not register the texture.
	 * @param type the type of texture to create. This handles how the aspect ratio is handled in loading the texture.
	 *             By default, the texture will be treated as a square tilesheet. There are two special cases:
	 *             "cape" and "skin". Capes are a tilesheet of half-squares, twice as long as they are high. Skins
	 *             on the other hand need special processing due to the two different skin texture formats supported by
	 *             the game.
	 * @param id the id of the texture.
	 * @param texture the raw base64 texture data, including the 22-character header.
	 * @param mspf the number of milliseconds each frame should last for. This is ignored if the texture is not a
	 *                tilesheet of multiple frames.
	 * @return the created texture.
	 * @throws IOException if there is an error reading the texture.
	 */
	private static AnimatedTexture createTexture(String type, ResourceLocation id, String texture, int mspf) throws IOException {
		texture = texture.substring(22);

		if ("cape".equals(type)) {
			return Base64Texture.cape(id, texture, mspf);
		} else if ("skin".equals(type)) {
			return Base64Texture.skin(id, texture);
		} else {
			return Base64Texture.square(id, texture, mspf);
		}
	}
}
