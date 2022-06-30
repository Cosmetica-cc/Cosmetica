package com.eyezah.cosmetics.utils.textures;

import com.eyezah.cosmetics.mixin.textures.NativeImageAccessorMixin;
import com.eyezah.cosmetics.utils.Debug;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;

public abstract class AnimatedTexture extends AbstractTexture {
	protected NativeImage image;
	protected int frameCounterTicks = 1;

	private int frames = 1;
	private int frameHeight;
	private int frame;
	private int tick;

	protected void setupAnimations() throws IllegalStateException {
		this.frame = 0;
		this.frames = (2 * this.image.getHeight()) / this.image.getWidth();
		this.frameHeight = this.image.getHeight() / this.frames;

		Debug.info("Setting up animations for " + this.frames + " frames");

		if (this.frames <= 0) {
			throw new IllegalStateException("Frames cannot be less than one! If you're not using a cape loaded locally, please contact the Cosmetica devs asap. Debug data: frames=" + this.frames + ",frameHeight=" + this.frameHeight + ",frameDelayTicks=" + this.frameCounterTicks + ",width=" + this.image.getWidth() + ",height=" + this.image.getHeight());
		}
	}

	protected void setupStatic() {
		this.frameHeight = this.image.getHeight();
	}

	protected void upload() {
		TextureUtil.prepareImage(this.getId(), 0, this.image.getWidth(), this.frameHeight);
		this.image.upload(0, 0, 0, 0, this.frameHeight * this.frame, this.image.getWidth(), this.frameHeight, this.blur, false, false, false);
	}

	protected void doTick() {
		if (((NativeImageAccessorMixin) (Object) this.image).getPixels() != 0) {
			this.tick = (this.tick + 1) % this.frameCounterTicks;

			if (this.tick == 0) {
				this.frame = (this.frame + 1) % this.frames;

				this.upload();
			}
		}
	}
}
