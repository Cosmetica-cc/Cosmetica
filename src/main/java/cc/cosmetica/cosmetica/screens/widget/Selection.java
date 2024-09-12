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

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

abstract class Selection<T extends Selection.Entry<T>> extends ObjectSelectionList<T> {
	Selection(Minecraft minecraft, Screen parent, Font font, int floorOffset, int ceilOffset, int elementHeight, Consumer<String> onSelect) {
		super(minecraft, parent.width, parent.height, 32 + ceilOffset, parent.height - 65 + 4 + floorOffset, elementHeight);

		this.parent = parent;
		this.font = font;
		this.onSelect = onSelect;
	}

	final Screen parent;
	protected final Font font;
	private final Consumer<String> onSelect;

	@Override
	public void setSelected(@Nullable T entry) {
		super.setSelected(entry);
		this.onSelect.accept(entry == null ? "" : entry.item);
	}

	public void recenter() {
		if (this.getSelected() != null) {
			this.centerScrollOn(this.getSelected());
		}
	}

	public void matchSelected(Selection<T> other) {
		T select = other.getSelected();
		if (select == null) return;
		select = this.findEntry(select); // switch to *our* one
		super.setSelected(select);
		this.centerScrollOn(select);
	}

	protected abstract T findEntry(T key);

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	@Override
	protected void renderBackground(PoseStack poseStack) {
		this.parent.renderBackground(poseStack);
	}

	@Override
	protected boolean isFocused() {
		return this.parent.getFocused() == this;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry<E extends Selection.Entry<E>> extends ObjectSelectionList.Entry<E> {
		final String item;
		protected final Selection selection;

		public Entry(Selection selection, String item) {
			this.selection = selection;
			this.item = item;
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (i == 0) {
				this.select();
				return true;
			} else {
				return false;
			}
		}

		private void select() {
			this.selection.setSelected(this);
		}
	}
}