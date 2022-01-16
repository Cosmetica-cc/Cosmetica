package com.eyezah.cosmetics.cosmetics.model;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ModifiableAtlasSprite extends TextureAtlasSprite {
	public ModifiableAtlasSprite(TextureAtlas textureAtlas, Info info, int i, int j, int k, int l, int m, NativeImage nativeImage) {
		super(textureAtlas, info, i, j, k, l, m, nativeImage);
	}

	public void setTexture(NativeImage image) {
		
	}
}
