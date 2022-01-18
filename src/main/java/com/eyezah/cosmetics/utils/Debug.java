package com.eyezah.cosmetics.utils;

import com.eyezah.cosmetics.Cosmetics;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.system.CallbackI;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A set of classes
 */
public class Debug {
	public static boolean DEBUG_MODE = FabricLoader.getInstance().isDevelopmentEnvironment() || System.getProperty("cosmetics.debug", "false").equals("true");
	public static File DUMP_FOLDER;
	// edit this to change debug settings
	private static Settings DEBUG_SETTINGS = new Settings();

	public static void info(String str) {
		if (DEBUG_MODE && DEBUG_SETTINGS.logging()) {
			Cosmetics.LOGGER.info(str);
		}
	}

	/**
	 * Dump images to the image dump folder.
	 * These images will be cleared on the next run.
	 */
	public static void dumpImages(String name, NativeImage... images) {
		if (DEBUG_MODE && DEBUG_SETTINGS.imageDumping()) {
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
		if (Debug.DEBUG_MODE && Debug.DEBUG_SETTINGS.imageDumping()) {
			for (File file : DUMP_FOLDER.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".png")) {
					file.delete();
				}
			}
		}
	}

	static {
		DUMP_FOLDER = new File(FabricLoader.getInstance().getGameDir().toFile(), "cosmetic_dumps");

		if (DEBUG_MODE) {
			DUMP_FOLDER.mkdir();
			File configDir = FabricLoader.getInstance().getConfigDir().toFile();
			configDir.mkdir();
			File settings = new File(configDir, "debug_settings.json");

			// load cosmetic settings or write to a file if it doesn't exist
			try {
				if (settings.createNewFile()) {
					JsonObject data = new JsonObject();
					data.add("logging", new JsonPrimitive(false));
					data.add("image_dumping", new JsonPrimitive(false));

					try (FileWriter writer = new FileWriter(settings)) {
						writer.write(data.toString());
					}
				} else {
					try (FileReader reader = new FileReader(settings)) {
						JsonObject data = new JsonParser().parse(reader).getAsJsonObject();
						DEBUG_SETTINGS = new Settings(
								data.get("logging").getAsBoolean(),
								data.get("image_dumping").getAsBoolean());
					}
				}
			} catch (IOException e) {
			}
		}
	}

	private record Settings(boolean logging, boolean imageDumping) {
		Settings() {
			this(false, false);
		}
	}
}
