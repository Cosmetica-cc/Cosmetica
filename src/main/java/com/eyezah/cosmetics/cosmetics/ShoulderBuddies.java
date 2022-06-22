package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.cosmetics.model.OverriddenModel;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.screens.fakeplayer.MenuRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class ShoulderBuddies<T extends Player> extends CustomLayer<T, PlayerModel<T>> implements MenuRenderLayer {
	private ModelManager modelManager;
	private EntityModelSet entityModelSet;

	public ShoulderBuddies(RenderLayerParent<T, PlayerModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.modelManager = Minecraft.getInstance().getModelManager();
		this.entityModelSet = entityModelSet;
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, T player, float f, float g, float pitch, float j, float k, float l) {
		if (player.isInvisible()) return;
		BakableModel left = overridden.get(() -> Cosmetica.getPlayerData(player).leftShoulderBuddy());
		BakableModel right = overridden.get(() -> Cosmetica.getPlayerData(player).rightShoulderBuddy());

		if (left != null && player.getShoulderEntityLeft().isEmpty()) render(left, stack, multiBufferSource, packedLight, player, true);
		if (right != null && player.getShoulderEntityLeft().isEmpty()) render(right, stack, multiBufferSource, packedLight, player, false);
	}

	public void render(BakableModel modelData, PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, boolean left) {
		stack.pushPose();

		if (modelData.id().equals("-sheep")) { // builtin live sheep
			LiveSheepModel model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
			stack.pushPose();
			stack.scale(0.8f, 0.8f, 0.8f);
			stack.translate(left ? 0.42 : -0.42, (player.isCrouching() ? -1.3 : -1.6D) + 1.07D, 0.0D);
			stack.scale(0.35f, 0.35f, 0.35f);

			// calculate colour like a jeb sheep
			final int rate = 25;

			int n = player.tickCount / rate + player.getId();
			int o = DyeColor.values().length;
			int p = n % o;
			int q = (n + 1) % o;
			float r = ((float)(player.tickCount % rate) + 0) / (float) rate;
			float[] fs = Sheep.getColorArray(DyeColor.byId(p));
			float[] gs = Sheep.getColorArray(DyeColor.byId(q));

			float red = fs[0] * (1.0F - r) + gs[0] * r;
			float green = fs[1] * (1.0F - r) + gs[1] * r;
			float blue = fs[2] * (1.0F - r) + gs[2] * r;

			// render
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep_fur.png")));
			model.root.render(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0f);

			stack.popPose();

			model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP));

			stack.pushPose();

			stack.scale(0.8f, 0.8f, 0.8f);
			stack.translate(left ? 0.42 : -0.42, (player.isCrouching() ? -1.3 : -1.6D) + 1.07D, 0.0D);

			vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep.png")));
			model.renderOnShoulder(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY);

			stack.popPose();
		} else {
			boolean staticPosition = (modelData.extraInfo() & 0x1) == 1;

			if (staticPosition) {
				stack.translate(left ? 0.35 : -0.35, -0.2, 0);
				doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLightProbably, 0, 0f, 0);
			} else {
				ModelPart modelPart = left ? this.getParentModel().leftArm : this.getParentModel().rightArm;
				doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0.37f, 0);
			}
		}

		stack.popPose();
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
		BakableModel left = overridden.get(() -> player.getData().leftShoulderBuddy());
		BakableModel right = overridden.get(() -> player.getData().rightShoulderBuddy());

		if (left != null) render(left, stack, bufferSource, packedLight, player, true);
		if (right != null) render(right, stack, bufferSource, packedLight, player, false);
	}

	public void render(BakableModel modelData, PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, FakePlayer player, boolean left) {
		stack.pushPose();

		if (modelData.id().equals("-sheep")) { // builtin live sheep
			LiveSheepModel model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
			stack.pushPose();
			stack.scale(0.8f, 0.8f, 0.8f);
			stack.translate(left ? 0.42 : -0.42, (player.isCrouching() ? -1.3 : -1.6D) + 1.07D, 0.0D);
			stack.scale(0.35f, 0.35f, 0.35f);

			// calculate colour like a jeb sheep
			final int rate = 25;

			int n = player.tickCount / rate + (int) player.getUUID().getMostSignificantBits();
			int o = DyeColor.values().length;
			int p = n % o;
			int q = (n + 1) % o;
			float r = ((float)(player.tickCount % rate) + 0) / (float) rate;
			float[] fs = Sheep.getColorArray(DyeColor.byId(p));
			float[] gs = Sheep.getColorArray(DyeColor.byId(q));

			float red = fs[0] * (1.0F - r) + gs[0] * r;
			float green = fs[1] * (1.0F - r) + gs[1] * r;
			float blue = fs[2] * (1.0F - r) + gs[2] * r;

			// render
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep_fur.png")));
			model.root.render(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0f);

			stack.popPose();

			model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP));

			stack.pushPose();

			stack.scale(0.8f, 0.8f, 0.8f);
			stack.translate(left ? 0.42 : -0.42, (player.isCrouching() ? -1.3 : -1.6D) + 1.07D, 0.0D);

			vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep.png")));
			model.renderOnShoulder(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY);

			stack.popPose();
		} else {
			boolean staticPosition = (modelData.extraInfo() & 0x1) == 1;

			if (staticPosition) {
				stack.translate(left ? 0.35 : -0.35, -0.2, 0);
				doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLightProbably, 0, 0f, 0);
			} else {
				ModelPart modelPart = left ? this.getParentModel().leftArm : this.getParentModel().rightArm;
				doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0.37f, 0);
			}
		}

		stack.popPose();
	}

	public static final OverriddenModel overridden = new OverriddenModel();
}
