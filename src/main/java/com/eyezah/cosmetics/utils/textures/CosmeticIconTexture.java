package com.eyezah.cosmetics.utils.textures;

import com.eyezah.cosmetics.mixin.textures.NativeImageAccessorMixin;
import com.eyezah.cosmetics.utils.Debug;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CosmeticIconTexture extends HttpTexture implements Tickable {
	public CosmeticIconTexture(@Nullable File file, String string) {
		super(file, string, new ResourceLocation("cosmetica", "textures/gui/loading.png"), false, null);
	}

	private int frameHeight;
	private int frames;
	private int frame;
	private int tick;
	private NativeImage image;

	public void firstUpload(NativeImage image, boolean loading) {
		// memory management
		if (this.image != null && ((NativeImageAccessorMixin)(Object)this.image).getPixels() != 0L) {
			//Debug.info("Closing image on thread {} due to load. Are we allowed? {}", Thread.currentThread(), RenderSystem.isOnRenderThreadOrInit());
			this.image.close();
			//Debug.info("Closed image.");
		}

		this.image = image;

		this.frames = image.getHeight() / image.getWidth();
		this.frameHeight = image.getHeight() / this.frames;
		this.frame = 0;

		this.upload(image, !loading);
	}

	public void upload(NativeImage image, boolean close) {
		TextureUtil.prepareImage(this.getId(), 0, image.getWidth(), this.frameHeight);
		image.upload(0, 0, 0, 0, this.frameHeight * this.frame, image.getWidth(), this.frameHeight, this.blur, false, false, close);
	}

	@Override
	public void tick() {
		if (this.frames > 1 && this.image != null && ((NativeImageAccessorMixin) (Object) this.image).getPixels() != 0) {
			this.tick = (this.tick + 1) % 2;

			if (this.tick == 0) {
				this.frame = (this.frame + 1) % this.frames;
				//Debug.info("Uploading frame {}", this.frame);
				this.upload(this.image, false);
			}
		}
	}

	@Override
	public void close() {
		//Debug.info("Closing image on thread {} due to dispose. Are we allowed? {}", Thread.currentThread(), RenderSystem.isOnRenderThreadOrInit());
		this.image.close();
		//Debug.info("Disposed of image.");
	}
}
