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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class DefaultSettingsConfig {
	private final Path propertiesPath;

	private String capeId = "";
	private boolean enableHats = false;
	private boolean enableShoulderBuddies = false;
	private boolean enableBackBlings = false;

	private final Map<String, CapeDisplay> capeServerSettings = new HashMap<>();

	public DefaultSettingsConfig(Path propertiesPath) {
		this.propertiesPath = propertiesPath;
	}

	public String getCapeId() {
		return capeId;
	}

	public boolean areHatsEnabled() {
		return this.enableHats;
	}

	public boolean areShoulderBuddiesEnabled() {
		return this.enableShoulderBuddies;
	}

	public boolean areBackBlingsEnabled() {
		return this.enableBackBlings;
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
		enableHats = Boolean.parseBoolean(properties.getProperty("enable-hats", "true"));
		enableShoulderBuddies = Boolean.parseBoolean(properties.getProperty("enable-shoulder-buddies", "true"));
		enableBackBlings = Boolean.parseBoolean(properties.getProperty("enable-back-blings", "true"));

		capeServerSettings.clear();

		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith("cape-setting-")) {
				String service = propertyName.substring(13);
				CapeDisplay value = CapeDisplay.valueOf(properties.getProperty(propertyName).toUpperCase(Locale.ROOT));
				if (value != null) capeServerSettings.put(service, value);
			}
		}
	}

	public Map<String, CapeDisplay> getCapeServerSettings() {
		return this.capeServerSettings;
	}

	public void save() throws IOException {
		File parentDir = propertiesPath.getParent().toFile();
		if (!parentDir.exists()) parentDir.mkdir();

		Properties properties = new Properties();
		properties.setProperty("starter-cape-id", capeId);
		properties.setProperty("enable-hats", Boolean.toString(enableHats));
		properties.setProperty("enable-shoulder-buddies", Boolean.toString(enableShoulderBuddies));
		properties.setProperty("enable-back-blings", Boolean.toString(enableBackBlings));

		capeServerSettings.forEach((capeServerSetting, display) -> properties.setProperty("cape-setting-" + capeServerSetting, display.name().toLowerCase(Locale.ROOT)));
		properties.store(Files.newOutputStream(propertiesPath), "Cosmetica Default Settings Config");
	}
}
