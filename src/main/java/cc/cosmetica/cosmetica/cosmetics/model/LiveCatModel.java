package cc.cosmetica.cosmetica.cosmetics.model;

import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.ModelUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;

public class LiveCatModel extends CatModel<Cat> {
	public LiveCatModel(ModelPart modelPart) {
		super(modelPart);
		this.root = modelPart;
	}

	final ModelPart root;

	void lieDown(int lifetime) {
		this.leftFrontLeg.xRot = (float)-Math.PI / 3f;
		this.leftFrontLeg.y += 3f;

		this.rightFrontLeg.xRot = (float)-Math.PI / 3f;
		this.rightFrontLeg.y += 3f;

		this.leftHindLeg.xRot = (float)Math.PI / 6f;
		this.rightHindLeg.xRot = (float)Math.PI / 6f;

		
		float rotation = 0;

		this.tail1.zRot = rotation;
		this.tail2.zRot = this.tail1.zRot;

		this.tail2.y += 4.19f * Mth.cos(this.tail1.zRot) - 5.04f;
		this.tail2.x += -4.19f * Mth.sin(this.tail1.zRot);
		this.tail2.z -= 0.1f;
	}
}
