package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"), method = "render")
	private Component getDisplayName(Entity entity) {
		if (entity instanceof Player) {
			String prefix = Cosmetica.getPlayerData(entity.getUUID(), entity.getName().getString(), false).prefix();
			String suffix = Cosmetica.getPlayerData(entity.getUUID(), entity.getName().getString(), false).suffix();

			return Component.literal(prefix).append(entity.getDisplayName()).append(suffix);
		} else {
			return entity.getDisplayName();
		}
	}
}
