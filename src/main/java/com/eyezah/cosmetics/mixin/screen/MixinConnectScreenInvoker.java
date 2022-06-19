package com.eyezah.cosmetics.mixin.screen;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConnectScreen.class)
public interface MixinConnectScreenInvoker {
	@Nullable
	@Accessor("connection")
	Connection getConnection();
}
