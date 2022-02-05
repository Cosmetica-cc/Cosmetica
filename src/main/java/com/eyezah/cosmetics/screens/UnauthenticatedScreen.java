package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.Cosmetics;
import com.eyezah.cosmetics.utils.Debug;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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

	public UnauthenticatedScreen(Screen parentScreen, Options parentOptions, boolean fromSave) {
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
		int buttonX = this.width / 2 - 100;
		int buttonStartY = Math.min((this.height / 2 + this.textHeight / 2) + 9, this.height - 30);

		if (fromSave) {
			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, new TranslatableComponent("extravagantCosmetics.okay"), (button) -> {
				this.minecraft.setScreen(new OptionsScreen(screenStorage, optionsStorage));
			}));
		} else {
			if (Debug.DEBUG_MODE) { // because I'm not authenticated in dev and can't use the normal button
				this.addRenderableWidget(new Button(buttonX, buttonStartY + 48, 200, 20, new TextComponent("Immediately Clear Caches"), btn -> Cosmetics.clearAllCaches()));
			}

			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
				this.minecraft.setScreen(new SkinCustomizationScreen(new OptionsScreen(screenStorage, optionsStorage), this.parentOptions));
			}));
			this.addRenderableWidget(new Button(buttonX, buttonStartY + 24, 200, 20, new TranslatableComponent("gui.cancel"), (button) -> {
				this.minecraft.setScreen(screenStorage);
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
