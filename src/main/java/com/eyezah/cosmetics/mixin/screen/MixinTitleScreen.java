package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.eyezah.cosmetics.Authentication.runAuthentication;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@Inject(at = @At("HEAD"), method = "init")
	private void titleScreenInject(CallbackInfo ci) {
		if (Cosmetica.api != null && Cosmetica.api.isAuthenticated()) runAuthentication(new TitleScreen(), 2);
	}
}