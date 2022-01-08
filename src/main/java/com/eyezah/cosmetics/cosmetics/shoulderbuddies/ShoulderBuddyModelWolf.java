package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Wolf;

public class ShoulderBuddyModelWolf<T extends Wolf> extends ColorableAgeableListModel<T> {
	private static final String REAL_HEAD = "real_head";
	private static final String UPPER_BODY = "upper_body";
	private static final String REAL_TAIL = "real_tail";
	private final ModelPart head;
	private final ModelPart realHead;
	private final ModelPart body;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart tail;
	private final ModelPart realTail;
	private final ModelPart upperBody;
	private static final int LEG_SIZE = 8;
	private String pose;
	private ModelPart root;

	public ShoulderBuddyModelWolf(ModelPart modelPart, String pose) {
		this.head = modelPart.getChild("head");
		this.realHead = this.head.getChild("real_head");
		this.body = modelPart.getChild("body");
		this.upperBody = modelPart.getChild("upper_body");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
		this.tail = modelPart.getChild("tail");
		this.realTail = this.tail.getChild("real_tail");
		this.pose = pose;
		this.root = modelPart;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 13.5F;
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, 13.5F, -7.0F));
		partDefinition2.addOrReplaceChild("real_head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F).texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(16, 14).addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3.0F, 3.0F, 4.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
		partDefinition.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, 1.5707964F, 0.0F, 0.0F));
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-2.5F, 16.0F, 7.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(0.5F, 16.0F, 7.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-2.5F, 16.0F, -4.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(0.5F, 16.0F, -4.0F));
		PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, 0.62831855F, 0.0F, 0.0F));
		partDefinition3.addOrReplaceChild("real_tail", CubeListBuilder.create().texOffs(9, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head);
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.tail, this.upperBody);
	}

	public void prepareMobModel(float f, float g, float h) {
		this.tail.yRot = Mth.cos(f * 0.6662F) * 1.4F * g;

		if (pose.equals("sitting")) {
			this.upperBody.setPos(-1.0F, 16.0F, -3.0F);
			this.upperBody.xRot = 1.2566371F;
			this.upperBody.yRot = 0.0F;
			this.body.setPos(0.0F, 18.0F, 0.0F);
			this.body.xRot = 0.7853982F;
			this.tail.setPos(-1.0F, 21.0F, 6.0F);
			this.rightHindLeg.setPos(-2.5F, 22.7F, 2.0F);
			this.rightHindLeg.xRot = 4.712389F;
			this.leftHindLeg.setPos(0.5F, 22.7F, 2.0F);
			this.leftHindLeg.xRot = 4.712389F;
			this.rightFrontLeg.xRot = 5.811947F;
			this.rightFrontLeg.setPos(-2.49F, 17.0F, -4.0F);
			this.leftFrontLeg.xRot = 5.811947F;
			this.leftFrontLeg.setPos(0.51F, 17.0F, -4.0F);
		} else {
			this.body.setPos(0.0F, 14.0F, 2.0F);
			this.body.xRot = 1.5707964F;
			this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
			this.upperBody.xRot = this.body.xRot;
			this.tail.setPos(-1.0F, 12.0F, 8.0F);
			this.rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
			this.leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
			this.rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
			this.leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
			this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
			this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
			this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
			this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		}

		this.realHead.zRot = 0;
		this.upperBody.zRot = 0;
		this.body.zRot = 0;
		this.realTail.zRot = 0;
	}

	public void setupAnim(T wolf, float f, float g, float h, float i, float j) {
		this.head.xRot = j * 0.017453292F;
		this.head.yRot = i * 0.017453292F;
		this.tail.xRot = h;
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
		this.prepareMobModel(f, g, h);
		this.root.render(poseStack, vertexConsumer, i, j);
	}
}
