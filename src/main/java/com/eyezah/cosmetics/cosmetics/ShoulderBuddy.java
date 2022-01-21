package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.Cosmetics;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public class ShoulderBuddy<T extends Player> extends CustomLayer<T, PlayerModel<T>> {
    private ModelManager modelManager;

    public ShoulderBuddy(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
        this.modelManager = Minecraft.getInstance().getModelManager();
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, float f, float g, float pitch, float j, float k, float l) {
        render(stack, multiBufferSource, packedLightProbably, player, f, g, pitch, j, k, l, true);
        render(stack, multiBufferSource, packedLightProbably, player, f, g, pitch, j, k, l, false);
    }

    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, float f, float g, float pitch, float j, float k, float l, boolean arm) {
        String useArm = "";
        if (player.getMainArm() == HumanoidArm.RIGHT) {
            if (player.getShoulderEntityLeft().isEmpty()) {
                useArm = "left";
            } else if (player.getShoulderEntityRight().isEmpty()) {
                useArm = "right";
            }
        } else {
            if (player.getShoulderEntityRight().isEmpty()) {
                useArm = "right";
            } else if (player.getShoulderEntityLeft().isEmpty()) {
                useArm = "left";
            }
        }
        if (useArm.equals("") || (arm && useArm.equals("right")) || (!arm && useArm.equals("left"))) return;
        BakableModel modelData = Cosmetics.getPlayerData(player).hat();
        ModelPart modelPart;
        if (arm) {
            modelPart = this.getParentModel().leftArm;
        } else {
            modelPart = this.getParentModel().rightArm;
        }
        doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0f, 0);
    }
}
