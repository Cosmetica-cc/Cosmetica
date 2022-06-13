package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.screens.RSEWarningScreen;
import com.eyezah.cosmetics.utils.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 69) // i must be the first
public abstract class MixinMinecraft {
	@Shadow public abstract void setScreen(@Nullable Screen screen);

	@Shadow @Nullable public Screen screen;

	@Shadow @Final public Gui gui;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;shutdownExecutors()V"), method = "close")
	private void onClose(CallbackInfo info) {
		Cosmetica.onShutdownClient();
	}

	@Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
	private void addRegionSpecificEffectsPrompt(Screen screen, CallbackInfo info) {
		// if the RSE warning screen should appear cancel the current screen set in favour of a wrapper thereof
		if (RSEWarningScreen.appearNextScreenChange) {
			RSEWarningScreen.appearNextScreenChange = false;
			this.setScreen(new RSEWarningScreen(screen));
			info.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "setLevel")
	private void maybeClearCosmetics(ClientLevel level, CallbackInfo info) {
		if (Cosmetica.getCacheSize() > 1024) {
			Debug.info("Clearing Cosmetica Caches");
			Cosmetica.clearAllCaches();
		}

		// also do the check thing
		if (Cosmetica.displayNext != null) {
			this.gui.getChat().addMessage(Component.literal(Cosmetica.displayNext));
			Cosmetica.displayNext = null;
		}
	}
}
