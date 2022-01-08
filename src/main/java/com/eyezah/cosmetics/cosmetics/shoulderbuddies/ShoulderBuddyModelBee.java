package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Bee;

public class ShoulderBuddyModelBee<T extends Bee> extends AgeableListModel<T> {
	private static final float BEE_Y_BASE = 19.0F;
	private static final String BONE = "bone";
	private static final String STINGER = "stinger";
	private static final String LEFT_ANTENNA = "left_antenna";
	private static final String RIGHT_ANTENNA = "right_antenna";
	private static final String FRONT_LEGS = "front_legs";
	private static final String MIDDLE_LEGS = "middle_legs";
	private static final String BACK_LEGS = "back_legs";
	private final ModelPart bone;
	private final ModelPart rightWing;
	private final ModelPart leftWing;
	private final ModelPart frontLeg;
	private final ModelPart midLeg;
	private final ModelPart backLeg;
	private final ModelPart stinger;
	private final ModelPart leftAntenna;
	private final ModelPart rightAntenna;
	private float rollAmount;
	private ModelPart root;

	public ShoulderBuddyModelBee(ModelPart modelPart) {
		super(false, 24.0F, 0.0F);
		this.bone = modelPart.getChild("bone");
		this.root = modelPart;
		ModelPart modelPart2 = this.bone.getChild("body");
		this.stinger = modelPart2.getChild("stinger");
		this.leftAntenna = modelPart2.getChild("left_antenna");
		this.rightAntenna = modelPart2.getChild("right_antenna");
		this.rightWing = this.bone.getChild("right_wing");
		this.leftWing = this.bone.getChild("left_wing");
		this.frontLeg = this.bone.getChild("front_legs");
		this.midLeg = this.bone.getChild("middle_legs");
		this.backLeg = this.bone.getChild("back_legs");
	}

	public static LayerDefinition createBodyLayer() {
		float f = 19.0F;
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F), PartPose.ZERO);
		partDefinition3.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(26, 7).addBox(0.0F, -1.0F, 5.0F, 0.0F, 1.0F, 2.0F), PartPose.ZERO);
		partDefinition3.addOrReplaceChild("left_antenna", CubeListBuilder.create().texOffs(2, 0).addBox(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F));
		partDefinition3.addOrReplaceChild("right_antenna", CubeListBuilder.create().texOffs(2, 3).addBox(-2.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F));
		CubeDeformation cubeDeformation = new CubeDeformation(0.001F);
		partDefinition2.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 18).addBox(-9.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, cubeDeformation), PartPose.offsetAndRotation(-1.5F, -4.0F, -3.0F, 0.0F, -0.2618F, 0.0F));
		partDefinition2.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, cubeDeformation), PartPose.offsetAndRotation(1.5F, -4.0F, -3.0F, 0.0F, 0.2618F, 0.0F));
		partDefinition2.addOrReplaceChild("front_legs", CubeListBuilder.create().addBox("front_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 1), PartPose.offset(1.5F, 3.0F, -2.0F));
		partDefinition2.addOrReplaceChild("middle_legs", CubeListBuilder.create().addBox("middle_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 3), PartPose.offset(1.5F, 3.0F, 0.0F));
		partDefinition2.addOrReplaceChild("back_legs", CubeListBuilder.create().addBox("back_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 5), PartPose.offset(1.5F, 3.0F, 2.0F));
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void prepareMobModel(float f, float g, float h) {
		this.rollAmount = 0;
		this.stinger.visible = true;
	}

	public void setupAnim(float f, float g, float h, float i, float j) {
		this.rightWing.xRot = 0.0F;
		this.leftAntenna.xRot = 0.0F;
		this.rightAntenna.xRot = 0.0F;
		this.bone.xRot = 0.0F;
		boolean bl = false;
		float l;
		l = h * 120.32113F * 0.017453292F;
		this.rightWing.yRot = 0.0F;
		this.rightWing.zRot = Mth.cos(l) * 3.1415927F * 0.15F;
		this.leftWing.xRot = this.rightWing.xRot;
		this.leftWing.yRot = this.rightWing.yRot;
		this.leftWing.zRot = -this.rightWing.zRot;
		this.frontLeg.xRot = 0.7853982F;
		this.midLeg.xRot = 0.7853982F;
		this.backLeg.xRot = 0.7853982F;
		this.bone.xRot = 0.0F;
		this.bone.yRot = 0.0F;
		this.bone.zRot = 0.0F;

		if (this.rollAmount > 0.0F) {
			this.bone.xRot = ModelUtils.rotlerpRad(this.bone.xRot, 3.0915928F, this.rollAmount);
		}

	}

	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of();
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.bone);
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
		this.setupAnim(f, g, h, i, j);
		this.prepareMobModel(f, g, h);
		this.root.render(poseStack, vertexConsumer, i, j);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {

	}
}
