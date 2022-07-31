package cc.cosmetica.cosmetica.utils.textures;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
import cc.cosmetica.cosmetica.utils.Debug;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Texture extends AnimatedTexture {
	private Base64Texture(ResourceLocation path, String base64, NativeImage initialImage, boolean cape) throws IOException {
		this.base64 = base64;
		this.cape = cape;
		this.path = path;

		this.loadImage(initialImage);
	}

	private final ResourceLocation path;
	private final boolean cape;
	private final String base64;

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
			this.loadImage(loadBase64(this.base64)); // load the image
			this.upload();
		} catch (IOException e) {
			Cosmetica.LOGGER.error("Error re-uploading Base64 Texture", e);
		}
	}

	private void loadImage(NativeImage image) {
		this.image = image;

		if (this.cape) {
			this.setupAnimations();
		}
		else {
			this.setupStatic();
		}
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

	public static Base64Texture cape(ResourceLocation path, String base64, int frameDelayMs) throws IOException {
		NativeImage image = loadBase64(base64);

		if (image.getHeight() >= image.getWidth()) {
			return new TickingCape(path, base64, image, frameDelayMs);
		}
		else {
			return new Base64Texture(path, base64, image, true);
		}
	}

	public static Base64Texture skin(ResourceLocation path, String base64) throws IOException {
		return new Base64Texture(path, base64, loadBase64(base64), false);
	}

	private static class TickingCape extends Base64Texture implements Tickable {
		private TickingCape(ResourceLocation path, String base64, NativeImage initialImage, int frameDelayMs) throws IOException {
			super(path, base64, initialImage, true);
			this.frameCounterTicks = Math.max(1, frameDelayMs / 50);
		}

		@Override
		public void tick() {
			this.doTick();
		}
	}
}
