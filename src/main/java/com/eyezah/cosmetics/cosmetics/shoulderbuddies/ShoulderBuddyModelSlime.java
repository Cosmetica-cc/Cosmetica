package com.eyezah.cosmetics.cosmetics.shoulderbuddies;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class ShoulderBuddyModelSlime<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart root;

	public ShoulderBuddyModelSlime(ModelPart modelPart) {
		this.root = modelPart;
	}

	public static LayerDefinition createOuterBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public static LayerDefinition createInnerBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(32, 0).addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(32, 4).addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
	}

	public ModelPart root() {
		return this.root;
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
		poseStack.scale(0.35F, 0.35F, 0.35F);
		this.root.render(poseStack, vertexConsumer, i, j);
	}
}
