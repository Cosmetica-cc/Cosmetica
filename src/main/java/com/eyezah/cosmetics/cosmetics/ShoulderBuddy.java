package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.Cosmetics;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class ShoulderBuddy<T extends Player> extends CustomLayer<T, PlayerModel<T>> {
    private ModelManager modelManager;

    public ShoulderBuddy(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
        this.modelManager = Minecraft.getInstance().getModelManager();
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, float f, float g, float pitch, float j, float k, float l) {
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

        if (left != null) render(stack, multiBufferSource, packedLightProbably, player, f, g, pitch, j, k, l, left);
    }

    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, float f, float g, float pitch, float j, float k, float l, boolean left) {
        BakableModel modelData = Cosmetics.getPlayerData(player).hat();
        boolean static_position = true;
        System.out.println(left);
        ModelPart modelPart;

        stack.pushPose();

        if (static_position) {
            modelPart = this.getParentModel().body;
            stack.translate(left ? 0.4 : -0.4, 0, 0);
        } else if (left) {
            modelPart = this.getParentModel().leftArm;
        } else {
            modelPart = this.getParentModel().rightArm;
        }

        doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0f, 0);
        stack.popPose();
    }
}
