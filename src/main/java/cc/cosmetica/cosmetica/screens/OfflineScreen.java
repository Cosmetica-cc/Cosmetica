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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Objects;

public class OfflineScreen extends Screen {
	private Screen parentScreen;

	private Component reason = new TranslatableComponent("cosmetica.offline.message");
	private MultiLineLabel message;
	private int textHeight;
	private final int reloadTarget;

	public OfflineScreen(Screen parentScreen) {
		this(parentScreen, 0);
	}

	public OfflineScreen(Screen parentScreen, int reloadTarget) {
		super(new TranslatableComponent("cosmetica.offline"));
		this.parentScreen = parentScreen;
		this.reloadTarget = reloadTarget;
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
			minecraft.setScreen(new LoadingScreen(this.parentScreen, this.minecraft.options, this.reloadTarget));
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
