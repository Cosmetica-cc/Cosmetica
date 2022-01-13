package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.Models;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Hat<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
	public Hat(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
		super(renderLayerParent);
		this.modelManager = Minecraft.getInstance().getModelManager();
	}

	private ModelManager modelManager;

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T entity, float f, float g, float pitch, float j, float k, float l) {
		stack.pushPose();
		float o = 1.001f; // 0.5 has z fighting

		this.getParentModel().getHead().translateAndRotate(stack);
		stack.scale(o, -o, -o);
		stack.mulPose(new Quaternion(0, 0.2f, 0, false));

		stack.translate(0, 1.22/2, 0); // vanilla: 0.0 second param

		BakedModel model = this.modelManager.getModel(new ModelResourceLocation("minecraft:stonecutter#inventory"));
		//if (is a world size model?) o = 0.5001f; // 0.5 has z fighting

		Models.renderModel(
				ensureNonNull(this.modelManager, model),
				stack,
				multiBufferSource,
				packedLightProbably);

		stack.popPose();
	}

	/**
	 * @return the missing model if no model. The most pointless separate method in all of history.
	 */
	@Nonnull
	private BakedModel ensureNonNull(ModelManager modelManager, @Nullable BakedModel model) {
		return model == null ? modelManager.getMissingModel() : model;
	}
}
