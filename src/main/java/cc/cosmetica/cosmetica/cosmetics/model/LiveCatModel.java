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

	void lieDown(float amount, float amountTail) {
		this.leftFrontLeg.xRot = (float)-Math.PI / 3f;
		this.leftFrontLeg.y += 3f;

		this.rightFrontLeg.xRot = (float)-Math.PI / 3f;
		this.rightFrontLeg.y += 3f;

		this.leftHindLeg.xRot = (float)Math.PI / 6f;
		this.rightHindLeg.xRot = (float)Math.PI / 6f;

		this.tail1.zRot = (float) Math.PI;
		this.tail2.xRot = (float) Math.PI;
		this.tail2.z += 1;
		this.tail2.y -= 8;
	}
}
