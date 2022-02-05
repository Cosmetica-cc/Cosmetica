package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import com.eyezah.cosmetics.utils.Response;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;
import java.util.Objects;

import static com.eyezah.cosmetics.Authentication.getToken;
import static com.eyezah.cosmetics.Cosmetics.*;

public class UpdatingSettingsScreen extends Screen implements LoadingTypeScreen {
	private Screen parentScreen;

	private Component reason = new TranslatableComponent("extravagantCosmetics.updating.message");
	private MultiLineLabel message;
	private int textHeight;

	public UpdatingSettingsScreen(Screen parentScreen, ServerOptions oldOptions, ServerOptions newOptions, boolean doReload) throws IOException, InterruptedException {
		super(new TranslatableComponent("extravagantCosmetics.updating"));
		this.parentScreen = parentScreen;

		StringBuilder endString = new StringBuilder();

		doReload |= newOptions.regionSpecificEffects.appendToIfChanged(oldOptions.regionSpecificEffects, endString);
		doReload |= newOptions.shoulderBuddies.appendToIfChanged(oldOptions.shoulderBuddies, endString);
		doReload |= newOptions.hats.appendToIfChanged(oldOptions.hats, endString);
		doReload |= newOptions.lore.appendToIfChanged(oldOptions.lore, endString);
		boolean finalDoReload = doReload;

		if (!endString.isEmpty()) {
			String finalEndString = endString.toString();

			Thread requestThread = new Thread(() -> {
				String url = "https://eyezah.com/cosmetics/api/client/updatesettings?token=" + getToken() + finalEndString;
				Debug.info(url, "always_print_urls");

				try (Response response = Response.request(url)) {
					String responseBody = response.getAsString();

					if (responseBody.equals("success")) {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(this.parentScreen));
					} else {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(this.parentScreen, true)));
					}
				} catch (IOException e) {
					e.printStackTrace();
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(this.parentScreen, true)));
				} finally {
					if (finalDoReload) clearAllCaches();
				}
			});
			requestThread.start();
		} else {
			Debug.info("No settings changed.");
			if (doReload) clearAllCaches();
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
