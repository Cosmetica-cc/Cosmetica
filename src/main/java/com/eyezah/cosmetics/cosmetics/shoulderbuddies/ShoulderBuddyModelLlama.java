package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

public class ShoulderBuddyModelLlama<T extends AbstractChestedHorse> extends EntityModel<T> {
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightChest;
	private final ModelPart leftChest;
	private ModelPart root;

	public ShoulderBuddyModelLlama(ModelPart modelPart) {
		this.root = modelPart;
		this.head = modelPart.getChild("head");
		this.body = modelPart.getChild("body");
		this.rightChest = modelPart.getChild("right_chest");
		this.leftChest = modelPart.getChild("left_chest");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, cubeDeformation).texOffs(0, 14).addBox("neck", -4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, cubeDeformation).texOffs(17, 0).addBox("ear", -4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, cubeDeformation).texOffs(17, 0).addBox("ear", 1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, cubeDeformation), PartPose.offset(0.0F, 7.0F, -6.0F));
		partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, cubeDeformation), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
		partDefinition.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, cubeDeformation), PartPose.offsetAndRotation(-8.5F, 3.0F, 3.0F, 0.0F, 1.5707964F, 0.0F));
		partDefinition.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, cubeDeformation), PartPose.offsetAndRotation(5.5F, 3.0F, 3.0F, 0.0F, 1.5707964F, 0.0F));
		boolean i = true;
		boolean j = true;
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.5F, 10.0F, 6.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.5F, 10.0F, 6.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.5F, 10.0F, -5.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.5F, 10.0F, -5.0F));
		return LayerDefinition.create(meshDefinition, 128, 64);
	}

	public void setupAnim(T abstractChestedHorse, float f, float g, float h, float i, float j) {
		this.head.xRot = j * 0.017453292F;
		this.head.yRot = i * 0.017453292F;
		this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
		this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
		this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		boolean bl = !abstractChestedHorse.isBaby() && abstractChestedHorse.hasChest();
		this.rightChest.visible = bl;
		this.leftChest.visible = bl;
	}

	public void setupAnim(float f, float g, float h, float i, float j) {
		this.head.xRot = j * 0.017453292F;
		this.head.yRot = i * 0.017453292F;
		this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
		this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
		this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		boolean bl = false;
		this.rightChest.visible = bl;
		this.leftChest.visible = bl;
	}

	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		if (this.young) {
			float l = 2.0F;
			poseStack.pushPose();
			float m = 0.7F;
			poseStack.scale(0.71428573F, 0.64935064F, 0.7936508F);
			poseStack.translate(0.0D, 1.3125D, 0.2199999988079071D);
			this.head.render(poseStack, vertexConsumer, i, j, f, g, h, k);
			poseStack.popPose();
			poseStack.pushPose();
			float n = 1.1F;
			poseStack.scale(0.625F, 0.45454544F, 0.45454544F);
			poseStack.translate(0.0D, 2.0625D, 0.0D);
			this.body.render(poseStack, vertexConsumer, i, j, f, g, h, k);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.scale(0.45454544F, 0.41322312F, 0.45454544F);
			poseStack.translate(0.0D, 2.0625D, 0.0D);
			ImmutableList.of(this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((modelPart) -> {
				modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k);
			});
			poseStack.popPose();
		} else {
			ImmutableList.of(this.head, this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((modelPart) -> {
				modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k);
			});
		}
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		this.setupAnim(f, g, 0.0F, h, k);
		poseStack.scale(0.25F, 0.25F, 0.25F);
		this.root.render(poseStack, vertexConsumer, i, j);
	}
}
