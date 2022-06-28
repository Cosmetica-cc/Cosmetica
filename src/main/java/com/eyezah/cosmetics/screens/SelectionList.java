package com.eyezah.cosmetics.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SelectionList extends ObjectSelectionList<SelectionList.Entry> {
	public SelectionList(Minecraft minecraft, Screen parent, Font font) {
		this(minecraft, parent, font, 0, s -> {});
	}

	public SelectionList(Minecraft minecraft, Screen parent, Font font, int floorOffset, Consumer<String> onSelect) {
		super(minecraft, parent.width, parent.height, 32, parent.height - 65 + 4 + floorOffset, 18);

		this.parent = parent;
		this.font = font;
		this.onSelect = onSelect;
	}

	private final Screen parent;
	private final Font font;
	private final Consumer<String> onSelect;
	private final Map<String, Entry> entries = new HashMap<>();

	public void addItem(String item) {
		Entry entry = new Entry(item);
		this.addEntry(entry);
		this.entries.put(item, entry);
	}

	public void selectItem(String item) {
		this.setSelected(this.entries.get(item));
	}

	@Override
	public void setSelected(@Nullable SelectionList.Entry entry) {
		super.setSelected(entry);
		this.onSelect.accept(entry == null ? "" : entry.item);
	}

	public void matchSelected(SelectionList other) {
		super.setSelected(other.getSelected());
		this.centerScrollOn(other.getSelected());
	}

	public void recenter() {
		if (this.getSelected() != null) {
			this.centerScrollOn(this.getSelected());
		}
	}

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
	public class Entry extends ObjectSelectionList.Entry<SelectionList.Entry> {
		final String item;

		public Entry(String item) {
			this.item = item;
		}

		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			SelectionList.this.font.drawShadow(poseStack, this.item, (float) (SelectionList.this.width / 2 - SelectionList.this.font.width(this.item) / 2), (float)(j + 3), 16777215, true);
		}

		public boolean mouseClicked(double d, double e, int i) {
			if (i == 0) {
				this.select();
				return true;
			} else {
				return false;
			}
		}

		private void select() {
			SelectionList.this.setSelected(this);
		}

		public Component getNarration() {
			return new TranslatableComponent("narrator.select", new Object[]{this.item});
		}
	}
}