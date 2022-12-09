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

package cc.cosmetica.cosmetica.screens.fakeplayer;

import cc.cosmetica.cosmetica.mixin.fakeplayer.PlayerModelAccessor;
import cc.cosmetica.cosmetica.utils.LinearAlgebra;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class MenuCapeLayer implements MenuRenderLayer {
	@Override
	public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float f, float g, float delta, float bob, float yRotDiff, float xRot) {
		if (player.getData().cape() != null) {
			stack.pushPose();
			stack.translate(0.0D, 0.0D, 0.125D);
			double d = 0 - 0;
			double e = 0 - 0;
			double m = 0 - 0;
			float n = player.getYRotBody(0);
			double o = Mth.sin(n * 0.017453292F);
			double p = -Mth.cos(n * 0.017453292F);
			float q = (float)e * 10.0F;
			q = Mth.clamp(q, -6.0F, 32.0F);
			float r = (float)(d * o + m * p) * 100.0F;
			r = Mth.clamp(r, 0.0F, 150.0F);
			float s = (float)(d * p - m * o) * 100.0F;
			s = Mth.clamp(s, -20.0F, 20.0F);
			if (r < 0.0F) {
				r = 0.0F;
			}

			if (player.isSneaking()) {
				q += 25.0F;
			}

			stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.XP, 6.0F + r / 2.0F + q));
			stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.ZP, s / 2.0F));
			stack.mulPose(LinearAlgebra.quaternionDegrees(LinearAlgebra.YP, 180.0F - s / 2.0F));
			VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(player.getRenderableCape()));
			((PlayerModelAccessor) player.getModel()).getCloak().render(stack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
			stack.popPose();
		}
	}
}
