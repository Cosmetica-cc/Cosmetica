package cc.cosmetica.cosmetica.mixin.textures;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NativeImage.class)
public interface NativeImageAccessorMixin {
	@Accessor
	long getPixels();
}
