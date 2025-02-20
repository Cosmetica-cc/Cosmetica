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

import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
import cc.cosmetica.cosmetica.utils.DebugMode;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;

public abstract class AnimatedTexture extends AbstractTexture {
	public AnimatedTexture(int aspectRatio) {
		this.aspectRatio = aspectRatio;
	}

	protected NativeImage image;
	protected int frameCounterTicks = 1;

	protected final int aspectRatio;
	private int frames = 1;
	private int frameHeight;
	private int frame;
	private int tick;

	protected void loadImage(NativeImage image) {
		this.image = image;

		if (this.isAnimatable()) {
			this.setupAnimations();
		}
		else {
			this.setupStatic();
		}

		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(this::upload);
		} else {
			this.upload();
		}
	}

	protected void setupAnimations() throws IllegalStateException {
		if (!this.isAnimatable()) {
			throw new IllegalStateException("Not an animatable texture but setupAnimations() was called!");
		}

		this.frame = 0;
		this.frames = (this.aspectRatio * this.image.getHeight()) / this.image.getWidth();
		this.frameHeight = this.image.getHeight() / this.frames;

		DebugMode.log("Setting up animations for " + this.frames + " frames");

		if (this.frames <= 0) {
			throw new IllegalStateException("Frames cannot be less than one! If you're not using a cape loaded locally, please contact the Cosmetica devs asap. Debug data: frames=" + this.frames + ",frameHeight=" + this.frameHeight + ",frameDelayTicks=" + this.frameCounterTicks + ",width=" + this.image.getWidth() + ",height=" + this.image.getHeight());
		}
	}

	private void setupStatic() {
		this.frameHeight = this.image.getHeight();
	}

	protected void upload() {
		TextureUtil.prepareImage(this.getId(), 0, this.image.getWidth(), this.frameHeight);
		this.image.upload(0, 0, this.frameHeight * this.frame, 0, 0, this.image.getWidth(), this.frameHeight, false);
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

	/**
	 * Get the raw image being used by this animated texture, including all frames.
	 */
	public NativeImage getRawImage() {
		return this.image;
	}

	public boolean isAnimatable() {
		return this.aspectRatio > 0;
	}

	public int getFrameHeight() {
		return this.isAnimatable() ? this.frameHeight : this.image.getHeight();
	}

	public int getFrameCount() {
		return this.frames;
	}

	@Override
	public String toString() {
		return "AnimatedTexture{" +
				"image=" + image +
				", frameCounterTicks=" + frameCounterTicks +
				", aspectRatio=" + aspectRatio +
				", frames=" + frames +
				", frameHeight=" + frameHeight +
				", frame=" + frame +
				", tick=" + tick +
				'}';
	}
	// do we have a memory leak? should I be closing the image on close()?
	// close image on close

	@Override
	public void close() {
		if (this.image != null) {
			this.image.close();
		}
	}
}
