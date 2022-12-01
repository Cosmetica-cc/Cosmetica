/*
 * Copyright 2022 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica;

import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.api.CosmeticPosition;
import cc.cosmetica.api.CosmeticaAPI;
import cc.cosmetica.api.LoginInfo;
import cc.cosmetica.cosmetica.config.CosmeticaConfig;
import cc.cosmetica.cosmetica.config.DefaultSettingsConfig;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.screens.CustomiseCosmeticsScreen;
import cc.cosmetica.cosmetica.screens.MainScreen;
import cc.cosmetica.cosmetica.screens.RSEWarningScreen;
import cc.cosmetica.cosmetica.screens.SnipeScreen;
import cc.cosmetica.cosmetica.screens.UnauthenticatedScreen;
import cc.cosmetica.cosmetica.screens.WelcomeScreen;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.utils.LoadingTypeScreen;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.io.UncheckedIOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Authentication {
	private static volatile boolean currentlyAuthenticated = false;
	private static volatile boolean currentlyAuthenticating = false;
	public static int settingLoadTarget; // 1 = customise cosmetics screen, 2 = snipe (steal his look) screen, 3 = tutorial customise screen, other = main screen
	@Nullable
	public static cc.cosmetica.api.User snipedPlayer;

	private static int bits; // to mark the two required things that must have happened to start cosmetics auth: fetching API url (may fail), and finishing loading.

	public static boolean isCurrentlyAuthenticated() {
		return currentlyAuthenticated;
	}

	private static void syncSettings() {
		if (Cosmetica.api == null) return;

		Thread requestThread = new Thread(() -> {
			if (!Cosmetica.api.isAuthenticated()) {
				runAuthentication();
				return;
			}

			Cosmetica.api.getUserSettings().ifSuccessfulOrElse(settings -> {
				DebugMode.log("Handling successful cosmetics settings response.");

				// regional effects checking
				RSEWarningScreen.appearNextScreenChange = !settings.hasPerRegionEffectsSet() && Cosmetica.getConfig().regionalEffectsPrompt();

				// only bother trying to do the next stage if on a loading screen
				if (Minecraft.getInstance().screen instanceof LoadingTypeScreen) {
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
					DebugMode.log("Loading skin " + info.skin());
					@Nullable PlayerData ownInfo = loadTarget == 2 ? Cosmetica.getPlayerData(ownUUID, ownName, true) : null;

					// check *again* in case they've closed it
					if (Minecraft.getInstance().screen instanceof LoadingTypeScreen lts) {
						Minecraft.getInstance().tell(() -> {
							FakePlayer fakePlayer = new FakePlayer(Minecraft.getInstance(), uuid, playerName, info);
							Minecraft.getInstance().setScreen(switch (loadTarget) {
								case 2 -> new SnipeScreen(TextComponents.literal(playerName), lts.getParent(), fakePlayer, settings, ownInfo, new cc.cosmetica.api.User(ownUUID, ownName));
								case 1 -> new CustomiseCosmeticsScreen(lts.getParent(), fakePlayer, settings);
								default -> new MainScreen(lts.getParent(), settings, fakePlayer, loadTarget == 3);
							});
						});
					}
				}
			},
			error -> {
				Cosmetica.LOGGER.error("Error during settings get:", error);

				showUnauthenticatedIfLoading();

				// don't repeat spam errors if the internet goes offline
				if (error instanceof UncheckedIOException) {
					if (((UncheckedIOException) error).getCause() instanceof UnknownHostException) {
						// don't run again immediately, see above. Africa or opening a menu will run authentication again inevitably anyway
						return;
					}
				}

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

	private static void prepareWelcome(UUID uuid, String name, boolean newPlayer) {
		// load the player's data if not loaded for later
		RenderSystem.recordRenderCall(() -> Cosmetica.getPlayerData(uuid, name, false));

		boolean isWelcomeScreenAllowed = newPlayer && Cosmetica.mayShowWelcomeScreen();
		DebugMode.log("Preparing potential welcome... || newPlayer=" + newPlayer + " mayShowWelcomeScreen=" + Cosmetica.mayShowWelcomeScreen());

		// do a separate request for some reason because I'm cringe
		Cosmetica.api.getUserInfo(uuid, name).ifSuccessfulOrElse(userInfo -> {
			final String colourlessLore = TextComponents.stripColour(userInfo.getLore());
			DebugMode.log("Received user info on Authenticate/prepareWelcome || displayNext=" + Cosmetica.displayNext + " colourlessLore=" + colourlessLore);

			// welcome new, authenticated players in chat
			if (Cosmetica.getConfig().shouldShowWelcomeMessage().shouldShowChatMessage(isWelcomeScreenAllowed) && Cosmetica.displayNext == null && colourlessLore.equals("New to Cosmetica")) {
				MutableComponent menuOpenText = TextComponents.translatable("cosmetica.linkhere");
				menuOpenText.setStyle(menuOpenText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "cosmetica.customise")));
				Cosmetica.displayNext = TextComponents.formattedTranslatable("cosmetica.welcome", menuOpenText);
			}

			// or... with the welcome screen!
			// Welcome tutorial. Only show the first time they start with the mod, and only if show-welcome-message is set to full.
			if (isWelcomeScreenAllowed && Cosmetica.getConfig().shouldShowWelcomeMessage() == CosmeticaConfig.WelcomeMessageState.FULL) {
				DebugMode.log("New Player: Showing Welcome Screen");

				RenderSystem.recordRenderCall(() -> {
					Minecraft.getInstance().setScreen(new WelcomeScreen(Minecraft.getInstance().screen, uuid, name, Cosmetica.newPlayerData(userInfo, uuid)));
				});
			}
		}, Cosmetica.logErr("Failed to request user info on authenticate."));
	}

	private static void runAuthentication() {
		if (!Cosmetica.api.isAuthenticated()) {
			String devToken = System.getProperty("cosmetica.token");

			if (devToken != null) {
				DebugMode.log("Authenticating API from provided token.");
				Cosmetica.api = CosmeticaAPI.fromToken(devToken);
				Cosmetica.api.setUrlLogger(DebugMode::logURL);
				Cosmetica.api.setForceHttps(Cosmetica.getConfig().paranoidHttps());

				// welcome players if they're new
				// this isn't really necesary for manual auth because you probably know what you're doing
				// but is useful for testing
				User user = Minecraft.getInstance().getUser();
				UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(user.getUuid()));
				prepareWelcome(uuid, user.getName(), false);
			} else {
				if (currentlyAuthenticating) {
					DebugMode.log("API is not authenticated but authentication is already in progress.");
				} else {
					DebugMode.log("API is not authenticated: starting authentication!");
					currentlyAuthenticating = true;

					new Thread("Cosmetica Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
						public void run() {
							try {
								User user = Minecraft.getInstance().getUser();
								UUID uuid = UUID.fromString(Cosmetica.dashifyUUID(user.getUuid()));

								Cosmetica.api = CosmeticaAPI.fromMinecraftToken(user.getAccessToken(), user.getName(), uuid, System.getProperty("cosmetica.client", "cosmetica")); // getUuid() better have the dashes... edit: it did not have the dashes.
								Cosmetica.api.setUrlLogger(DebugMode::logURL);
								Cosmetica.api.setForceHttps(Cosmetica.getConfig().paranoidHttps());

								LoginInfo info = Cosmetica.api.getLoginInfo().get();

								// success response
								currentlyAuthenticated = true;
								currentlyAuthenticating = false;

								if (info.isNewPlayer()) {
									DefaultSettingsConfig defaults = Cosmetica.getDefaultSettingsConfig();

									// only set defaults if there was a file present on first run with the mod
									if (defaults.wasLoaded()) {
										// Create map of settings to update
										final Map<String, Object> settings = new HashMap<>();

										// add the various fields to the map
										defaults.areHatsEnabled().ifPresent(v -> settings.put("dohats", v));
										defaults.areShoulderBuddiesEnabled().ifPresent(v -> settings.put("doshoulderbuddies", v));
										defaults.areBackBlingsEnabled().ifPresent(v -> settings.put("dobackblings", v));
										defaults.isLoreEnabled().ifPresent(v -> settings.put("dolore", v));
										defaults.getIconSettings().ifPresent(v -> settings.put("iconsettings", v));

										// post the settings to update if they exist
										if (!settings.isEmpty()) {
											Cosmetica.api.updateUserSettings(settings);
										}

										// handle default-setting-defined capes
										if (!info.hasSpecialCape()) {
											String capeId = defaults.getCapeId();

											if (!capeId.isEmpty()) {
												Cosmetica.api.setCosmetic(CosmeticPosition.CAPE, capeId);
											}
										}

										// handle default-setting-defined cape server settings
										Map<String, CapeDisplay> capeServerSettings = defaults.getCapeServerSettings();

										if (!capeServerSettings.isEmpty()) {
											Cosmetica.api.setCapeServerSettings(capeServerSettings);
										}
									}
								}

								// welcome players
								// and by new I mean has new to cosmetica lore
								prepareWelcome(uuid, user.getName(), info.isNewPlayer());

								// synchronise settings from the server to the mod
								syncSettings();
							} catch (Exception e) {
								Cosmetica.LOGGER.error("Couldn't connect to cosmetica auth server", e);

								currentlyAuthenticating = false;
								Authentication.showUnauthenticatedIfLoading();
							}
						}
					}.start();
				}
			}
		} else {
			DebugMode.log("Api is authenticated: syncing settings!");
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
