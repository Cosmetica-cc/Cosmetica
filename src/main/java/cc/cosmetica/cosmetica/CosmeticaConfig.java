package cc.cosmetica.cosmetica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class CosmeticaConfig {
    private final Path propertiesPath;
    private boolean showNametagInThirdPerson = true;
    private boolean showNonVitalUpdateMessages = true;
    private boolean showWelcomeMessage = true;

    public CosmeticaConfig(Path propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public void initialize() throws IOException {
        load();
        // in case there are new settings
        save();
    }

    public void load() throws IOException {
        if (!Files.exists(propertiesPath)) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("show-nametag-in-third-person", "true"); // default
        properties.setProperty("show-welcome-message", "true"); // default 2: electric boogaloo
        properties.setProperty("honestly-believe-me-i-know-what-the-heck-i-am-doing-trust-me-bro-ask-joe-if-you-dont-believe-me", "false"); // default 3: the return of the king

        properties.load(Files.newInputStream(propertiesPath));
        showNametagInThirdPerson    =  Boolean.parseBoolean(properties.getProperty("show-nametag-in-third-person"));
        showWelcomeMessage          =  Boolean.parseBoolean(properties.getProperty("show-welcome-message"));
        showNonVitalUpdateMessages  = !Boolean.parseBoolean(properties.getProperty("honestly-believe-me-i-know-what-the-heck-i-am-doing-trust-me-bro-ask-joe-if-you-dont-believe-me"));
    }

    public void save() throws IOException {
        File parentDir = propertiesPath.getParent().toFile();
        if (!parentDir.exists()) parentDir.mkdir();

        Properties properties = new Properties();
        properties.setProperty("show-nametag-in-third-person", String.valueOf(showNametagInThirdPerson));
        properties.setProperty("show-welcome-message", String.valueOf(showWelcomeMessage));
        properties.setProperty("honestly-believe-me-i-know-what-the-heck-i-am-doing-trust-me-bro-ask-joe-if-you-dont-believe-me", String.valueOf(!showNonVitalUpdateMessages));
        properties.store(Files.newOutputStream(propertiesPath), "Cosmetica Config");
    }

    public boolean shouldShowNametagInThirdPerson() {
        return showNametagInThirdPerson;
    }

    public void setShowNametagInThirdPerson(boolean showNametagInThirdPerson) {
        this.showNametagInThirdPerson = showNametagInThirdPerson;
    }

    public boolean shouldShowWelcomeMessage() {
        return showWelcomeMessage;
    }

    public boolean shouldShowNonVitalUpdateMessages() {
        return showNonVitalUpdateMessages;
    }
}
