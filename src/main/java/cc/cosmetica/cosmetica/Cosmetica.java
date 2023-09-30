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

import cc.cosmetica.api.Cape;
import cc.cosmetica.api.CosmeticaAPI;
import cc.cosmetica.api.CustomCape;
import cc.cosmetica.api.Model;
import cc.cosmetica.api.ShoulderBuddies;
import cc.cosmetica.api.User;
import cc.cosmetica.api.UserInfo;
import cc.cosmetica.cosmetica.config.CosmeticaConfig;
import cc.cosmetica.cosmetica.config.DefaultSettingsConfig;
import cc.cosmetica.cosmetica.cosmetics.CapeData;
import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.cosmetics.model.Models;
import cc.cosmetica.cosmetica.screens.LoadingScreen;
import cc.cosmetica.cosmetica.screens.fakeplayer.Playerish;
import cc.cosmetica.cosmetica.utils.DebugMode;
import cc.cosmetica.cosmetica.utils.NamedThreadFactory;
import cc.cosmetica.cosmetica.utils.SpecialKeyMapping;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cc.cosmetica.cosmetica.Authentication.runSyncSettingsThread;

@Environment(EnvType.CLIENT)
public class Cosmetica implements ClientModInitializer {
	public static String authServer;
	public static String websiteHost;
	// Initialise to an unauthenticated instance, Authenticate later, if possible.
	public static CosmeticaAPI api;

	// for cosmetic sniper
	public static Player farPickPlayer;
	public static HitResult farPickHitResult;

	// for welcome & vcheck
	public static Component displayNext;

	public static String currentServerAddressCache = "";
	public static KeyMapping openCustomiseScreen;
	public static KeyMapping snipe;

	public static final Logger LOGGER = LogManager.getLogger("Cosmetica");

	private static final ExecutorService MAIN_POOL = Executors.newFixedThreadPool(
			Integer.parseInt(System.getProperty("cosmetica.lookupThreads", "8")),
			new NamedThreadFactory("Cosmetica Lookup Thread"));

	private static CosmeticaConfig config;
	private static DefaultSettingsConfig defaultSettingsConfig;
	private static Path configDirectory;
	private static Path cacheDirectory;

	private static boolean mayShowWelcomeScreen = false;

 	/**
	 * The timestamp for the africa endpoint.
	 */
	private static OptionalLong toto = OptionalLong.empty();
	private static final Pattern UNDASHED_UUID_GAPS = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
	private static final String UUID_DASHIFIER_REPLACEMENT = "$1-$2-$3-$4-$5";

	private static final List<String> splashes = new LinkedList<>();

	private static void addSplash(String splash) {
		splashes.add(splash);
	}

	public static Collection<String> getSplashes() {
		return splashes;
	}

	public static CosmeticaConfig getConfig() {
		return config;
	}

	public static Path getConfigDirectory() {
		return configDirectory;
	}

	public static Path getCacheDirectory() {
		return cacheDirectory;
	}

	public static DefaultSettingsConfig getDefaultSettingsConfig() {
		return defaultSettingsConfig;
	}

	/**
	 * Gets whether the client is allowed to show the welcome screen.
	 * @return a boolean giving the status of whether the client is allowed to show the welcome screen.
	 */
	public static boolean mayShowWelcomeScreen() {
		return mayShowWelcomeScreen;
	}

	@Override
	public void onInitializeClient() {
		config = new CosmeticaConfig(FabricLoader.getInstance().getConfigDir().resolve("cosmetica").resolve("cosmetica.properties"));

		setupDirectories();

		// initialise config
		try {
			config.initialize();
			defaultSettingsConfig.initialize();
		} catch (IOException e) {
			LOGGER.warn("Failed to load config, falling back to defaults!");
			e.printStackTrace();
		}

		// delete debug dump images
		DebugMode.clearImages();

		// Set up API stuff
		try {
			File apiCache = new File(cacheDirectory.toFile(), "cosmetica_get_api_cache.json");
			CosmeticaAPI.setAPICache(apiCache);
			
			CosmeticaAPI.setDefaultForceHttps(config.paranoidHttps());
			api = CosmeticaAPI.newUnauthenticatedInstance();

			// API Url Getter
			runOffthread(() -> {
				try {
					api.setUrlLogger(DebugMode::logURL);

					DebugMode.log("Finished retrieving API Url. Conclusion: the API should be contacted at " + CosmeticaAPI.getAPIServer());
					LOGGER.info(CosmeticaAPI.getMessage());

					if (config.shouldAddCosmeticaSplashMessage()) {
						addSplash(CosmeticaAPI.getMessage());
					}

					Cosmetica.authServer = CosmeticaAPI.getAuthServer();
					Cosmetica.websiteHost = CosmeticaAPI.getWebsite();

					DebugMode.log("Checking Version...");

					api.checkVersion(
							SharedConstants.getCurrentVersion().getId(),
							FabricLoader.getInstance().getModContainer("cosmetica").get().getMetadata().getVersion().getFriendlyString()
					).ifSuccessfulOrElse(versionInfo -> {
						DebugMode.log("Handling version check response");

						String s = versionInfo.minecraftMessage();

						if (!s.isEmpty()) {
							// log every time
							Cosmetica.LOGGER.warn(versionInfo.plainMessage());

							// always show in game if vital, otherwise the user can choose whether to show
							if (versionInfo.isVital() || Cosmetica.getConfig().shouldShowNonVitalUpdateMessages()) {
								displayNext = TextComponents.literal(s);
							}
						}

						mayShowWelcomeScreen = versionInfo.megaInvasiveTutorial();
					}, Cosmetica.logErr("Error checking version"));

					Authentication.runAuthentication();
				} catch (Exception e) {
					LOGGER.error("Error retrieving API Url. Mod functionality will be disabled!");
					e.printStackTrace();
				}
			}, ThreadPool.GENERAL_THREADS);
		} catch (IllegalStateException e) {
			e.printStackTrace();

			// in the case that Cosmetica API host can't be received, give the user more info on what this entails with regards to the mod.
			// TODO retry authentication later so this isn't really an issue. Offline screen should trigger so perhaps this isn't too necessary?
			if ("Could not receive Cosmetica API host".equals(e.getMessage())) {
				Cosmetica.LOGGER.warn("Authenticated functionality will be disabled!");
			}
		}

		// make sure it clears relevant caches on resource reload
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation("cosmetica", "cache_clearer");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				Models.resetTextureBasedCaches(); // reset only the caches that need to be reset after a resource reload
			}
		});

		runSyncSettingsThread();
	}

	private static void setupDirectories() {
		configDirectory = FabricLoader.getInstance().getConfigDir().resolve("cosmetica");
		defaultSettingsConfig = new DefaultSettingsConfig(FabricLoader.getInstance().getConfigDir().resolve("cosmetica").resolve("default-settings.properties"));

		Path minecraftDir = findDefaultInstallDir("minecraft");

		if (Files.isDirectory(minecraftDir)) {
			cacheDirectory = minecraftDir.resolve(".cosmetica");
		}
		else {
			cacheDirectory = FabricLoader.getInstance().getGameDir().resolve(".cosmetica");
		}

		// create cache directory if it doesn't exist
		if (!Files.exists(cacheDirectory)) {
			try {
				Files.createDirectory(cacheDirectory);

				// stupid windows
				if (Util.getPlatform() == Util.OS.WINDOWS) {
					try {
						Files.setAttribute(cacheDirectory, "dos:hidden", true);
					} catch (Exception e) {
						Cosmetica.LOGGER.warn("Failed to set dos:hidden for cache file on windows", e);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Error creating cache directory", e);
			}
		}
	}

	public static void registerKeyMappings(List<KeyMapping> keymappings) {
		keymappings.add(openCustomiseScreen = new SpecialKeyMapping(
				"key.cosmetica.customise",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_RSHIFT, // not bound by default
				"key.categories.misc"
		));

		keymappings.add(snipe = new SpecialKeyMapping(
				"key.cosmetica.snipe",
				InputConstants.Type.MOUSE,
				InputConstants.MOUSE_BUTTON_MIDDLE, // not bound by default
				"key.categories.misc"
		));
	}

	public static boolean isProbablyNPC(UUID uuid) {
		return uuid.version() == 2; // NPCs are uuid version 2. Of course, this can't always be guaranteed with the many different server software, but it seems to be the case at least on hypixel
	}

	private static String loadOrCache(File file, @Nullable String value) {
		try {
			if (value != null) {
				file.createNewFile();

				try (FileWriter writer = new FileWriter(file)) {
					writer.write(value);
				}
			} else if (file.isFile()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					value = reader.readLine().trim();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	/*
	 * Adapted from code at https://github.com/FabricMC/fabric-installer
	 * Original license has been preserved for this method.
	 *
	 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
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
	private static Path findDefaultInstallDir(String application) {
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		Path dir;

		if (os.contains("win") && System.getenv("APPDATA") != null) {
			dir = Paths.get(System.getenv("APPDATA")).resolve("." + application);
		} else {
			String home = System.getProperty("user.home", ".");
			Path homeDir = Paths.get(home);

			if (os.contains("mac")) {
				dir = homeDir.resolve("Library").resolve("Application Support").resolve(application);
			} else {
				dir = homeDir.resolve("." + application);
			}
		}

		return dir.toAbsolutePath().normalize();
	}

	public static void onShutdownClient() {
		try {
			MAIN_POOL.shutdownNow();
		} catch (RuntimeException e) { // Just in case.
			e.printStackTrace();
		}
	}

	// =================
	//       IMPL
	// =================

	public static String dashifyUUID(String uuid) {
		return UNDASHED_UUID_GAPS.matcher(uuid).replaceAll(UUID_DASHIFIER_REPLACEMENT);
	}

	public static String base64Ip(InetSocketAddress ip) {
		byte[] arr = (ip.getAddress().getHostAddress() + ":" + ip.getPort()).getBytes(StandardCharsets.UTF_8);
		return Base64.encodeBase64String(arr);
	}

	// Start Africa

	public static void safari(Minecraft minecraft, boolean yourFirstRodeo, boolean ignoreSelf) {
		InetSocketAddress prideRock = minecraft.isLocalServer() ? new InetSocketAddress("127.0.0.1", 25565) : null;
		if (prideRock == null && minecraft.getConnection().getConnection().getRemoteAddress() instanceof InetSocketAddress ip)
			prideRock = ip;
		if (prideRock != null)
			safari(prideRock, yourFirstRodeo, ignoreSelf);
	}

	/**
	 * Keep track of africa fails to silence it after 3 fails.
	 * If elevated logging is on, this is ignored.
	 */
	private static int africaFails = 0;

	public static void safari(InetSocketAddress prideRock, boolean yourFirstRodeo, boolean ignoreSelf) {
		if (api != null && api.isAuthenticated()) {
			DebugMode.log("Thread for safari {}", Thread.currentThread().getName());

			api.everyThirtySecondsInAfricaHalfAMinutePasses(prideRock, yourFirstRodeo || !Cosmetica.toto.isPresent() ? 0 : Cosmetica.toto.getAsLong())
					.ifSuccessfulOrElse(theLionSleepsTonight -> {
						// the speech from the lion king
						for (String notification : theLionSleepsTonight.getNotifications()) { // let's hope I made sure this isn't null
							try {
								Minecraft.getInstance().gui.getChat().addMessage(
										new TextComponent("§6§lCosmetica§f §l>§7 ").append(TextComponents.chatEncode(notification))
								);
							}
							catch (Exception e) {
								Cosmetica.LOGGER.error("Error sending cosmetica notification.", e);
							}
						}

						Cosmetica.toto = OptionalLong.of(theLionSleepsTonight.getTimestamp());

						if (!yourFirstRodeo) {
							DebugMode.log("Processing updates found on the safari.");

							for (User individual : theLionSleepsTonight.getNeedsUpdating()) {
								UUID uuid = individual.getUUID();
								DebugMode.log("Your amazing lion king with expected uuid {} seems to be requesting we update his (or her, their, faer, ...) cosmetics! :lion:", uuid);

								if (PlayerData.has(uuid)) {
									PlayerData.clear(uuid);

									// if ourselves, refresh asap
									if (!ignoreSelf && uuid.equals(Minecraft.getInstance().player.getUUID())) {
										PlayerData.get(Minecraft.getInstance().player);
									}
								} else {
									// Here are EyezahMC inc. we strive to be extremely descriptive with our debug messages.
									DebugMode.log("Lol cringe they went scampering into a bush or something!");

									// use username to clear the info - might be in offline mode or something
									String username = individual.getUsername();

									PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(username);

									if (info != null) {
										UUID serverUuid = info.getProfile().getId();

										if (PlayerData.has(serverUuid)) {
											DebugMode.log("Found them :). They were hiding at uuid {}", serverUuid);
											PlayerData.clear(serverUuid);

											// if ourselves, refresh asap
											if (!ignoreSelf && username.equals(String.valueOf(Minecraft.getInstance().player.getName()))) {
												PlayerData.get(Minecraft.getInstance().player);
											}
										}
									}
								}
							}
						}

						africaFails = 0;
			}, logErr("Error checking for cosmetic updates on the remote server", e -> {
				if (africaFails < 3) {
					africaFails++;
					return true;
				}
				else if (DebugMode.elevatedLogging()) {
					return true;
				}
				else {
					return false;
				}
			}));
		}
	}

	// End Africa

	public static void cinder(Minecraft minecraft, float yawProbably) {
		Entity entity = minecraft.getCameraEntity();

		if (entity != null) {
			if (minecraft.level != null) {
				minecraft.getProfiler().push("snipe");
				Cosmetica.farPickPlayer = null;

				final double maxDist = 64.0f;
				Cosmetica.farPickHitResult = entity.pick(maxDist, yawProbably, false);
				Vec3 eyePosition = entity.getEyePosition(yawProbably);

				double maxDistSqr = maxDist;
				maxDistSqr *= maxDistSqr;

				if (Cosmetica.farPickHitResult != null) {
					maxDistSqr = Cosmetica.farPickHitResult.getLocation().distanceToSqr(eyePosition);
				}

				Vec3 view = entity.getViewVector(1.0F);
				Vec3 castTowards = eyePosition.add(view.x * maxDist, view.y * maxDist, view.z * maxDist);

				final float inflation = 1.0F;
				AABB selectionBoundingBox = entity.getBoundingBox().expandTowards(view.scale(maxDist)).inflate(inflation, inflation, inflation);
				EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, eyePosition, castTowards, selectionBoundingBox, e -> !e.isSpectator() && e.isPickable(), maxDistSqr);

				if (entityHitResult != null) {
					Entity entity2 = entityHitResult.getEntity();
					Vec3 resultLocation = entityHitResult.getLocation();
					double distance = eyePosition.distanceToSqr(resultLocation);

					if (distance < maxDistSqr || Cosmetica.farPickHitResult == null) {
						Cosmetica.farPickHitResult = entityHitResult;

						if (entity2 instanceof Player player) { // vanilla crosshair pick: entity2 instanceof LivingEntity || entity2 instanceof ItemFrame
							Cosmetica.farPickPlayer = player;
						}
					}
				}

				minecraft.getProfiler().pop();
			}
		}
	}

	public static boolean handleComponentClicked(Minecraft minecraft, Style style) {
		// handle clicking the "here" in the welcome message
		if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.CHANGE_PAGE && style.getClickEvent().getValue().equals("cosmetica.customise")) {
			minecraft.setScreen(new LoadingScreen(null, Minecraft.getInstance().options, 1));
			return true;
		}

		return false;
	}

	@Nullable
	public static void runOffthread(Runnable runnable, @SuppressWarnings("unused") ThreadPool pool) {
		if (Thread.currentThread().getName().startsWith("Cosmetica")) {
			runnable.run();
		} else {
			MAIN_POOL.execute(runnable);
		}
	}

	public static boolean shouldRenderUpsideDown(Player player) {
		return PlayerData.get(player).upsideDown();
	}

	public static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	public static <K, V, V2> Map<K, V2> map(Map<K, V> original, Function<V, V2> mapper) {
		HashMap<K, V2> result = new HashMap<>();
		original.forEach((k, v) -> result.put(k, mapper.apply(v)));
		return result;
	}

	public static String pickFirst(String... strings) {
		for (String s : strings) {
			if (!s.isEmpty()) return s;
		}

		return "";
	}

	public static PlayerData newPlayerData(UserInfo info, UUID uuid) {
		List<Model> hats = info.getHats();
		Optional<ShoulderBuddies> shoulderBuddies = info.getShoulderBuddies();
		Optional<Model> backBling = info.getBackBling();
		Optional<Cape> cloak = info.getCape();
		String icon = info.getIcon();
		Optional<String> client = info.getClient();

		Optional<Model> leftShoulderBuddy = shoulderBuddies.isEmpty() ? Optional.empty() : shoulderBuddies.get().getLeft();
		Optional<Model> rightShoulderBuddy = shoulderBuddies.isEmpty() ? Optional.empty() : shoulderBuddies.get().getRight();

		return new PlayerData(
				info.getLore(),
				info.isUpsideDown(),
				icon.isEmpty() ? null : CosmeticaSkinManager.processIcon(icon),
				info.isOnline(),
				info.getPrefix(),
				info.getSuffix(),
				hats.stream().map(Models::createBakableModel).collect(Collectors.toList()),
				cloak.isPresent() ? new CapeData(
						CosmeticaSkinManager.processCape(cloak.get()),
						pickFirst(cloak.get().getName(), cloak.get().getOrigin() + " Cape"),
						cloak.get().getId(),
						!cloak.get().isCosmeticaAlternative() && !(cloak.get() instanceof CustomCape),
						cloak.get().getOrigin()
				) : CapeData.NO_CAPE,
				leftShoulderBuddy.isEmpty() ? null : Models.createBakableModel(leftShoulderBuddy.get()),
				rightShoulderBuddy.isEmpty() ? null : Models.createBakableModel(rightShoulderBuddy.get()),
				backBling.isEmpty() ? null : Models.createBakableModel(backBling.get()),
				CosmeticaSkinManager.processSkin(info.getSkin(), uuid),
				info.isSlim()
		);
	}

	public static void renderLore(EntityRenderDispatcher entityRenderDispatcher, Entity entity, PlayerModel<AbstractClientPlayer> playerModel, PoseStack stack, MultiBufferSource multiBufferSource, Font font, int packedLight) {
		if (entity instanceof Player player) {
			UUID lookupId = player.getUUID();

			if (lookupId != null) {
				double squaredDistance = entityRenderDispatcher.distanceToSqr(entity);
				PlayerData data = PlayerData.get(player);

				if (squaredDistance <= 4096.0D) {
					renderLore(
							stack,
							entityRenderDispatcher.cameraOrientation(),
							font,
							multiBufferSource,
							data.lore(),
							Hats.OVERRIDDEN.getList(() -> data.hats()),
							player.hasItemInSlot(EquipmentSlot.HEAD),
							!player.isSleeping(), // doNametagShift
							entity.isDiscrete(),
							data.upsideDown(),
							entity.getBbHeight(),
							playerModel.head.xRot,
							packedLight);
				}
			}
		}
	}

	public static void renderLore(PoseStack stack, Quaternion cameraOrientation, Font font, MultiBufferSource multiBufferSource, String lore, List<BakableModel> hats, boolean wearingHelmet, boolean doNametagShift, boolean discrete, boolean upsideDown, float playerHeight, float xRotHead, int packedLight) {
		// how much do we need to shift up nametags?

		// upside down players don't need nametags shifted up
		if (!upsideDown) {
			float hatTopY = 0;

			if (doNametagShift) {
				for (BakableModel hat : hats) {
					if (!((hat.extraInfo() & 0x1) == 0 && wearingHelmet)) {
						hatTopY = Math.max(hatTopY, (float) hat.bounds().y1());
					}
				}
			}

			if (hatTopY > 0) {
				float normalizedAngleMultiplier = (float) -(Math.abs(xRotHead) / 1.57 - 1);
				float lookAngleMultiplier;
				if (normalizedAngleMultiplier == 0.49974638F) { // Gliding with elytra, swimming, or crouching
					lookAngleMultiplier = 0;
				} else {
					lookAngleMultiplier = normalizedAngleMultiplier;
				}
				stack.translate(0, (hatTopY / 16) * lookAngleMultiplier, 0);
			}
		}

		// render lore
		if (!lore.equals("")) {
			Component component = new TextComponent(lore);

			boolean fullyRender = !discrete;

			float height = playerHeight + 0.25F;

			stack.translate(0, 0.1, 0);

			stack.pushPose();
			stack.translate(0.0D, height, 0.0D);
			stack.mulPose(cameraOrientation);
			stack.scale(-0.025F, -0.025F, 0.025F);
			stack.scale(0.75F, 0.75F, 0.75F);
			Matrix4f textModel = stack.last().pose();

			@SuppressWarnings("resource")
			float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
			int alphaARGB = (int) (backgroundOpacity * 255.0F) << 24;

			float xOffset = (float) (-font.width(component) / 2);

			font.drawInBatch(component, xOffset, 0, 553648127, false, textModel, multiBufferSource, fullyRender, alphaARGB, packedLight);

			if (fullyRender) {
				font.drawInBatch(component, xOffset, 0, -1, false, textModel, multiBufferSource, false, 0, packedLight);
			}

			stack.popPose();
		}
	}

	public static void renderTabIcon(PoseStack stack, int x, int y, UUID playerUUID, String name) {
		PlayerData data = PlayerData.get(playerUUID, name, false);
		@Nullable ResourceLocation iconTexture = data.icon();

		if (iconTexture != null) {
			// don't do discrete in tab. That could be classified as cheating, as you'd know if anyone online is sneaking.
			// I'm sure there's some minigame out there where that's important
			RenderSystem.enableBlend();
			renderTexture(stack.last().pose(), iconTexture, x + 1, x + 1 + 8, y, y + 8, 0, data.online() ? 1.0f : 0.5f);
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	public static void renderIcon(PoseStack poseStack, MultiBufferSource bufferSource, Playerish player, Font font, int packedLight, Component component) {
		PlayerData playerData = player.getCosmeticaPlayerData();
		@Nullable ResourceLocation iconTexture = playerData.icon();

		if (iconTexture != null) {
			float xOffset = -font.width(component) / 2.0f;

			poseStack.pushPose();
			poseStack.translate(xOffset + 1, 0, 0);
			renderTextureLikeText(poseStack.last().pose(), bufferSource, iconTexture, -1, 9, -1, 9, 0, packedLight, playerData.online() ? 1.0f : 0.5f, player.renderDiscreteNametag());

			poseStack.popPose();
		}
	}

	private static Vector3f rotateVertex(Vector3f vertex, Vector3f origin, Direction.Axis axis, float angle) {
		vertex.sub(origin);
		if (axis == Direction.Axis.X) {
			return new Vector3f(vertex.x() + origin.x(), (float) (vertex.y() * Math.cos(angle) - vertex.z() * Math.sin(angle)) + origin.y(), (float) (vertex.z() * Math.cos(angle) + vertex.y() * Math.sin(angle)) + origin.z());
		} else if (axis == Direction.Axis.Y) {
			return new Vector3f((float) (vertex.x() * Math.cos(angle) + vertex.z() * Math.sin(angle)) + origin.x(), vertex.y() + origin.y(), (float) (vertex.z() * Math.cos(angle) - vertex.x() * Math.sin(angle)) + origin.z());
		} else if (axis == Direction.Axis.Z) {
			return new Vector3f((float) (vertex.x() * Math.cos(angle) - vertex.y() * Math.sin(angle)) + origin.x(), (float) (vertex.y() * Math.cos(angle) + vertex.x() * Math.sin(angle)) + origin.y(), vertex.z() + origin.z());
		}

		throw new UnsupportedOperationException();
	}

	public static void renderTexture(Matrix4f matrix4f, ResourceLocation texture, int x0, int x1, int y0, int y1, int z, float transparency) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, transparency);

		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix4f, (float) x0, (float) y1, (float) z).uv(0, 1).endVertex();
		bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z).uv(1, 1).endVertex();
		bufferBuilder.vertex(matrix4f, (float) x1, (float) y0, (float) z).uv(1, 0).endVertex();
		bufferBuilder.vertex(matrix4f, (float) x0, (float) y0, (float) z).uv(0, 0).endVertex();
		bufferBuilder.end();
		BufferUploader.end(bufferBuilder);
	}

	private static int getMaxLight() {
		return (0xF << 20) | (0xF << 4);
	}

	public static void renderTextureLikeText(Matrix4f matrix4f, MultiBufferSource bufferSource, ResourceLocation texture, int x0, int x1, int y0, int y1, int z, int packedLight, float alpha, boolean discrete) {
		// Background
		// ==========
		if (!discrete) {
			RenderSystem.enableBlend();
			RenderSystem.disableDepthTest();

			int skylight = (packedLight >> 20) & 0xF;
			int blocklight = (packedLight >> 4) & 0xF;
			float shaderColour = Math.max(skylight, blocklight) / 15.0f;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, texture);
			RenderSystem.setShaderColor(shaderColour, shaderColour, shaderColour, 0.25f * alpha);

			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

			bufferBuilder.vertex(matrix4f, (float) x0, (float) y1, (float) z).uv(0, 1).endVertex();
			bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z).uv(1, 1).endVertex();
			bufferBuilder.vertex(matrix4f, (float) x1, (float) y0, (float) z).uv(1, 0).endVertex();
			bufferBuilder.vertex(matrix4f, (float) x0, (float) y0, (float) z).uv(0, 0).endVertex();

			bufferBuilder.end();
			BufferUploader.end(bufferBuilder);
		}

		// Regular Text Rendering
		// ======================

		float mainRenderAlpha = (discrete ? 0.3f : 1.0f) * alpha;

		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.text(texture));

		vertexConsumer.vertex(matrix4f, (float) x0, (float) y1, (float) z).color(1.0f, 1.0f, 1.0f, mainRenderAlpha).uv(0, 1).uv2(packedLight).endVertex();
		vertexConsumer.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(1.0f, 1.0f, 1.0f, mainRenderAlpha).uv(1, 1).uv2(packedLight).endVertex();
		vertexConsumer.vertex(matrix4f, (float) x1, (float) y0, (float) z).color(1.0f, 1.0f, 1.0f, mainRenderAlpha).uv(1, 0).uv2(packedLight).endVertex();
		vertexConsumer.vertex(matrix4f, (float) x0, (float) y0, (float) z).color(1.0f, 1.0f, 1.0f, mainRenderAlpha).uv(0, 0).uv2(packedLight).endVertex();
	}

	public static void clearAllCaches() {
		DebugMode.log("Clearing all Cosmetica Caches");
		PlayerData.clearCaches();
		Models.resetCaches();
		CosmeticaSkinManager.clearCaches();
		System.gc(); // force jvm to garbage collect our unused data
	}

	public static Consumer<RuntimeException> logErr(String message) {
		return e -> LOGGER.error(message + ": ", e);
	}

	public static Consumer<RuntimeException> logErr(String message, Predicate<RuntimeException> predicate) {
		return e -> {
			if (predicate.test(e)) {
				LOGGER.error(message + ": ", e);
			}
		};
	}
}
