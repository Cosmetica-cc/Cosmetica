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

import com.google.common.collect.ImmutableList;
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
		throw new UnsupportedOperationException("I am a teapot. Tried to call atlas() on cosmetica ModelSprite.");
	}

	@Override
	public boolean isTransparent(int i, int j, int k) {
		throw new UnsupportedOperationException("I am a teapot. Tried to call isTransparent() on cosmetica ModelSprite.");
	}

	@Override
	public void uploadFirstFrame() {
		throw new UnsupportedOperationException("I am a teapot. Tried to call isTransparent() on cosmetica ModelSprite.");
	}
}
