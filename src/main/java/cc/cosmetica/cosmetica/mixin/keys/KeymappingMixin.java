package cc.cosmetica.cosmetica.mixin.keys;

import cc.cosmetica.cosmetica.utils.SpecialKeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(KeyMapping.class)
public class KeymappingMixin {
	@Inject(at = @At("RETURN"), method = "click")
	private static void onClick(InputConstants.Key key, CallbackInfo ci) {
		SpecialKeyMapping.click(key);
	}

	@Inject(at = @At("RETURN"), method = "set")
	private static void onSet(InputConstants.Key key, boolean bl, CallbackInfo ci) {
		SpecialKeyMapping.set(key, bl);
	}

	@Inject(at = @At("HEAD"), method = "resetMapping")
	private static void beforeReset(CallbackInfo ci) {
		SpecialKeyMapping.clearMappings();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), method = "resetMapping")
	private static Object set(Map map, Object key, Object keyMapping) {
		return (key instanceof InputConstants.Key && keyMapping instanceof SpecialKeyMapping) ? SpecialKeyMapping.putMapping((InputConstants.Key) key, (SpecialKeyMapping) keyMapping) : map.put(key, keyMapping);
	}
}
