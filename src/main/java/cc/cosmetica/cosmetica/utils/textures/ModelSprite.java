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

import cc.cosmetica.cosmetica.utils.DebugMode;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;

public class ModelSprite extends TextureAtlasSprite {
	public ModelSprite(ResourceLocation location, cc.cosmetica.cosmetica.utils.textures.AnimatedTexture texture) {
		this(location, texture, texture.image.getWidth(), texture.getFrameHeight());
	}

	private ModelSprite(ResourceLocation location, cc.cosmetica.cosmetica.utils.textures.AnimatedTexture texture, int width, int height) {
		// textureAtlas, info, mipLevels, uScale (atlasTextureWidth), vScale (atlasTextureHeight), width, height, image
		super(null,
				// dummy data for the animation metadata: we want to handle the animation ourselves.
				new Info(location, width, height, new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0)), width, height, 69, false)),
				Math.min(4, getMaximumMipmapLevels(texture.image)),
				width,
				height,
				width,
				height,
				texture.image
		);

		this.animatedTexture = texture;
	}

	private final cc.cosmetica.cosmetica.utils.textures.AnimatedTexture animatedTexture;

	@Override
	public int getFrameCount() {
		return this.animatedTexture.getFrameCount();
	}

	@Override
	public void close() {
		this.animatedTexture.close();
	}

	@Override
	public String toString() {
		return "ModelSprite{" +
				"animatedTexture=" + animatedTexture +
				", resourceLocation=" + this.getName() +
				", u=[" + this.getU0() + "," + this.getU1() + "]" +
				", v=[" + this.getV0() + ", " + this.getV1() + "]" +
				'}';
	}

	@Override
	public TextureAtlas atlas() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			throw new UnsupportedOperationException("I am a teapot. Tried to call atlas() on cosmetica ModelSprite.");
		}
		else {
			// fix compat with ModelGapFix (modelfix)
			// pretend to be the block atlas
			DebugMode.warnOnce("UnsafeAtlasAccess", "A mod called atlas() on a cosmetica ModelSprite. Behaviour could be unpredictable.");
			return Minecraft.getInstance().getModelManager().getAtlas(BLOCK_ATLAS);
		}
	}
	private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");

	@Override
	public boolean isTransparent(int i, int j, int k) {
		throw new UnsupportedOperationException("I am a teapot. Tried to call isTransparent() on cosmetica ModelSprite.");
	}

	@Override
	public void uploadFirstFrame() {
		throw new UnsupportedOperationException("I am a teapot. Tried to call uploadFirstFrame() on cosmetica ModelSprite.");
	}

	private static int getMaximumMipmapLevels(NativeImage image) {
		return log2(Math.min(image.getWidth(), image.getHeight()));
	}

	// Fast, Integer Log2
	private static int log2(int in) {
		int i = 0;

		while (in > 1) {
			i++;
			in >>= 1;
		}

		return i;
	}
}
