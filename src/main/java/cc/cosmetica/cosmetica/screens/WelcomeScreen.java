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

import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;

import java.util.UUID;

public class WelcomeScreen extends PlayerRenderScreen {
	public WelcomeScreen(Screen parent, UUID uuid, String playerName, PlayerData data) {
		super(TextComponents.translatable("cosmetica.welcome.blanktitle"), null, null);
		this.parent = parent;
		this.playerUUID = uuid;
		this.playerName = playerName;
		this.playerData = data;
		this.panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
		isInTutorial = true;

		this.setTransitionProgress(1.0f);
		this.playerTopMod = 40;
	}

	// to construct a fake player
	private final UUID playerUUID;
	private final String playerName;
	private final PlayerData playerData;

	private final Screen parent;
	private final PanoramaRenderer panorama;

	@Override
	protected void addWidgets() {
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 30 + 48, 200, 20,
				TextComponents.translatable("cosmetica.welcome.continue"), bn -> this.minecraft.setScreen(new WelcomeOptionsScreen(this.parent, this.minecraft.options))));

		this.initialPlayerLeft = this.width / 2;
		this.deltaPlayerLeft = 0;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(PoseStack stack, int x, int y, float time) {
		if (this.fakePlayer == null) {
			this.fakePlayer = new FakePlayer(Minecraft.getInstance(), this.playerUUID, this.playerName, this.playerData);
		}

		this.panorama.render(time, 1);
		super.render(stack, x, y, time);

		stack.pushPose();
		stack.scale(1.75f, 1.75f, 0);
		drawCenteredString(stack, this.font, TextComponents.translatable("cosmetica.welcome.header"), (int)(this.width / 3.5), (int)(this.height / 1.75f) / 2 + 30, 0xEEEEEE);
		stack.popPose();
	}

	@Override
	public void renderBackground(PoseStack stack) {
		// no dirt background.
	}

	public static boolean isInTutorial = false;
}