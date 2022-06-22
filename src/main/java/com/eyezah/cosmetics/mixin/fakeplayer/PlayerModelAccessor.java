package com.eyezah.cosmetics.mixin.fakeplayer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerModel.class)
public interface PlayerModelAccessor {
	@Accessor
	ModelPart getCloak();

	@Accessor
	ModelPart getEar();
}
