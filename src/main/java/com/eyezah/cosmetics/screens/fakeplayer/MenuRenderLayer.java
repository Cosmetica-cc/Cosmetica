package com.eyezah.cosmetics.screens.fakeplayer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * For rendering a {@link net.minecraft.client.renderer.entity.layers.RenderLayer} on a menu.
 */
public interface MenuRenderLayer {
	void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot);
}
