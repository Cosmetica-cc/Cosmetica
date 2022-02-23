package com.eyezah.cosmetics.api;

import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BlockModel;

/**
 * Data class for Player Data
 */
public record PlayerData(String lore, boolean upsideDown, String prefix, String suffix, BakableModel hat, BakableModel shoulderBuddy) {
	public PlayerData() {
		this("", false, "", "", null, null);
	}

	public static PlayerData NONE = new PlayerData();
}
