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

package cc.cosmetica.cosmetica.utils;

import cc.cosmetica.api.Box;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import cc.cosmetica.cosmetica.cosmetics.BackBling;
import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.utils.textures.LocalCapeTexture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
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
import java.util.Properties;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A set of classes
 */
public class Debug {
	public static final boolean DEBUG_MODE = FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean("cosmetica.debug");
	public static final boolean TEST_MODE;

	private static final File CONFIG_DIR;
	private static final File TEST_PROPERTIES_FILE;
	private static final Properties TEST_PROPERTIES;

	private static final Logger DEBUG_LOGGER = LogManager.getLogger("Cosmetica Debug");
	public static final File DUMP_FOLDER;

	public static final CosmeticStack<ResourceLocation> CAPE_OVERRIDER = new CosmeticStack<>();
	public static final ResourceLocation TEST_CAPE = new ResourceLocation("cosmetica", "test/loaded_cape");
	public static int frameDelayMs = 50;

	// edit this to change debug settings
	private static Settings debugSettings = new Settings();
	private static Set<String> complainedAbout = new HashSet<>();

	public static void complainOnce(String key, String str, Object... objects) {
		if (DEBUG_MODE && debugSettings.logging() && !complainedAbout.contains(key)) {
			complainedAbout.add(key);
			DEBUG_LOGGER.info("[COMPLAINT] " + str, objects);
		}
	}

	public static void info(String str, Object... objects) {
		if (DEBUG_MODE && debugSettings.logging()) {
			DEBUG_LOGGER.info(str, objects);
		}
	}

	public static void checkedInfo(String str, String check) {
		if (DEBUG_MODE && debugSettings.other.test(check)) {
			DEBUG_LOGGER.info(str);
		}
	}

	public static void info(Supplier<String> str) {
		if (DEBUG_MODE && debugSettings.logging()) {
			DEBUG_LOGGER.info(str.get());
		}
	}

	public static boolean debugCommands() {
		return DEBUG_MODE && debugSettings.testUnverifiedCosmetics();
	}

	/**
	 * Dump images to the image dump folder.
	 * These images will be cleared on the next run.
	 */
	public static void dumpImages(String name, boolean capeModification, NativeImage... images) {
		if (DEBUG_MODE && (capeModification ? debugSettings.imageDumping().capeModifications() : debugSettings.imageDumping().textureLoading())) {
			int i = 0;
			for (NativeImage image : images) {
				try {
					File file = new File(DUMP_FOLDER, name + "_dump_" + i + ".png");
					file.createNewFile();
					image.writeToFile(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				++i;
			}
		}
	}

	public static void clearImages() {
		if (Debug.DEBUG_MODE && Debug.debugSettings.imageDumping().either()) {
			for (File file : DUMP_FOLDER.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".png")) {
					file.delete();
				}
			}
		}
	}

	private static boolean loadTestModel(CosmeticStack<BakableModel> model, String modelLoc, int extraInfo) {
		File modelJsonF = new File(CONFIG_DIR, modelLoc + ".json");

		if (modelJsonF.isFile()) {
			File imageF = new File(CONFIG_DIR, modelLoc + ".png");

			if (imageF.isFile()) {
				BlockModel blockModel;
				NativeImage image;

				try (FileReader reader = new FileReader(modelJsonF)) {
					blockModel = BlockModel.fromStream(reader);
				} catch (IOException | JsonParseException e) {
					Cosmetica.LOGGER.error("Error reading test block model for " + modelLoc, e);
					return false;
				}

				try (InputStream stream = new BufferedInputStream(new FileInputStream(imageF))) {
					image = NativeImage.read(stream);
				} catch (IOException | JsonParseException e) {
					Cosmetica.LOGGER.error("Error reading test block model for " + modelLoc, e);
					return false;
				}

				model.push(new BakableModel(
						"test-" + modelLoc,
						modelLoc,
						blockModel,
						image,
						extraInfo, new Box(0, 0, 0, 0, 0 , 0)));

				return true;
			} else {
				Cosmetica.LOGGER.warn("Json for test model {} found but no associated 32x32 image. Skipping loading the model override!", modelLoc);
			}
		}

		return false;
	}

	public static void reloadTestModels() {
		loadTestProperties();
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
		String location = TEST_PROPERTIES.getProperty("cape_location");
		File imageF = new File(CONFIG_DIR, location + ".png");

		if (imageF.isFile()) {
			Minecraft.getInstance().getTextureManager().register(TEST_CAPE, new LocalCapeTexture(new ResourceLocation("cosmetica_test_mode", location.toLowerCase(Locale.ROOT)), () -> {
				try (InputStream stream = new BufferedInputStream(new FileInputStream(imageF))) {
					return NativeImage.read(stream);
				} catch (IOException e) {
					Cosmetica.LOGGER.error("Error reading test cape image for " + location, e);
					return null;
				}
			}));

			CAPE_OVERRIDER.push(TEST_CAPE);
			CosmeticaSkinManager.setTestUploaded("loaded_cape");

			frameDelayMs = Integer.parseInt(TEST_PROPERTIES.getProperty("cape_frame_delay"));

			return true;
		} else {
			Cosmetica.LOGGER.warn("No cape image found at {}. Skipping loading the cape override!", location);
		}

		CAPE_OVERRIDER.clear();
		return false;
	}

	public static void loadTestProperties() {
		try (FileReader reader = new FileReader(TEST_PROPERTIES_FILE)) {
			TEST_PROPERTIES.load(reader);
		} catch (Exception e) {
			Cosmetica.LOGGER.error("Error loading cosmetica_testing.properties", e);
		}
	}

	public static void saveTestProperties() {
		try (FileWriter writer = new FileWriter(TEST_PROPERTIES_FILE)) {
			TEST_PROPERTIES.store(writer, "Cosmetica Testing Properties");
		} catch (Exception e) {
			Cosmetica.LOGGER.error("Exception writing cosmetica_testing.properties", e);
		}
	}

	static {
		// cosmetica's data folders
		DUMP_FOLDER = new File(FabricLoader.getInstance().getGameDir().toFile(), "cosmetic_dumps");
		CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "cosmetica");

		// test properties
		TEST_PROPERTIES_FILE = new File(CONFIG_DIR, "cosmetica_testing.properties");

		TEST_PROPERTIES = new Properties();
		TEST_PROPERTIES.setProperty("hat_location", "hat");
		TEST_PROPERTIES.setProperty("lock_hat_orientation", "false");
		TEST_PROPERTIES.setProperty("show_hat_under_helmet", "false");
		TEST_PROPERTIES.setProperty("left_shoulderbuddy_location", "shoulderbuddy");
		TEST_PROPERTIES.setProperty("right_shoulderbuddy_location", "shoulderbuddy");
		TEST_PROPERTIES.setProperty("lock_left_shoulderbuddy_orientation", "false");
		TEST_PROPERTIES.setProperty("lock_right_shoulderbuddy_orientation", "false");
		TEST_PROPERTIES.setProperty("backbling_location", "backbling");
		TEST_PROPERTIES.setProperty("cape_location", "cape");
		TEST_PROPERTIES.setProperty("cape_frame_delay", "50");

		boolean foundPropertiesFile = TEST_PROPERTIES_FILE.isFile();

		if (foundPropertiesFile) {
			loadTestProperties();
		}

		// test for test models
		boolean testCosmeticExists = false;

		testCosmeticExists |= loadTestModel(LocalModelType.HAT);
		testCosmeticExists |= loadTestModel(LocalModelType.LEFT_SHOULDERBUDDY);
		testCosmeticExists |= loadTestModel(LocalModelType.RIGHT_SHOULDERBUDDY);
		testCosmeticExists |= loadTestModel(LocalModelType.BACK_BLING);
		testCosmeticExists |= loadTestCape();

		if (testCosmeticExists || foundPropertiesFile) {
			Cosmetica.LOGGER.info("Test mode enabled! Special test settings available in the cosmetica menu.");
			TEST_MODE = true;
		} else {
			TEST_MODE = false;
		}

		if (testCosmeticExists && !TEST_PROPERTIES_FILE.isFile()) { // configDir definitely exists if a test model exists.
			saveTestProperties();
		}

		if (DEBUG_MODE) {
			CONFIG_DIR.mkdirs(); // because debug mode and test mode are independent

			File settings = new File(CONFIG_DIR, "debug_settings.json");

			// load cosmetic settings or write to a file if it doesn't exist
			try {
				if (settings.createNewFile()) {
					JsonObject imageDumping = new JsonObject();
					imageDumping.add("texture_loading", new JsonPrimitive(false));
					imageDumping.add("cape_modifications", new JsonPrimitive(false));

					JsonObject data = new JsonObject();
					data.add("logging", new JsonPrimitive(true));
					data.add("image_dumping", imageDumping);
					data.add("always_print_urls", new JsonPrimitive(false));
					data.add("debug_commands", new JsonPrimitive(false));

					try (FileWriter writer = new FileWriter(settings)) {
						writer.write(data.toString());
					}
				} else {
					try (FileReader reader = new FileReader(settings)) {
						JsonObject data = new JsonParser().parse(reader).getAsJsonObject();
						Object2BooleanMap<String> cache = new Object2BooleanArrayMap<>();
						Predicate<String> predicate = k_ -> data.has(k_) && Boolean.parseBoolean(data.get(k_).getAsString());

						JsonElement imageDumping = data.get("image_dumping");
						ImageDumpingSettings imageDumpingSettings;

						if (imageDumping.isJsonPrimitive()) {
							imageDumpingSettings = new ImageDumpingSettings(imageDumping.getAsBoolean());
						} else {
							JsonObject jo = imageDumping.getAsJsonObject();

							imageDumpingSettings = new ImageDumpingSettings(
									jo.get("texture_loading").getAsBoolean(),
									jo.get("cape_modifications").getAsBoolean()
							);
						}

						// legacy property first
						boolean useDebugCommands = data.has("test_unverified_cosmetics") ? data.get("test_unverified_cosmetics").getAsBoolean() : false;
						// then OR with current name for the property
						useDebugCommands |= data.has("debug_commands") ? data.get("debug_commands").getAsBoolean() : false;

						debugSettings = new Settings(
								data.get("logging").getAsBoolean(),
								imageDumpingSettings,
								useDebugCommands,
								key -> cache.computeIfAbsent(key, predicate::test));

						if (debugSettings.imageDumping().either()) {
							DUMP_FOLDER.mkdir();
						}
					}
				}
			} catch (IOException e) {
			}
		}
	}

	private record Settings(boolean logging, ImageDumpingSettings imageDumping, boolean testUnverifiedCosmetics, Predicate<String> other) {
		Settings() {
			this(false, new ImageDumpingSettings(false), false, i -> false);
		}
	}

	public record ImageDumpingSettings(boolean textureLoading, boolean capeModifications) {
		ImageDumpingSettings(boolean val) {
			this(val, val);
		}

		boolean either() {
			return this.textureLoading || this.capeModifications;
		}
	}

	public record LocalModelType(CosmeticStack<BakableModel> modelOverride, Supplier<String> localIdProvider, IntSupplier extraInfoLoader) {
		public static final LocalModelType HAT = new LocalModelType(
				Hats.OVERRIDDEN,
				() -> TEST_PROPERTIES.getProperty("hat_location"),
				() -> (Boolean.parseBoolean(TEST_PROPERTIES.getProperty("show_hat_under_helmet")) ? 1 : 0) | (Boolean.parseBoolean(TEST_PROPERTIES.getProperty("lock_hat_orientation")) ? 2 : 0)
		);
		public static final LocalModelType LEFT_SHOULDERBUDDY = new LocalModelType(
				ShoulderBuddies.LEFT_OVERRIDDEN,
				() -> TEST_PROPERTIES.getProperty("left_shoulderbuddy_location"),
				() -> Boolean.parseBoolean(TEST_PROPERTIES.getProperty("lock_left_shoulderbuddy_orientation")) ? 1 : 0
		);
		public static final LocalModelType RIGHT_SHOULDERBUDDY = new LocalModelType(
				ShoulderBuddies.RIGHT_OVERRIDDEN,
				() -> TEST_PROPERTIES.getProperty("right_shoulderbuddy_location"),
				() -> Boolean.parseBoolean(TEST_PROPERTIES.getProperty("lock_right_shoulderbuddy_orientation")) ? 1 : 0
		);
		public static final LocalModelType BACK_BLING = new LocalModelType(
				BackBling.OVERRIDDEN,
				() -> TEST_PROPERTIES.getProperty("backbling_location"),
				() -> 0
		);
	}
}
