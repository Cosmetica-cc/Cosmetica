package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Turtle;

public class ShoulderBuddyModelTurtle<T extends Turtle> extends QuadrupedModel<T> {
	private static final String EGG_BELLY = "egg_belly";
	private final ModelPart eggBelly;
	private ModelPart root;

	public ShoulderBuddyModelTurtle(ModelPart modelPart) {
		super(modelPart, true, 120.0F, 0.0F, 9.0F, 6.0F, 120);
		this.root = modelPart;
		this.eggBelly = modelPart.getChild("egg_belly");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(3, 0).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F), PartPose.offset(0.0F, 19.0F, -10.0F));
		partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(7, 37).addBox("shell", -9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F).texOffs(31, 1).addBox("belly", -5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 11.0F, -10.0F, 1.5707964F, 0.0F, 0.0F));
		partDefinition.addOrReplaceChild("egg_belly", CubeListBuilder.create().texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 11.0F, -10.0F, 1.5707964F, 0.0F, 0.0F));
		boolean i = true;
		partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(1, 23).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), PartPose.offset(-3.5F, 22.0F, 11.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(1, 12).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), PartPose.offset(3.5F, 22.0F, 11.0F));
		partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(27, 30).addBox(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), PartPose.offset(-5.0F, 21.0F, -4.0F));
		partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(27, 24).addBox(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), PartPose.offset(5.0F, 21.0F, -4.0F));
		return LayerDefinition.create(meshDefinition, 128, 64);
	}

	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(this.eggBelly));
	}

	public void setupAnim(T turtle, float f, float g, float h, float i, float j) {
		super.setupAnim(turtle, f, g, h, i, j);
		this.rightHindLeg.xRot = Mth.cos(f * 0.6662F * 0.6F) * 0.5F * g;
		this.leftHindLeg.xRot = Mth.cos(f * 0.6662F * 0.6F + 3.1415927F) * 0.5F * g;
		this.rightFrontLeg.zRot = Mth.cos(f * 0.6662F * 0.6F + 3.1415927F) * 0.5F * g;
		this.leftFrontLeg.zRot = Mth.cos(f * 0.6662F * 0.6F) * 0.5F * g;
		this.rightFrontLeg.xRot = 0.0F;
		this.leftFrontLeg.xRot = 0.0F;
		this.rightFrontLeg.yRot = 0.0F;
		this.leftFrontLeg.yRot = 0.0F;
		this.rightHindLeg.yRot = 0.0F;
		this.leftHindLeg.yRot = 0.0F;
		if (!turtle.isInWater() && turtle.isOnGround()) {
			float k = 1.0F;
			float l = 1.0F;
			float m = 5.0F;
			this.rightFrontLeg.yRot = Mth.cos(k * f * 5.0F + 3.1415927F) * 8.0F * g * l;
			this.rightFrontLeg.zRot = 0.0F;
			this.leftFrontLeg.yRot = Mth.cos(k * f * 5.0F) * 8.0F * g * l;
			this.leftFrontLeg.zRot = 0.0F;
			this.rightHindLeg.yRot = Mth.cos(f * 5.0F + 3.1415927F) * 3.0F * g;
			this.rightHindLeg.xRot = 0.0F;
			this.leftHindLeg.yRot = Mth.cos(f * 5.0F) * 3.0F * g;
			this.leftHindLeg.xRot = 0.0F;
		}

		this.eggBelly.visible = false;
	}

	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		boolean bl = this.eggBelly.visible;
		if (bl) {
			poseStack.pushPose();
			poseStack.translate(0.0D, -0.07999999821186066D, 0.0D);
		}

		super.renderToBuffer(poseStack, vertexConsumer, i, j, f, g, h, k);
		if (bl) {
			poseStack.popPose();
		}

	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		poseStack.scale(0.2F, 0.2F, 0.2F);
		this.root.render(poseStack, vertexConsumer, i, j);
	}
}
