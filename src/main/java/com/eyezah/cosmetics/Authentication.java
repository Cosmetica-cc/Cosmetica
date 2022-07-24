package com.eyezah.cosmetics;

import cc.cosmetica.api.CosmeticPosition;
import cc.cosmetica.api.CosmeticaAPI;
import cc.cosmetica.api.LoginInfo;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.screens.CustomiseCosmeticsScreen;
import com.eyezah.cosmetics.screens.MainScreen;
import com.eyezah.cosmetics.screens.RSEWarningScreen;
import com.eyezah.cosmetics.screens.SnipeScreen;
import com.eyezah.cosmetics.screens.UnauthenticatedScreen;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.LoadingTypeScreen;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.eyezah.cosmetics.Cosmetica.*;

public class Authentication {
	private static volatile boolean currentlyAuthenticated = false;
	private static volatile boolean currentlyAuthenticating = false;
	public static int settingLoadTarget; // 1 = customise cosmetics screen, 2 = snipe (steal his look) screen, other =
	@Nullable
	public static cc.cosmetica.api.User snipedPlayer;

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
					final UUID ownUUID = UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid()));
					final String ownName = Minecraft.getInstance().getUser().getName();
					UUID uuid;
					String playerName;
					int loadTarget = settingLoadTarget;

					// can't wait for a race condition to make snipedPlayer null after the check and before extracting its uuid/name
					if (loadTarget == 2 && Authentication.snipedPlayer != null) {
						uuid = Authentication.snipedPlayer.getUUID();
						playerName = Authentication.snipedPlayer.getUsername();
					} else {
						uuid = ownUUID;
						playerName = ownName;
					}

					PlayerData info = Cosmetica.getPlayerData(uuid, playerName, true);
					Debug.info("Loading skin " + info.skin());
					@Nullable PlayerData ownInfo = loadTarget == 2 ? Cosmetica.getPlayerData(ownUUID, ownName, true) : null;

					Minecraft.getInstance().tell(() -> {
						FakePlayer fakePlayer = new FakePlayer(Minecraft.getInstance(), uuid, playerName, info, info.slim());
						Minecraft.getInstance().setScreen(switch (loadTarget) {
							case 2 -> new SnipeScreen(TextComponents.literal(playerName), lts.getParent(), fakePlayer, settings, ownInfo);
							case 1 -> new CustomiseCosmeticsScreen(lts.getParent(), fakePlayer, settings);
							default -> new MainScreen(lts.getParent(), settings, fakePlayer);
						});
					});
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

	private static void prepareWelcome(UUID uuid, String name) {
		// load the player's data if not loaded for later
		RenderSystem.recordRenderCall(() -> Cosmetica.getPlayerData(uuid, name, false));

		// do a separate request for some reason because I'm cringe
		api.getUserInfo(uuid, name).ifSuccessfulOrElse(userInfo -> {
			// welcome new, authenticated players
			if (displayNext == null && TextComponents.stripColour(userInfo.getLore()).equals("New to Cosmetica")) {
				MutableComponent menuOpenText = TextComponents.translatable("cosmetica.linkhere");
				menuOpenText.setStyle(menuOpenText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "cosmetica.customise")));
				displayNext = TextComponents.formattedTranslatable("cosmetica.welcome", menuOpenText);
			}
		}, Cosmetica.logErr("Failed to request user info on authenticate."));
	}

	private static void runAuthentication() {
		if (!api.isAuthenticated()) {
			String devToken = System.getProperty("cosmetica.token");

			if (devToken != null) {
				Debug.info("Authenticating API from provided token.");
				api = CosmeticaAPI.fromToken(devToken);
				api.setUrlLogger(str -> Debug.checkedInfo(str, "always_print_urls"));

				// welcome players if they're new
				// this isn't really necesary for manual auth because you probably know what you're doing
				// but is useful for testing
				User user = Minecraft.getInstance().getUser();
				UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(user.getUuid()));
				prepareWelcome(uuid, user.getName());
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
								UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(user.getUuid()));

								api = CosmeticaAPI.fromMinecraftToken(user.getAccessToken(), user.getName(), uuid); // getUuid() better have the dashes... edit: it did not have the dashes.
								api.setUrlLogger(str -> Debug.checkedInfo(str, "always_print_urls"));

								LoginInfo info = api.getLoginInfo().get();

								// success response
								currentlyAuthenticated = true;
								currentlyAuthenticating = false;

								if (info.isNewPlayer() && !info.hasSpecialCape()) {
									String capeId = getDefaultSettingsConfig().getCapeId();

									if (!capeId.isEmpty()) {
										api.setCosmetic(CosmeticPosition.CAPE, capeId);
									}

									var capeServerSettings = Cosmetica.getDefaultSettingsConfig().getCapeServerSettings();

									if (!capeServerSettings.isEmpty()) {
										api.setCapeServerSettings(capeServerSettings);
									}
								}

								// welcome players
								// and by new I mean has new to cosmetica lore
								prepareWelcome(uuid, user.getName());

								// synchronise settings from the server to the mod
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
