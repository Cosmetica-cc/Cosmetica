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

package cc.cosmetica.cosmetica.screens;

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.config.ArmourConflictHandlingMode;
import cc.cosmetica.cosmetica.config.CosmeticaConfig;
import cc.cosmetica.cosmetica.utils.TextComponents;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ArmourConflictModeScreen extends SulphateScreen {
	protected ArmourConflictModeScreen(Screen parent) {
		super(TextComponents.translatable("cosmetica.armourMode"), parent);
	}

	@Override
	protected void addWidgets() {
		this.addArmourConflictToggle("cosmetica.armourMode.hats", CosmeticaConfig::getHatConflictMode, CosmeticaConfig::setHatConflictMode);
		this.addArmourConflictToggle("cosmetica.armourMode.backBlings", CosmeticaConfig::getBackBlingConflictMode, CosmeticaConfig::setBackBlingConflictMode);
		this.addDone();
	}

	private void addArmourConflictToggle(String translationKey,
										 Function<CosmeticaConfig, ArmourConflictHandlingMode> optionGetter,
										 BiConsumer<CosmeticaConfig, ArmourConflictHandlingMode> optionSetter) {
		this.addButton(
				TextComponents.formattedTranslatable(translationKey, TextComponents.translatable(optionGetter.apply(Cosmetica.getConfig()).getLanguageKey())),
				button -> {
					// set new value
					int selected = optionGetter.apply(Cosmetica.getConfig()).ordinal();
					ArmourConflictHandlingMode newMode = ArmourConflictHandlingMode.values()[(selected + 1) % ArmourConflictHandlingMode.values().length];
					optionSetter.accept(Cosmetica.getConfig(), newMode);

					// change button text
					button.setMessage(
							TextComponents.formattedTranslatable(translationKey, TextComponents.translatable(optionGetter.apply(Cosmetica.getConfig()).getLanguageKey()))
					);
				});
	}
}
