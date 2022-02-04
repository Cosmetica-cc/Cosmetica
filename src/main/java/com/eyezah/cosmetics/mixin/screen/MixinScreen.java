package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.utils.ExtendedScreen;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Screen.class)
public class MixinScreen implements ExtendedScreen {
	@Shadow
	private List<GuiEventListener> children;

	@Override
	public Iterable<GuiEventListener> getChildren() {
		return this.children;
	}
}