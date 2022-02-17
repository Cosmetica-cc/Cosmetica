package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Objects;

import static com.eyezah.cosmetics.Authentication.runAuthentication;

public class LoadingScreen extends Screen implements LoadingTypeScreen {
	private Screen parentScreen;
	private Options parentOptions;

	private Component reason = new TranslatableComponent("cosmetica.loading.message");
	private MultiLineLabel message;
	private int textHeight;

	public LoadingScreen(Screen parentScreen, Options parentOptions) {
		super(new TranslatableComponent("cosmetica.loading"));
		this.parentScreen = parentScreen;
		this.parentOptions = parentOptions;

		Minecraft.getInstance().tell(() -> {
			if (!runAuthentication(parentScreen, 2)) {
				Minecraft.getInstance().setScreen(new OfflineScreen(parentScreen));
			}
		});
	}

	@Override
	public Screen getParent() {
		return this.parentScreen;
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		this.textHeight = this.message.getLineCount() * 9;

		this.addRenderableWidget(new Button(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30), 200, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
			this.minecraft.setScreen(new SkinCustomizationScreen(this.parentScreen, this.parentOptions));
		}));
		this.addRenderableWidget(new Button(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30) + 24, 200, 20, new TranslatableComponent("gui.cancel"), (button) -> {
			this.minecraft.setScreen(this.parentScreen);
		}));
	}

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
		this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
		super.render(poseStack, i, j, f);
	}
}
