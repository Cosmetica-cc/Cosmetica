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
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class ModelSprite extends TextureAtlasSprite {
	public ModelSprite(ResourceLocation location, AnimatedTexture texture) {
		this(location, texture, texture.image.getWidth(), texture.getFrameHeight());
	}

	private ModelSprite(ResourceLocation location, AnimatedTexture texture, int width, int height) {
		// textureAtlas, info, mipLevels, uScale (atlasTextureWidth), vScale (atlasTextureHeight), width, height, image
		super(null,
				new ModelSpriteContents(location, new FrameSize(width, height), texture, null),
				width, height,
				0, 0
		);

		this.animatedTexture = texture;
		this.location = location;
	}

	private final AnimatedTexture animatedTexture;
	private final ResourceLocation location;

	@Override
	public String toString() {
		return "ModelSprite{" +
				"animatedTexture=" + animatedTexture +
				", resourceLocation=" + this.location +
				", u=[" + this.getU0() + "," + this.getU1() + "]" +
				", v=[" + this.getV0() + ", " + this.getV1() + "]" +
				'}';
	}

	@Override
	public ResourceLocation atlasLocation() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			throw new UnsupportedOperationException("I am a teapot. Tried to call atlasLocation() on cosmetica ModelSprite.");
		}
		else {
			// fix compat with ModelGapFix (modelfix)
			// pretend to be the block atlas
			DebugMode.warnOnce("UnsafeAtlasAccess", "A mod called atlas() on a cosmetica ModelSprite. Behaviour could be unpredictable.");
			return BLOCK_ATLAS;
		}
	}

	private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");

	@Override
	public void uploadFirstFrame() {
		throw new UnsupportedOperationException("I am a teapot. Tried to call uploadFirstFrame() on cosmetica ModelSprite.");
	}

	public static class ModelSpriteContents extends SpriteContents {
		public ModelSpriteContents(ResourceLocation resourceLocation, FrameSize frameSize, cc.cosmetica.cosmetica.utils.textures.AnimatedTexture animatedTexture, AnimationMetadataSection animationMetadataSection) {
			super(resourceLocation, frameSize, animatedTexture.image, animationMetadataSection);
			this.animatedTexture = animatedTexture;
		}

		private final cc.cosmetica.cosmetica.utils.textures.AnimatedTexture animatedTexture;

		@Override
		protected int getFrameCount() {
			return this.animatedTexture.getFrameCount();
		}

		@Override
		public IntStream getUniqueFrames() {
			return IntStream.range(0, getFrameCount());
		}

		// TODO what are these two close() functions for?
		// This one seems to close the image in vanilla, whereas ticker/close seems to close the interpolation data object
		// the latter does effectively the same thing but whatever texture is currently active in the interpolation data object
		@Override
		public void close() {
			this.animatedTexture.close();
		}

		@Nullable
		@Override
		public SpriteTicker createTicker() {
			return this.animatedTexture instanceof Tickable ? new SpriteTicker() {
				@Override
				public void tickAndUpload(int i, int j) {
					ModelSpriteContents.this.animatedTexture.doTick();
				}

				@Override
				public void close() {
					ModelSpriteContents.this.animatedTexture.close();
				}
			} : null;
		}
	}
}
