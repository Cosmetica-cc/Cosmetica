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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;

public class CosmeticaConfig {
    public CosmeticaConfig(Path propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    private final Path propertiesPath;
    private final List<Option<?>> options = new ArrayList<>();

    private final Option<Boolean> showNametagInThirdPerson = new Option<>("show-nametag-in-third-person", true, Boolean::parseBoolean);
    private final Option<Boolean> hideNonVitalUpdateMessages = new Option<>("honestly-believe-me-i-know-what-the-heck-i-am-doing-trust-me-bro-ask-joe-if-you-dont-believe-me", false, Boolean::parseBoolean);
    private final Option<WelcomeMessageState> showWelcomeMessage = new Option<>(
            "show-welcome-message",
            WelcomeMessageState.FULL,
            WelcomeMessageState::of,
            s -> s.toString().toLowerCase(Locale.ROOT)
    );
    private final Option<Boolean> addCosmeticaSplashMessage = new Option<>("add-cosmetica-splash-message", true, Boolean::parseBoolean);
    private final Option<Boolean> regionalEffectsPrompt = new Option<>("regional-effects-prompt", true, Boolean::parseBoolean);
    private final Option<Boolean> paranoidHttps = new Option<>("paranoid-https", false, Boolean::parseBoolean);
    private final Option<ArmourConflictHandlingMode> armourConflictHandlingMode = new Option<>(
            "armour-conflict-handling-mode",
            ArmourConflictHandlingMode.HIDE_COSMETICS,
            s -> ArmourConflictHandlingMode.valueOf(s.toUpperCase(Locale.ROOT)),
            mode -> mode.toString().toLowerCase(Locale.ROOT)
    );

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

        // add defaults
        for (Option<?> option : this.options) {
            properties.setProperty(option.getName(), option.getSerialisedValue());
        }

        // load properties
        properties.load(Files.newInputStream(propertiesPath));

        for (Option<?> option : this.options) {
            option.loadValue(properties.getProperty(option.getName()));
        }

        // update paranoid https status on existing API instance, if one exists
        if (Cosmetica.api != null) {
            Cosmetica.api.setForceHttps(paranoidHttps.getValue());
        }
    }

    public void save() throws IOException {
        File parentDir = propertiesPath.getParent().toFile();
        if (!parentDir.exists()) parentDir.mkdir();

        Properties properties = new Properties();

        for (Option<?> option : options) {
            properties.setProperty(option.getName(), option.getSerialisedValue());
        }

        properties.store(Files.newOutputStream(propertiesPath), "Cosmetica Config");
    }

    public boolean shouldShowNametagInThirdPerson() {
        return showNametagInThirdPerson.getValue();
    }

    public void setShowNametagInThirdPerson(boolean showNametagInThirdPerson) {
        this.showNametagInThirdPerson.setValue(showNametagInThirdPerson);
    }

    public WelcomeMessageState showWelcomeMessage() {
        return showWelcomeMessage.getValue();
    }

    public boolean shouldShowNonVitalUpdateMessages() {
        return !hideNonVitalUpdateMessages.getValue();
    }

    public boolean shouldAddCosmeticaSplashMessage() {
        return addCosmeticaSplashMessage.getValue();
    }

    public boolean regionalEffectsPrompt() {
        return regionalEffectsPrompt.getValue();
    }

    public boolean paranoidHttps() {
        return paranoidHttps.getValue();
    }

    public ArmourConflictHandlingMode getArmourConflictHandlingMode() {
        return this.armourConflictHandlingMode.getValue();
    }

    public void setArmourConflictHandlingMode(ArmourConflictHandlingMode mode) {
        this.armourConflictHandlingMode.setValue(mode);
    }

    private class Option<T> {
        Option(String name, T defaultValue, Function<String, T> deserialiser) {
            this(name, defaultValue, deserialiser, String::valueOf);
        }

        Option(String name, T defaultValue, Function<String, T> deserialiser, Function<T, String> serialiser) {
            this.name = name;
            this.value = defaultValue;
            this.deserialiser = deserialiser;
            this.serialiser = serialiser;
            CosmeticaConfig.this.options.add(this);
        }

        private final String name;
        private final Function<String, T> deserialiser;
        private final Function<T, String> serialiser;
        private T value;

        public String getName() {
            return this.name;
        }

        public T getValue() {
            return this.value;
        }

        public String getSerialisedValue() {
            return this.serialiser.apply(this.value);
        }

        public void loadValue(String serialisedForm) {
            this.value = this.deserialiser.apply(serialisedForm);
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    /**
     * The set of modes for handling server-declared conflicts between cosmetics and armour.
     * @apiNote This will almost certainly be replaced by a better solution we have in the works for Cosmetica 2.0.
     * In the meantime, this is easier to implement and should satisfy everyone.
     */
    public enum ArmourConflictHandlingMode {
        /**
         * The default solution, and the solution used prior to this version.
         */
        HIDE_COSMETICS,
        /**
         * Hide the armour instead of the cosmetics.
         */
        HIDE_ARMOUR,
        /**
         * Show both the cosmetic and armour, as if the flag invoking this behaviour was never set.
         */
        SHOW_BOTH
    }

    public enum WelcomeMessageState {
        FULL,
        SCREEN_ONLY,
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

        /**
         * Get whether the welcome tutorial should be shown.
         * @param welcomeScreenAllowed whether the welcome screen should be allowed. Generally {@link VersionInfo#megaInvasiveTutorial()} {@code && newPlayer}
         * @return whether the welcome tutorial should be shown.
         */
        public boolean shouldShowWelcomeTutorial(boolean welcomeScreenAllowed) {
            return welcomeScreenAllowed && (this == FULL || this == SCREEN_ONLY);
        }

        public static WelcomeMessageState of(String string) {
            switch (string.toUpperCase(Locale.ROOT)) {
            case "FULL":
            case "TRUE":
            case "ON":
            default:
                return FULL;
            case "SCREEN_ONLY":
            case "SCREENONLY":
            case "SCREEN ONLY":
            case "SCREEN-ONLY":
                return SCREEN_ONLY;
            case "CHAT_ONLY":
            case "CHATONLY":
            case "CHAT ONLY":
            case "CHAT-ONLY":
                return CHAT_ONLY;
            case "OFF":
            case "FALSE":
                return OFF;
            }
        }
    }
}
