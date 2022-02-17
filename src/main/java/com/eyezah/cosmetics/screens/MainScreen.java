package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import com.eyezah.cosmetics.Authentication;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.utils.Debug;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

public class MainScreen extends SulphateScreen {
	public MainScreen(Screen parentScreen, boolean doShoulderBuddies, boolean doHats, boolean doRegionSpecificEffects, boolean doLore) {
		super(new TranslatableComponent("cosmetica.cosmeticsMenu"), parentScreen);
		this.parentScreen = parentScreen;

		this.oldOptions = new ServerOptions(doShoulderBuddies, doHats, doRegionSpecificEffects, doLore);
		this.newOptions = new ServerOptions(this.oldOptions);

		this.setAnchorY(Anchor.TOP, () -> this.height / 6 + 12);
		this.setRows(2);
	}

	private final Screen parentScreen;
	private final ServerOptions oldOptions;
	private final ServerOptions newOptions;

	private boolean doReload;
	private boolean doTestReload;

	private TextComponent generateButtonToggleText(String translatable, boolean toggle) {
		TextComponent component = new TextComponent("");
		component.append(new TranslatableComponent(translatable));
		component.append(": ");
		if (toggle) {
			component.append(new TranslatableComponent("cosmetica.enabled"));
		} else {
			component.append(new TranslatableComponent("cosmetica.disabled"));
		}
		return component;
	}

	@Override
	protected void addWidgets() {
		// top row

		this.addButton(new TranslatableComponent("options.skinCustomisation"), (button) -> {
			this.minecraft.setScreen(new SkinCustomizationScreen(this, Minecraft.getInstance().options));
		});

		this.addButton(generateButtonToggleText("cosmetica.australians", this.newOptions.regionSpecificEffects.get()), button -> {
			this.newOptions.regionSpecificEffects.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.australians", this.newOptions.regionSpecificEffects.get()));
		});

		// second row, etc...

		this.addButton(new TranslatableComponent("cosmetica.reloadCosmetics"), (button) -> {
			doReload = !doReload;
			if (doReload) {
				button.setMessage(new TranslatableComponent("cosmetica.willReload"));
			} else {
				button.setMessage(new TranslatableComponent("cosmetica.reloadCosmetics"));
			}
		});

		this.addButton(generateButtonToggleText("cosmetica.doHats", this.newOptions.hats.get()), button -> {
			this.newOptions.hats.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.doHats", this.newOptions.hats.get()));
		});

		this.addButton(generateButtonToggleText("cosmetica.doLore", this.newOptions.lore.get()), (button) -> {
			this.newOptions.lore.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.doLore", this.newOptions.lore.get()));
		});

		this.addButton(generateButtonToggleText("cosmetica.doShoulderBuddies", this.newOptions.shoulderBuddies.get()), (button) -> {
			this.newOptions.shoulderBuddies.toggle();
			button.setMessage(generateButtonToggleText("cosmetica.doShoulderBuddies", this.newOptions.shoulderBuddies.get()));
		});

		if (Debug.TEST_MODE) {
			this.addButton(200, 20, new TranslatableComponent("cosmetica.reloadTestCosmetics"), (button) -> {
				doTestReload = !doTestReload;

				if (doTestReload) {
					button.setMessage(new TranslatableComponent("cosmetica.willReload"));
				} else {
					button.setMessage(new TranslatableComponent("cosmetica.reloadTestCosmetics"));
				}
			});
		}

		// bottom of the menu
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 5, 200, 20, new TranslatableComponent("cosmetica.customizeCosmetics"), (button) -> {
			try {
				Util.getPlatform().openUri(Cosmetica.websiteHost + "/manage?" + Authentication.getToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		}));

		// when done, update settings
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 6, 200, 20, CommonComponents.GUI_DONE, (button) -> {
			if (this.doTestReload) {
				Debug.loadTestProperties();
				Debug.loadTestModel(Debug.LocalModelType.HAT);
				Debug.loadTestModel(Debug.LocalModelType.SHOULDERBUDDY);
			}

			try {
				this.minecraft.setScreen(new UpdatingSettingsScreen(this.parentScreen, this.oldOptions, this.newOptions, this.doReload));
			} catch (IOException e) {
				e.printStackTrace();
				this.minecraft.setScreen(this.parentScreen);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.minecraft.setScreen(this.parentScreen);
			}
		}));
	}

	// on close is like cancel
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}
}
