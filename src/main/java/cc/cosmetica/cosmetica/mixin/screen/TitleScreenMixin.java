package cc.cosmetica.cosmetica.mixin.screen;

import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cc.cosmetica.cosmetica.Authentication.runAuthentication;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Inject(at = @At("HEAD"), method = "init")
	private void titleScreenInject(CallbackInfo ci) {
		runAuthentication(2);
	}
}