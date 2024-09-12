/*
 * Copyright 2022, 2023 EyezahMC
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

import cc.cosmetica.cosmetica.utils.LoadingTypeScreen;
import cc.cosmetica.cosmetica.Authentication;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import static cc.cosmetica.cosmetica.Authentication.runAuthentication;

public class LoadingScreen extends Screen implements LoadingTypeScreen {
	private Screen parentScreen;
	private Options parentOptions;

	private Component reason = new TranslatableComponent("cosmetica.loading.message");
	private MultiLineLabel message;
	private int textHeight;

	public LoadingScreen(Screen parentScreen, Options parentOptions) {
		this(parentScreen, parentOptions, 0);
	}

	public LoadingScreen(Screen parentScreen, Options parentOptions, int target) {
		super(new TranslatableComponent("cosmetica.loading"));
		this.parentScreen = parentScreen;
		this.parentOptions = parentOptions;
		Authentication.settingLoadTarget = target;
		
		Minecraft.getInstance().tell(Authentication::runAuthentication);
	}

	@Override
	public Screen getParent() {
		return this.parentScreen;
	}

	@Override
	public void onClose() {
		Authentication.snipedPlayer = null;
		super.onClose();
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		this.textHeight = this.message.getLineCount() * 9;

		this.addRenderableWidget(new Button(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30), 200, 20, new TranslatableComponent("options.skinCustomisation"), (button) -> {
			this.minecraft.setScreen(new SkinCustomizationScreen(this.parentScreen, this.parentOptions));
		})).active = Authentication.settingLoadTarget != 3;
		this.addRenderableWidget(new Button(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30) + 24, 200, 20, new TranslatableComponent("gui.cancel"), (button) -> {
			this.onClose();
		})).active = Authentication.settingLoadTarget != 3;
	}

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
		this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return Authentication.settingLoadTarget != 3;
	}
}
