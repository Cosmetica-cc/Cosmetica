package com.eyezah.cosmetics;

import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CosmeticaAPI;
import cc.cosmetica.api.LoginInfo;
import com.eyezah.cosmetics.screens.MainScreen;
import com.eyezah.cosmetics.screens.RSEWarningScreen;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import com.eyezah.cosmetics.utils.Response;
import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.io.IOException;
import java.util.UUID;

import static com.eyezah.cosmetics.Cosmetica.*;

public class Authentication {
	private static boolean currentlyAuthenticated = false;
	public static boolean currentlyAuthenticating = false;
	private static int bits; // to mark the two required things that must have happened to start cosmetics auth: fetching API url (may fail), and finishing loading.

	public static boolean isCurrentlyAuthenticated() {
		return currentlyAuthenticated;
	}

	private static void syncSettings() {
		if (api == null) return;

		Thread requestThread = new Thread(() -> {
			if (!api.isAuthenticated()) {
				runAuthentication(Minecraft.getInstance().screen);
				return;
			}

			Cosmetica.api.getUserSettings().ifSuccessfulOrElse(settings -> {
				Debug.info("Handling successful cosmetics settings response.");

				// regional effects checking
				boolean regionSpecificEffects = settings.perRegionEffects();
				RSEWarningScreen.appearNextScreenChange = !settings.perRegionEffectsSet();

				boolean doShoulderBuddies = settings.doShoulderBuddies();
				boolean doHats = settings.doHats();
				boolean doLore = settings.doLore();

				if (Minecraft.getInstance().screen instanceof LoadingTypeScreen lts) {
					Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new MainScreen(lts.getParent(), doShoulderBuddies, doHats, regionSpecificEffects, doLore)));
				}
			},
			error -> {
				LOGGER.error("Error during settings get: {}", error);

				showUnauthenticatedIfLoading();
				runAuthentication(Minecraft.getInstance().screen);
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

	public static void requestTokens(String testToken) {
		Thread requestThread = new Thread(() -> {
			try {
				// the thing that can error
				api = CosmeticaAPI.fromAuthToken(testToken);
				api.setUrlLogger(str -> Debug.checkedInfo(str, "always_print_urls"));
				LoginInfo info = api.exchangeTokens(UUID.fromString(Minecraft.getInstance().getUser().getUuid())).get(); // getUuid() better have the dashes...

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
			} catch (IllegalStateException | NullPointerException e) {
				Cosmetica.LOGGER.warn("Error on authentication. Will be offline. {}", e);
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
		if (!api.isAuthenticated()) {
			if (!currentlyAuthenticating) {
				currentlyAuthenticating = true;
				ConnectScreen.startConnecting(screen, Minecraft.getInstance(), ServerAddress.parseString(CosmeticaAPI.getAuthServer()), new ServerData("Authentication Server", CosmeticaAPI.getAuthServer(), false));
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
