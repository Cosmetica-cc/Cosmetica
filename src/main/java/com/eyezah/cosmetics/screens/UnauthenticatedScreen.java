package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.Cosmetics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Objects;

import static com.eyezah.cosmetics.Cosmetics.*;

public class UnauthenticatedScreen extends Screen {
	private Screen parentScreen;
	private Options parentOptions;
	private boolean fromSave;

	private Component reason = new TranslatableComponent("extravagantCosmetics.unauthenticated.message");
	private MultiLineLabel message;
	private int textHeight;

	public UnauthenticatedScreen(Screen parentSceen, Options parentOptions, boolean fromSave) {
		super(new TranslatableComponent("extravagantCosmetics.unauthenticated"));
		this.parentScreen = parentScreen;
		this.parentOptions = parentOptions;
		this.fromSave = fromSave;
		//updateParentScreen(parentScreen, parentOptions);
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		int var10001 = this.message.getLineCount();
		Objects.requireNonNull(this.font);
		this.textHeight = var10001 * 9;
		int var10003 = this.width / 2 - 100;
		int var10004 = this.height / 2 + this.textHeight / 2;
		Objects.requireNonNull(this.font);
		if (fromSave) {
			this.addRenderableWidget(new Button(var10003, Math.min(var10004 + 9, this.height - 30), 200, 20, new TranslatableComponent("extravagantCosmetics.okay"), (button) -> {
				this.minecraft.setScreen(new OptionsScreen(screenStorage, optionsStorage));
			}));
		} else {
			this.addRenderableWidget(new Button(var10003, Math.min(var10004 + 9, this.height - 30), 200, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
				this.minecraft.setScreen(new SkinCustomizationScreen(new OptionsScreen(screenStorage, optionsStorage), this.parentOptions));
			}));
			this.addRenderableWidget(new Button(var10003, Math.min(var10004 + 9, this.height - 30) + 24, 200, 20, new TranslatableComponent("gui.cancel"), (button) -> {
				this.minecraft.setScreen(new OptionsScreen(screenStorage, optionsStorage));
			}));
		}
	}

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		Font var10001 = this.font;
		Component var10002 = this.title;
		int var10003 = this.width / 2;
		int var10004 = this.height / 2 - this.textHeight / 2;
		Objects.requireNonNull(this.font);
		drawCenteredString(poseStack, var10001, var10002, var10003, var10004 - 9 * 2, 11184810);
		this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
		super.render(poseStack, i, j, f);
	}
}
