package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Data class for Player Data
 */
public record PlayerData(String lore, boolean upsideDown, String prefix, String suffix, List<BakableModel> hats, BakableModel leftShoulderBuddy, BakableModel rightShoulderBuddy, BakableModel backBling, ResourceLocation cape) {

	public static PlayerData NONE = new PlayerData("", false, "", "", null, null, null, null, null);
}
