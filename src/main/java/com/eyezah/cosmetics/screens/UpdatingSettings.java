package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.Cosmetics;
import com.eyezah.cosmetics.utils.Response;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.eyezah.cosmetics.Authentication.getToken;
import static com.eyezah.cosmetics.Cosmetics.*;

public class UpdatingSettings extends Screen {
	private Screen parentScreen;
	private Options parentOptions;

	private Component reason = new TranslatableComponent("extravagantCosmetics.updating.message");
	private MultiLineLabel message;
	private int textHeight;

	public UpdatingSettings(Screen parentScreen, Options parentOptions, boolean doRegionEffects, boolean doReload, boolean doShoulderBuddies) throws IOException, InterruptedException {
		super(new TranslatableComponent("extravagantCosmetics.updating"));
		this.parentScreen = parentScreen;
		this.parentOptions = parentOptions;

		String endString = "";
		if (doRegionEffects != doRegionSpecificEffects()) endString += "&doregioneffects=" + doRegionEffects;
		if (doShoulderBuddies != doShoulderBuddies()) endString += "&doshoulderbuddies=" + doShoulderBuddies;

		if (!endString.equals("")) {
			String finalEndString = endString;
			Thread requestThread = new Thread(() -> {
				try (Response response = Response.request("https://eyezah.com/cosmetics/api/client/updatesettings?token=" + getToken() + finalEndString)) {
					String responseBody = response.getAsString();

					if (doReload) reloadCosmetics();
					if (responseBody.equals("success")) {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(screenStorage));
					} else {
						Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(screenStorage, this.parentOptions, true)));
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (doReload) reloadCosmetics();
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new UnauthenticatedScreen(screenStorage, this.parentOptions, true)));
				}
			});
			requestThread.start();
		} else {
			if (doReload) reloadCosmetics();
			Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(screenStorage));
		}
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
