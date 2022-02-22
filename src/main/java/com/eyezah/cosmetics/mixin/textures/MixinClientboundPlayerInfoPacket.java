package com.eyezah.cosmetics.mixin.textures;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.eyezah.cosmetics.api.PlayerData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.apache.commons.codec.binary.Base64;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;
import java.util.Collection;
import java.util.List;

@Mixin(ClientboundPlayerInfoPacket.class)
public class MixinClientboundPlayerInfoPacket {
	@Shadow @Final private List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V")
	private void afterConstructClient(FriendlyByteBuf friendlyByteBuf, CallbackInfo info) {
		if (Minecraft.getInstance().getMinecraftSessionService() instanceof YggdrasilMinecraftSessionService ygg) {
			MixinYggdrasilAuthenticationServiceInvoker yggi = (MixinYggdrasilAuthenticationServiceInvoker) ygg.getAuthenticationService();

			for (ClientboundPlayerInfoPacket.PlayerUpdate data : this.entries) {
				GameProfile existing = data.getProfile();
				URL url = CosmeticaSkinManager.getCosmeticaURL(null, existing, false);

				if (url != null) {
					try {
						CosmeticaSkinManager.CosmeticaProfilePropertiesResponse cmaResponse = yggi.invokeMakeRequest(url, null, CosmeticaSkinManager.CosmeticaProfilePropertiesResponse.class);

						// A ton of nonsense to not use our textures if the server is replacing the skin.
						// starting with this code to get the original skin
						var textures = existing.getProperties().get("textures");

						for (Property property : textures) {
							if (property.getName().equals("textures")) {
								JsonObject jo = JsonParser.parseString(property.getValue()).getAsJsonObject();
								String originalSkinURL = jo.get("SKIN").getAsJsonObject().get("url").getAsString();

								// and finally checking it against our "original skin" requirement
								if (cmaResponse.getOriginalSkin().equals(originalSkinURL)) {
									// now we can replace with our game profile.
									GameProfile cosmeticaProfile = new GameProfile(cmaResponse.getId(), cmaResponse.getName());
									cosmeticaProfile.getProperties().putAll(cmaResponse.getProperties());
									// minecraft does this so may as well. I think it does nothing really
									existing.getProperties().putAll(cmaResponse.getProperties());
									// set our one
									((MixinPlayerUpdateAccessor) data).setProfile(cosmeticaProfile);
								}

								break;
							}
						}
					} catch (Exception e) {
						Cosmetica.LOGGER.error("Error adding cosmetica skin service to multiplayer skins", e);
					}
				}
			}
		}
	}
}
