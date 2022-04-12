package com.eyezah.cosmetics.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Objects;

import static com.eyezah.cosmetics.Authentication.currentlyAuthenticating;
import static com.eyezah.cosmetics.Cosmetica.authServerHost;
import static com.eyezah.cosmetics.Cosmetica.authServerPort;

public class OfflineScreen extends Screen {
	private Screen parentScreen;

	private Component reason = new TranslatableComponent("cosmetica.offline.message");
	private MultiLineLabel message;
	private int textHeight;

	public OfflineScreen(Screen parentScreen) {
		super(new TranslatableComponent("cosmetica.offline"));
		this.parentScreen = parentScreen;
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		int var10001 = this.message.getLineCount();
		Objects.requireNonNull(this.font);
		this.textHeight = var10001 * 9;
		int buttonX = this.width / 2 - 100;
		int buttonStartY = Math.min((this.height / 2 + this.textHeight / 2) + 9, this.height - 30);

		this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, new TranslatableComponent("options.skinCustomisation"), button -> this.minecraft.setScreen(new SkinCustomizationScreen(this.parentScreen, Minecraft.getInstance().options))));
		this.addRenderableWidget(new Button(buttonX, buttonStartY + 24, 200, 20, new TranslatableComponent("cosmetica.unauthenticated.retry"), (button) -> {
			minecraft.setScreen(new LoadingScreen(this.parentScreen, minecraft.options));
		}));
		this.addRenderableWidget(new Button(buttonX, buttonStartY + 48, 200, 20, new TranslatableComponent("cosmetica.okay"), button -> this.onClose()));
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		Objects.requireNonNull(this.font);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
		this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
		super.render(poseStack, i, j, f);
	}
}
