package com.eyezah.cosmetics.utils;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public interface ExtendedScreen {
	Iterable<GuiEventListener> getChildren();
	void setTitle(Component title);
}
