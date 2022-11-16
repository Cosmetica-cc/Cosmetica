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

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Supplier;

public class LocalCapeTexture extends AnimatedTexture implements Tickable {
	public LocalCapeTexture(ResourceLocation debugPath, Supplier<NativeImage> image) {
		this.imageSupplier = image;
		this.debugPath = debugPath;

		DebugMode.log("Uploading native texture {}", this.debugPath);

		this.image = this.imageSupplier.get();

		if (this.image == null) {
			Cosmetica.LOGGER.warn("Sorry texture machine broke for {}", this.debugPath);
		}
		else {
			this.setupAnimations();
		}
	}

	private final ResourceLocation debugPath;
	private final Supplier<NativeImage> imageSupplier;

	@Override
	protected void setupAnimations() throws IllegalStateException {
		super.setupAnimations();
		this.frameCounterTicks = Math.max(1, DebugMode.frameDelayMs / 50);
	}

	@Override
	public void load(ResourceManager resourceManager) {
		if (this.image == null) {
			return;
		}

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
		DebugMode.log("Re-uploading native texture {}", this.debugPath);
		this.image = this.imageSupplier.get();

		if (this.image == null) {
			Cosmetica.LOGGER.warn("Sorry texture machine broke for {}", this.debugPath);
		}
		else {
			this.setupAnimations();
			this.upload();
		}
	}

	@Override
	public void tick() {
		this.doTick();
	}
}
