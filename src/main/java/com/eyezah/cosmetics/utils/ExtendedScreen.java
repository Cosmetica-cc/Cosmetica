package com.eyezah.cosmetics.utils;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ExtendedScreen {
	Iterable<GuiEventListener> getChildren();
}
