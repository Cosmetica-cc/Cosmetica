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

package cc.cosmetica.cosmetica.screens;

import cc.cosmetica.api.UserSettings;

import java.util.Map;

/**
 * Options handled by the server, modifiable on the client.
 */
class ServerOptions {
	private ServerOptions(boolean shoulderBuddies, boolean hats, boolean doBackBlings, boolean regionSpecificEffects, boolean lore) {
		this.shoulderBuddies = new Option("doshoulderbuddies", shoulderBuddies);
		this.hats = new Option("dohats", hats);
		this.backBlings = new Option("dobackblings", doBackBlings);
		this.regionSpecificEffects = new Option("doregioneffects", regionSpecificEffects);
		this.lore = new Option("dolore", lore);
	}

	ServerOptions(ServerOptions other) {
		this.shoulderBuddies = other.shoulderBuddies.clone();
		this.hats = other.hats.clone();
		this.backBlings = other.backBlings.clone();
		this.regionSpecificEffects = other.regionSpecificEffects.clone();
		this.lore = other.lore.clone();
	}

	ServerOptions(UserSettings settings) {
		this(settings.doShoulderBuddies(), settings.doHats(), settings.doBackBlings(), settings.hasPerRegionEffects(), settings.doLore());
	}

	final Option shoulderBuddies;
	final Option hats;
	final Option backBlings;
	final Option regionSpecificEffects;
	final Option lore;
}

class Option implements Cloneable {
	Option(String urlKey, boolean defaultValue) {
		this.urlKey = urlKey;
		this.value = defaultValue;
	}

	private boolean value;
	final String urlKey;

	boolean get() {
		return this.value;
	}

	void toggle() {
		this.value = !this.value;
	}

	boolean appendToIfChanged(Option old, Map<String, Object> diff) {
		if (old.value != this.value) {
			diff.put(this.urlKey, this.value);
			return true;
		}

		return false;
	}

	@Override
	protected Option clone() {
		return new Option(this.urlKey, this.value);
	}
}
