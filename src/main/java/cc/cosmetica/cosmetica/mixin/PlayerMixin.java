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

package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(at = @At(value = "RETURN"), method = "getDisplayName", cancellable = true)
	private void getDisplayName(CallbackInfoReturnable<Component> cir) {
		final Player player = ((Player) (Object) this);

		if (player.getLevel().isClientSide()) {
			String prefix = Cosmetica.getPlayerData(player.getUUID(), player.getName().getString(), false).prefix();
			String suffix = Cosmetica.getPlayerData(player.getUUID(), player.getName().getString(), false).suffix();

			cir.setReturnValue(new TextComponent(prefix).append(cir.getReturnValue()).append(suffix));
		}
	}
}
