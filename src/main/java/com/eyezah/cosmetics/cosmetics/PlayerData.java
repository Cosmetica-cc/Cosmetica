package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class for Player Data
 */
public record PlayerData(String lore, boolean upsideDown, String prefix, String suffix, List<BakableModel> hats, @Nullable BakableModel leftShoulderBuddy, @Nullable BakableModel rightShoulderBuddy, @Nullable BakableModel backBling, String capeName, @Nullable ResourceLocation cape, ResourceLocation skin, boolean slim) {

	public static PlayerData NONE = new PlayerData("", false, "", "", new ArrayList<>(), null, null, null, "", null, DefaultPlayerSkin.getDefaultSkin(), false);
}
