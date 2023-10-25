/*
 * Copyright 2022, 2023 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica.screens.widget;

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
			this.setFocused(false);
			this.onEnter.accept(this.getValue());
			return true;
		}

		return super.keyPressed(i, j, k);
	}

	public boolean isEmpty() {
		return this.getValue().isEmpty();
	}
}
