package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.Authentication;
import com.eyezah.cosmetics.Cosmetica;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

public class MainScreen extends Screen {
	public MainScreen(Screen parentScreen, boolean doShoulderBuddies, boolean doHats, boolean doRegionSpecificEffects, boolean doLore) {
		super(new TranslatableComponent("extravagantCosmetics.cosmeticsMenu"));
		this.parentScreen = parentScreen;

		this.oldOptions = new ServerOptions(doShoulderBuddies, doHats, doRegionSpecificEffects, doLore);
		this.newOptions = new ServerOptions(this.oldOptions);
	}

	private final Screen parentScreen;
	private final ServerOptions oldOptions;
	private final ServerOptions newOptions;

	private boolean doReload;

	private TextComponent generateButtonToggleText(String translatable, boolean toggle) {
		TextComponent component = new TextComponent("");
		component.append(new TranslatableComponent(translatable));
		component.append(": ");
		if (toggle) {
			component.append(new TranslatableComponent("extravagantCosmetics.enabled"));
		} else {
			component.append(new TranslatableComponent("extravagantCosmetics.disabled"));
		}
		return component;
	}

	@Override
	protected void init() {
		// normal stuff

		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 - 12 + 24 * 1, 150, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
			this.minecraft.setScreen(new SkinCustomizationScreen(this, Minecraft.getInstance().options));
		}));

		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 - 12 + 24 * 2, 150, 20, new TranslatableComponent("extravagantCosmetics.reloadCosmetics"), (button) -> {
			doReload = !doReload;
			if (doReload) {
				button.setMessage(new TranslatableComponent("extravagantCosmetics.willReload"));
			} else {
				button.setMessage(new TranslatableComponent("extravagantCosmetics.reloadCosmetics"));
			}
		}));

		// toggles

		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 - 12 + 24 * 1, 150, 20, generateButtonToggleText("extravagantCosmetics.australians", this.newOptions.regionSpecificEffects.get()), button -> {
			this.newOptions.regionSpecificEffects.toggle();
			button.setMessage(generateButtonToggleText("extravagantCosmetics.australians", this.newOptions.regionSpecificEffects.get()));
		}));

		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 - 12 + 24 * 2, 150, 20, generateButtonToggleText("extravagantCosmetics.doHats", this.newOptions.hats.get()), button -> {
			this.newOptions.hats.toggle();
			button.setMessage(generateButtonToggleText("extravagantCosmetics.doHats", this.newOptions.hats.get()));
		}));

		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 - 12 + 24 * 3, 150, 20, generateButtonToggleText("extravagantCosmetics.doShoulderBuddies", this.newOptions.shoulderBuddies.get()), (button) -> {
			this.newOptions.shoulderBuddies.toggle();
			button.setMessage(generateButtonToggleText("extravagantCosmetics.doShoulderBuddies", this.newOptions.shoulderBuddies.get()));
		}));

		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 - 12 + 24 * 3, 150, 20, generateButtonToggleText("extravagantCosmetics.doLore", this.newOptions.lore.get()), (button) -> {
			this.newOptions.lore.toggle();
			button.setMessage(generateButtonToggleText("extravagantCosmetics.doLore", this.newOptions.lore.get()));
		}));

		// bottom of the menu
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 5, 200, 20, new TranslatableComponent("extravagantCosmetics.customizeCosmetics"), (button) -> {
			try {
				Util.getPlatform().openUri(Cosmetica.websiteHost + "/manage?" + Authentication.getToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		}));

		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 6, 200, 20, CommonComponents.GUI_DONE, (button) -> {
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

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
		super.render(poseStack, i, j, f);
	}
}
