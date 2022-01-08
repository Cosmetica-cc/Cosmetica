package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

import java.util.Map;

public class ShoulderBuddyModelAxolotl<T extends Axolotl & LerpingModel> extends AgeableListModel<T> {
	public static final float SWIMMING_LEG_XROT = 1.8849558F;
	private final ModelPart tail;
	private final ModelPart leftHindLeg;
	private final ModelPart rightHindLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart topGills;
	private final ModelPart leftGills;
	private final ModelPart rightGills;
	private String pose;
	private ModelPart root;

	public ShoulderBuddyModelAxolotl(ModelPart modelPart, String pose) {
		super(true, 8.0F, 3.35F);
		this.pose = pose;
		this.root = modelPart;
		this.body = modelPart.getChild("body");
		this.head = this.body.getChild("head");
		this.rightHindLeg = this.body.getChild("right_hind_leg");
		this.leftHindLeg = this.body.getChild("left_hind_leg");
		this.rightFrontLeg = this.body.getChild("right_front_leg");
		this.leftFrontLeg = this.body.getChild("left_front_leg");
		this.tail = this.body.getChild("tail");
		this.topGills = this.head.getChild("top_gills");
		this.leftGills = this.head.getChild("left_gills");
		this.rightGills = this.head.getChild("right_gills");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.0F, -2.0F, -9.0F, 8.0F, 4.0F, 10.0F).texOffs(2, 17).addBox(0.0F, -3.0F, -8.0F, 0.0F, 5.0F, 9.0F), PartPose.offset(0.0F, 20.0F, 5.0F));
		CubeDeformation cubeDeformation = new CubeDeformation(0.001F);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 5.0F, 5.0F, cubeDeformation), PartPose.offset(0.0F, 0.0F, -9.0F));
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 0.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder3 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubeDeformation);
		partDefinition3.addOrReplaceChild("top_gills", cubeListBuilder, PartPose.offset(0.0F, -3.0F, -1.0F));
		partDefinition3.addOrReplaceChild("left_gills", cubeListBuilder2, PartPose.offset(-4.0F, 0.0F, -1.0F));
		partDefinition3.addOrReplaceChild("right_gills", cubeListBuilder3, PartPose.offset(4.0F, 0.0F, -1.0F));
		CubeListBuilder cubeListBuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder5 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubeDeformation);
		partDefinition2.addOrReplaceChild("right_hind_leg", cubeListBuilder5, PartPose.offset(-3.5F, 1.0F, -1.0F));
		partDefinition2.addOrReplaceChild("left_hind_leg", cubeListBuilder4, PartPose.offset(3.5F, 1.0F, -1.0F));
		partDefinition2.addOrReplaceChild("right_front_leg", cubeListBuilder5, PartPose.offset(-3.5F, 1.0F, -8.0F));
		partDefinition2.addOrReplaceChild("left_front_leg", cubeListBuilder4, PartPose.offset(3.5F, 1.0F, -8.0F));
		partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 1.0F));
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of();
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.body);
	}

	public void setupAnim(T axolotl, float f, float g, float h, float i, float j) {
		this.setupInitialAnimationValues(axolotl, i, j);
		if (pose.equals("playingdead")) {
			this.setupPlayDeadAnimation(i);
		} else if (pose.equals("swimming")) {
			this.setupSwimmingAnimation(h, j);
		} else if (pose.equals("crawling")) {
			this.setupGroundCrawlingAnimation(h, i);
		} else {
			this.setupLayStillOnGroundAnimation(h, i);
		}
		this.saveAnimationValues(axolotl);
	}

	private void saveAnimationValues(T axolotl) {
		Map<String, Vector3f> map = axolotl.getModelRotationValues();
		map.put("body", this.getRotationVector(this.body));
		map.put("head", this.getRotationVector(this.head));
		map.put("right_hind_leg", this.getRotationVector(this.rightHindLeg));
		map.put("left_hind_leg", this.getRotationVector(this.leftHindLeg));
		map.put("right_front_leg", this.getRotationVector(this.rightFrontLeg));
		map.put("left_front_leg", this.getRotationVector(this.leftFrontLeg));
		map.put("tail", this.getRotationVector(this.tail));
		map.put("top_gills", this.getRotationVector(this.topGills));
		map.put("left_gills", this.getRotationVector(this.leftGills));
		map.put("right_gills", this.getRotationVector(this.rightGills));
	}

	private Vector3f getRotationVector(ModelPart modelPart) {
		return new Vector3f(modelPart.xRot, modelPart.yRot, modelPart.zRot);
	}

	private void setRotationFromVector(ModelPart modelPart, Vector3f vector3f) {
		modelPart.setRotation(vector3f.x(), vector3f.y(), vector3f.z());
	}

	private void setupInitialAnimationValues(T axolotl, float f, float g) {
		this.body.x = 0.0F;
		this.head.y = 0.0F;
		this.body.y = 20.0F;
		Map<String, Vector3f> map = axolotl.getModelRotationValues();
		if (map.isEmpty()) {
			this.body.setRotation(g * 0.017453292F, f * 0.017453292F, 0.0F);
			this.head.setRotation(0.0F, 0.0F, 0.0F);
			this.leftHindLeg.setRotation(0.0F, 0.0F, 0.0F);
			this.rightHindLeg.setRotation(0.0F, 0.0F, 0.0F);
			this.leftFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
			this.rightFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
			this.leftGills.setRotation(0.0F, 0.0F, 0.0F);
			this.rightGills.setRotation(0.0F, 0.0F, 0.0F);
			this.topGills.setRotation(0.0F, 0.0F, 0.0F);
			this.tail.setRotation(0.0F, 0.0F, 0.0F);
		} else {
			this.setRotationFromVector(this.body, (Vector3f)map.get("body"));
			this.setRotationFromVector(this.head, (Vector3f)map.get("head"));
			this.setRotationFromVector(this.leftHindLeg, (Vector3f)map.get("left_hind_leg"));
			this.setRotationFromVector(this.rightHindLeg, (Vector3f)map.get("right_hind_leg"));
			this.setRotationFromVector(this.leftFrontLeg, (Vector3f)map.get("left_front_leg"));
			this.setRotationFromVector(this.rightFrontLeg, (Vector3f)map.get("right_front_leg"));
			this.setRotationFromVector(this.leftGills, (Vector3f)map.get("left_gills"));
			this.setRotationFromVector(this.rightGills, (Vector3f)map.get("right_gills"));
			this.setRotationFromVector(this.topGills, (Vector3f)map.get("top_gills"));
			this.setRotationFromVector(this.tail, (Vector3f)map.get("tail"));
		}

	}

	private float lerpTo(float f, float g) {
		return this.lerpTo(0.05F, f, g);
	}

	private float lerpTo(float f, float g, float h) {
		return Mth.rotLerp(f, g, h);
	}

	private void lerpPart(ModelPart modelPart, float f, float g, float h) {
		modelPart.setRotation(this.lerpTo(modelPart.xRot, f), this.lerpTo(modelPart.yRot, g), this.lerpTo(modelPart.zRot, h));
	}

	private void setupLayStillOnGroundAnimation(float f, float g) {
		float h = f * 0.09F;
		float i = Mth.sin(h);
		float j = Mth.cos(h);
		float k = i * i - 2.0F * i;
		float l = j * j - 3.0F * i;
		this.head.xRot = this.lerpTo(this.head.xRot, -0.09F * k);
		this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
		this.head.zRot = this.lerpTo(this.head.zRot, -0.2F);
		this.tail.yRot = this.lerpTo(this.tail.yRot, -0.1F + 0.1F * k);
		this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F + 0.05F * l);
		this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
		this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
		this.lerpPart(this.leftHindLeg, 1.1F, 1.0F, 0.0F);
		this.lerpPart(this.leftFrontLeg, 0.8F, 2.3F, -0.5F);
		this.applyMirrorLegRotations();
		this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
		this.body.yRot = this.lerpTo(this.body.yRot, g * 0.017453292F);
		this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
	}

	private void setupGroundCrawlingAnimation(float f, float g) {
		float h = f * 0.11F;
		float i = Mth.cos(h);
		float j = (i * i - 2.0F * i) / 5.0F;
		float k = 0.7F * i;
		this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
		this.head.yRot = this.lerpTo(this.head.yRot, 0.09F * i);
		this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
		this.tail.yRot = this.lerpTo(this.tail.yRot, this.head.yRot);
		this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F - 0.08F * (i * i + 2.0F * Mth.sin(h)));
		this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
		this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
		this.lerpPart(this.leftHindLeg, 0.9424779F, 1.5F - j, -0.1F);
		this.lerpPart(this.leftFrontLeg, 1.0995574F, 1.5707964F - k, 0.0F);
		this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -1.0F - j, 0.0F);
		this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, -1.5707964F - k, 0.0F);
		this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
		this.body.yRot = this.lerpTo(this.body.yRot, g * 0.017453292F);
		this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
	}

	private void setupSwimmingAnimation(float f, float g) {
		float h = f * 0.33F;
		float i = Mth.sin(h);
		float j = Mth.cos(h);
		float k = 0.13F * i;
		this.body.xRot = this.lerpTo(0.1F, this.body.xRot, g * 0.017453292F + k);
		this.head.xRot = -k * 1.8F;
		ModelPart var10000 = this.body;
		var10000.y -= 0.45F * j;
		this.topGills.xRot = this.lerpTo(this.topGills.xRot, -0.5F * i - 0.8F);
		this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, 0.3F * i + 0.9F);
		this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
		this.tail.yRot = this.lerpTo(this.tail.yRot, 0.3F * Mth.cos(h * 0.9F));
		this.lerpPart(this.leftHindLeg, 1.8849558F, -0.4F * i, 1.5707964F);
		this.lerpPart(this.leftFrontLeg, 1.8849558F, -0.2F * j - 0.1F, 1.5707964F);
		this.applyMirrorLegRotations();
		this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
		this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
	}

	private void setupPlayDeadAnimation(float f) {
		this.lerpPart(this.leftHindLeg, 1.4137167F, 1.0995574F, 0.7853982F);
		this.lerpPart(this.leftFrontLeg, 0.7853982F, 2.042035F, 0.0F);
		this.body.xRot = this.lerpTo(this.body.xRot, -0.15F);
		this.body.zRot = this.lerpTo(this.body.zRot, 0.35F);
		this.applyMirrorLegRotations();
		this.body.yRot = this.lerpTo(this.body.yRot, f * 0.017453292F);
		this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
		this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
		this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
		this.tail.yRot = this.lerpTo(this.tail.yRot, 0.0F);
		this.lerpPart(this.topGills, 0.0F, 0.0F, 0.0F);
		this.lerpPart(this.leftGills, 0.0F, 0.0F, 0.0F);
		this.lerpPart(this.rightGills, 0.0F, 0.0F, 0.0F);
	}

	private void applyMirrorLegRotations() {
		this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -this.leftHindLeg.yRot, -this.leftHindLeg.zRot);
		this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, -this.leftFrontLeg.yRot, -this.leftFrontLeg.zRot);
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
		this.root.render(poseStack, vertexConsumer, i, j);
	}
}
