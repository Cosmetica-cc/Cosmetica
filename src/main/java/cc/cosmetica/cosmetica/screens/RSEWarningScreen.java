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

import benzenestudios.sulphate.ClassicButton;
import cc.cosmetica.api.Box;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.cosmetics.CapeData;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.cosmetics.model.Models;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.DefaultPlayerSkin;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class RSEWarningScreen extends Screen {
	public RSEWarningScreen(@Nullable Screen parent) {
		super(TextComponents.translatable("cosmetica.rsewarning.title"));
		this.parent = parent;
		hasShown = true;
	}

	@Nullable
	private final Screen parent;
	private MultiLineLabel message;

	private FakePlayer australian;
	private FakePlayer persian;

	@Override
	protected void init() {
		UUID aussieUUID = UUID.nameUUIDFromBytes("Aussie".getBytes(StandardCharsets.UTF_8));
		UUID canadianUUID = UUID.nameUUIDFromBytes("Eh".getBytes(StandardCharsets.UTF_8));

		this.australian = new FakePlayer(this.minecraft, aussieUUID, "Aussie", new PlayerData(
				"",
				true,
				null,
				true,
				"",
				"",
				List.of(),
				CapeData.NO_CAPE,
				null,
				null,
				null,
				DefaultPlayerSkin.get(aussieUUID).texture(),
				false
		));

		this.persian = new FakePlayer(this.minecraft, canadianUUID, "Canadian", new PlayerData(
				"",
				false,
				null,
				true,
				"",
				", eh?",
				List.of(),
				CapeData.NO_CAPE,
				null,
				null,
				null,
				DefaultPlayerSkin.get(canadianUUID).texture(),
				true
		));

		this.message = MultiLineLabel.create(this.font, TextComponents.translatable("cosmetica.rsewarning.description"), this.width - 30);

		int y = this.height / 2 + 8 * this.message.getLineCount() + 24 + bottomTextOffset;

		this.addRenderableWidget(new ClassicButton(this.width / 2 + 20, y, 120, 20, TextComponents.translatable("cosmetica.options.yes"), bn -> setRSEAndClose(true)));
		this.addRenderableWidget(new ClassicButton(this.width / 2 - 140, y, 120, 20, TextComponents.translatable("cosmetica.options.no"), bn -> setRSEAndClose(false)));
	}

	private void setRSEAndClose(boolean enabled) {
		Thread requestThread = new Thread(() -> {
			if (Cosmetica.api.isAuthenticated()) {
				Cosmetica.api.updateUserSettings(ImmutableMap.of("doregioneffects", enabled)).ifSuccessfulOrElse(j -> DebugMode.log("Received successful response for RSE update."), Cosmetica.logErr("Error while setting region specific effects!"));
			}
			else {
				Cosmetica.LOGGER.warn("Could not update RSE because you are not authenticated!");
			}
		});

		requestThread.start();
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float f) {
		this.renderBackground(graphics, mouseX, mouseY, f);
		super.render(graphics, mouseX, mouseY, f);

		PoseStack stack = graphics.pose();

		stack.pushPose();
		stack.scale(1.5f, 1.5f, 0);
		graphics.drawCenteredString(this.font, TextComponents.translatable("cosmetica.rsewarning.title"), this.width / 3, this.height / 3 - 60, 0xDADADA);
		stack.popPose();

		this.message.renderCentered(graphics, this.width / 2, this.height / 2 + 12 + bottomTextOffset);

		int characterOffset = this.width / 2 - this.width / 12;
		int characterY = this.height / 2 + 24;

		PlayerRenderScreen.renderFakePlayerInMenu(characterOffset, characterY, 20.0f, characterOffset - mouseX, characterY - 90 - mouseY, this.australian);
		PlayerRenderScreen.renderFakePlayerInMenu(this.width - characterOffset, characterY, 20.0f, this.width - characterOffset - mouseX, characterY - 90 - mouseY, this.persian);
	}

	public static boolean hasShown() {
		return hasShown;
	}

	// only set this true if we are online
	public static boolean appearNextScreenChange = false;
	private static boolean hasShown = false;
	private static int bottomTextOffset = 24;
}
