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

package cc.cosmetica.cosmetica.utils;

import cc.cosmetica.api.Box;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import cc.cosmetica.cosmetica.config.DebugModeConfig;
import cc.cosmetica.cosmetica.cosmetics.BackBling;
import cc.cosmetica.cosmetica.cosmetics.CustomLayer;
import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.utils.textures.LocalCapeTexture;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * A set of utilities for debug
 */
public class DebugMode {
	public static final boolean ENABLED = FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean("cosmetica.debug");
	private static final boolean EXTRA_LOGGING = Boolean.getBoolean("cosmetica.extraLogging");

	private static final File CONFIG_DIR;
	private static final File DEBUG_SETTINGS;

	private static final Logger DEBUG_LOGGER = LogManager.getLogger("Cosmetica Debug");

	public static final ResourceLocation TEST_CAPE = new ResourceLocation("cosmetica", "test/loaded_cape");
	public static int frameDelayMs = 50;

	// edit this to change debug settings
	private static DebugModeConfig debugSettings = new DebugModeConfig();
	private static final Set<String> complainedAbout = new HashSet<>();
	private static final Set<String> warnedAbout = new HashSet<>();

	public static void complainOnce(String key, String str, Object... objects) {
		if (elevatedLogging() && !complainedAbout.contains(key)) {
			complainedAbout.add(key);
			DEBUG_LOGGER.info("[COMPLAINT] " + str, objects);
		}
	}

	// not actually a debug mode thing but it fits here
	public static void warnOnce(String key, String str, Object... objects) {
		if (!warnedAbout.contains(key)) {
			warnedAbout.add(key);
			Cosmetica.LOGGER.warn(str, objects);
		}
	}

	public static void log(String str, Object... objects) {
		if (elevatedLogging()) {
			DEBUG_LOGGER.info(str, objects);
		}
		else {
			Cosmetica.LOGGER.debug(str, objects);
		}
	}

	public static void logError(String message, Exception e) {
		if (elevatedLogging()) {
			DEBUG_LOGGER.info(message, e);
		}
		else {
			Cosmetica.LOGGER.debug(message + " " + e.getClass().getName() + " " + e.getMessage());
		}
	}

	public static void logURL(String str) {
		if (urlLogging()) {
			DEBUG_LOGGER.info(str);
		}
	}

	public static void log(Supplier<String> str) {
		if (elevatedLogging()) {
			DEBUG_LOGGER.info(str.get());
		}
		else {
			Cosmetica.LOGGER.debug(str.get());
		}
	}

	public static boolean debugCommands() {
		return ENABLED && debugSettings.debugCommands;
	}

	public static boolean alwaysShowCosmeticaSplash() {
		return ENABLED && debugSettings.forceCosmeticaSplash;
	}

	public static boolean elevatedLogging() {
		return EXTRA_LOGGING || (ENABLED && debugSettings.elevateDebugLogging);
	}

	private static boolean urlLogging() {
		return EXTRA_LOGGING || (ENABLED && debugSettings.logURLs);
	}

	public static boolean forceRSEScreen() {
		return ENABLED && debugSettings.forceRseScreen;
	}

	private static boolean loadTestModel(CosmeticStack<BakableModel> model, String modelLoc, int extraInfo) {
		File modelJsonF = new File(CONFIG_DIR, modelLoc + ".json");

		if (modelJsonF.isFile()) {
			File imageF = new File(CONFIG_DIR, modelLoc + ".png");

			if (imageF.isFile()) {
				BlockModel blockModel;

				try (FileReader reader = new FileReader(modelJsonF)) {
					blockModel = BlockModel.fromStream(reader);
				} catch (IOException | JsonParseException e) {
					Cosmetica.LOGGER.error("Error reading test block model for " + modelLoc, e);
					return false;
				}

				ResourceLocation resourceLocation = new ResourceLocation("cosmetica_debug", "test/" + modelLoc.toLowerCase(Locale.ROOT));

				Minecraft.getInstance().getTextureManager().register(resourceLocation, new LocalCapeTexture(new ResourceLocation("cosmetica_debug", modelLoc.toLowerCase(Locale.ROOT)), 1, () -> {
					try (InputStream stream = new BufferedInputStream(new FileInputStream(imageF))) {
						return NativeImage.read(stream);
					} catch (IOException e) {
						Cosmetica.LOGGER.error("Error reading test model texture for " + modelLoc, e);
						return null;
					}
				}));

				model.push(new BakableModel(
						"test-" + modelLoc,
						modelLoc,
						blockModel,
						resourceLocation,
						extraInfo, new Box(0, 0, 0, 0, 0 , 0)));

				return true;
			} else {
				Cosmetica.LOGGER.warn("Json for test model {} found but no associated 32x32 image. Skipping loading the model override!", modelLoc);
			}
		}

		return false;
	}

	public static void reloadTestModels() {
		try {
			loadDebugSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}

		loadTestModel(LocalModelType.HAT);
		loadTestModel(LocalModelType.LEFT_SHOULDERBUDDY);
		loadTestModel(LocalModelType.RIGHT_SHOULDERBUDDY);
		loadTestModel(LocalModelType.BACK_BLING);
		loadTestCape();
	}

	public static boolean loadTestModel(LocalModelType type) {
		String model = type.localIdProvider.get();

		if (model.isBlank()) {
			type.modelOverride.clear();
			return false;
		} else {
			return loadTestModel(
					type.modelOverride,
					model,
					type.extraInfoLoader.getAsInt()
			);
		}
	}

	public static boolean loadTestCape() {
		String location = debugSettings.cape.location;
		File imageF = new File(CONFIG_DIR, location + ".png");

		if (imageF.isFile()) {
			Minecraft.getInstance().getTextureManager().register(TEST_CAPE, new LocalCapeTexture(new ResourceLocation("cosmetica_debug", location.toLowerCase(Locale.ROOT)), 2, () -> {
				try (InputStream stream = new BufferedInputStream(new FileInputStream(imageF))) {
					return NativeImage.read(stream);
				} catch (IOException e) {
					Cosmetica.LOGGER.error("Error reading test cape image for " + location, e);
					return null;
				}
			}));

			CustomLayer.CAPE_OVERRIDER.push(TEST_CAPE);
			CosmeticaSkinManager.setTestUploaded("loaded_cape");

			frameDelayMs = debugSettings.cape.frameDelay;

			return true;
		} else {
			Cosmetica.LOGGER.warn("No cape image found at {}. Skipping loading the cape override!", location);
		}

		CustomLayer.CAPE_OVERRIDER.clear();
		return false;
	}

	public static boolean loadDebugSettings() throws IOException {
		if (!DEBUG_SETTINGS.exists()) {
			return false;
		}

		try (FileReader reader = new FileReader(DEBUG_SETTINGS)) {
			debugSettings = new Gson().fromJson(reader, DebugModeConfig.class);
		}

		return true;
	}

	public static void saveDebugSettings() throws IOException {
		try (FileWriter writer = new FileWriter(DEBUG_SETTINGS)) {
			new Gson().toJson(debugSettings, writer);
		}
	}

	static {
		// cosmetica's data folders
		CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cosmetica");
		DEBUG_SETTINGS = new File(CONFIG_DIR, "debug_settings.json");

		if (ENABLED) {
			CONFIG_DIR.mkdirs();
			DEBUG_LOGGER.info("Debug Mode Enabled.");

			// create file if not exists. replenish with default config settings
			try {
				if (!loadDebugSettings()) {
					DEBUG_SETTINGS.createNewFile();
				}

				// write data in case there are new/changed settings!
				saveDebugSettings();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			Minecraft.getInstance().tell(() -> {
				log("Loading test models and capes...");
				loadTestModel(LocalModelType.HAT);
				loadTestModel(LocalModelType.LEFT_SHOULDERBUDDY);
				loadTestModel(LocalModelType.RIGHT_SHOULDERBUDDY);
				loadTestModel(LocalModelType.BACK_BLING);
				loadTestCape();
			});
		}
	}

	public record LocalModelType(CosmeticStack<BakableModel> modelOverride, Supplier<String> localIdProvider, IntSupplier extraInfoLoader) {
		public static final LocalModelType HAT = new LocalModelType(
				Hats.OVERRIDDEN,
				() -> debugSettings.hat.location,
				() -> debugSettings.hat.flags
		);
		public static final LocalModelType LEFT_SHOULDERBUDDY = new LocalModelType(
				ShoulderBuddies.LEFT_OVERRIDDEN,
				() -> debugSettings.leftShoulderBuddy.location,
				() -> debugSettings.rightShoulderBuddy.flags
		);
		public static final LocalModelType RIGHT_SHOULDERBUDDY = new LocalModelType(
				ShoulderBuddies.RIGHT_OVERRIDDEN,
				() -> debugSettings.rightShoulderBuddy.location,
				() -> debugSettings.rightShoulderBuddy.flags
		);
		public static final LocalModelType BACK_BLING = new LocalModelType(
				BackBling.OVERRIDDEN,
				() -> debugSettings.backBling.location,
				() -> debugSettings.backBling.flags
		);
	}
}
