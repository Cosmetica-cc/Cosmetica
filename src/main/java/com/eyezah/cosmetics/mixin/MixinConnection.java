package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Authentication;
import com.eyezah.cosmetics.screens.LoadingScreen;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.eyezah.cosmetics.Cosmetics.optionsStorage;

@Mixin(Connection.class)
public class MixinConnection {

	@Inject(at = @At("HEAD"), method = "disconnect")
	public void onDisconnect(Component component, CallbackInfo ci) {
		if (component.getString().startsWith("ExtravagantCosmeticsToken:")) {
			Authentication.setToken(component.getString().substring(26));
		} else {
			Authentication.currentlyAuthenticating = false;
			if (Minecraft.getInstance().screen instanceof LoadingScreen) {
				Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(new OptionsScreen(new TitleScreen(), optionsStorage), optionsStorage, false)));
			}
		}
	}
}
