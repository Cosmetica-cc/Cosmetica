package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetics;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
//	@Inject(at = @At("RETURN"), method = "<init>")
//	private void lateLoad(GameConfig gameConfig, CallbackInfo info) {
//	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;shutdownExecutors()V"), method = "close")
	private void onClose(CallbackInfo info) {
		Cosmetics.onShutdownClient();
	}
}
