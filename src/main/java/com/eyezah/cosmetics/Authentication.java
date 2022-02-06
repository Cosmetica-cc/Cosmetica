package com.eyezah.cosmetics;

import com.eyezah.cosmetics.screens.LoadingScreen;
import com.eyezah.cosmetics.screens.MainScreen;
import com.eyezah.cosmetics.screens.RSEWarningScreen;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import com.eyezah.cosmetics.screens.UpdatingSettingsScreen;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import com.eyezah.cosmetics.utils.Response;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.eyezah.cosmetics.Cosmetics.*;

public class Authentication {
	private static boolean currentlyAuthenticated = false;
	private static String token = "";
	public static boolean currentlyAuthenticating = false;

	public static boolean isCurrentlyAuthenticated() {
		return currentlyAuthenticated;
	}

	public static String getToken() {
		return token;
	}

	private static void syncSettings() {
		Thread requestThread = new Thread(() -> {
			if (token.equals("")) {
				runAuthentication(Minecraft.getInstance().screen);
				return;
			}

			try (Response response = Response.request("https://eyezah.com/cosmetics/api/get/settings?token=" + token)) {
				Debug.info("Handling successful cosmetics settings response.");

				JsonObject jsonObject = response.getAsJson();
				if (jsonObject.has("error")) {
					Debug.info("error: " + jsonObject.get("error").getAsString());
					showUnauthenticatedIfLoading();
					return;
				}

				boolean regionSpecificEffects;

				// regional effects checking. the absence of this field indicates the user has not set it yet, and the presence of it allows us to cache the current setting on the client
				if (jsonObject.has("per-region effects")) {
					regionSpecificEffects = jsonObject.get("per-region effects").getAsBoolean();
				} else {
					regionSpecificEffects = false;
					RSEWarningScreen.appearNextScreenChange = true;
				}

				boolean doShoulderBuddies = jsonObject.get("do shoulder buddies").getAsBoolean();
				boolean doHats = jsonObject.get("do hats").getAsBoolean();
				boolean doLore = jsonObject.get("do lore").getAsBoolean();

				if (Minecraft.getInstance().screen instanceof LoadingTypeScreen lts) {
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new MainScreen(lts.getParent(), doShoulderBuddies, doHats, regionSpecificEffects, doLore)));
				}
			} catch (IOException e) {
				e.printStackTrace();
				token = "";
				showUnauthenticatedIfLoading();
				runAuthentication(Minecraft.getInstance().screen);
			}
		});
		requestThread.start();
	}

	private static void showUnauthenticatedIfLoading() {
		Minecraft minecraft = Minecraft.getInstance();
		Screen previous = minecraft.screen;

		if (previous instanceof LoadingTypeScreen lts) {
			minecraft.tell(() -> minecraft.setScreen(new UnauthenticatedScreen(lts.getParent(), false)));
		}
	}

	public static void setToken(String testToken) {
		Thread requestThread = new Thread(() -> {
			try (Response response = Response.request(Cosmetics.apiUrl + "/client/verifyauthtoken?token=" + testToken + "&uuid=" + Minecraft.getInstance().getUser().getUuid() + "&access-token=" + Minecraft.getInstance().getUser().getAccessToken())) {
				String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8).trim();
				if (responseBody.startsWith("token:")) {
					token = responseBody.substring(6);
					currentlyAuthenticated = true;
					currentlyAuthenticating = false;
					syncSettings();
				} else {
					currentlyAuthenticating = false;
					showUnauthenticatedIfLoading();
				}
			} catch (IOException e) {
				e.printStackTrace();
				currentlyAuthenticating = false;
				showUnauthenticatedIfLoading();
			}
		});
		requestThread.start();
	}

	public static void runAuthentication(Screen screen) {
		if (token.equals("")) {
			if (!currentlyAuthenticating) {
				currentlyAuthenticating = true;
				ConnectScreen.startConnecting(screen, Minecraft.getInstance(), new ServerAddress(authServerHost, authServerPort), new ServerData("Authentication Server", authServerHost + ":" + authServerPort, false));
			}
		} else {
			syncSettings();
		}
	}

	protected static void runAuthenticationCheckThread() {
		Thread requestThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000 * 60 * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				syncSettings();
			}
		});
		requestThread.start();
	}
}
