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

import benzenestudios.sulphate.Anchor;
import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.api.CapeServer;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.impl.CosmeticaWebAPI;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainScreen extends PlayerRenderScreen {
	public MainScreen(Screen parentScreen, UserSettings settings, FakePlayer fakePlayer) {
		super(TextComponents.translatable("cosmetica.cosmeticaMainMenu"), parentScreen, fakePlayer);

		this.cosmeticaOptions = new ServerOptions(settings);
		this.capeServerSettings = Cosmetica.map(settings.getCapeServerSettings(), CapeServer::getDisplay);
		this.capeServerSettingsForButtons = new ArrayList<>(settings.getCapeServerSettings().entrySet());
		Collections.sort(this.capeServerSettingsForButtons, Comparator.comparingInt(a -> a.getValue().getCheckOrder()));;

		this.setAnchorX(Anchor.LEFT, () -> this.width / 2);
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2);
		this.setTransitionProgress(1.0f);
	}

	private ServerOptions cosmeticaOptions;
	private Map<String, CapeDisplay> capeServerSettings;
	private List<Map.Entry<String, CapeServer>> capeServerSettingsForButtons;
	private AbstractButton done;
	private boolean doReload;

	@Override
	protected void addWidgets() {
		this.addButton(150, 20, TextComponents.translatable("cosmetica.customizeCosmetics"), button ->
			this.minecraft.setScreen(new CustomiseCosmeticsScreen(this, this.fakePlayer, this.cosmeticaOptions, 1.0 - this.getTransitionProgress()))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.capeServerSettings"), button ->
			this.minecraft.setScreen(new CapeServerSettingsScreen(this, this.capeServerSettings, this.capeServerSettingsForButtons))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.cosmeticaSettings"), button ->
			this.minecraft.setScreen(new CosmeticaSettingsScreen(this, this.cosmeticaOptions))
		);

		this.addButton(150, 20, TextComponents.translatable("options.skinCustomisation"), button ->
			this.minecraft.setScreen(new SkinCustomizationScreen(this, Minecraft.getInstance().options))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.openWebPanel"), button -> {
			try {
				Minecraft.getInstance().keyboardHandler.setClipboard(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
				Util.getPlatform().openUri(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		});

		class ReloadingButton extends Button {
			public ReloadingButton(int i, int j, int k, int l, Component component, Button.OnPress onPress, Button.OnTooltip tooltip) {
				super(i, j, k, l, component, onPress, tooltip);
			}

			public void onPress() {
				if (doReload) {
					Cosmetica.clearAllCaches();

					if (DebugMode.ENABLED) {
						DebugMode.reloadTestModels();
					}
				}

				this.onPress.onPress(this);
			}
		}

		this.done = this.addDoneWithOffset(ReloadingButton::new, 12 + 24);

		this.initialPlayerLeft = this.width / 2;
		this.deltaPlayerLeft = this.width / 3 + 10 - this.initialPlayerLeft;
	}

	@Override
	public void afterInit() {
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.done.y - 24, 200, 20, TextComponents.translatable("cosmetica.reloadCosmetics"), button -> {
			doReload = !doReload;

			if (doReload) {
				button.setMessage(TextComponents.translatable("cosmetica.willReload"));
			} else {
				button.setMessage(TextComponents.translatable("cosmetica.reloadCosmetics"));
			}
		}));
	}

	void setCapeServerSettings(Map<String, CapeDisplay> settings) {
		this.capeServerSettings = settings;
	}

	void setCosmeticaOptions(ServerOptions options) {
		this.cosmeticaOptions = options;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.renderRSENotif(matrices, mouseX, mouseY);
	}
}
