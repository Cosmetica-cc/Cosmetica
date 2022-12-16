/*
 * Copyright 2022 EyezahMC
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

package cc.cosmetica.cosmetica.screens;

import benzenestudios.sulphate.Anchor;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CustomCosmetic;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.cosmetica.cosmetics.BackBling;
import cc.cosmetica.cosmetica.cosmetics.CustomLayer;
import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.screens.widget.TextWidget;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ViewCosmeticsScreen extends PlayerRenderScreen {
	public ViewCosmeticsScreen(Component title, Screen parentScreen, FakePlayer player, UserSettings settings) {
		this(title, parentScreen, player, new ServerOptions(settings), 1.0);
	}

	protected ViewCosmeticsScreen(Component title, Screen parentScreen, FakePlayer player, ServerOptions options, double transitionProgress) {
		super(title, parentScreen, player);

		this.setAnchorX(Anchor.RIGHT, () -> this.width / 2 - 50);
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2);

		this.options = options;

		this.setTransitionProgress(transitionProgress);
	}

	private final ServerOptions options;

	private Section cloakSection;
	private Section loreSection;
	private Section hatsSection;
	private Section shoulderBuddiesSection;
	private Section backBlingSection;

	private Section selected;

	private Section createDisabledSection(String title) {
		Div section = Div.create(title);

		this.addTextTo(section, TextComponents.translatable("cosmetica.entry." + title.replace(" ", "")), 100, false);
		this.addTextTo(section, TextComponents.translatable("cosmetica.entry.disabled"), 100, false);

		section.calculateDimensions();
		return section;
	}

	protected  <T extends CustomCosmetic, E> Section createActiveSection(String title, List<String> items, @Nullable CosmeticType<T> type, @Nullable CosmeticStack<E> stack) {
		Div section = Div.create(title);
		Component headerText = TextComponents.translatable("cosmetica.entry." + title.replace(" ", ""));

		this.addTextTo(section, headerText, 200, false);

		if (items.isEmpty()) {
			this.addTextTo(section, TextComponents.translatable("cosmetica.entry.none"), 200, false).active = false;
		}
		else {
			for (String item : items) {
				this.addTextTo(section, TextComponents.literal(item), 200, false).active = false;
			}
		}

		section.calculateDimensions();
		return section;
	}

	private List<String> immutableListOf(String str) {
		return str.isEmpty() ? ImmutableList.of() : ImmutableList.of(str);
	}

	private List<String> immutableListOf(String mainString, String additionalString) {
		return mainString.isEmpty() ? ImmutableList.of() : ImmutableList.of(mainString, additionalString);
	}

	@Override
	protected void addWidgets() {
		PlayerData data = this.fakePlayer.getData();

		// cape
		this.cloakSection = this.createActiveSection("Cape", immutableListOf(data.cape().getName(), "\u00A78" + data.cape().getOrigin()), CosmeticType.CAPE, CustomLayer.CAPE_OVERRIDER);

		// lore
		this.loreSection = this.options.lore.get() ? this.createActiveSection("Lore", immutableListOf(data.lore()), null, null) : this.createDisabledSection("Lore");

		// hats
		this.hatsSection = this.options.hats.get() ? this.createActiveSection("Hats", data.hats().stream().map(BakableModel::name).collect(Collectors.toList()), CosmeticType.HAT, Hats.OVERRIDDEN) : this.createDisabledSection("Hats");

		// sbs
		List<String> shoulderBuddies = ImmutableList.of(
				"Left: " + (data.leftShoulderBuddy() == null ? "None" : data.leftShoulderBuddy().name()),
				"Right: " + (data.rightShoulderBuddy() == null ? "None" : data.rightShoulderBuddy().name())
		);

		this.shoulderBuddiesSection = this.options.shoulderBuddies.get() ? this.createActiveSection("Shoulder Buddies", shoulderBuddies, CosmeticType.SHOULDER_BUDDY, ShoulderBuddies.RIGHT_OVERRIDDEN) : this.createDisabledSection("Shoulder Buddies");

		// back bling
		this.backBlingSection = this.options.backBlings.get() ? this.createActiveSection("Back Bling", data.backBling() == null ? ImmutableList.of() : ImmutableList.of(data.backBling().name()), CosmeticType.BACK_BLING, BackBling.OVERRIDDEN) : this.createDisabledSection("Back Bling");

		// the whole gang
		List<Section> availableSections = ImmutableList.of(this.cloakSection, this.loreSection, this.hatsSection, this.shoulderBuddiesSection, this.backBlingSection);

		// if first time, initialise selected to capes
		// otherwise, set selected to the *current* div of the section we want
		// we need to make sure it can resize correctly so we can't just generate stuff once
		if (this.selected == null) {
			this.selected = this.cloakSection;
		}
		else {
			// I did this as a foreach loop instead of a stream b/c I'm hotswapping this in ;)
			for (Section s : availableSections) {
				if (s.getMessage().equals(this.selected.getMessage())) {
					this.selected = s;
					break;
				}
			}
		}

		// left selection menu

		for (Section section : availableSections) {
			Button button = this.addButton(100, 20, section.getMessage(), b -> this.select(section));

			if (section == this.selected) {
				button.active = false;
			}
		}

		// right selected area
		this.selected.x = this.width / 2 + 50;
		this.selected.y = this.height / 2 - availableSections.size() * 12 - 2;
		this.addRenderableWidget(this.selected);

		// done button
		this.addDone(this.height - 40);

		this.initialPlayerLeft = this.width / 3 + 10;
		this.deltaPlayerLeft = this.width / 2 - this.initialPlayerLeft;
	}

	private void select(Section section) {
		this.selected = section;
		this.init(this.minecraft, this.width, this.height);
	}

	@Override
	public void afterInit() {
		for (GuiEventListener widget : this.children()) {
			if (widget instanceof Section) ((Section)widget).repositionChildren();
		}
	}

	@Override
	public void onClose() {
		if (this.parent instanceof MainScreen) {
			MainScreen main = (MainScreen) this.parent;
			main.setTransitionProgress(1.0 - this.getTransitionProgress());
		}

		super.onClose();
	}

	// helper stuff

	private TextWidget addText(Component text, int width, boolean centered) {
		return this.addWidget((x, y, w, h, component) -> new TextWidget(x, y, w, h, centered, component), text, width, 20);
	}

	TextWidget addTextTo(Section section, Component text, int width, boolean centered) {
		return section.addChild(new TextWidget(0, 0, width, 20, centered, text));
	}

	static class Div extends Section {
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

		static Div create(String name) {
			return new Div(0, 0, 0, 0, TextComponents.literal(name));
		}
	}

	static class Span extends Section {
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

		static Span create(String name) {
			return new Span(0, 0, 0, 0, TextComponents.literal(name));
		}
	}

	abstract static class Section extends AbstractWidget {
		public Section(int i, int j, int k, int l, Component component) {
			super(i, j, k, l, component);
		}

		protected List<AbstractWidget> children = new LinkedList<>();

		public void calculateDimensions() {
			for (AbstractWidget child : this.children) {
				if (child instanceof Section) ((Section) child).calculateDimensions();
			}
		}

		public void repositionChildren() {
			for (AbstractWidget child : this.children) {
				if (child instanceof Section) ((Section) child).repositionChildren();
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
		public void render(PoseStack poseStack, int i, int j, float f) {
			for (AbstractWidget child : this.children) {
				child.render(poseStack, i, j, f);
			}
		}
	}
}
