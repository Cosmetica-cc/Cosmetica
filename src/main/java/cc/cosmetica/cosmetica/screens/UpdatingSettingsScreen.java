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

import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.Debug;
import cc.cosmetica.cosmetica.utils.LoadingTypeScreen;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UpdatingSettingsScreen extends Screen implements LoadingTypeScreen {
	private Screen parentScreen;

	private Component reason = TextComponents.translatable("cosmetica.updating.message");
	private MultiLineLabel message;
	private int textHeight;

	/**
	 * For regular settings.
	 */
	public UpdatingSettingsScreen(Screen parentScreen, ServerOptions oldOptions, ServerOptions newOptions) throws IOException, InterruptedException {
		super(TextComponents.translatable("cosmetica.updating"));
		this.parentScreen = parentScreen;

		Map<String, Object> changedSettings = new HashMap<>();

		boolean doReload = newOptions.regionSpecificEffects.appendToIfChanged(oldOptions.regionSpecificEffects, changedSettings);
		doReload |= newOptions.shoulderBuddies.appendToIfChanged(oldOptions.shoulderBuddies, changedSettings);
		doReload |= newOptions.hats.appendToIfChanged(oldOptions.hats, changedSettings);
		doReload |= newOptions.lore.appendToIfChanged(oldOptions.lore, changedSettings);
		doReload |= newOptions.backBlings.appendToIfChanged(oldOptions.backBlings, changedSettings);
		boolean finalDoReload = doReload;

		if (!changedSettings.isEmpty()) {
			Thread requestThread = new Thread(() -> {
				Cosmetica.api.updateUserSettings(changedSettings).ifSuccessfulOrElse(response -> {
					if (finalDoReload) Minecraft.getInstance().tell(() -> {
						Cosmetica.clearAllCaches();

						if (response.booleanValue() && this.parentScreen instanceof PlayerRenderScreen playerRenderScreen) {
							UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid()));
							playerRenderScreen.setPlayerData(Cosmetica.getPlayerData(uuid, Minecraft.getInstance().getUser().getName(), true));
						}
					});

					if (response.booleanValue()) {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(this.parentScreen));
					} else {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(this.parentScreen, true)));
					}
				},
				e -> {
					e.printStackTrace();
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(this.parentScreen, true)));
				});
			});
			requestThread.start();
		} else {
			Debug.info("No settings changed.");
			if (doReload) Cosmetica.clearAllCaches();
			Minecraft.getInstance().tell(this::onClose);
		}
	}

	/**
	 * For Cape Server Settings
	 */
	public UpdatingSettingsScreen(Screen parentScreen, Map<String, CapeDisplay> oldOptions, Map<String, CapeDisplay> newOptions) throws IOException, InterruptedException {
		super(TextComponents.translatable("cosmetica.updating"));
		this.parentScreen = parentScreen;

		boolean updateCapeServerSettings = oldOptions.entrySet().stream().anyMatch(entry -> entry.getValue().id != newOptions.get(entry.getKey()).id);

		if (updateCapeServerSettings) {
			Debug.info("Updating cape server settings.");
			Thread requestThread = new Thread(() -> {
				Cosmetica.api.setCapeServerSettings(newOptions).ifSuccessfulOrElse(response -> {
					if (this.parentScreen instanceof MainScreen main) {
						main.setCapeServerSettings(response);

						UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid()));
						Cosmetica.getPlayerData(uuid, Minecraft.getInstance().getUser().getName(), true);
					}

					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(this.parentScreen));
				},
				e -> {
					e.printStackTrace();
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(this.parentScreen, true)));
				});

				Minecraft.getInstance().tell(() -> Cosmetica.clearAllCaches());
			});
			requestThread.start();
		} else {
			Debug.info("No cape server settings changed.");
			Minecraft.getInstance().tell(this::onClose);
		}
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(this.parentScreen);
	}

	@Override
	public Screen getParent() {
		return this.parentScreen;
	}

	@Override
	protected void init() {
		this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
		int var10001 = this.message.getLineCount();
		Objects.requireNonNull(this.font);
		this.textHeight = var10001 * 9;
	}

	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		int x = this.width / 2;
		int y = this.height / 2 - this.textHeight / 2;
		Objects.requireNonNull(this.font);
		drawCenteredString(poseStack, this.font, this.title, x, y - 9 * 2, 11184810);
		this.message.renderCentered(poseStack, this.width / 2, this.height / 2 - this.textHeight / 2);
		super.render(poseStack, i, j, f);
	}
}
