package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Redirect(method = "setupRotations", at = @At(value = "INVOKE", target = "Lnet/minecraft/ChatFormatting;stripFormatting(Ljava/lang/String;)Ljava/lang/String;"))
	private String redirectPlayersToOnlyOurCheck(String name, LivingEntity entity) {
		return entity instanceof AbstractClientPlayer ? "Dinnerbone" : ChatFormatting.stripFormatting(name);
	}

	@Redirect(method = "setupRotations", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isModelPartShown(Lnet/minecraft/world/entity/player/PlayerModelPart;)Z"))
	private boolean checkAustralians(Player player, PlayerModelPart part) {
		String deformattedReal = ChatFormatting.stripFormatting(player.getName().getString());
		boolean real = (deformattedReal.equals("Dinnerbone") || deformattedReal.equals("Grumm")); // if they're dinnerbone or grumm use normal
		return real && player.isModelPartShown(part)
				|| !real && Cosmetica.shouldRenderUpsideDown(player); // TODO should this also use player.isModelPartShown?
	}

	@Inject(
			method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void shouldShowName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (Cosmetica.getConfig().shouldShowNametagInThirdPerson() && entity == Minecraft.getInstance().getCameraEntity()) cir.setReturnValue(Minecraft.renderNames());
	}
}
