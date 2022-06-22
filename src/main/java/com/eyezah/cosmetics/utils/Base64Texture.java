package com.eyezah.cosmetics.utils;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.mixin.textures.NativeImageAccessorMixin;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Texture extends AbstractTexture {
	public Base64Texture(ResourceLocation path, String base64, boolean cape) throws IOException {
		this.base64 = base64; // TODO for large images is it stupid to store this as a string? Should I store it as a direct byte[]? String is just a byte wrapper so perhapsn't
		this.cape = cape;
		this.path = path;

		this.loadBase64();
	}

	private final ResourceLocation path;
	private final boolean cape;
	private final String base64;

	private NativeImage image;

	@Override
	public void load(ResourceManager resourceManager) {
		if (((NativeImageAccessorMixin) (Object) this.image).getPixels() == 0) {
			if (RenderSystem.isOnRenderThreadOrInit()) {
				this.reload();
			} else {
				RenderSystem.recordRenderCall(this::reload);
			}

			return;
		}

		this.upload();
	}

	private void reload() {
		Debug.info("Re-uploading texture {}", this.path);
		try {
			this.loadBase64();
			this.upload();
		} catch (IOException e) {
			Cosmetica.LOGGER.error("Error re-uploading Base64 Texture", e);
		}
	}

	private void loadBase64() throws IOException {
		NativeImage image = loadBase64(base64); // load the image
		this.image = cape ? processBadCapes(image) : image;
	}

	private void upload() {
		TextureUtil.prepareImage(this.getId(), 0, this.image.getWidth(), this.image.getHeight());
		this.image.upload(0, 0, 0, 0, 0, this.image.getWidth(), this.image.getHeight(), this.blur, false, false, true);
	}

    private static NativeImage loadBase64(String base64) throws IOException {
        if(base64.length() < 1000) { //TODO: Tweak this number
            return NativeImage.fromBase64(base64);
        } else {
            //For large images, NativeImage.fromBase64 does not work because it tries to allocate it on the stack and fails
            byte[] bs = Base64.getDecoder().decode(base64.replace("\n", "").getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = MemoryUtil.memAlloc(bs.length);
            buffer.put(bs);
            buffer.rewind();
            NativeImage image = NativeImage.read(buffer);
            MemoryUtil.memFree(buffer);
            return image;
        }
    }

	// bad cape processor

	private static NativeImage processBadCapes(NativeImage image) {
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
					((NativeImageAccessorMixin) (Object) src).getPixels() + (long)m,
					((NativeImageAccessorMixin) (Object) dest).getPixels() + (long)n,
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
}
