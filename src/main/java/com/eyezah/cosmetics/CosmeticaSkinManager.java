package com.eyezah.cosmetics;

import com.eyezah.cosmetics.mixin.textures.MixinNativeImageAccessor;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.NativeTexture;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CosmeticaSkinManager {
	private static Map<ResourceLocation, NativeTexture> capeTextures = new HashMap<>();

	public static void clearCaches() {
		capeTextures = new HashMap<>();
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

	public static ResourceLocation processCape(JsonObject object) {
		ResourceLocation id = new ResourceLocation("cosmetica", pathify(object.get("id").getAsString()));

		if (!capeTextures.containsKey(id)) {
			try {
				NativeTexture cloakTex = new NativeTexture(object.get("texture").getAsString().substring(22));
				RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getTextureManager().register(id, cloakTex));
				capeTextures.put(id, cloakTex);
			} catch (IOException e) {
				Cosmetica.LOGGER.error("Error loading cape texture", e);
				return null;
			}
		}

		return id;
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
