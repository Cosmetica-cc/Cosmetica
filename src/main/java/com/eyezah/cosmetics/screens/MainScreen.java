package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.api.CapeServer;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.impl.CosmeticaWebAPI;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.TextComponents;
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
			this.minecraft.setScreen(new CustomiseCosmeticsScreen(this, this.fakePlayer, this.cosmeticaOptions, Cosmetica.getConfig().shouldInlineChangeButton(), 1.0 - this.getTransitionProgress()))
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

					if (Debug.TEST_MODE) {
						Debug.loadTestProperties();
						Debug.loadTestModel(Debug.LocalModelType.HAT);
						Debug.loadTestModel(Debug.LocalModelType.LEFT_SHOULDERBUDDY);
						Debug.loadTestModel(Debug.LocalModelType.RIGHT_SHOULDERBUDDY);
						Debug.loadTestModel(Debug.LocalModelType.BACK_BLING);
						Debug.loadTestCape();
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
}
