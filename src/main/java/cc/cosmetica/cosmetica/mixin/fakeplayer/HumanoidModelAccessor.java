package cc.cosmetica.cosmetica.mixin.fakeplayer;

import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HumanoidModel.class)
public interface HumanoidModelAccessor {
	@Invoker
	float invokeRotlerpRad(float f, float g, float h);

	@Invoker
	float invokeQuadraticArmUpdate(float f);
}
