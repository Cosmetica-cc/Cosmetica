package com.eyezah.cosmetics.screens.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class SearchEditBox extends EditBox {
	public SearchEditBox(Font font, int x, int y, int w, int h, Component component) {
		super(font, x, y, w, h, component);
	}

	@Nullable
	private Consumer<String> onEnter = null;

	public void setOnEnter(@Nullable Consumer<String> onEnter) {
		this.onEnter = onEnter;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.canConsumeInput() && (i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) && this.onEnter != null) {
			this.setFocus(false);
			this.onEnter.accept(this.getValue());
			return true;
		}

		return super.keyPressed(i, j, k);
	}
}
