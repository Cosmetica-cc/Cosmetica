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
import net.minecraft.world.entity.player.Player;

public class Hat<T extends Player> extends CustomLayer<T, PlayerModel<T>> {
    private ModelManager modelManager;

    public Hat(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
        this.modelManager = Minecraft.getInstance().getModelManager();
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, float f, float g, float pitch, float j, float k, float l) {
        if (player.isInvisible()) return;
        BakableModel modelData = Cosmetics.getPlayerData(player).hat();
        ModelPart modelPart = this.getParentModel().getHead();
        doCoolRenderThings(modelData, modelPart, stack, multiBufferSource, packedLightProbably, 0, 0.61f, 0);
    }
}
