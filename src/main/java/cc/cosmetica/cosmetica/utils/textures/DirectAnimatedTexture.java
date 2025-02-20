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
import cc.cosmetica.cosmetica.utils.DebugMode;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class DirectAnimatedTexture extends AnimatedTexture implements Tickable {
	public DirectAnimatedTexture(ResourceLocation debugPath, int aspectRatio, Supplier<NativeImage> image, int animationDelay) {
		super(aspectRatio);
		this.imageSupplier = image;
		this.debugPath = debugPath;

		DebugMode.log("Uploading native texture {}", this.debugPath);

		this.image = this.imageSupplier.get();
		this.frameCounterTicks = animationDelay;

		if (this.image == null) {
			Cosmetica.LOGGER.warn("Sorry texture machine broke for {}", this.debugPath);
		}
		else {
			this.loadImage(this.image);
		}
	}

	private final ResourceLocation debugPath;
	private final Supplier<NativeImage> imageSupplier;

	@Override
	protected void setupAnimations() throws IllegalStateException {
		super.setupAnimations();
	}

	@Override
	public void tick() {
		this.doTick();
	}
}
