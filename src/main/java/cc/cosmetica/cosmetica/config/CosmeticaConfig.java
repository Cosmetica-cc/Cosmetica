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

import cc.cosmetica.api.VersionInfo;
import cc.cosmetica.cosmetica.Cosmetica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public class CosmeticaConfig {
    private final Path propertiesPath;
    private boolean showNametagInThirdPerson = true;
    private boolean showNonVitalUpdateMessages = true;
    private WelcomeMessageState showWelcomeMessage = WelcomeMessageState.FULL;
    private boolean addCosmeticaSplashMessage = true;
    private boolean regionalEffectsPrompt = true;
    private boolean paranoidHttps = false;

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
        properties.setProperty("show-welcome-message", "full"); // default 2: electric boogaloo
        properties.setProperty("honestly-believe-me-i-know-what-the-heck-i-am-doing-trust-me-bro-ask-joe-if-you-dont-believe-me", "false"); // default 3: the return of the king
        properties.setProperty("add-cosmetica-splash-message", "true");
        properties.setProperty("regional-effects-prompt", "true");
        properties.setProperty("paranoid-https", "false");

        properties.load(Files.newInputStream(propertiesPath));
        showNametagInThirdPerson    =  Boolean.parseBoolean(properties.getProperty("show-nametag-in-third-person"));
        showWelcomeMessage          =  WelcomeMessageState.of(properties.getProperty("show-welcome-message"));
        showNonVitalUpdateMessages  = !Boolean.parseBoolean(properties.getProperty("honestly-believe-me-i-know-what-the-heck-i-am-doing-trust-me-bro-ask-joe-if-you-dont-believe-me"));
        addCosmeticaSplashMessage   =  Boolean.parseBoolean(properties.getProperty("add-cosmetica-splash-message"));
        regionalEffectsPrompt       =  Boolean.parseBoolean(properties.getProperty("regional-effects-prompt"));
        paranoidHttps               =  Boolean.parseBoolean(properties.getProperty("paranoid-https"));

        // update paranoid https status on existing API instance
        Cosmetica.api.setForceHttps(paranoidHttps);
    }

    public void save() throws IOException {
        File parentDir = propertiesPath.getParent().toFile();
        if (!parentDir.exists()) parentDir.mkdir();

        Properties properties = new Properties();
        properties.setProperty("show-nametag-in-third-person", String.valueOf(showNametagInThirdPerson));
        properties.setProperty("show-welcome-message", String.valueOf(showWelcomeMessage).toLowerCase(Locale.ROOT));
        properties.setProperty("honestly-believe-me-i-know-what-the-heck-i-am-doing-trust-me-bro-ask-joe-if-you-dont-believe-me", String.valueOf(!showNonVitalUpdateMessages));
        properties.setProperty("add-cosmetica-splash-message", String.valueOf(addCosmeticaSplashMessage));
        properties.setProperty("regional-effects-prompt", String.valueOf(regionalEffectsPrompt));
        properties.setProperty("paranoid-https", String.valueOf(paranoidHttps));
        properties.store(Files.newOutputStream(propertiesPath), "Cosmetica Config");
    }

    public boolean shouldShowNametagInThirdPerson() {
        return showNametagInThirdPerson;
    }

    public void setShowNametagInThirdPerson(boolean showNametagInThirdPerson) {
        this.showNametagInThirdPerson = showNametagInThirdPerson;
    }

    public WelcomeMessageState shouldShowWelcomeMessage() {
        return showWelcomeMessage;
    }

    public boolean shouldShowNonVitalUpdateMessages() {
        return showNonVitalUpdateMessages;
    }

    public boolean shouldAddCosmeticaSplashMessage() {
        return addCosmeticaSplashMessage;
    }

    public boolean regionalEffectsPrompt() {
        return regionalEffectsPrompt;
    }

    public boolean paranoidHttps() {
        return paranoidHttps;
    }

    public enum WelcomeMessageState {
        FULL,
        CHAT_ONLY,
        OFF;

        /**
         * Get whether a chat message for welcome should be shown.
         * @param welcomeScreenAllowed whether the welcome screen should be allowed. Generally {@link VersionInfo#megaInvasiveTutorial()} {@code && newPlayer}
         * @return whether the welcome chat message should be shown.
         */
        public boolean shouldShowChatMessage(boolean welcomeScreenAllowed) {
            return this == CHAT_ONLY || (this == FULL && !welcomeScreenAllowed);
        }

        public static WelcomeMessageState of(String string) {
            switch (string.toUpperCase(Locale.ROOT)) {
            case "FULL":
            case "TRUE":
            case "ON":
            default:
                return FULL;
            case "CHAT_ONLY":
            case "CHATONLY":
            case "CHAT-ONLY":
                return CHAT_ONLY;
            case "OFF":
            case "FALSE":
                return OFF;
            }
        }
    }
}
