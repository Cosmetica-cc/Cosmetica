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

package cc.cosmetica.cosmetica.config;

import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.cosmetica.Cosmetica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;

public class DefaultSettingsConfig {
	private final Path propertiesPath;

	private String capeId = "";
	private Optional<Boolean> enableHats = Optional.empty();
	private Optional<Boolean> enableShoulderBuddies = Optional.empty();
	private Optional<Boolean> enableBackBlings = Optional.empty();
	private Optional<Boolean> enableLore = Optional.empty();
	private OptionalInt iconSettings = OptionalInt.empty();

	private boolean loaded = false;

	private final Map<String, CapeDisplay> capeServerSettings = new HashMap<>();

	public DefaultSettingsConfig(Path propertiesPath) {
		this.propertiesPath = propertiesPath;
	}

	public String getCapeId() {
		return capeId;
	}

	public Optional<Boolean> areHatsEnabled() {
		return enableHats;
	}

	public Optional<Boolean> areShoulderBuddiesEnabled() {
		return enableShoulderBuddies;
	}

	public Optional<Boolean> areBackBlingsEnabled() {
		return enableBackBlings;
	}

	public Optional<Boolean> isLoreEnabled() {
		return enableLore;
	}

	public OptionalInt getIconSettings() {
		return this.iconSettings;
	}

	public void initialize() throws IOException {
		load();
		if (!Files.exists(propertiesPath)) {
			save();
		}
	}

	public void load() throws IOException {
		if (!Files.exists(propertiesPath)) {
			return;
		}

		Properties properties = new Properties();
		properties.load(Files.newInputStream(propertiesPath));
		capeId = properties.getProperty("starter-cape-id");
		enableHats = parseBlankableBoolean(properties.getProperty("enable-hats", ""));
		enableShoulderBuddies = parseBlankableBoolean(properties.getProperty("enable-shoulder-buddies", ""));
		enableBackBlings = parseBlankableBoolean(properties.getProperty("enable-back-blings", ""));
		enableLore = parseBlankableBoolean(properties.getProperty("enable-lore", ""));
		iconSettings = properties.getProperty("enable-icons", "").isEmpty() ? OptionalInt.empty() : OptionalInt.of(
				flag(UserSettings.DISABLE_ICONS, !Boolean.parseBoolean(properties.getProperty("enable-icons", "true")))
				| flag(UserSettings.DISABLE_OFFLINE_ICONS, !parseBlankableBoolean(properties.getProperty("enable-online-icons", ""), true))
				| flag(UserSettings.DISABLE_SPECIAL_ICONS, !parseBlankableBoolean(properties.getProperty("enable-special-icons", ""), true))
		);

		capeServerSettings.clear();

		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith("cape-setting-")) {
				String service = propertyName.substring(13);

				try {
					CapeDisplay value = CapeDisplay.valueOf(properties.getProperty(propertyName).toUpperCase(Locale.ROOT));
					capeServerSettings.put(service, value);
				}
				catch (IllegalArgumentException e) {
					Cosmetica.LOGGER.warn("Unknown cape display type: \"" + properties.getProperty(propertyName) + "\". Putting \"show\"! (Valid options are \"show\", \"hide\", \"replace\"");
					capeServerSettings.put(service, CapeDisplay.SHOW);
				}
			}
		}

		this.loaded = true;
	}

	public Map<String, CapeDisplay> getCapeServerSettings() {
		return this.capeServerSettings;
	}

	public void save() throws IOException {
		File parentDir = propertiesPath.getParent().toFile();
		if (!parentDir.exists()) parentDir.mkdir();

		Properties properties = new Properties();
		properties.setProperty("starter-cape-id", capeId);
		properties.setProperty("enable-hats", toStringBlankable(enableHats));
		properties.setProperty("enable-shoulder-buddies", toStringBlankable(enableShoulderBuddies));
		properties.setProperty("enable-back-blings", toStringBlankable(enableBackBlings));
		properties.setProperty("enable-lore", toStringBlankable(enableLore));

		if (this.iconSettings.isPresent()) {
			properties.setProperty("enable-icons", Boolean.toString((this.iconSettings.getAsInt() & UserSettings.DISABLE_ICONS) == 0));
			properties.setProperty("enable-offline-icons", Boolean.toString((this.iconSettings.getAsInt() & UserSettings.DISABLE_OFFLINE_ICONS) == 0));
			properties.setProperty("enable-special-icons", Boolean.toString((this.iconSettings.getAsInt() & UserSettings.DISABLE_SPECIAL_ICONS) == 0));
		}
		else {
			properties.setProperty("enable-icons", "");
			properties.setProperty("enable-offline-icons", "");
			properties.setProperty("enable-special-icons", "");
		}

		capeServerSettings.forEach((capeServerSetting, display) -> properties.setProperty("cape-setting-" + capeServerSetting, display.name().toLowerCase(Locale.ROOT)));
		properties.store(Files.newOutputStream(propertiesPath), "Cosmetica Default Settings Config");
	}

	public boolean wasLoaded() {
		return this.loaded;
	}

	private static int flag(int flag, boolean condition) {
		return condition ? flag : 0;
	}

	private static Optional<Boolean> parseBlankableBoolean(String property) {
		if (property.equals("")) {
			return Optional.empty();
		}
		else {
			return Optional.of(Boolean.parseBoolean(property));
		}
	}

	private static boolean parseBlankableBoolean(String property, boolean defaultValue) {
		if (property.equals("")) {
			return defaultValue;
		}
		else {
			return Boolean.parseBoolean(property);
		}
	}

	private static String toStringBlankable(Optional<Boolean> property) {
		return property.isPresent() ? Boolean.toString(property.get()) : "";
	}
}
