package com.eyezah.cosmetics;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import com.eyezah.cosmetics.utils.Debug;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.platform.NativeImage;

public class SessionWrapperService implements MinecraftSessionService {
	public SessionWrapperService(MinecraftSessionService original) {
		this.original = original;
	}

	private final MinecraftSessionService original;

	@Override
	public void joinServer(GameProfile profile, String authenticationToken, String serverId)
			throws AuthenticationException {
		this.original.joinServer(profile, authenticationToken, serverId);
	}

	@Override
	public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address)
			throws AuthenticationUnavailableException {
		return this.original.hasJoinedServer(user, serverId, address);
	}

	@Override
	public Map<Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
		Map<Type, MinecraftProfileTexture> textures = this.original.getTextures(profile, requireSecure);

		if (!textures.isEmpty()) { // if is a request that is returning a result
			textures.put(Type.CAPE, new MinecraftProfileTexture(Cosmetica.apiServerHost + "/get/cloak?uuid=" + profile.getId() + "&username=" + profile.getName() + "&timestamp=" + System.currentTimeMillis() + "&token=" + Authentication.getToken(), new HashMap<>()));
		}

		return textures;
	}

	@Override
	public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
		return this.original.fillProfileProperties(profile, requireSecure);
	}

	public static NativeImage processBadCapes(NativeImage image) {
		int srcWidth = image.getWidth();
		int srcHeight = image.getHeight();

		if (srcHeight == srcWidth) {
			// imagine using square textures in 2022
			return image;
		} else if (22 * srcWidth == 46 * srcHeight) {
			// very bad format from a very bad server
			int height = 32;

			// get the right dimensions
			while (height < srcHeight) {
				height *= 2;
			}

			NativeImage result = new NativeImage(height * 2, height, true);
			result.copyFrom(image);

			String imgid = "cape_" + System.currentTimeMillis();
			Debug.dumpImages(imgid + "_src", true, image);
			Debug.dumpImages(imgid + "_formatted", true, image);
			return result;
		} else {
			return image;
		}
	}
}
