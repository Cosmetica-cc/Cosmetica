package com.eyezah.cosmetics.api;

import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.utils.NativeTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BlockModel;

/**
 * Data class for Player Data
 */
public record PlayerData(String lore, boolean upsideDown, String prefix, String suffix, BakableModel hat, BakableModel shoulderBuddy, NativeTexture cape) {

	public static PlayerData NONE = new PlayerData("", false, "", "", null, null, null);
}
