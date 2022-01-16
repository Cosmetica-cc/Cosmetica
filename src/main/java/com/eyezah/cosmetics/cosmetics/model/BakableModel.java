package com.eyezah.cosmetics.cosmetics.model;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BlockModel;

public record BakableModel(String id, BlockModel model, NativeImage image) {
}
