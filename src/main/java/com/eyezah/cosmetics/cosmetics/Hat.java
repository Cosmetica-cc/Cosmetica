package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.Models;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.player.Player;

public class Hat<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
	public Hat(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
		super(renderLayerParent);
		this.modelManager = Minecraft.getInstance().getModelManager();
	}

	private ModelManager modelManager;

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T entity, float f, float g, float pitch, float j, float k, float l) {
		stack.pushPose();
		boolean bl = false;
		float o = 1.1875F;

		this.getParentModel().getHead().translateAndRotate(stack);
		stack.scale(o, -o, -o);

		GameProfile gameProfile = null;

		stack.translate(-0.5D, 0.25D, -0.5D); // vanilla: 0.0 second param
		Models.renderModel(this.modelManager.getModel(new ModelResourceLocation("minecraft:spyglass#inventory")), stack, multiBufferSource, packedLightProbably);

		stack.popPose();
	}
}
