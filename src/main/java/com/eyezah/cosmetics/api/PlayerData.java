package com.eyezah.cosmetics.api;

import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Data class for Player Data
 */
public record PlayerData(String lore, boolean upsideDown, String prefix, String suffix, BakableModel hat, BakableModel shoulderBuddy, ResourceLocation cape) {

	public static PlayerData NONE = new PlayerData("", false, "", "", null, null, null);
}
