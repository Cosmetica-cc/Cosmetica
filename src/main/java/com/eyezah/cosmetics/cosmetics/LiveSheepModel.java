package com.eyezah.cosmetics.cosmetics;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Sheep;

public class LiveSheepModel<T extends Sheep> extends QuadrupedModel<T> {
	private float headXRot;
	ModelPart root;

	public LiveSheepModel(ModelPart modelPart) {
		super(modelPart, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
		this.root = modelPart;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = QuadrupedModel.createBodyMesh(12, CubeDeformation.NONE);
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F), PartPose.offset(0.0F, 6.0F, -8.0F));
		partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void prepareMobModel(T sheep, float f, float g, float h) {
		super.prepareMobModel(sheep, f, g, h);
		this.head.y = 6.0F + sheep.getHeadEatPositionScale(h) * 9.0F;
		this.headXRot = sheep.getHeadEatAngleScale(h);
	}

	public void setupAnim(T sheep, float f, float g, float h, float i, float j) {
		super.setupAnim(sheep, f, g, h, i, j);
		this.head.xRot = this.headXRot;
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int tickCount) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
		root.render(poseStack, vertexConsumer, i, j);
	}
}
