package com.eyezah.cosmetics;

import com.eyezah.cosmetics.screens.MainScreen;
import com.eyezah.cosmetics.screens.RSEWarningScreen;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import com.eyezah.cosmetics.utils.Response;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.io.IOException;

import static com.eyezah.cosmetics.Cosmetica.*;

public class Authentication {
	private static boolean currentlyAuthenticated = false;
	private static String token = "";
	private static String limitedToken = "";
	public static boolean currentlyAuthenticating = false;
	private static int bits; // to mark the two required things that must have happened to start cosmetics auth: fetching API url (may fail), and finishing loading.

	public static boolean isCurrentlyAuthenticated() {
		return currentlyAuthenticated;
	}

	public static String getToken() {
		return token;
	}

	/**
	 * Retrieves a token which only has GET power, for use on faster, less secure 'HTTP' connections.
	 * @return the limited token.
	 */
	public static String getLimitedToken() {
		return limitedToken;
	}

	private static void syncSettings() {
		Thread requestThread = new Thread(() -> {
			if (token.equals("")) {
				runAuthentication(Minecraft.getInstance().screen);
				return;
			}

			try (Response response = Response.request(Cosmetica.apiServerHost + "/get/settings?token=" + token)) {
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

	public static void showUnauthenticatedIfLoading() {
		Minecraft minecraft = Minecraft.getInstance();
		Screen previous = minecraft.screen;

		if (previous instanceof LoadingTypeScreen lts) {
			minecraft.tell(() -> minecraft.setScreen(new UnauthenticatedScreen(lts.getParent(), false)));
		} // TODO if in-game some small, unintrusive text on bottom right
	}

	public static void requestTokens(String testToken) {
		Thread requestThread = new Thread(() -> {
			try (Response response = Response.request(Cosmetica.apiServerHost + "/client/verifyforauthtokens?token=" + testToken + "&uuid=" + Minecraft.getInstance().getUser().getUuid() + "&access-token=" + Minecraft.getInstance().getUser().getAccessToken())) {
				JsonObject object = response.getAsJson();

				if (object.has("error")) {
					Cosmetica.LOGGER.warn("Error on authentication. Will be offline. {}", object.get("error"));
					currentlyAuthenticating = false;
					showUnauthenticatedIfLoading();
				} else {
					token = object.get("master_token").getAsString();
					limitedToken = object.get("limited_token").getAsString();
					currentlyAuthenticated = true;
					currentlyAuthenticating = false;
					if (object.get("is_new_player").getAsBoolean() && !object.get("has_special_cape").getAsBoolean()) {
						String capeId = getDefaultSettingsConfig().getCapeId();
						if (!capeId.isEmpty()) {
							try (Response response2 = Response.request(Cosmetica.apiServerHost + "/client/setcosmetic?requireofficial&token=" + token + "&type=cape&id=" + capeId)) {} catch (IOException e) {}
						}
						String capeSettingsString = Cosmetica.getDefaultSettingsConfig().getCapeSettingsString(true);
						if (!capeSettingsString.isEmpty()) {
							try (Response response2 = Response.request(Cosmetica.apiServerHost + "/client/capesettings?token=" + token + capeSettingsString)) {} catch (IOException e) {}
						}
					}
					syncSettings();
				}
			} catch (IOException e) {
				e.printStackTrace();
				currentlyAuthenticating = false;
				showUnauthenticatedIfLoading();
			}
		});
		requestThread.start();
	}

	public static boolean runAuthentication(Screen screen, int flag) {
		// 0x1 = API_URL_FETCH || 0x2 = LOAD_FINISH
		bits |= flag;

		if (bits >= 3) {
			runAuthentication(screen);
			return true;
		}

		return false;
	}

	private static void runAuthentication(Screen screen) {
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
