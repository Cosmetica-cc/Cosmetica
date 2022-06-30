package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.utils.textures.CosmeticIconTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleTexture.class)
public class SimpleTextureMixin {
	@Inject(at = @At("HEAD"), method = "doLoad", cancellable = true)
	private void onUpload(NativeImage nativeImage, boolean blur, boolean clamp, CallbackInfo ci) {
		if ((Object) this instanceof CosmeticIconTexture t) {
			t.firstUpload(nativeImage, true);
			ci.cancel();
		}
	}
}
