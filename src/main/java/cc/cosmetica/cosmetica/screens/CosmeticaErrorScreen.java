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

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.cosmetica.Authentication;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CosmeticaErrorScreen extends SulphateScreen {
	public CosmeticaErrorScreen(Screen parentScreen, Component title, Component message) {
		super(title);
		this.parentScreen = parentScreen;
		this.message = message;
	}

	private final Screen parentScreen;
	private final Component message;

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(this.parentScreen);
	}

	@Override
	protected void addWidgets() {
		this.addButton(CommonComponents.GUI_CANCEL, k -> this.onClose());
	}

	@Override
	public void render(GuiGraphics stack, int i, int j, float f) {
		super.render(stack, i, j, f);
		stack.drawCenteredString(this.font, this.message, this.width / 2, this.height / 2, 16777215);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return Authentication.settingLoadTarget != 3;
	}
}
