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

import benzenestudios.sulphate.ModernScreen;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.DebugMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Objects;

public class UnauthenticatedScreen extends ModernScreen {
	private Screen parentScreen;
	private boolean fromSave;

	private Component reason = new TranslatableComponent("cosmetica.unauthenticated.message");
	private MultiLineLabel message;
	private int textHeight;

	public UnauthenticatedScreen(Screen parentScreen, boolean fromSave) {
		super(new TranslatableComponent("cosmetica.unauthenticated"));
		this.parentScreen = parentScreen;
		this.fromSave = fromSave;
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		Objects.requireNonNull(this.font);
		this.textHeight = this.message.getLineCount() * 9;
		int buttonX = this.width / 2 - 100;
		int buttonStartY = Math.min((this.height / 2 + this.textHeight / 2) + 9, this.height - 30);

		if (fromSave) {
			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, new TranslatableComponent("cosmetica.okay"), button -> this.onClose()));
		} else {
			if (DebugMode.ENABLED) { // because I'm not authenticated in dev and can't use the normal button
				this.addRenderableWidget(new Button(buttonX, buttonStartY + 48, 100, 20, new TextComponent("Clear Caches"), btn -> {
					Cosmetica.clearAllCaches();
					if (DebugMode.ENABLED) DebugMode.reloadTestModels();
				}));
			}

			this.addRenderableWidget(new Button(buttonX, buttonStartY, 200, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
				this.minecraft.setScreen(new SkinCustomizationScreen(this.parentScreen, Minecraft.getInstance().options));
			}));
			this.addRenderableWidget(new Button(buttonX, buttonStartY + 24, 200, 20, new TranslatableComponent("cosmetica.unauthenticated.retry"), (button) -> {
				minecraft.setScreen(new LoadingScreen(this.parentScreen, minecraft.options));
			}));
			this.addRenderableWidget(new Button(DebugMode.ENABLED ? buttonX + 100 : buttonX, buttonStartY + 48, DebugMode.ENABLED ? 100 : 200, 20, new TranslatableComponent("gui.cancel"), button -> this.onClose()));
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
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
