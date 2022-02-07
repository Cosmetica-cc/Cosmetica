package com.eyezah.cosmetics;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;

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
			textures.put(Type.CAPE, new MinecraftProfileTexture(Cosmetics.apiUrl + "/get/cloak?uuid=" + profile.getId() + "&username=" + profile.getName() + "&timestamp=" + System.currentTimeMillis(), new HashMap<>()));
		}

		return textures;
	}

	@Override
	public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
		return this.original.fillProfileProperties(profile, requireSecure);
	}
}
