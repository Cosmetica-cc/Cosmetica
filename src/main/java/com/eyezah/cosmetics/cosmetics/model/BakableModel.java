package com.eyezah.cosmetics.cosmetics.model;

import cc.cosmetica.api.Box;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BlockModel;

public record BakableModel(String id, String name, BlockModel model, NativeImage image, int extraInfo, Box bounds) {
}
