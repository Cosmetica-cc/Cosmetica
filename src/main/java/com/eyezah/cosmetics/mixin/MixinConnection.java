package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Authentication;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection {

	@Inject(at = @At("HEAD"), method = "disconnect")
	public void onDisconnect(Component component, CallbackInfo ci) {
		if (component.getString().startsWith("ExtravagantCosmeticsToken:")) Authentication.setToken(component.getString().substring(26));
	}
}
