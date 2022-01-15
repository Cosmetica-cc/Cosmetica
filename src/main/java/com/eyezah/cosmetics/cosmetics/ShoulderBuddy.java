package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.cosmetics.shoulderbuddies.*;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

import static com.eyezah.cosmetics.Cosmetics.LOGGER;
import static com.eyezah.cosmetics.Cosmetics.getPlayerData;

public class ShoulderBuddy<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
	private EntityModelSet entityModelSet;

	public ShoulderBuddy(RenderLayerParent<T, PlayerModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.entityModelSet = entityModelSet;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T player, float f, float g, float h, float j, float k, float l) {
		this.render(poseStack, multiBufferSource, i, player, f, g, k, l, true);
		this.render(poseStack, multiBufferSource, i, player, f, g, k, l, false);
	}

	private void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T player, float f, float g, float h, float j, boolean bl) {
		String useArm = "";
		if (player.getMainArm() == HumanoidArm.RIGHT) {
			if (player.getShoulderEntityLeft().isEmpty()) {
				useArm = "left";
			} else if (player.getShoulderEntityRight().isEmpty()) {
				useArm = "right";
			}
		} else {
			if (player.getShoulderEntityRight().isEmpty()) {
				useArm = "right";
			} else if (player.getShoulderEntityLeft().isEmpty()) {
				useArm = "left";
			}
		}
		if (useArm.equals("") || (bl && useArm.equals("right")) || (!bl && useArm.equals("left"))) return;

		String buddyType = getPlayerData(player.getUUID(), player.getName().getString()).shoulderBuddy;
		CompoundTag compoundTag = bl ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();

		buddyType = "snail"; // temp force set

		if (buddyType.equals("eyezahparrot")) {
			ShoulderBuddyModelParrot model = new ShoulderBuddyModelParrot(entityModelSet.bakeLayer(ModelLayers.PARROT), "sitting");
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4 : -0.4, (player.isCrouching() ? -1.3 : -1.5), 0.0);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("extravagant_cosmetics", "textures/eyezah_parrot.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("fox")) {
			ShoulderBuddyModelFox model = new ShoulderBuddyModelFox(entityModelSet.bakeLayer(ModelLayers.FOX), "sitting");
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4 : -0.4, (player.isCrouching() ? -1.3 : -1.6) + 1.07, 0.0);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/fox/fox.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("snowfox")) {
			ShoulderBuddyModelFox model = new ShoulderBuddyModelFox(entityModelSet.bakeLayer(ModelLayers.FOX), "sitting");
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4000000059604645D : -0.4000000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/fox/snow_fox.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("nakedsheep")) {
			ShoulderBuddyModelSheep model = new ShoulderBuddyModelSheep(entityModelSet.bakeLayer(ModelLayers.SHEEP));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("sheep")) {
			ShoulderBuddyModelSheep model = new ShoulderBuddyModelSheep(entityModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep_fur.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();

			model = new ShoulderBuddyModelSheep(entityModelSet.bakeLayer(ModelLayers.SHEEP));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("cat")) {
			ShoulderBuddyModelCat model = new ShoulderBuddyModelCat(entityModelSet.bakeLayer(ModelLayers.CAT), "sitting");
			poseStack.pushPose();
			poseStack.translate(bl ? 0.400000059604645D : -0.400000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/cat/black.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("wolf")) {
			ShoulderBuddyModelWolf model = new ShoulderBuddyModelWolf(entityModelSet.bakeLayer(ModelLayers.WOLF), "sitting");
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/wolf/wolf_tame.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("bee")) {
			ShoulderBuddyModelBee model = new ShoulderBuddyModelBee(entityModelSet.bakeLayer(ModelLayers.BEE));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4000000059604645D : -0.4000000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/bee/bee.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("nectarbee")) {
			ShoulderBuddyModelBee model = new ShoulderBuddyModelBee(entityModelSet.bakeLayer(ModelLayers.BEE));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4000000059604645D : -0.4000000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/bee/bee_nectar.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("slime")) {
			ShoulderBuddyModelSlime model = new ShoulderBuddyModelSlime(entityModelSet.bakeLayer(ModelLayers.SLIME));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/slime/slime.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();

			model = new ShoulderBuddyModelSlime(entityModelSet.bakeLayer(ModelLayers.SLIME_OUTER));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/slime/slime.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("axolotl")) {
			ShoulderBuddyModelAxolotl model = new ShoulderBuddyModelAxolotl(entityModelSet.bakeLayer(ModelLayers.AXOLOTL), "playingdead");
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/axolotl/axolotl_wild.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("turtle")) {
			ShoulderBuddyModelTurtle model = new ShoulderBuddyModelTurtle(entityModelSet.bakeLayer(ModelLayers.TURTLE));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.3D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/turtle/big_sea_turtle.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("ghast")) {
			ShoulderBuddyModelGhast model = new ShoulderBuddyModelGhast(entityModelSet.bakeLayer(ModelLayers.GHAST));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.4000000059604645D : -0.4000000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.0D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/ghast/ghast.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("creeper")) {
			ShoulderBuddyModelCreeper model = new ShoulderBuddyModelCreeper(entityModelSet.bakeLayer(ModelLayers.CREEPER));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.23D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/creeper/creeper.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("goat")) {
			ShoulderBuddyModelGoat model = new ShoulderBuddyModelGoat(entityModelSet.bakeLayer(ModelLayers.GOAT));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.23D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/goat/goat.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("llama")) {
			ShoulderBuddyModelLlama model = new ShoulderBuddyModelLlama(entityModelSet.bakeLayer(ModelLayers.LLAMA));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.23D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/llama/creamy.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("rabbit")) {
			ShoulderBuddyModelRabbit model = new ShoulderBuddyModelRabbit(entityModelSet.bakeLayer(ModelLayers.RABBIT));
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.07D, 0.0D);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/rabbit/salt.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		if (buddyType.equals("snail")) {
			ShoulderBuddyModelSnail model = new ShoulderBuddyModelSnail(ShoulderBuddyModelSnail.createBodyLayer().bakeRoot());
			poseStack.pushPose();
			poseStack.translate(bl ? 0.3500000059604645D : -0.3500000059604645D, (player.isCrouching() ? -1.2999999523162842D : -1.6D) + 1.3D, 0.0D);
			//VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("extravagant_cosmetics", "textures/snail.png")));
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/rabbit/salt.png")));
			model.renderOnShoulder(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, f, g, h, j, player.tickCount);
			poseStack.popPose();
			return;
		}

		//LOGGER.debug("not valid shoulder buddy: '" + buddyType + "'");
	}
}
