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

import cc.cosmetica.api.ServerResponse;
import cc.cosmetica.cosmetica.Authentication;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.utils.LoadingTypeScreen;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.utils.DebugMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static cc.cosmetica.cosmetica.Cosmetica.clearAllCaches;

public class UpdatingCosmeticsScreen<T> extends Screen implements LoadingTypeScreen {
	private Screen parentScreen;

	private Component reason = new TranslatableComponent("cosmetica.updating.message");
	private MultiLineLabel message;
	private int textHeight;

	/**
	 * For Cape Server Settings
	 */
	public UpdatingCosmeticsScreen(Screen parentScreen, Supplier<ServerResponse<T>> contact) {
		super(new TranslatableComponent("cosmetica.updating"));
		this.parentScreen = parentScreen;

		DebugMode.log("Updating cape server settings.");
		Thread requestThread = new Thread(() -> {
			contact.get().ifSuccessfulOrElse(response -> {
				if (this.parentScreen instanceof PlayerRenderScreen prs) {
					UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid()));

					if (this.minecraft.level == null)
						PlayerData.clear(uuid);
					else
						Cosmetica.safari(this.minecraft, false, true);

					prs.setPlayerData(PlayerData.get(uuid, Minecraft.getInstance().getUser().getName(), true));
				}

				Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(this.parentScreen));
			},
			e -> {
				e.printStackTrace();
				Authentication.showUnauthenticatedIfLoading(true, e);
			});

			Minecraft.getInstance().tell(() -> clearAllCaches());
		});

		requestThread.start();
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
