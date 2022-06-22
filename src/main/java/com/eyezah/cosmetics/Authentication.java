package com.eyezah.cosmetics;

import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CosmeticaAPI;
import cc.cosmetica.api.LoginInfo;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.screens.MainScreen;
import com.eyezah.cosmetics.screens.RSEWarningScreen;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.Screen;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.eyezah.cosmetics.Cosmetica.*;

public class Authentication {
	private static volatile boolean currentlyAuthenticated = false;
	public static volatile boolean currentlyAuthenticating = false;
	private static int bits; // to mark the two required things that must have happened to start cosmetics auth: fetching API url (may fail), and finishing loading.

	public static boolean isCurrentlyAuthenticated() {
		return currentlyAuthenticated;
	}

	private static void syncSettings() {
		if (api == null) return;

		Thread requestThread = new Thread(() -> {
			if (!api.isAuthenticated()) {
				runAuthentication();
				return;
			}

			Cosmetica.api.getUserSettings().ifSuccessfulOrElse(settings -> {
				Debug.info("Handling successful cosmetics settings response.");

				// regional effects checking
				RSEWarningScreen.appearNextScreenChange = !settings.hasPerRegionEffectsSet();

				if (Minecraft.getInstance().screen instanceof LoadingTypeScreen lts) {
					// load player info
					UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid()));

					PlayerData info = Cosmetica.getPlayerData(uuid, Minecraft.getInstance().getUser().getName(), true);
					Debug.info("Loading skin " + info.skin());
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new MainScreen(lts.getParent(), settings, info)));
				}
			},
			error -> {
				LOGGER.error("Error during settings get: {}", error);

				showUnauthenticatedIfLoading();
				runAuthentication();
			});
		});
		requestThread.start();
	}

	public static void showUnauthenticatedIfLoading() {
		Minecraft minecraft = Minecraft.getInstance();
		Screen current = minecraft.screen;

		if (current instanceof LoadingTypeScreen lts) {
			minecraft.tell(() -> minecraft.setScreen(new UnauthenticatedScreen(lts.getParent(), false)));
		} // TODO if in-game some small, unintrusive text on bottom right
	}

	public static boolean runAuthentication(int flag) {
		// 0x1 = API_URL_FETCH || 0x2 = LOAD_FINISH
		bits |= flag;

		if (bits >= 3) {
			runAuthentication();
			return true;
		}

		return false;
	}

	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);

	private static void runAuthentication() {
		if (!api.isAuthenticated()) {
			String devToken = System.getProperty("cosmetica.token");

			if (devToken != null) {
				Debug.info("Authenticating API from provided token.");
				api = CosmeticaAPI.fromToken(devToken);
				api.setUrlLogger(str -> Debug.checkedInfo(str, "always_print_urls"));
			} else {
				if (currentlyAuthenticating) {
					Debug.info("API is not authenticated but authentication is already in progress.");
				} else {
					Debug.info("API is not authenticated: starting authentication!");
					currentlyAuthenticating = true;

					new Thread("Cosmetica Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
						public void run() {
							try {
								User user = Minecraft.getInstance().getUser();
								api = CosmeticaAPI.fromMinecraftToken(user.getAccessToken(), user.getName(), UUID.fromString(Cosmetica.dashifyUUID(user.getUuid()))); // getUuid() better have the dashes... edit: it did not have the dashes.
								api.setUrlLogger(str -> Debug.checkedInfo(str, "always_print_urls"));

								LoginInfo info = api.getLoginInfo().get();

								// success response
								currentlyAuthenticated = true;
								currentlyAuthenticating = false;

								if (info.isNewPlayer() && !info.hasSpecialCape()) {
									String capeId = getDefaultSettingsConfig().getCapeId();

									if (!capeId.isEmpty()) {
										api.setCosmetic(CosmeticType.CAPE, capeId);
									}

									var capeServerSettings = Cosmetica.getDefaultSettingsConfig().getCapeServerSettings();

									if (!capeServerSettings.isEmpty()) {
										api.setCapeServerSettings(capeServerSettings);
									}
								}
								syncSettings();
							} catch (Exception e) {
								LOGGER.error("Couldn't connect to cosmetica auth server", e);

								currentlyAuthenticating = false;
								Authentication.showUnauthenticatedIfLoading();
							}
						}
					}.start();
				}
			}
		} else {
			Debug.info("Api is authenticated: syncing settings!");
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
