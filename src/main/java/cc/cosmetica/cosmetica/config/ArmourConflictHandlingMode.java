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

/**
 * The set of modes for handling server-declared conflicts between cosmetics and armour. This includes cape/elytra.
 *
 * @apiNote This will almost certainly be replaced by a better solution we have in the works for Cosmetica 2.0.
 * In the meantime, this is easier to implement and should satisfy everyone.
 */
public enum ArmourConflictHandlingMode {
	/**
	 * The default solution, and the solution used prior to this version.
	 */
	HIDE_COSMETICS("cosmetica.armourMode.hideCosmetics"),
	/**
	 * Hide the armour instead of the cosmetics.
	 */
	HIDE_ARMOUR("cosmetica.armourMode.hideArmour"),
	/**
	 * Show both the cosmetic and armour, as if the flag invoking this behaviour were never set.
	 */
	SHOW_BOTH("cosmetica.armourMode.showBoth");

	ArmourConflictHandlingMode(String languageKey) {
		this.languageKey = languageKey;
	}

	private final String languageKey;

	public String getLanguageKey() {
		return this.languageKey;
	}
}
