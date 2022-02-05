package com.eyezah.cosmetics.screens;

/**
 * Options handled by the server, modifiable on the client.
 */
class ServerOptions {
	ServerOptions(boolean shoulderBuddies, boolean hats, boolean regionSpecificEffects) {
		this.shoulderBuddies = new Option("doshoulderbuddies", shoulderBuddies);
		this.hats = new Option("dohats", hats);
		this.regionSpecificEffects = new Option("doregioneffects", regionSpecificEffects);
	}

	ServerOptions(ServerOptions other) {
		this.shoulderBuddies = other.shoulderBuddies.clone();
		this.hats = other.hats.clone();
		this.regionSpecificEffects = other.regionSpecificEffects.clone();
	}

	final Option shoulderBuddies;
	final Option hats;
	final Option regionSpecificEffects;
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

	void appendToIfChanged(Option old, StringBuilder sb) {
		if (old.value != this.value) {
			sb.append('&').append(this.urlKey).append('=').append(this.value);
		}
	}

	@Override
	protected Option clone() {
		return new Option(this.urlKey, this.value);
	}
}
