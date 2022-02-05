package com.eyezah.cosmetics.cosmetics;

import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.cosmetics.model.Models;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.player.Player;

public abstract class CustomLayer<T extends Player, P extends HumanoidModel<T>> extends RenderLayer<T, PlayerModel<T>> {
    public ModelManager modelManager;

    public CustomLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
        super(renderLayerParent);
        this.modelManager = Minecraft.getInstance().getModelManager();
    }

    public void doCoolRenderThings(BakableModel bakableModel, ModelPart modelPart, PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, float x, float y, float z) {
        BakedModel model = Models.getBakedModel(bakableModel);
        if (model == null) return; // if it has errors with the baked model or cannot render it for another reason will return null
        stack.pushPose();
        float o = 1.001f; // 0.5 has z fighting
        modelPart.translateAndRotate(stack);
        stack.scale(o, -o, -o);
        stack.translate(x, y, z); // vanilla: 0.0 second param
        Models.renderModel(
                model,
                stack,
                multiBufferSource,
                packedLightProbably);

        stack.popPose();
    }
}
