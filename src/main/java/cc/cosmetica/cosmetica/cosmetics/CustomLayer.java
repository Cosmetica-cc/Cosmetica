/*
 * Copyright 2022 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica.cosmetics;

import cc.cosmetica.cosmetica.cosmetics.model.BakableModel;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.cosmetics.model.Models;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.screens.fakeplayer.MenuRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class CustomLayer<T extends LivingEntity, P extends HumanoidModel<T>> extends RenderLayer<T, P> implements MenuRenderLayer {
    public ModelManager modelManager;

    public CustomLayer(RenderLayerParent<T, P> renderLayerParent) {
        super(renderLayerParent);
        this.modelManager = Minecraft.getInstance().getModelManager();
    }

    @Override
    public final void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, T player, float f, float g, float pitch, float bob, float yRotDiff, float xRot) {
        this.render(stack, multiBufferSource, packedLight, player, f, g, pitch, bob, yRotDiff, xRot);
    }

    @Override
    public final void renderInMenu(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
        this.render(stack, bufferSource, packedLight, player, o, n, delta, bob, yRotDiff, xRot);
    }

    protected abstract void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, Playerish entity, float f, float g, float pitch, float bob, float yRotDiff, float xRot);

    public void doCoolRenderThings(BakableModel bakableModel, ModelPart modelPart, PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, float x, float y, float z) {
        this.doCoolRenderThings(bakableModel, modelPart, stack, multiBufferSource, packedLightProbably, x, y, z, false);
    }

    public void doCoolRenderThings(BakableModel bakableModel, ModelPart modelPart, PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, float x, float y, float z, boolean mirror) {
        BakedModel model = Models.getBakedModel(bakableModel);
        if (model == null) return; // if it has errors with the baked model or cannot render it for another reason will return null
        stack.pushPose();
        float o = 1.001f; // prevent z fighting
        modelPart.translateAndRotate(stack);
        stack.scale(o, -o, -o);
        stack.mulPose(new Quaternion(Vector3f.YP, (float)Math.PI, false)); // pi radians on y axis
        stack.translate(x, y, z); // vanilla: 0.0 second param
        if (mirror) stack.scale(-1, 1, 1);
        Models.renderModel(
                model,
                stack,
                multiBufferSource,
                bakableModel.image(),
                packedLightProbably);

        stack.popPose();
    }

    protected boolean canOverridePlayerCosmetics(Playerish playerish) {
        return playerish instanceof Player player && (Minecraft.getInstance().player == null || Minecraft.getInstance().player.getUUID().equals(player.getUUID()));
    }

	public static final CosmeticStack<ResourceLocation> CAPE_OVERRIDER = new CosmeticStack<>();
}
