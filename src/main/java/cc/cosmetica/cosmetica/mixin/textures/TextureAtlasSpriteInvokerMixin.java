package cc.cosmetica.cosmetica.mixin.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureAtlasSprite.class)
public interface TextureAtlasSpriteInvokerMixin {
	@Accessor
	NativeImage[] getMainImage();

	@Accessor
	@Mutable
	void setMainImage(NativeImage[] images);

	@Accessor
	TextureAtlas getAtlas();

	@Invoker
	void callUpload(int i, int j, NativeImage[] nativeImages);
}
