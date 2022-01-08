package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Parrot;

public class ShoulderBuddyModelFox<T extends Fox> extends AgeableListModel<T> {

	public final ModelPart root;
	public final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart tail;
	private static final int LEG_SIZE = 6;
	private static final float HEAD_HEIGHT = 16.5F;
	private static final float LEG_POS = 17.5F;
	private float legMotionPos;
	private String pose;

	public ShoulderBuddyModelFox(ModelPart modelPart, String pose) {
		super(true, 8.0F, 3.35F);
		this.pose = pose;
		this.root = modelPart;
		this.head = modelPart.getChild("head");
		this.body = modelPart.getChild("body");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
		this.tail = this.body.getChild("tail");
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
		this.prepare(f, g, h);
		this.root.render(poseStack, vertexConsumer, i, j);
	}

	public void setupAnim(T fox, float f, float g, float h, float i, float j) {
		this.head.xRot = 0.0F;
		this.head.yRot = -2.0943952F;
		this.head.zRot = Mth.cos(h * 0.027F) / 22.0F;

		if (!pose.equals("sleeping") && !pose.equals("faceplanted") && !pose.equals("crouching")) {
			this.head.xRot = j * 0.017453292F;
			this.head.yRot = i * 0.017453292F;
		}

		if (pose.equals("sleeping")) {
			this.head.xRot = 0.0F;
			this.head.yRot = -2.0943952F;
			this.head.zRot = Mth.cos(h * 0.027F) / 22.0F;
		}

		float l;
		if (pose.equals("crouching")) {
			l = Mth.cos(h) * 0.01F;
			this.body.yRot = l;
			this.rightHindLeg.zRot = l;
			this.leftHindLeg.zRot = l;
			this.rightFrontLeg.zRot = l / 2.0F;
			this.leftFrontLeg.zRot = l / 2.0F;
		}

		if (pose.equals("faceplanted")) {
			l = 0.1F;
			this.legMotionPos += 0.67F;
			this.rightHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
			this.leftHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + 3.1415927F) * 0.1F;
			this.rightFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + 3.1415927F) * 0.1F;
			this.leftFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 5).addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F), PartPose.offset(-1.0F, 16.5F, -3.0F));
		partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(8, 1).addBox(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
		partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(15, 1).addBox(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
		partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F), PartPose.ZERO);
		PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 15).addBox(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 16.0F, -6.0F, 1.5707964F, 0.0F, 0.0F));
		CubeDeformation cubeDeformation = new CubeDeformation(0.001F);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(4, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(13, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder2, PartPose.offset(-5.0F, 17.5F, 7.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(-1.0F, 17.5F, 7.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-5.0F, 17.5F, 0.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(-1.0F, 17.5F, 0.0F));
		partDefinition3.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0).addBox(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F), PartPose.offsetAndRotation(-4.0F, 15.0F, -1.0F, -0.05235988F, 0.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 48, 32);
	}

	public void prepare(float f, float g, float h) {
		this.body.xRot = 1.5707964F;
		this.tail.xRot = -0.05235988F;
		this.rightHindLeg.xRot = 0;
		this.leftHindLeg.xRot = 0;
		this.rightFrontLeg.xRot = 0;
		this.leftFrontLeg.xRot = 0;
		this.body.setPos(0.0F, 16.0F, -6.0F);
		this.body.zRot = 0.0F;
		this.rightHindLeg.setPos(-5.0F, 17.5F, 7.0F);
		this.leftHindLeg.setPos(-1.0F, 17.5F, 7.0F);
		this.body.zRot = -1.5707964F;
		this.body.setPos(0.0F, 21.0F, -6.0F);
		this.tail.xRot = -2.6179938F;
		if (this.young || true) {
			this.tail.xRot = -2.1816616F;
			this.body.setPos(0.0F, 21.0F, -2.0F);
		}
		this.head.setPos(1.0F, 19.49F, -3.0F);
		this.head.xRot = 0.0F;
		this.head.yRot = -2.0943952F;
		this.head.zRot = 0.0F;
		this.rightHindLeg.visible = false;
		this.leftHindLeg.visible = false;
		this.rightFrontLeg.visible = false;
		this.leftFrontLeg.visible = false;



		this.body.xRot = 1.5707964F;
		this.tail.xRot = -0.05235988F;
		this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
		this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
		this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.head.setPos(-1.0F, 16.5F, -3.0F);
		this.head.yRot = 0.0F;
		this.head.zRot = 0; // temp
		this.rightHindLeg.visible = true;
		this.leftHindLeg.visible = true;
		this.rightFrontLeg.visible = true;
		this.leftFrontLeg.visible = true;
		this.body.setPos(0.0F, 16.0F, -6.0F);
		this.body.zRot = 0.0F;
		this.rightHindLeg.setPos(-5.0F, 17.5F, 7.0F);
		this.leftHindLeg.setPos(-1.0F, 17.5F, 7.0F);
		if (pose.equals("crouching")) {
			this.body.xRot = 1.6755161F;
			float i = 1;
			this.body.setPos(0.0F, 16.0F + i, -6.0F);
			this.head.setPos(-1.0F, 16.5F + i, -3.0F);
			this.head.yRot = 0.0F;
		} else if (pose.equals("sleeping")) {
			this.body.zRot = -1.5707964F;
			this.body.setPos(0.0F, 21.0F, -6.0F);
			this.tail.xRot = -2.6179938F;
			if (this.young) {
				this.tail.xRot = -2.1816616F;
				this.body.setPos(0.0F, 21.0F, -2.0F);
			}

			this.head.setPos(1.0F, 19.49F, -3.0F);
			this.head.xRot = 0.0F;
			this.head.yRot = -2.0943952F;
			this.head.zRot = 0.0F;
			this.rightHindLeg.visible = false;
			this.leftHindLeg.visible = false;
			this.rightFrontLeg.visible = false;
			this.leftFrontLeg.visible = false;
		} else if (pose.equals("sitting")) {
			this.body.xRot = 0.5235988F;
			this.body.setPos(0.0F, 9.0F, -3.0F);
			this.tail.xRot = 0.7853982F;
			this.tail.setPos(-4.0F, 15.0F, -2.0F);
			this.head.setPos(-1.0F, 10.0F, -0.25F);
			this.head.xRot = 0.0F;
			this.head.yRot = 0.0F;
			if (this.young) {
				this.head.setPos(-1.0F, 13.0F, -3.75F);
			}

			this.rightHindLeg.xRot = -1.3089969F;
			this.rightHindLeg.setPos(-5.0F, 21.5F, 6.75F);
			this.leftHindLeg.xRot = -1.3089969F;
			this.leftHindLeg.setPos(-1.0F, 21.5F, 6.75F);
			this.rightFrontLeg.xRot = -0.2617994F;
			this.leftFrontLeg.xRot = -0.2617994F;
		}
	}

	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head);
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg);
	}
}
