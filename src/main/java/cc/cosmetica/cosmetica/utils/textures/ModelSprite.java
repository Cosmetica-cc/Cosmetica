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

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class ModelSprite extends TextureAtlasSprite {
	public ModelSprite(ResourceLocation location, cc.cosmetica.cosmetica.utils.textures.AnimatedTexture texture) {
		this(location, texture, texture.image.getWidth(), texture.getFrameHeight());
	}

	private ModelSprite(ResourceLocation location, cc.cosmetica.cosmetica.utils.textures.AnimatedTexture texture, int width, int height) {
		// textureAtlas, info, mipLevels, uScale (atlasTextureWidth), vScale (atlasTextureHeight), width, height, image
		super(null,
				new Info(location, width, height, null),
				4,
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
	protected int getFrameCount() {
		return this.animatedTexture.getFrameCount();
	}

	@Override
	public IntStream getUniqueFrames() {
		return IntStream.range(0, getFrameCount());
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

	@Nullable
	@Override
	public Tickable getAnimationTicker() {
		return this.animatedTexture instanceof Tickable ? (Tickable) this.animatedTexture : null;
	}
}
