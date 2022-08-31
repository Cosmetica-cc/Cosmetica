package cc.cosmetica.cosmetica.mixin.textures;

import cc.cosmetica.cosmetica.utils.textures.CosmeticIconTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.HttpTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.HttpURLConnection;

@Mixin(HttpTexture.class)
public class HttpTextureMixin {
	@Inject(at = @At("HEAD"), method = "upload", cancellable = true)
	private void onUpload(NativeImage nativeImage, CallbackInfo ci) {
		if ((Object) this instanceof CosmeticIconTexture) {
			CosmeticIconTexture t = (CosmeticIconTexture) (Object) this;
			t.firstUpload(nativeImage, false);
			ci.cancel();
		}
	}

	@Redirect(method = "method_22801", at = @At(value = "INVOKE", target = "Ljava/net/HttpURLConnection;connect()V"))
	private void asfd(HttpURLConnection instance) {
		if ((Object) this instanceof CosmeticIconTexture) {
			// https://stackoverflow.com/questions/13670692/403-forbidden-with-java-but-not-web-browser
			instance.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		}
	}
}
