package com.eyezah.cosmetics;

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

	private final Map<String, CapeDisplay> capeServerSettings = new HashMap<>();

	public DefaultSettingsConfig(Path propertiesPath) {
		this.propertiesPath = propertiesPath;
	}

	public String getCapeId() {
		return capeId;
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
		properties.store(Files.newOutputStream(propertiesPath), "Cosmetica Default Settings Config");
	}
}
