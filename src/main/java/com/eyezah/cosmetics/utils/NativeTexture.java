package com.eyezah.cosmetics.utils;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class NativeTexture extends AbstractTexture {
	public NativeTexture(String base64) throws IOException {
		this(NativeImage.fromBase64(base64));
	}

	public NativeTexture(NativeImage image) {
		this.image = image;
	}

	private final NativeImage image;

	@Override
	public void load(ResourceManager resourceManager) {
		TextureUtil.prepareImage(this.getId(), 0, this.image.getWidth(), this.image.getHeight());
		this.image.upload(0, 0, 0, 0, 0, this.image.getWidth(), this.image.getHeight(), this.blur, false, false, true);
	}
}
