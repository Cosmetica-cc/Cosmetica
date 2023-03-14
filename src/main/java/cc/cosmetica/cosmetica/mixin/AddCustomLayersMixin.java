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

package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.cosmetics.BackBling;
import cc.cosmetica.cosmetica.cosmetics.Hats;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class AddCustomLayersMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {
	@Shadow protected abstract boolean addLayer(RenderLayer<T, M> renderLayer);

	protected AddCustomLayersMixin(EntityRendererProvider.Context context) {
		super(context);
	}

	@Inject(at=@At("TAIL"), method="<init>", allow=1)
	private void init(EntityRendererProvider.Context context, EntityModel entityModel, float f, CallbackInfo ci) {
		if (entityModel instanceof HumanoidModel) {
			this.addLayer(new Hats((RenderLayerParent) this));
			this.addLayer(new ShoulderBuddies((RenderLayerParent) this, context.getModelSet()));
			this.addLayer(new BackBling((RenderLayerParent) this));
		}
	}
}