package com.eyezah.cosmetics.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ConnectScreen.class)
public interface MixinConnectScreenInvoker {
	@Invoker("<init>")
	static ConnectScreen getConnectScreen(Screen screen) {
		return null;
	}

	@Invoker("updateStatus")
	void doUpdateStatus(Component component);
}
