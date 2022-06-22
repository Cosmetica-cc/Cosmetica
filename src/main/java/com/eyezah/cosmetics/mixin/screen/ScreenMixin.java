package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.utils.ExtendedScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin implements ExtendedScreen {
	@Shadow
	private List<GuiEventListener> children;

	@Override
	public Iterable<GuiEventListener> getChildren() {
		return this.children;
	}
}