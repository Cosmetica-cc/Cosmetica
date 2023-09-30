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
public class ServerOptions {
	private ServerOptions(boolean shoulderBuddies, boolean hats, boolean doBackBlings, boolean regionSpecificEffects, boolean lore, boolean onlineActivity, int iconSettings) {
		this.shoulderBuddies = new SimpleOption("doshoulderbuddies", shoulderBuddies);
		this.hats = new SimpleOption("dohats", hats);
		this.backBlings = new SimpleOption("dobackblings", doBackBlings);
		this.regionSpecificEffects = new SimpleOption("doregioneffects", regionSpecificEffects);
		this.lore = new SimpleOption("dolore", lore);
		this.onlineActivity = new SimpleOption("doonlineactivity", onlineActivity);
		this.icons = new MultiOption("iconsettings", iconSettings);
	}

	ServerOptions(ServerOptions other) {
		this.shoulderBuddies = other.shoulderBuddies.clone();
		this.hats = other.hats.clone();
		this.backBlings = other.backBlings.clone();
		this.regionSpecificEffects = other.regionSpecificEffects.clone();
		this.lore = other.lore.clone();
		this.onlineActivity = other.onlineActivity.clone();
		this.icons = other.icons.clone();
	}

	public ServerOptions(UserSettings settings) {
		this(settings.doShoulderBuddies(), settings.doHats(), settings.doBackBlings(), settings.hasPerRegionEffects(), settings.doLore(), settings.doOnlineActivity(), settings.getIconSettings());
	}

	final SimpleOption shoulderBuddies;
	final SimpleOption hats;
	final SimpleOption backBlings;
	final SimpleOption regionSpecificEffects;
	final SimpleOption lore;
	final SimpleOption onlineActivity;
	// icons
	final MultiOption icons;
}

class MultiOption extends Option<MultiOption> {
	MultiOption(String urlKey, int defaultValue) {
		super(urlKey);
		this.value = defaultValue;
	}

	private int value;

	public boolean get(int flag) {
		return (this.value & flag) == flag;
	}

	public void toggle(int flag) {
		if (this.get(flag)) {
			this.value &= ~flag;
		}
		else {
			this.value |= flag;
		}
	}

	@Override
	boolean appendToIfChanged(MultiOption old, Map<String, Object> diff) {
		if (old.value != this.value) {
			diff.put(this.urlKey, this.value);
			return true;
		}

		return false;
	}

	@Override
	protected MultiOption clone() {
		return new MultiOption(this.urlKey, this.value);
	}
}

class SimpleOption extends Option<SimpleOption> {
	SimpleOption(String urlKey, boolean defaultValue) {
		super(urlKey);
		this.value = defaultValue;
	}

	private boolean value;

	boolean get() {
		return this.value;
	}

	void toggle() {
		this.value = !this.value;
	}

	boolean appendToIfChanged(SimpleOption old, Map<String, Object> diff) {
		if (old.value != this.value) {
			diff.put(this.urlKey, this.value);
			return true;
		}

		return false;
	}

	@Override
	protected SimpleOption clone() {
		return new SimpleOption(this.urlKey, this.value);
	}
}

abstract class Option<T extends Option> implements Cloneable {
	Option(String urlKey) {
		this.urlKey = urlKey;
	}

	final String urlKey;

	abstract boolean appendToIfChanged(T old, Map<String, Object> diff);
}
