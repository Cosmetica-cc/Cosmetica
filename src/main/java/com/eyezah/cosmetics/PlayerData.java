package com.eyezah.cosmetics;

public class PlayerData {
	public String lore = "";
	public boolean upsideDown = false;
	public String prefix = "";
	public String suffix = "";
	public String shoulderBuddy = "";
	public PlayerData(String lore, boolean upsideDown, String prefix, String suffix, String shoulderBuddy) {
		this.lore = lore;
		this.upsideDown = upsideDown;
		this.prefix = prefix;
		this.suffix = suffix;
		this.shoulderBuddy = shoulderBuddy;
	}

	public PlayerData() {
		new PlayerData("", false, "", "", "");
	}
}
