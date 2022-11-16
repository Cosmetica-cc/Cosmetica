/*
 * Copyright 2022 EyezahMC
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

import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
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

		DebugMode.log("Setting up animations for " + this.frames + " frames");

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
