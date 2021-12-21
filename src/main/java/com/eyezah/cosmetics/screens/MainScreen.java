package com.eyezah.cosmetics.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;


public class MainScreen extends Screen {

	private Screen parentScreen;
	private Options parentOptions;

	public MainScreen(Screen parentScreen, Options parentOptions) {
		super(new TranslatableComponent("extravagantCosmetics.cosmeticsMenu"));
		this.parentScreen = parentScreen;
		this.parentOptions = parentOptions;
	}




	@Override
	protected void init() {
		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 - 12 + 24 * 1, 150, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
			this.minecraft.setScreen(new SkinCustomizationScreen(this, this.parentOptions));
		}));

		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 - 12 + 24 * 1, 150, 20, new TranslatableComponent("extravagantCosmetics.autralians"), (button) -> {
			//this.minecraft.setScreen(new SkinCustomizationScreen(this, this.parentOptions));
		}));




		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 3, 200, 20, new TranslatableComponent("extravagantCosmetics.customizeCosmetics"), (button) -> {
			try {
				Util.getPlatform().openUri("https://eyezah.com/cosmetics/link?" + Minecraft.getInstance().getUser().getAccessToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		}));

		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 - 12 + 24 * 4, 200, 20, CommonComponents.GUI_DONE, (button) -> {
			this.minecraft.setScreen(this.parentScreen);
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
