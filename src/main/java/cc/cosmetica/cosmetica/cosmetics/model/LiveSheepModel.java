/*
 * Copyright 2022, 2023 EyezahMC
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

package cc.cosmetica.cosmetica.cosmetics.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SheepModel;
import net.minecraft.world.entity.animal.Sheep;

public class LiveSheepModel extends SheepModel<Sheep> {
	public LiveSheepModel() {
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
		this.body.xRot += 0.5 * Math.PI; // i dont know why, i dont know if i want to know why, but this doesn't work in
		                                 // 1.16.5 unless we rotate the body like this??!?!

		poseStack.scale(0.35F, 0.35F, 0.35F);
		this.renderToBuffer(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
		this.body.xRot -= 0.5 * Math.PI;
	}
}
