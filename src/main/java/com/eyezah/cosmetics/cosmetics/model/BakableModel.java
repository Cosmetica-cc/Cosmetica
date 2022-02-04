package com.eyezah.cosmetics.cosmetics.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.jetbrains.annotations.Nullable;

public record BakableModel(String id, BlockModel model, NativeImage image, int extraInfo) {
}
