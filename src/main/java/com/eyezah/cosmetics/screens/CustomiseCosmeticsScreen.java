package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.utils.TextComponents;
import com.google.common.collect.ImmutableList;
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

import java.util.LinkedList;
import java.util.List;

public class CustomiseCosmeticsScreen extends PlayerRenderScreen {
	protected CustomiseCosmeticsScreen(Screen parentScreen, FakePlayer player, ServerOptions options) {
		super(TextComponents.translatable("cosmetica.customizeCosmetics"), parentScreen, player);

		this.setAnchorX(Anchor.LEFT, () -> 16);
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2);

		this.options = options;
	}

	private final ServerOptions options;
	private Section cloakSection;
	private Section loreSection;
	private Section hatsSection;
	private Section shoulderBuddiesSection;
	private Section backBlingSection;

	private Section selected;

	private Section createDisabledSection(String title) {
		Span section = this.addWidget(Span::new, TextComponents.literal(title + " Section"));

		this.addTextTo(section, TextComponents.literal(title), 100, false);
		this.addTextTo(section, TextComponents.literal("Disabled"), 100, false).active = false;

		section.calculateDimensions();
		return section;
	}

	@Override
	protected void addWidgets() {
		PlayerData data = this.fakePlayer.getData();

		// cape
		this.cloakSection = Div.create("Cape");

		Span capeHeader = this.cloakSection.addChild(new Span(0, 0, 200, 20, TextComponents.literal("Cloak Header")));

		this.addTextTo(capeHeader, TextComponents.literal(data.capeName().isEmpty() ? "No Cape" : "Cape"), 100, false);
		capeHeader.addChild(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

		if (data.cape() != null) this.addTextTo(this.cloakSection, TextComponents.literal(data.capeName()), 200, false).active = false;

		this.cloakSection.calculateDimensions();

		// lore
		if (this.options.lore.get()) {
			this.loreSection = Div.create("Lore");

			Span loreHeader = loreSection.addChild(new Span(0, 0, 200, 20, TextComponents.literal("Lore Header")));

			this.addTextTo(loreHeader, TextComponents.literal(data.lore().isEmpty() ? "No Lore" : "Lore"), 100, false);
			loreHeader.addChild(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

			if (!data.lore().isEmpty()) this.addTextTo(this.loreSection, TextComponents.literal(data.lore()), 200, false);

			this.loreSection.calculateDimensions();
		}
		else {
			this.loreSection = this.createDisabledSection("Lore");
		}

		// hats
		if (this.options.hats.get()) {
			this.hatsSection = Div.create("Hats");

			Span hatsHeader = this.hatsSection.addChild(new Span(0, 0, 200, 20, TextComponents.literal("Hats Header")));

			this.addTextTo(hatsHeader, TextComponents.literal(data.hats().isEmpty() ? "No Hats" : "Hats"), 100, false);
			hatsHeader.addChild(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

			for (BakableModel hat : data.hats()) {
				this.addTextTo(this.hatsSection, TextComponents.literal(hat.name()), 200, false).active = false;
			}

			this.hatsSection.calculateDimensions();
		}
		else {
			this.hatsSection = this.createDisabledSection("Hats");
		}

		// sbs
		if (this.options.shoulderBuddies.get()) {
			this.shoulderBuddiesSection = Div.create("Shoulder Buddies");

			Span shoulderBuddyHeader = this.shoulderBuddiesSection.addChild(new Span(0, 0, 200, 20, TextComponents.literal("Shoulder Buddies Header")));

			this.addTextTo(shoulderBuddyHeader, TextComponents.literal("Shoulder Buddies"), 100, false);
			shoulderBuddyHeader.addChild(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

			this.addTextTo(this.shoulderBuddiesSection, TextComponents.literal("Left: " + (data.leftShoulderBuddy() == null ? "None" : data.leftShoulderBuddy().name())), 200, false).active = false;
			this.addTextTo(this.shoulderBuddiesSection, TextComponents.literal("Right: " + (data.rightShoulderBuddy() == null ? "None" : data.rightShoulderBuddy().name())), 200, false).active = false;

			this.shoulderBuddiesSection.calculateDimensions();
		}
		else {
			this.shoulderBuddiesSection = this.createDisabledSection("Shoulder Buddies");
		}

		// back bling
		if (this.options.backBlings.get()) {
			this.backBlingSection = Div.create("Back Bling");

			Span backBlingHeader = this.backBlingSection.addChild(new Span(0, 0, 200, 20, TextComponents.literal("Back Bling Header")));

			this.addTextTo(backBlingHeader, TextComponents.literal(data.backBling() == null ? "No Back Bling" : "Back Bling"), 100, false);
			backBlingHeader.addChild(new Button(0, 0, 100, 20, TextComponents.literal("Change"), b -> System.out.println("would change")));

			if (data.backBling() != null) {
				this.addTextTo(this.backBlingSection, TextComponents.literal(data.backBling().name()), 200, false).active = false;
			}

			this.backBlingSection.calculateDimensions();
		}
		else {
			this.backBlingSection = this.createDisabledSection("Back Bling");
		}

		List<Section> availableDivs = ImmutableList.of(this.cloakSection, this.loreSection, this.hatsSection, this.shoulderBuddiesSection, this.backBlingSection);

		// if first time, initialise selected to capes
		// otherwise, set selected to the *current* div of the section we want
		// we need to make sure it can resize correctly so we can't just generate stuff once
		if (this.selected == null) {
			this.selected = this.cloakSection;
		}
		else {
			// I did this as a foreach loop instead of a stream b/c I'm hotswapping this in ;)
			for (Section s : availableDivs) {
				if (s.getMessage().equals(this.selected.getMessage())) {
					this.selected = s;
					break;
				}
			}
		}

		// left selection menu

		for (Section section : availableDivs) {
			Button button = this.addButton(100, 20, section.getMessage(), b -> this.select(section));

			if (section == this.selected) {
				button.active = false;
			}
		}

		// right selected area
		this.selected.x = this.width / 2;
		this.selected.y = this.height / 2 - availableDivs.size() * 12 - 2;
		this.addRenderableWidget(this.selected);

		// done button
		this.addDone(this.height - 40);
	}

	private void select(Section section) {
		this.selected = section;
		this.init(this.minecraft, this.width, this.height);
	}

	@Override
	public void afterInit() {
		for (GuiEventListener widget : this.children()) {
			if (widget instanceof Section section) section.repositionChildren();
		}
	}

	// helper stuff

	private TextWidget addText(Component text, int width, boolean centered) {
		return this.addWidget((x, y, w, h, component) -> new TextWidget(x, y, w, h, centered, component), text, width, 20);
	}

	private TextWidget addTextTo(Section section, Component text, int width, boolean centered) {
		return section.addChild(new TextWidget(0, 0, width, 20, centered, text));
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
			this.setWidth(this.children.stream().mapToInt(w -> w.getWidth()).max().orElse(0));
			this.height = this.children.stream().mapToInt(w -> w.getHeight()).sum();
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
		
		private static Div create(String name) {
			return new Div(0, 0, 0, 0, TextComponents.literal(name));
		}
	}

	private static class Span extends Section {
		public Span(int x, int y, int width, int height, Component component) {
			super(x, y, width, height, component);
		}

		@Override
		public void calculateDimensions() {
			super.calculateDimensions();
			this.setWidth(this.children.stream().mapToInt(w -> w.getWidth()).sum());
			this.height = this.children.stream().mapToInt(w -> w.getHeight()).max().orElse(0);
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

		private static Span create(String name) {
			return new Span(0, 0, 0, 0, TextComponents.literal(name));
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

		public <T extends AbstractWidget> T addChild(T widget) {
			this.children.add(widget);
			return widget;
		}

		public void removeChildren() {
			this.children.clear();
		}

		@Override
		public boolean mouseClicked(double x, double y, int i) {
			for (AbstractWidget child : this.children) {
				if (child.x <= x && x < child.x + child.getWidth()) {
					if (child.y <= y && y < child.y + child.getHeight()) {
						return child.mouseClicked(x, y, i);
					}
				}
			}

			return false;
		}

		@Override
		public void onRelease(double x, double y) {
			for (AbstractWidget child : this.children) {
				if (child.x <= x && x < child.x + child.getWidth()) {
					if (child.y <= y && y < child.y + child.getHeight()) {
						child.onRelease(x, y);
						return;
					}
				}
			}
		}

		@Override
		public boolean mouseDragged(double x, double y, int button, double prevX, double prevY) {
			for (AbstractWidget child : this.children) {
				if (child.x <= x && x < child.x + child.getWidth()) {
					if (child.y <= y && y < child.y + child.getHeight()) {
						return child.mouseDragged(x, y, button, prevX, prevY);
					}
				}
			}

			return false;
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
