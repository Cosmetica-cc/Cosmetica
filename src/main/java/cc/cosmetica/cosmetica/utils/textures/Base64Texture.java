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

package cc.cosmetica.cosmetica.utils.textures;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
import cc.cosmetica.cosmetica.utils.DebugMode;
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
	private Base64Texture(ResourceLocation path, String base64, NativeImage initialImage, int aspectRatio) throws IOException {
		super(aspectRatio);
		this.base64 = base64;
		this.path = path;

		this.loadImage(initialImage);
	}

	private final ResourceLocation path;
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
		DebugMode.log("Re-uploading texture {}", this.path);
		try {
			this.loadImage(loadBase64(this.base64)); // load the image
			this.upload();
		} catch (IOException e) {
			Cosmetica.LOGGER.error("Error re-uploading Base64 Texture", e);
		}
	}

	private void loadImage(NativeImage image) {
		this.image = image;

		if (this.isAnimatable()) {
			this.setupAnimations();
		}
		else {
			this.setupStatic();
		}
	}

    private static NativeImage loadBase64(String base64) throws IOException {
		// fromBase64 was removed in 1.19.4
//        if(base64.length() < 1000) { //TODO: Tweak this number
//            return NativeImage.read(base64);
//        } else {
            //For large images, NativeImage.fromBase64 does not work because it tries to allocate it on the stack and fails
            byte[] bs = Base64.getDecoder().decode(base64.replace("\n", "").getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = MemoryUtil.memAlloc(bs.length);
            buffer.put(bs);
            buffer.rewind();
            NativeImage image = NativeImage.read(buffer);
            MemoryUtil.memFree(buffer);
            return image;
//        }
    }

	public static Base64Texture square(ResourceLocation path, String base64, int frameDelayMs) throws IOException {
		NativeImage image = loadBase64(base64);

		if (image.getHeight() > image.getWidth()) {
			return new TickingCape(path, base64, image, frameDelayMs, 1);
		}
		else {
			return new Base64Texture(path, base64, image, 0);
		}
	}

	public static Base64Texture cape(ResourceLocation path, String base64, int frameDelayMs) throws IOException {
		NativeImage image = loadBase64(base64);

		if (image.getHeight() >= image.getWidth()) {
			return new TickingCape(path, base64, image, frameDelayMs, 2);
		}
		else {
			return new Base64Texture(path, base64, image, 0);
		}
	}

	public static Base64Texture skin(ResourceLocation path, String base64) throws IOException {
		return new Base64Texture(path, base64, loadBase64(base64), 0);
	}

	private static class TickingCape extends Base64Texture implements Tickable {
		private TickingCape(ResourceLocation path, String base64, NativeImage initialImage, int frameDelayMs, int aspectRatio) throws IOException {
			super(path, base64, initialImage, aspectRatio);
			this.frameCounterTicks = Math.max(1, frameDelayMs / 50);
		}

		@Override
		public void tick() {
			this.doTick();
		}
	}
}
