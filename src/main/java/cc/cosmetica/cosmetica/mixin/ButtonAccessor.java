package cc.cosmetica.cosmetica.mixin;

import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Button.class)
public interface ButtonAccessor {
	@Accessor
	@Final
	@Mutable
	void setOnPress(Button.OnPress onPress);
}
