package com.eyezah.cosmetics.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureAtlasSprite.class)
public interface MixinTextureAtlasSpriteInvoker {
	@Accessor
	NativeImage[] getMainImage();

	@Invoker
	void callUpload(int i, int j, NativeImage[] nativeImages);
}
