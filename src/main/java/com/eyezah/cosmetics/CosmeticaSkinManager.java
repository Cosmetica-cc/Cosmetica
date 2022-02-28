package com.eyezah.cosmetics;

import com.eyezah.cosmetics.mixin.textures.MixinNativeImageAccessor;
import com.eyezah.cosmetics.utils.Debug;
import com.eyezah.cosmetics.utils.NativeTexture;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CosmeticaSkinManager {
	private static Map<ResourceLocation, NativeTexture> capeTextures = new HashMap<>();

	public static void clearCaches() {
		capeTextures = new HashMap<>();
	}

	private static String pathify(String id) {
		StringBuilder result = new StringBuilder();

		for (char c : id.toCharArray()) {
			if (c == '+') {
				result.append(".");
			}
			else if (Character.isUpperCase(c)) {
				result.append("_").append(Character.toLowerCase(c));
			}
			else {
				result.append(c);
			}
		}

		return result.toString();
	}

	public static ResourceLocation processCape(JsonObject object) {
		ResourceLocation id = new ResourceLocation("cosmetica", pathify(object.get("id").getAsString()));

		if (!capeTextures.containsKey(id)) {
			try {
				NativeTexture cloakTex = new NativeTexture(object.get("image").getAsString().substring(22), true);
				RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getTextureManager().register(id, cloakTex));
				capeTextures.put(id, cloakTex);
			} catch (IOException e) {
				Cosmetica.LOGGER.error("Error loading cape texture", e);
				return null;
			}
		}

		return id;
	}
}
