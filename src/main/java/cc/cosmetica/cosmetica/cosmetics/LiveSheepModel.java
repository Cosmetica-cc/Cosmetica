package cc.cosmetica.cosmetica.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SheepModel;
import net.minecraft.world.entity.animal.Sheep;

public class LiveSheepModel extends SheepModel<Sheep> {
	public LiveSheepModel() {
		super();
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
		// poseStack.scale(0.35F, 0.35F, 0.35F);
		this.renderToBuffer(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
	}
}
