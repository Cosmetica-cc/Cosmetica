package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.utils.textures.CosmeticIconTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.HttpTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HttpTexture.class)
public class HttpTextureMixin {
	@Inject(at = @At("HEAD"), method = "upload", cancellable = true)
	private void onUpload(NativeImage nativeImage, CallbackInfo ci) {
		if ((Object) this instanceof CosmeticIconTexture t) {
			t.firstUpload(nativeImage, false);
			ci.cancel();
		}
	}
}
