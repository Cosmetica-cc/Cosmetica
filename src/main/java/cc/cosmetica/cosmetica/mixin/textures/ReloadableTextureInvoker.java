package cc.cosmetica.cosmetica.mixin.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ReloadableTexture.class)
public interface ReloadableTextureInvoker {
    @Invoker
    void invokeDoLoad(NativeImage image, boolean blur, boolean clamp);
}
