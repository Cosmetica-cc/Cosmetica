package cc.cosmetica.cosmetica.mixin;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.ThreadPool;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundPlayerInfoUpdatePacket.Action.class)
public class ClientboundPlayerInfoUpdatePacketActionMixin {
	// This captures only other players
	// See comment in Cosmetica.forwardPublicUserInfoToNametag
	@Inject(at = @At("RETURN"), method = "method_46342")
	private static void afterAddPlayer(ClientboundPlayerInfoUpdatePacket.EntryBuilder entryBuilder, RegistryFriendlyByteBuf registryFriendlyByteBuf, CallbackInfo ci) {
		final GameProfile profile = entryBuilder.profile;

		if (profile != null) {
			Cosmetica.runOffthread(() -> Cosmetica.forwardPublicUserInfoToNametag(profile), ThreadPool.GENERAL_THREADS);
		}
	}
}
