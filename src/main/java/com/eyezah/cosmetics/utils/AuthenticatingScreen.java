package com.eyezah.cosmetics.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public interface AuthenticatingScreen {
	void eyezahAuthConnect(Minecraft minecraft, ServerAddress serverAddress);
}
