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

import benzenestudios.sulphate.ClassicButton;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class ButtonList extends ContainerObjectSelectionList<ButtonList.Entry> {
	public ButtonList(Minecraft minecraft, Screen parent, int spacing) {
		super(minecraft, parent.width, parent.height, 32, spacing);
		this.parent = parent;
	}

	public ButtonList(Minecraft minecraft, Screen parent) {
		this(minecraft, parent, 20);
	}

	private final Screen parent;

	public void addButton(int width, Component text, Button.OnPress callback, @Nullable Component tooltip) {
		this.addEntry(new Entry(width, text, callback, tooltip));
	}

	class Entry extends ContainerObjectSelectionList.Entry<ButtonList.Entry> {
		Entry(int width, Component text, Button.OnPress callback, @Nullable Component tooltip) {
			this.button = new ClassicButton(0, 0, width, 20, text, callback);

			if (tooltip != null) {
				this.button.setTooltip(Tooltip.create(tooltip));
			}
		}

		private final Button button;

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.button);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.button);
		}

		@Override
		public void render(GuiGraphics poseStack, int i, int y, int k, int l, int m, int passMe1, int passMe2, boolean bl, float passMe3) {
			this.button.setX(ButtonList.this.width / 2 - this.button.getWidth() / 2);
			this.button.setY(y);
			this.button.render(poseStack, passMe1, passMe2, passMe3);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			return this.button.mouseClicked(d, e, i);
		}
	}
}
