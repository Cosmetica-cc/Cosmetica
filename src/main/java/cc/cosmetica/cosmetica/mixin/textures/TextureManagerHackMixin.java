package cc.cosmetica.cosmetica.mixin.textures;

import cc.cosmetica.cosmetica.utils.TextureManagerHack;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(TextureManager.class)
public class TextureManagerHackMixin implements TextureManagerHack {

    @Shadow @Final private Map<ResourceLocation, AbstractTexture> byPath;

    @Override
    public AbstractTexture cosmetica$getTextureOr(ResourceLocation location, AbstractTexture or) {
        AbstractTexture abstractTexture = this.byPath.get(location);
        if (abstractTexture != null) {
            return abstractTexture;
        } else {
            return or;
        }
    }
}
