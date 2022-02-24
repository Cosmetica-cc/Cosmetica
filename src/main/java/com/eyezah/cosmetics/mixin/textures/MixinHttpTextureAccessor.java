package com.eyezah.cosmetics.mixin.textures;

import net.minecraft.client.renderer.texture.HttpTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HttpTexture.class)
public interface MixinHttpTextureAccessor {
	@Accessor
	boolean isUploaded();
}
