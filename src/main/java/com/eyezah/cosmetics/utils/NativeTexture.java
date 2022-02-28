package com.eyezah.cosmetics.utils;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class NativeTexture extends AbstractTexture {
	public NativeTexture(String base64) throws IOException {
		this(loadBase64(base64));
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

    private static NativeImage loadBase64(String base64) throws IOException {
        if(base64.length() < 1000) { //TODO: Tweak this number
            return NativeImage.fromBase64(base64);
        } else {
            //For large images, NativeImage.fromBase64 does not work because it tries to allocate it on the stack and fails
            byte[] bs = Base64.getDecoder().decode(base64.replace("\n", "").getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = MemoryUtil.memAlloc(bs.length);
            buffer.put(bs);
            buffer.rewind();
            NativeImage image = NativeImage.read(buffer);
            MemoryUtil.memFree(buffer);
            return image;
        }
    }
}
