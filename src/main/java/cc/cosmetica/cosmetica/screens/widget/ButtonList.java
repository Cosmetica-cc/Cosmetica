package cc.cosmetica.cosmetica.screens.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class ButtonList extends ContainerObjectSelectionList<ButtonList.Entry> {
	public ButtonList(Minecraft minecraft, Screen parent, int spacing) {
		super(minecraft, parent.width, parent.height, 32, parent.height - 32, spacing);
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
			this.button = new Button(0, 0, width, 20, text, callback, tooltip == null ? Button.NO_TOOLTIP : new Button.OnTooltip() {
				@Nonnull // to make intellij shut up about null warnings
				private final Component text = tooltip;

				public void onTooltip(Button button, PoseStack poseStack, int i, int j) {
					Screen screen = ButtonList.this.parent;
					screen.renderTooltip(poseStack, ButtonList.this.minecraft.font.split(this.text, Math.max(screen.width / 2 - 43, 170)), i, j + 18);
				}

				public void narrateTooltip(Consumer<Component> consumer) {
					consumer.accept(this.text);
				}
			});
		}

		private final Button button;

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.button);
		}

		@Override
		public void render(PoseStack poseStack, int i, int y, int k, int l, int m, int passMe1, int passMe2, boolean bl, float passMe3) {
			this.button.x = ButtonList.this.width / 2 - this.button.getWidth() / 2;
			this.button.y = y;
			this.button.render(poseStack, passMe1, passMe2, passMe3);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			return this.button.mouseClicked(d, e, i);
		}
	}
}
