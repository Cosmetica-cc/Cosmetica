package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.screens.LoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
	@Shadow @Nullable protected Minecraft minecraft;

	@Inject(at = @At("HEAD"), method = "handleComponentClicked", cancellable = true)
	private void onHandleClick(Style style, CallbackInfoReturnable<Boolean> info) {
		if (Cosmetica.handleComponentClicked(this.minecraft, style)) {
			info.setReturnValue(true);
		}
	}
}
