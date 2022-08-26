package cc.cosmetica.cosmetica.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.world.entity.animal.Sheep;

public class LiveSheepFurModel extends SheepFurModel<Sheep> {
	public LiveSheepFurModel() {
		super();
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		this.body.xRot += 0.5 * Math.PI; // please refer to LiveSheepModel#render for a comprehensive explanation of this line
		super.renderToBuffer(poseStack, vertexConsumer, i, j, f, g, h, k);
		this.body.xRot -= 0.5 * Math.PI;
	}
}
