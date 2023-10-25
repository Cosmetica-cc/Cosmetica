/*
 * Copyright 2022, 2023 EyezahMC
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
    private final Option<ArmourConflictHandlingMode> hatConflictMode = new Option<>(
            "hat-conflict-mode",
            ArmourConflictHandlingMode.HIDE_COSMETICS,
            s -> ArmourConflictHandlingMode.valueOf(s.toUpperCase(Locale.ROOT)),
            mode -> mode.toString().toLowerCase(Locale.ROOT)
    );
    private final Option<ArmourConflictHandlingMode> backBlingConflictMode = new Option<>(
            "back-bling-conflict-mode",
            ArmourConflictHandlingMode.HIDE_COSMETICS,
            s -> ArmourConflictHandlingMode.valueOf(s.toUpperCase(Locale.ROOT)),
            mode -> mode.toString().toLowerCase(Locale.ROOT)
    );
    private final Option<ArmourConflictHandlingMode> backBlingElytraConflictMode = new Option<>(
            "back-bling-elytra-conflict-mode",
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

    public ArmourConflictHandlingMode getHatConflictMode() {
        return this.hatConflictMode.getValue();
    }

    public void setHatConflictMode(ArmourConflictHandlingMode mode) {
        this.hatConflictMode.setValue(mode);
    }

    public ArmourConflictHandlingMode getBackBlingConflictMode() {
        return this.backBlingConflictMode.getValue();
    }

    public void setBackBlingConflictMode(ArmourConflictHandlingMode mode) {
        this.backBlingConflictMode.setValue(mode);
    }

    public ArmourConflictHandlingMode getBackBlingElytraConflictMode() {
        return this.backBlingElytraConflictMode.getValue();
    }

    public void setBackBlingElytraConflictMode(ArmourConflictHandlingMode mode) {
        this.backBlingElytraConflictMode.setValue(mode);
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
}
