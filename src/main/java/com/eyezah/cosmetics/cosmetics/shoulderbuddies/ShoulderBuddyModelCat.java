package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Cat;

public class ShoulderBuddyModelCat<T extends Cat> extends OcelotModel<T> {
	private float lieDownAmount;
	private float lieDownAmountTail;
	private float relaxStateOneAmount;
	private ModelPart root;
	public String pose;

	public ShoulderBuddyModelCat(ModelPart modelPart, String pose) {
		super(modelPart);
		this.root = modelPart;
		this.pose = pose;
	}

	public void prepareMobModel(float f, float g, float h) {
		if (pose.equals("sleeping")) {
			this.head.xRot = 0.0F;
			this.head.zRot = 0.0F;
			this.leftFrontLeg.xRot = 0.0F;
			this.leftFrontLeg.zRot = 0.0F;
			this.rightFrontLeg.xRot = 0.0F;
			this.rightFrontLeg.zRot = 0.0F;
			this.rightFrontLeg.x = -1.2F;
			this.leftHindLeg.xRot = 0.0F;
			this.rightHindLeg.xRot = 0.0F;
			this.rightHindLeg.zRot = 0.0F;
			this.rightHindLeg.x = -1.1F;
			this.rightHindLeg.y = 18.0F;
		}

		//super.prepareMobModel(cat, f, g, h);
		if (pose.equals("sitting")) {
			this.body.xRot = 0.7853982F;
			ModelPart var10000 = this.body;
			var10000.y += -4.0F;
			var10000 = this.body;
			var10000.z += 5.0F;
			var10000 = this.head;
			var10000.y += -3.3F;
			++this.head.z;
			var10000 = this.tail1;
			var10000.y += 8.0F;
			var10000 = this.tail1;
			var10000.z += -2.0F;
			var10000 = this.tail2;
			var10000.y += 2.0F;
			var10000 = this.tail2;
			var10000.z += -0.8F;
			this.tail1.xRot = 1.7278761F;
			this.tail2.xRot = 2.670354F;
			this.leftFrontLeg.xRot = -0.15707964F;
			this.leftFrontLeg.y = 16.1F;
			this.leftFrontLeg.z = -7.0F;
			this.rightFrontLeg.xRot = -0.15707964F;
			this.rightFrontLeg.y = 16.1F;
			this.rightFrontLeg.z = -7.0F;
			this.leftHindLeg.xRot = -1.5707964F;
			this.leftHindLeg.y = 21.0F;
			this.leftHindLeg.z = 1.0F;
			this.rightHindLeg.xRot = -1.5707964F;
			this.rightHindLeg.y = 21.0F;
			this.rightHindLeg.z = 1.0F;
			this.state = 3;
		}

	}

	public void setupAnim(T cat, float f, float g, float h, float i, float j) {
		super.setupAnim(cat, f, g, h, i, j);
		if (this.lieDownAmount > 0.0F) {
			this.head.zRot = ModelUtils.rotlerpRad(this.head.zRot, -1.2707963F, this.lieDownAmount);
			this.head.yRot = ModelUtils.rotlerpRad(this.head.yRot, 1.2707963F, this.lieDownAmount);
			this.leftFrontLeg.xRot = -1.2707963F;
			this.rightFrontLeg.xRot = -0.47079635F;
			this.rightFrontLeg.zRot = -0.2F;
			this.rightFrontLeg.x = -0.2F;
			this.leftHindLeg.xRot = -0.4F;
			this.rightHindLeg.xRot = 0.5F;
			this.rightHindLeg.zRot = -0.5F;
			this.rightHindLeg.x = -0.3F;
			this.rightHindLeg.y = 20.0F;
			this.tail1.xRot = ModelUtils.rotlerpRad(this.tail1.xRot, 0.8F, this.lieDownAmountTail);
			this.tail2.xRot = ModelUtils.rotlerpRad(this.tail2.xRot, -0.4F, this.lieDownAmountTail);
		}

		if (this.relaxStateOneAmount > 0.0F) {
			this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, -0.58177644F, this.relaxStateOneAmount);
		}
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
		this.prepareMobModel(f, g, h);
		this.root.render(poseStack, vertexConsumer, i, j);
	}
}
