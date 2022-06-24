package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.api.CapeServer;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.impl.CosmeticaWebAPI;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.utils.TextComponents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainScreen extends PlayerRenderScreen {
	public MainScreen(Screen parentScreen, UserSettings settings, PlayerData data) {
		super(TextComponents.translatable("cosmetica.cosmeticaMainMenu"), parentScreen, new FakePlayer(Minecraft.getInstance(), UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid())), Minecraft.getInstance().getUser().getName(), data, data.slim()));

		this.cosmeticaOptions = new ServerOptions(settings.doShoulderBuddies(), settings.doHats(), settings.doBackBlings(), settings.hasPerRegionEffects(), settings.doLore());
		this.capeServerSettings = Cosmetica.map(settings.getCapeServerSettings(), CapeServer::getDisplay);
		this.capeServerSettingsForButtons = new ArrayList<>(settings.getCapeServerSettings().entrySet());
		Collections.sort(this.capeServerSettingsForButtons, Comparator.comparingInt(a -> a.getValue().getCheckOrder()));;

		this.setAnchorX(Anchor.LEFT, () -> this.width / 2);
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2);
		this.setTransitionProgress(1.0f);
	}

	private final ServerOptions cosmeticaOptions;
	private Map<String, CapeDisplay> capeServerSettings;
	private List<Map.Entry<String, CapeServer>> capeServerSettingsForButtons;

	@Override
	protected void addWidgets() {
		this.addButton(150, 20, TextComponents.translatable("cosmetica.customizeCosmetics"), button ->
			this.minecraft.setScreen(new CustomiseCosmeticsScreen(this, this.fakePlayer, this.cosmeticaOptions, true, true))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.capeServerSettings"), button ->
			this.minecraft.setScreen(new CapeServerSettingsScreen(this, this.capeServerSettings, this.capeServerSettingsForButtons))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.cosmeticaSettings"), button ->
			this.minecraft.setScreen(new CosmeticaSettingsScreen(this, this.cosmeticaOptions))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.openWebPanel"), button -> {
			try {
				Minecraft.getInstance().keyboardHandler.setClipboard(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
				Util.getPlatform().openUri(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		});

		this.addButton(150, 20, TextComponents.translatable("options.skinCustomisation"), button ->
			this.minecraft.setScreen(new SkinCustomizationScreen(this, Minecraft.getInstance().options))
		);

		this.addDoneWithOffset(12);

		this.initialPlayerLeft = this.width / 2;
		this.deltaPlayerLeft = this.width / 3 + 10 - this.initialPlayerLeft;
	}

	void setCapeServerSettings(Map<String, CapeDisplay> settings) {
		this.capeServerSettings = settings;
	}
}
