package cc.cosmetica.cosmetica.screens.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

public class StringSelection extends Selection<StringSelection.Entry> {
	public StringSelection(Minecraft minecraft, Screen parent, Font font, Consumer<String> onSelect) {
		super(minecraft, parent, font, 0, 0, 18, onSelect);
	}

	private Map<String, StringSelection.Entry> entries = new HashMap<>();
	private int multiSelect = 1;
	// if multiselecting, the non-current items in the selection stack
	private LinkedList<Entry> selectionStack = new LinkedList<>();

	public StringSelection multiSelect(int cap) {
		this.multiSelect = cap;
		return this;
	}

	public void addItem(String item) {
		Entry entry = new Entry(item, this.width);
		this.addEntry(entry);
		this.entries.put(item, entry);
	}

	public void selectItem(String item) {
		this.setSelected(this.entries.get(item));
	}

	@Override
	protected Entry findEntry(Entry key) {
		return this.entries.get(key.item);
	}

	public void matchSelected(StringSelection other) {
		if (this.multiSelect > 1) {
			this.selectionStack = new LinkedList<>(other.selectionStack);
		}

		super.matchSelected(other);
	}

	@Environment(EnvType.CLIENT)
	public class Entry extends Selection.Entry<StringSelection.Entry> {
		public Entry(String item, int width) {
			super(StringSelection.this, item);
			this.width = width;
		}

		private final int width;

		@Override
		public void render(PoseStack poseStack, int x, int y, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.selection.font.drawShadow(poseStack, this.item, (float) (this.width / 2 - this.selection.font.width(this.item) / 2), (float)(y + 3), 16777215, true);
		}

		@Override
		public Component getNarration() {
			return new TranslatableComponent("narrator.select", new Object[]{this.item});
		}
	}
}