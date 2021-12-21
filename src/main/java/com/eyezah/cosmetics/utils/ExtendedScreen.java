package com.eyezah.cosmetics.utils;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public interface ExtendedScreen {
	void setTitle(Component title);
	Iterable<GuiEventListener> getChildren();
}
