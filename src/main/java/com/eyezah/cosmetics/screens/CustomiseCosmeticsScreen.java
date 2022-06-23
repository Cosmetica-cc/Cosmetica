package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomiseCosmeticsScreen extends PlayerRenderScreen {
	protected CustomiseCosmeticsScreen(Screen parentScreen, FakePlayer player) {
		super(TextComponents.translatable("cosmetica.customizeCosmetics"), parentScreen, player);

		this.setAnchorX(Anchor.LEFT, () -> this.width / 2);
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2 - 40);
		this.setYSeparation(20 * 3 + 10);
	}

	@Override
	protected void addWidgets() {
		PlayerData data = this.fakePlayer.getData();

		// hats

		Div hatsSection = this.addWidget(Div::new, TextComponents.literal("Hats Section"));

		Span hatsHeader = hatsSection.addWidget(new Span(0, 0, 200, 20, TextComponents.literal("Hats Header")));

		this.addText(hatsHeader, TextComponents.literal("Hats"), 100, false);
		hatsHeader.addWidget(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

		for (BakableModel hat : data.hats()) {
			hat.id();
		}

		this.addText(hatsSection, TextComponents.literal("Bowler Hat"), 200, false).active = false;
		this.addText(hatsSection, TextComponents.literal("Rain Hat"), 200, false).active = false;

		hatsSection.calculateDimensions();

		// sbs

		Div shoulderBuddiesSection = this.addWidget(Div::new, TextComponents.literal("Shoulder Buddies Section"));

		Span shoulderBuddyHeader = shoulderBuddiesSection.addWidget(new Span(0, 0, 200, 20, TextComponents.literal("Shoulder Buddies Header")));

		this.addText(shoulderBuddyHeader, TextComponents.literal("Shoulder Buddies"), 100, false);
		shoulderBuddyHeader.addWidget(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

		this.addText(shoulderBuddiesSection, TextComponents.literal("Some OP Dragon by Lysanderoth"), 200, false).active = false;
		this.addText(shoulderBuddiesSection, TextComponents.literal("Slim Bald Eagle"), 200, false).active = false;

		shoulderBuddiesSection.calculateDimensions();

		// back bling

		Div backBlingSection = this.addWidget(Div::new, TextComponents.literal("Back Bling Section"));

		Span backBlingHeader = backBlingSection.addWidget(new Span(0, 0, 200, 20, TextComponents.literal("Back Bling Header")));

		this.addText(backBlingHeader, TextComponents.literal("Back Bling"), 100, false);
		backBlingHeader.addWidget(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

		this.addText(backBlingSection, TextComponents.literal("Wings"), 200, false).active = false;

		backBlingSection.calculateDimensions();

		// lore

		Div loreSection = this.addWidget(Div::new, TextComponents.literal("Lore Section"));

		Span loreHeader = loreSection.addWidget(new Span(0, 0, 200, 20, TextComponents.literal("Lore Header")));

		this.addText(loreHeader, TextComponents.literal("Lore"), 100, false);
		loreHeader.addWidget(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

		this.addText(loreSection, TextComponents.literal("New to Cosmetica"), 200, false);

		loreSection.calculateDimensions();

		this.addDone(this.height - 40);
	}

	@Override
	public void afterInit() {
		for (GuiEventListener widget : this.children()) {
			if (widget instanceof Section section) section.repositionChildren();
		}
	}

	private TextWidget addText(Component text, int width, boolean centered) {
		return this.addWidget((x, y, w, h, component) -> new TextWidget(x, y, w, h, centered, component), text, width, 20);
	}

	private TextWidget addText(Section section, Component text, int width, boolean centered) {
		return section.addWidget(new TextWidget(0, 0, width, 20, centered, text));
	}

	private static class TextWidget extends AbstractWidget {
		public TextWidget(int x, int y, int width, int height, boolean centered, Component component) {
			super(x, y, width, height, component);
			this.centered = centered;
		}

		private boolean centered;

		@Override
		public void updateNarration(NarrationElementOutput narration) {
			this.defaultButtonNarrationText(narration);
		}

		public boolean mouseClicked(double d, double e, int i) {
			return false;
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, float f) {
			Minecraft minecraft = Minecraft.getInstance();
			Font font = minecraft.font;

			int colour = this.active ? 16777215 : 10526880;

			if (this.centered) {
				drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, colour | Mth.ceil(this.alpha * 255.0F) << 24);
			}
			else {
				drawString(poseStack, font, this.getMessage(), this.x, this.y + (this.height - 8) / 2, colour | Mth.ceil(this.alpha * 255.0F) << 24);
			}
		}
	}

	private static class Div extends Section {
		public Div(int x, int y, int width, int height, Component component) {
			super(x, y, width, height, component);
		}

		@Override
		public void calculateDimensions() {
			super.calculateDimensions();
			this.height = this.children.stream().map(w -> w.getHeight()).collect(Collectors.summingInt(i -> i));
		}

		@Override
		public void repositionChildren() {
			int y0 = this.y;

			for (AbstractWidget child : this.children) {
				child.x += this.x;
				child.y += y0;

				y0 += child.getHeight();
			}

			super.repositionChildren();
		}
	}

	private static class Span extends Section {
		public Span(int x, int y, int width, int height, Component component) {
			super(x, y, width, height, component);
		}

		@Override
		public void calculateDimensions() {
			super.calculateDimensions();
			this.setWidth(this.children.stream().map(w -> w.getWidth()).collect(Collectors.summingInt(i -> i)));
			this.height = this.children.stream().map(w -> w.getHeight()).collect(Collectors.maxBy(Comparator.comparingInt(a -> a))).orElse(0);
		}

		@Override
		public void repositionChildren() {
			int x0 = this.x;

			for (AbstractWidget child : this.children) {
				child.x += x0;
				child.y += this.y;

				x0 += child.getWidth();
			}

			super.repositionChildren();
		}
	}

	private abstract static class Section extends AbstractWidget {
		public Section(int i, int j, int k, int l, Component component) {
			super(i, j, k, l, component);
		}

		protected List<AbstractWidget> children = new LinkedList<>();

		public void calculateDimensions() {
			for (AbstractWidget child : this.children) {
				if (child instanceof Section section) section.calculateDimensions();
			}
		}

		public void repositionChildren() {
			for (AbstractWidget child : this.children) {
				if (child instanceof Section section) section.repositionChildren();
			}
		}

		public <T extends AbstractWidget> T addWidget(T widget) {
			this.children.add(widget);
			return widget;
		}

		@Override
		public void onClick(double x, double y) {
			for (AbstractWidget child : this.children) {
				if (child.x <= x && x < child.x + child.getWidth()) {
					if (child.y <= y && y < child.y + child.getHeight()) {
						child.onClick(x, y);
						return;
					}
				}
			}
		}

		@Override
		public void updateNarration(NarrationElementOutput narration) {
			for (AbstractWidget child : this.children) {
				child.updateNarration(narration);
			}
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, float f) {
			for (AbstractWidget child : this.children) {
				child.render(poseStack, i, j, f);
			}
		}
	}
}
