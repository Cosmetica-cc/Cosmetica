/*
 * Copyright 2022 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		return (key instanceof InputConstants.Key k && keyMapping instanceof SpecialKeyMapping spkm) ? SpecialKeyMapping.putMapping(k, spkm) : map.put(key, keyMapping);
	}
}
