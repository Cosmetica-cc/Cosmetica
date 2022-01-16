package com.eyezah.cosmetics.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureAtlasSprite.class)
public interface MixinTextureAtlasSpriteInvoker {
	@Invoker("<init>")
	static TextureAtlasSprite create(TextureAtlas textureAtlas, TextureAtlasSprite.Info info, int i, int j, int k, int l, int m, NativeImage nativeImage) {
		throw new IllegalStateException("Mixin failed");
	}
}
