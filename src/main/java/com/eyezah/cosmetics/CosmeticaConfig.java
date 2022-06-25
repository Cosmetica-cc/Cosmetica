package com.eyezah.cosmetics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class CosmeticaConfig {
    private final Path propertiesPath;
    private boolean showNametagInThirdPerson = true;
    private boolean inlineChangeButton = true;

    public CosmeticaConfig(Path propertiesPath) {
        this.propertiesPath = propertiesPath;
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
        properties.setProperty("inline-change-button", "true"); // default

        properties.load(Files.newInputStream(propertiesPath));
        showNametagInThirdPerson = Boolean.parseBoolean(properties.getProperty("show-nametag-in-third-person"));
        inlineChangeButton = Boolean.parseBoolean(properties.getProperty("show-nametag-in-third-person"));
    }

    public void save() throws IOException {
        File parentDir = propertiesPath.getParent().toFile();
        if (!parentDir.exists()) parentDir.mkdir();

        Properties properties = new Properties();
        properties.setProperty("show-nametag-in-third-person", String.valueOf(showNametagInThirdPerson));
        properties.setProperty("inline-change-button", String.valueOf(inlineChangeButton));
        properties.store(Files.newOutputStream(propertiesPath), "Cosmetica Config");
    }

    public boolean shouldShowNametagInThirdPerson() {
        return showNametagInThirdPerson;
    }

    public void setShowNametagInThirdPerson(boolean showNametagInThirdPerson) {
        this.showNametagInThirdPerson = showNametagInThirdPerson;
    }

    public boolean shouldInlineChangeButton() {
        return inlineChangeButton;
    }

    public void setInlineChangeButton(boolean showNametagInThirdPerson) {
        this.inlineChangeButton = showNametagInThirdPerson;
    }
}
