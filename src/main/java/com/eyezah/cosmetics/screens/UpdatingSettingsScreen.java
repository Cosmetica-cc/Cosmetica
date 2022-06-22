package com.eyezah.cosmetics.screens;

import cc.cosmetica.api.CapeDisplay;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.eyezah.cosmetics.Cosmetica.clearAllCaches;

public class UpdatingSettingsScreen extends Screen implements LoadingTypeScreen {
	private Screen parentScreen;

	private Component reason = new TranslatableComponent("cosmetica.updating.message");
	private MultiLineLabel message;
	private int textHeight;

	/**
	 * For regular settings.
	 */
	public UpdatingSettingsScreen(Screen parentScreen, ServerOptions oldOptions, ServerOptions newOptions, boolean doReload) throws IOException, InterruptedException {
		super(new TranslatableComponent("cosmetica.updating"));
		this.parentScreen = parentScreen;

		Map<String, Object> changedSettings = new HashMap<>();

		doReload |= newOptions.regionSpecificEffects.appendToIfChanged(oldOptions.regionSpecificEffects, changedSettings);
		doReload |= newOptions.shoulderBuddies.appendToIfChanged(oldOptions.shoulderBuddies, changedSettings);
		doReload |= newOptions.hats.appendToIfChanged(oldOptions.hats, changedSettings);
		doReload |= newOptions.lore.appendToIfChanged(oldOptions.lore, changedSettings);
		boolean finalDoReload = doReload;

		if (!changedSettings.isEmpty()) {
			Thread requestThread = new Thread(() -> {
				Cosmetica.api.updateUserSettings(changedSettings).ifSuccessfulOrElse(response -> {
					if (response.booleanValue()) {
						if (this.parentScreen instanceof MainScreen) {
							UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid()));
							Cosmetica.getPlayerData(uuid, Minecraft.getInstance().getUser().getName(), true);
						}

						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(this.parentScreen));
					} else {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(this.parentScreen, true)));
					}
				},
				e -> {
					e.printStackTrace();
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(this.parentScreen, true)));
				});

				if (finalDoReload) Minecraft.getInstance().tell(() -> clearAllCaches());
			});
			requestThread.start();
		} else {
			Debug.info("No settings changed.");
			if (doReload) clearAllCaches();
			Minecraft.getInstance().tell(this::onClose);
		}
	}

	/**
	 * For Cape Server Settings
	 */
	public UpdatingSettingsScreen(Screen parentScreen, Map<String, CapeDisplay> oldOptions, Map<String, CapeDisplay> newOptions) throws IOException, InterruptedException {
		super(new TranslatableComponent("cosmetica.updating"));
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

				Minecraft.getInstance().tell(() -> clearAllCaches());
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
