package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.Cosmetics;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public class ShoulderBuddy<T extends Player> extends CustomLayer<T, PlayerModel<T>> {
    private ModelManager modelManager;
    private EntityModelSet entityModelSet;

    public ShoulderBuddy(RenderLayerParent<T, PlayerModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.modelManager = Minecraft.getInstance().getModelManager();
        this.entityModelSet = entityModelSet;
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, float f, float g, float pitch, float j, float k, float l) {
        if (player.isInvisible()) return;
        Boolean left = null;

        if (player.getMainArm() == HumanoidArm.RIGHT) {
            if (player.getShoulderEntityLeft().isEmpty()) {
                left = true;
            } else if (player.getShoulderEntityRight().isEmpty()) {
                left = false;
            }
        } else {
            if (player.getShoulderEntityRight().isEmpty()) {
                left = false;
            } else if (player.getShoulderEntityLeft().isEmpty()) {
                left = true;
            }
        }

        if (left != null) render(stack, multiBufferSource, packedLightProbably, player, left);
    }

    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, boolean left) {
        BakableModel modelData = Cosmetics.getPlayerData(player).shoulderBuddy();

        if (modelData == null) {
            return;
        }

        stack.pushPose();

        if (modelData.id().equals("-sheep")) { // builtin live sheep
            LiveSheepModel model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
            stack.pushPose();
            stack.translate(left ? 0.35 : -0.35, (player.isCrouching() ? -1.3 : -1.6D) + 1.07D, 0.0D);
            stack.scale(0.8f, 0.8f, 0.8f);
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep_fur.png")));
            model.renderOnShoulder(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY, player.tickCount);
            stack.popPose();

            model = new LiveSheepModel(entityModelSet.bakeLayer(ModelLayers.SHEEP));
            stack.pushPose();
            stack.translate(left ? 0.35 : -0.35, (player.isCrouching() ? -1.3 : -1.6D) + 1.07D, 0.0D);
            stack.scale(0.8f, 0.8f, 0.8f);
            vertexConsumer = multiBufferSource.getBuffer(model.renderType(new ResourceLocation("textures/entity/sheep/sheep.png")));
            model.renderOnShoulder(stack, vertexConsumer, packedLightProbably, OverlayTexture.NO_OVERLAY, player.tickCount);
            stack.popPose();
        } else {
            boolean staticPosition = (modelData.extraInfo() & 0b1) == 1;
            ModelPart modelPart;

            if (staticPosition) {
                modelPart = this.getParentModel().body;
                stack.translate(left ? 0.35 : -0.35, 0, 0);
            } else if (left) {
                modelPart = this.getParentModel().leftArm;
            } else {
                modelPart = this.getParentModel().rightArm;
            }

            stack.translate(0, -0.2, 0);
            doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0f, 0);
        }

        stack.popPose();
    }
}
