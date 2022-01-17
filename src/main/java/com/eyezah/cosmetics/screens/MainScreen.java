package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.Authentication;
import com.eyezah.cosmetics.Cosmetics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

import static com.eyezah.cosmetics.Cosmetics.*;


public class MainScreen extends Screen {

	private Screen parentScreen;
	private Options parentOptions;

	public boolean regionSpecificEffects = doRegionSpecificEffects();
	public boolean doShoulderBuddies = doShoulderBuddies();
	public boolean doReload = false;

	public MainScreen(Screen parentScreen, Options parentOptions) {
		super(new TranslatableComponent("extravagantCosmetics.cosmeticsMenu"));
		this.parentScreen = parentScreen;
		this.parentOptions = parentOptions;
	}

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
		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 - 12 + 24 * 1, 150, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
			this.minecraft.setScreen(new SkinCustomizationScreen(this, this.parentOptions));
		}));
		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 - 12 + 24 * 1, 150, 20, generateButtonToggleText("extravagantCosmetics.australians", regionSpecificEffects), (button) -> {
			regionSpecificEffects = !regionSpecificEffects;
			button.setMessage(generateButtonToggleText("extravagantCosmetics.australians", regionSpecificEffects));
		}));
		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 - 12 + 24 * 2, 150, 20, new TranslatableComponent("extravagantCosmetics.reloadCosmetics"), (button) -> {
			doReload = !doReload;
			if (doReload) {
				button.setMessage(new TranslatableComponent("extravagantCosmetics.willReload"));
			} else {
				button.setMessage(new TranslatableComponent("extravagantCosmetics.reloadCosmetics"));
			}
		}));

		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 - 12 + 24 * 2, 150, 20, generateButtonToggleText("extravagantCosmetics.doShoulderBuddies", doShoulderBuddies), (button) -> {
			doShoulderBuddies = !doShoulderBuddies;
			button.setMessage(generateButtonToggleText("extravagantCosmetics.doShoulderBuddies", doShoulderBuddies));
		}));


		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 5, 200, 20, new TranslatableComponent("extravagantCosmetics.customizeCosmetics"), (button) -> {
			try {
				Util.getPlatform().openUri("https://eyezah.com/cosmetics/manage?" + Authentication.getToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		}));

		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 6, 200, 20, CommonComponents.GUI_DONE, (button) -> {
			try {
				this.minecraft.setScreen(new UpdatingSettings(this.parentScreen, this.parentOptions, regionSpecificEffects, doReload, doShoulderBuddies));
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

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
		super.render(poseStack, i, j, f);
	}
}
