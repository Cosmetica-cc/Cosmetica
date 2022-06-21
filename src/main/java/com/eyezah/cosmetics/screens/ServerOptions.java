package com.eyezah.cosmetics.screens;

import java.util.Map;

/**
 * Options handled by the server, modifiable on the client.
 */
class ServerOptions {
	ServerOptions(boolean shoulderBuddies, boolean hats, boolean regionSpecificEffects, boolean doBackBlings, boolean lore) {
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
