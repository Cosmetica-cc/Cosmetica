package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.cosmetics.model.RuntimeTextureManager;
import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for debug purposes.
 */
@Mixin(NativeImage.class)
public class MixinNativeImage implements RuntimeTextureManager.NIDebugInfoProvider {
	@Shadow @Final private boolean useStbFree;

	@Inject(method = "<init>(Lcom/mojang/blaze3d/platform/NativeImage$Format;IIZ)V", at = @At("RETURN"))
	private void onInit(NativeImage.Format format, int i, int j, boolean bl, CallbackInfo info) {
		this.pixelAllocType = bl ? RuntimeTextureManager.PixelAllocType.CALLOC : RuntimeTextureManager.PixelAllocType.ALLOC;
	}

	@Unique
	private RuntimeTextureManager.PixelAllocType pixelAllocType = RuntimeTextureManager.PixelAllocType.PROVIDED;

	@Override
	public boolean usesStbFree() {
		return this.useStbFree;
	}

	@Override
	public RuntimeTextureManager.PixelAllocType getAllocType() {
		return this.pixelAllocType;
	}
}
