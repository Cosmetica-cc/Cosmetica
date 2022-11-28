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

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;

public class WelcomeScreen extends Screen {
	public WelcomeScreen(Screen parent) {
		super(TextComponents.translatable("cosmetica.welcome.header"));
		this.parent = parent;
		isInTutorial = true;
	}

	private final Screen parent;

	@Override
	protected void init() {
		this.addRenderableWidget(new Button(this.width / 2 - 100, 2 * this.height / 3, 200, 20,
				TextComponents.translatable("cosmetica.welcome.continue"), bn -> this.minecraft.setScreen(new WelcomeOptionsScreen(this.parent, this.minecraft.options))));
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(PoseStack stack, int i, int j, float f) {
		this.renderBackground(stack);
		super.render(stack, i, j, f);

		stack.pushPose();
		stack.scale(1.5f, 1.5f, 0);
		drawCenteredString(stack, this.font, TextComponents.translatable("cosmetica.welcome.header"), this.width / 3, this.height / 3 - 30, 0xDADADA);
		stack.popPose();
	}

	@Override
	public void renderBackground(PoseStack poseStack) {
		Cosmetica.renderTexture(poseStack.last().pose(), new ResourceLocation("cosmetica", "textures/welcome.png"), 0, this.width, 0 ,this.height, -1);
	}

	public static boolean isInTutorial = false;
}