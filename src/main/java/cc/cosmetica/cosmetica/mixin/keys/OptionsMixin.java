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

import cc.cosmetica.cosmetica.Cosmetica;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Options.class)
public class OptionsMixin {
	@Shadow
	@Final
	@Mutable
	public KeyMapping[] keyMappings;

	@Inject(at = @At("HEAD"), method = "load")
	private void beforeLoad(CallbackInfo ci) {
		List<KeyMapping> moddedKeyMappings = new ArrayList<>();
		Cosmetica.registerKeyMappings(moddedKeyMappings);

		KeyMapping[] newKeyMappings = new KeyMapping[this.keyMappings.length + moddedKeyMappings.size()];
		System.arraycopy(keyMappings, 0, newKeyMappings, 0, keyMappings.length);
		System.arraycopy(moddedKeyMappings.toArray(new KeyMapping[0]), 0, newKeyMappings, keyMappings.length, moddedKeyMappings.size());
		keyMappings = newKeyMappings;
	}
}
