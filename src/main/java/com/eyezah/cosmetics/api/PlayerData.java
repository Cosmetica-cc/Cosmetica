package com.eyezah.cosmetics.api;

/**
 * Data class for Player Data
 */
public record PlayerData(String lore, boolean upsideDown, String prefix, String suffix, String shoulderBuddy) {
	public PlayerData() {
		this("", false, "", "", "");
	}
}
