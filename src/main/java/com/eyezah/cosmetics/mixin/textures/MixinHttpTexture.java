package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.SessionWrapperService;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.HttpTexture;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(HttpTexture.class)
public class MixinHttpTexture {
	@Shadow @Final private boolean processLegacySkin;

	@Inject(at = @At("RETURN"), method = "load(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;", cancellable = true)
	private void load(InputStream stream, CallbackInfoReturnable<NativeImage> info) {
		@Nullable NativeImage image = info.getReturnValue();

		if (!this.processLegacySkin && image != null) {
			info.setReturnValue(SessionWrapperService.processBadCapes(image));
		}
	}
}
