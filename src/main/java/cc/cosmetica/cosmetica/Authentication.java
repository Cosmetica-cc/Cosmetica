/*
 * Copyright 2022, 2023 EyezahMC
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
import cc.cosmetica.api.FatalServerErrorException;
import cc.cosmetica.api.LoginInfo;
import cc.cosmetica.api.ServerResponse;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.cosmetica.config.DefaultSettingsConfig;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.screens.CosmeticaErrorScreen;
import cc.cosmetica.cosmetica.screens.CustomiseCosmeticsScreen;
import cc.cosmetica.cosmetica.screens.MainScreen;
import cc.cosmetica.cosmetica.screens.RSEWarningScreen;
import cc.cosmetica.cosmetica.screens.ServerOptions;
import cc.cosmetica.cosmetica.screens.SnipeScreen;
import cc.cosmetica.cosmetica.screens.UnauthenticatedScreen;
import cc.cosmetica.cosmetica.screens.WelcomeScreen;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.utils.LoadingTypeScreen;
import cc.cosmetica.cosmetica.utils.TextComponents;
import cc.cosmetica.impl.CosmeticaWebAPI;
import cc.cosmetica.util.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Authentication {
	private static volatile boolean currentlyAuthenticated = false;
	private static volatile boolean currentlyAuthenticating = false;
	private static UUID authenticatedAsUUID;

	public static int settingLoadTarget; // 1 = customise cosmetics screen, 2 = snipe (steal his look) screen, 3 = tutorial customise screen, other = main screen
	@Nullable
	public static cc.cosmetica.api.User snipedPlayer;

	public static boolean isCurrentlyAuthenticated() {
		return currentlyAuthenticated;
	}

	private static ServerOptions savedOptions;

	public static void setCachedOptions(ServerOptions options) {
		savedOptions = options;
	}

	public static boolean hasCachedOptions() {
		return savedOptions != null;
	}

	public static void openSnipeScreen(Screen parent, PlayerData foreignData, PlayerData ownData) {
		if (snipedPlayer == null) {
			throw new IllegalStateException("Can't show snipe screen for null sniped player.");
		}

		FakePlayer player = new FakePlayer(
				Minecraft.getInstance(),
				snipedPlayer.getUUID(),
				snipedPlayer.getUsername(),
				foreignData
		);

		Minecraft.getInstance().setScreen(
				new SnipeScreen(TextComponents.literal(player.getName()), parent, player, savedOptions,
						ownData, new cc.cosmetica.api.User(player.getUUID(), player.getName()))
		);
	}

	public static void openCustomiseCosmeticsScreen(Screen parent, PlayerData playerData) {
		FakePlayer player = new FakePlayer(
				Minecraft.getInstance(),
				Minecraft.getInstance().getUser().getProfileId(),
				Minecraft.getInstance().getUser().getName(),
				playerData
		);

		Minecraft.getInstance().setScreen(new CustomiseCosmeticsScreen(parent, player, savedOptions));
	}

	private static void syncSettings() {
		if (Cosmetica.api == null) return;
		DebugMode.log("Synchronising Settings");

		Thread requestThread = new Thread(() -> {
			if (!Cosmetica.api.isAuthenticated() || !Minecraft.getInstance().getUser().getProfileId().equals(authenticatedAsUUID)) {
				DebugMode.log("Not authenticated. [Re]authenticating...");
				runAuthentication(true, false);
				return; // sync settings is called after auth anyway
			}

			final ServerResponse<UserSettings> settings_ = Cosmetica.api.getUserSettings();

			settings_.ifSuccessfulOrElse(settings -> {
				DebugMode.log("Handling successful cosmetics settings response.");
				savedOptions = new ServerOptions(settings);

				// regional effects checking
				RSEWarningScreen.appearNextScreenChange = !settings.hasPerRegionEffectsSet() && Cosmetica.getConfig().regionalEffectsPrompt();

				// only bother trying to do the next stage if on a loading screen
				if (Minecraft.getInstance().screen instanceof LoadingTypeScreen) {
					// load player info
					final UUID ownUUID = Minecraft.getInstance().getUser().getProfileId();
					final String ownName = Minecraft.getInstance().getUser().getName();
					final cc.cosmetica.api.User snipedPlayer = Authentication.snipedPlayer; // stop background changes messing with it

					int loadTarget = settingLoadTarget;

					DebugMode.log("Loading own player info for menu (mode: " + loadTarget + ")");
					PlayerData ownInfo = PlayerData.get(ownUUID, ownName, true);

					if (loadTarget == 2) DebugMode.log("Loading sniped player info");

					// might take time; load before checking whether still relevant to open screen
					@Nullable PlayerData snipedInfo = loadTarget == 2 && snipedPlayer != null ? PlayerData.get(
							snipedPlayer.getUUID(),
							snipedPlayer.getUsername(),
							true
					) : null;

					// PlayerData.get can never return null (only PlayerData.NONE) so we can guarantee the reason
					if (loadTarget == 2 && snipedInfo == null) DebugMode.log("Failed to load sniped player info (sniped player was null)");

					if (loadTarget != 2 || snipedInfo != null)
						DebugMode.log("Will use skin " + (loadTarget == 2 ? snipedInfo : ownInfo).skin());

					// check *again* in case they've closed it
					if (Minecraft.getInstance().screen instanceof LoadingTypeScreen lts) {
						Minecraft.getInstance().tell(() -> {
							switch (loadTarget) {
							case 2:
								if (snipedInfo == null || snipedInfo == PlayerData.NONE) {
									Minecraft.getInstance().setScreen(new CosmeticaErrorScreen(
											lts.getParent(),
											TextComponents.translatable("cosmetica.stealhislook.snipe"),
											snipedInfo == null ?
													TextComponents.translatable("cosmetica.stealhislook.snipe.cannotFind") :
													TextComponents.formattedTranslatable("cosmetica.stealhislook.snipe.err", snipedPlayer.getUsername())
									));
								} else {
									openSnipeScreen(lts.getParent(), snipedInfo, ownInfo);
								}
								break;
							case 1:
								openCustomiseCosmeticsScreen(lts.getParent(), ownInfo);
								break;
							default:
								Minecraft.getInstance().setScreen(new MainScreen(
										lts.getParent(),
										settings,
										new FakePlayer(Minecraft.getInstance(), ownUUID, ownName, ownInfo),
										loadTarget == 3
								));
								break;
							};
						});
					}
				}
			},
			error -> {
				if (error.getMessage().contains("invalid token")) {
					Cosmetica.LOGGER.info("Invalid token found on settings sync. Reauthenticating...");
					runAuthentication(true, true);
				} else {
					Cosmetica.LOGGER.error("Error during settings get:", error);

					showUnauthenticatedIfLoading(false, error);

					if (error instanceof JsonSyntaxException) {
						if (DebugMode.elevatedLogging()) {
							//TODO proper way of dong this lmao
							Cosmetica.LOGGER.error("The Json causing this error is as follows, assuming repetitive issue:");
							try {
								Cosmetica.LOGGER.error(Response.get(settings_.getURL()).getAsString());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					// don't repeat spam errors if the internet goes offline. So don't run again immediately
					// opening a menu will run authentication again inevitably anyway
				}
			});
		});
		requestThread.start();
	}

	public static void showUnauthenticatedIfLoading(boolean fromSave, @Nullable Exception exception) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen current = minecraft.screen;
		UnauthenticatedScreen.UnauthenticatedReason reason = diagnose(exception);

		if (current instanceof LoadingTypeScreen lts) {
			minecraft.tell(() -> minecraft.setScreen(new UnauthenticatedScreen(lts.getParent(), fromSave, reason)));
		} // TODO if in-game some small, unintrusive text on bottom right
	}

	private static UnauthenticatedScreen.UnauthenticatedReason diagnose(@Nullable Exception exception) {
		// if the exception is provided, look at that first
		if (exception != null) {
			UnauthenticatedScreen.UnauthenticatedReason reason = diagnoseError(exception);

			if (reason != null) {
				return reason;
			}
		}

		// check network connection & cracked
		// reset to very simple cracked detection, as the overloaded cosmetica servers are giving false cracked reports
		// default uuid generation is v3 but real users are v4, so check for not v4
		final User currentUser = Minecraft.getInstance().getUser();
		final UUID uuid = currentUser.getProfileId();

		if (uuid.version() != 4) {
			return new UnauthenticatedScreen.UnauthenticatedReason(
					UnauthenticatedScreen.UnauthenticatedReason.CRACKED,
					null //exception not relevant as it's not part of the diagnosis
			);
		}

		return new UnauthenticatedScreen.UnauthenticatedReason(
				UnauthenticatedScreen.UnauthenticatedReason.GENERIC,
				exception
		);
	}

	@Nullable
	private static UnauthenticatedScreen.UnauthenticatedReason diagnoseError(Exception exception) {
		// unwrap any unchecked IO exceptions
		if (exception instanceof UncheckedIOException) {
			exception = ((UncheckedIOException)exception).getCause();
		}

		// check if exception is NoRoute (= offline)
		if (exception instanceof NoRouteToHostException) {
			return new UnauthenticatedScreen.UnauthenticatedReason(
					UnauthenticatedScreen.UnauthenticatedReason.OFFLINE,
					exception
			);
		}

		// check if exception is UnknownHost (= one side is offline)
		if (exception instanceof UnknownHostException) {
			return new UnauthenticatedScreen.UnauthenticatedReason(
					UnauthenticatedScreen.UnauthenticatedReason.UNKNOWN_HOST,
					exception
			);
		}

		// or exception is SocketException (includes ConnectException) (reset, timed out, or refused.)
		// "Connection refused" is most likely a server issue but could also be due to a firewall
		if (exception instanceof SocketException) {
			return new UnauthenticatedScreen.UnauthenticatedReason(
					UnauthenticatedScreen.UnauthenticatedReason.CONNECTION_ISSUE,
					exception
			);
		}

		// check if exception is FatalServerError (= server had issues processing the request)
		if (exception instanceof FatalServerErrorException) {
			return new UnauthenticatedScreen.UnauthenticatedReason(
					UnauthenticatedScreen.UnauthenticatedReason.FIVE_HUNDRED,
					exception
			);
		}

		return null;
	}

	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);

	/**
	 * Prepare welcome screen or welcome message for the player based on settings and the player's cosmetica info.
	 * Specifically, their lore, and whether it's their first time using cosmetica.
	 * @param uuid the uuid of the player.
	 * @param name the username of the player.
	 * @param newPlayer whether this is the player's first time using cosmetica.
	 * @param suppressErrors whether to suppress errors from the user info call.
	 */
	private static void prepareWelcome(UUID uuid, String name, boolean newPlayer, boolean suppressErrors) {
		boolean isWelcomeScreenAllowed = newPlayer && Cosmetica.mayShowWelcomeScreen();
		DebugMode.log("Preparing potential welcome... || newPlayer=" + newPlayer + " mayShowWelcomeScreen=" + Cosmetica.mayShowWelcomeScreen());

		Cosmetica.api.getUserInfo(uuid, name).ifSuccessfulOrElse(userInfo -> {
			final String colourlessLore = TextComponents.stripColour(userInfo.getLore());
			DebugMode.log("Received user info on Authenticate/prepareWelcome || displayNext=" + Cosmetica.displayNext + " colourlessLore=" + colourlessLore + " show-welcome-message=" + Cosmetica.getConfig().showWelcomeMessage());

			// welcome new, authenticated players in chat
			if (Cosmetica.getConfig().showWelcomeMessage().shouldShowChatMessage(isWelcomeScreenAllowed) && Cosmetica.displayNext == null && colourlessLore.equals("New to Cosmetica")) {
				MutableComponent menuOpenText = TextComponents.translatable("cosmetica.linkhere");
				menuOpenText.setStyle(menuOpenText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "cosmetica.customise")));
				Cosmetica.displayNext = TextComponents.formattedTranslatable("cosmetica.welcome", menuOpenText);
			}

			// or... with the welcome screen!
			// Welcome tutorial. Only show the first time they start with the mod, and only if show-welcome-message is set to full.
			if (Cosmetica.getConfig().showWelcomeMessage().shouldShowWelcomeTutorial(isWelcomeScreenAllowed)) {
				DebugMode.log("New Player: Showing Welcome Screen");

				RenderSystem.recordRenderCall(() -> {
					Screen screen = Minecraft.getInstance().screen;

					// prevent flashbang on some versions of minecraft
//					if (screen instanceof TitleScreen) {
//						((TitleScreenAccessorMixin) screen).setFading(false);
//					}

					Minecraft.getInstance().setScreen(new WelcomeScreen(screen, uuid, name, Cosmetica.newPlayerData(userInfo, uuid)));
				});
			}
		}, e -> {
			if (suppressErrors) {
				DebugMode.logError("Suppressed Error:", e);
			} else {
				Cosmetica.LOGGER.error("Failed to request user info on authenticate for preparing welcome.", e);
			}
		});
	}

	public static void runAuthentication() {
		runAuthentication(false, false);
	}

	private static void runAuthentication(boolean force, boolean ignoreCache) {
		if (!Cosmetica.api.isAuthenticated() || force) {
			String devToken = System.getProperty("cosmetica.token");

			if (devToken != null) {
				DebugMode.log("Authenticating API from provided token.");
				Cosmetica.api = CosmeticaAPI.fromToken(devToken);
				Cosmetica.api.setUrlLogger(DebugMode::logURL);

				// welcome players if they're new
				// this isn't really necesary for manual auth because you probably know what you're doing
				// but is useful for testing
				User user = Minecraft.getInstance().getUser();
				authenticatedAsUUID = user.getProfileId();
				currentlyAuthenticated = true;
				prepareWelcome(authenticatedAsUUID, user.getName(), false, false);
			} else {
				if (currentlyAuthenticating) {
					DebugMode.log("API is not authenticated but authentication is already in progress.");
				} else {
					DebugMode.log("Starting authentication!");
					currentlyAuthenticating = true;

					new Thread("Cosmetica Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
						public void run() {
							try {
								boolean reauthenticate = true;
								String reason = "Forced token refresh.";
								Properties tokens = new Properties();
								Path tokensPath = Cosmetica.getCacheDirectory().resolve("tokens");

								// Get user
								User user = Minecraft.getInstance().getUser();
								UUID uuid = user.getProfileId();

								if (!ignoreCache) {
									// First, check if a cosmetica token already exists
									if (Files.isRegularFile(tokensPath)) {
										try (InputStream inputStream = Files.newInputStream(tokensPath)) {
											tokens.load(inputStream);
										} catch (IOException e) {
											Cosmetica.LOGGER.error("Failed to read tokens file.", e);
										}
									} else {
										try {
											Files.createFile(tokensPath);
										} catch (IOException e) {
											Cosmetica.LOGGER.error("Failed to create tokens file.", e);
										}
									}

									reason = "No cached Cosmetica token found.";
									String foundToken = tokens.getProperty(uuid.toString());

									if (foundToken != null) {
										DebugMode.log("Found cached token. Trying to authenticate...");

										Cosmetica.api = CosmeticaAPI.fromTokens(
												foundToken,
												tokens.getProperty(uuid + "-l")
										);
										Cosmetica.api.setUrlLogger(DebugMode::logURL);

										authenticatedAsUUID = user.getProfileId();

										// try welcome
										reauthenticate = isTokenInvalid(((CosmeticaWebAPI) Cosmetica.api).getMasterToken());
										reason = "Invalid Cosmetica Token.";
									}
								}

								if (reauthenticate) {
									// If can't authenticate that way, authenticate from minecraft token
									DebugMode.log(reason + " Authenticating from minecraft access token.");
									Cosmetica.api = CosmeticaAPI.fromMinecraftToken(user.getAccessToken(), user.getName(), uuid, System.getProperty("cosmetica.client", "cosmetica")); // getUuid() better have the dashes... edit: it did not have the dashes.
									Cosmetica.api.setUrlLogger(DebugMode::logURL);
									authenticatedAsUUID = user.getProfileId();

									// Update master token
									tokens.setProperty(uuid.toString(), ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
									// Update limited token
									Field fieldLT = CosmeticaWebAPI.class.getDeclaredField("limitedToken");
									fieldLT.setAccessible(true);
									tokens.setProperty(uuid + "-l", fieldLT.get(Cosmetica.api).toString());

									// Save updated tokens
									DebugMode.log("Caching authentication tokens");
									try (OutputStream stream = Files.newOutputStream(tokensPath)) {
										tokens.store(stream, "Cosmetica Tokens (shh!)");
									} catch (IOException e) {
										Cosmetica.LOGGER.error("Failed to save tokens.", e);
									}
 								} else {
									DebugMode.log("Authentication Successful.");
								}

								// success response
								currentlyAuthenticated = true;
								currentlyAuthenticating = false;

								Optional<LoginInfo> oInfo = Cosmetica.api.getLoginInfo();

								oInfo.ifPresent(info -> {
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
											defaults.shouldDoOnlineActivity().ifPresent(v -> settings.put("doonlineactivity", v));
											defaults.getIconSettings().ifPresent(v -> settings.put("iconsettings", v));

											// post the settings to update if they exist
											if (!settings.isEmpty()) {
												Cosmetica.api.updateUserSettings(settings);
											}

											// handle default-setting-defined capes
											if (!info.hasSpecialCape()) {
												String capeId = defaults.getCapeId();

												if (!capeId.isEmpty()) {
													Cosmetica.api.setCosmetic(CosmeticPosition.CAPE, capeId, true);
												}
											}

											// handle default-setting-defined cape server settings
											Map<String, CapeDisplay> capeServerSettings = defaults.getCapeServerSettings();

											if (!capeServerSettings.isEmpty()) {
												Cosmetica.api.setCapeServerSettings(capeServerSettings);
											}
										}
									}
								});

								// welcome players
								// and by new I mean has new to cosmetica lore
								if (reauthenticate) {
									prepareWelcome(uuid, user.getName(), oInfo.map(LoginInfo::isNewPlayer).orElse(false), false);
								}

								// load the player's data if not loaded for later
								RenderSystem.recordRenderCall(() -> PlayerData.get(uuid, user.getName(), false));

								// synchronise settings from the server to the mod
								syncSettings();
							} catch (Exception e) {
								Cosmetica.LOGGER.error("Couldn't connect to cosmetica auth server", e);

								currentlyAuthenticating = false;
								Authentication.showUnauthenticatedIfLoading(false, e);
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

	private static boolean isTokenInvalid(String token) throws UncheckedIOException {
		try (Response response = Response.get("https://api.cosmetica.cc/get/uuid?token=" + token)) {
			JsonObject object = response.getAsJson();

			if (object.has("error")) {
				return object.get("error").getAsString().contains("invalid token");
			}
		} catch (IOException e) {
			// this would run if offline, but shouldn't get to this point if we can already authenticate eh?
			throw new UncheckedIOException("Checking token validity", e);
		}

		return false;
	}

	protected static void runSyncSettingsThread() {
		Thread settingsSyncThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000 * 60 * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				syncSettings();
			}
		});
		settingsSyncThread.start();
	}
}
