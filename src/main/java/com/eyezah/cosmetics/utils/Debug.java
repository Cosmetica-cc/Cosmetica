package com.eyezah.cosmetics.utils;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.Hat;
import com.eyezah.cosmetics.cosmetics.ShoulderBuddy;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.cosmetics.model.OverriddenModel;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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

	// edit this to change debug settings
	private static Settings debugSettings = new Settings();

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

	private static boolean loadTestModel(OverriddenModel model, String modelLoc, int extraInfo) {
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

				model.setTestModel(new BakableModel(
						"test-" + modelLoc,
						blockModel,
						image,
						extraInfo));

				return true;
			} else {
				Cosmetica.LOGGER.warn("Json for test model {} found but no associated 32x32 image. Skipping loading the model override!", modelLoc);
			}
		}

		return false;
	}

	public static boolean loadTestModel(LocalModelType type) {
		String model = type.localIdProvider.get();

		if (model.isBlank()) {
			type.modelOverride.removeTestModel();
			return false;
		} else {
			return loadTestModel(
					type.modelOverride,
					model,
					type.extraInfoLoader.getAsInt()
			);
		}
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
		TEST_PROPERTIES.setProperty("show_hat_under_helmet", "false");
		TEST_PROPERTIES.setProperty("shoulderbuddy_location", "shoulderbuddy");
		TEST_PROPERTIES.setProperty("lock_shoulderbuddy_orientation", "false");

		boolean foundPropertiesFile = TEST_PROPERTIES_FILE.isFile();

		if (foundPropertiesFile) {
			loadTestProperties();
		}

		// test for test models
		boolean testModelExists = false;

		testModelExists |= loadTestModel(LocalModelType.HAT);
		testModelExists |= loadTestModel(LocalModelType.SHOULDERBUDDY);

		if (testModelExists || foundPropertiesFile) {
			Cosmetica.LOGGER.info("Test mode enabled! Special test settings available in the cosmetica menu.");
			TEST_MODE = true;
		} else {
			TEST_MODE = false;
		}

		if (testModelExists && !TEST_PROPERTIES_FILE.isFile()) { // configDir definitely exists if a test model exists.
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
					data.add("logging", new JsonPrimitive(false));
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
								key -> cache.computeBooleanIfAbsent(key, predicate));

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

	public record LocalModelType(OverriddenModel modelOverride, Supplier<String> localIdProvider, IntSupplier extraInfoLoader) {
		public static final LocalModelType HAT = new LocalModelType(
				Hat.overridden,
				() -> TEST_PROPERTIES.getProperty("hat_location"),
				() -> Boolean.parseBoolean(TEST_PROPERTIES.getProperty("show_hat_under_helmet")) ? 1 : 0
		);
		public static final LocalModelType SHOULDERBUDDY = new LocalModelType(
				ShoulderBuddy.overridden,
				() -> TEST_PROPERTIES.getProperty("shoulderbuddy_location"),
				() -> Boolean.parseBoolean(TEST_PROPERTIES.getProperty("lock_shoulderbuddy_orientation")) ? 1 : 0
		);
	}
}
