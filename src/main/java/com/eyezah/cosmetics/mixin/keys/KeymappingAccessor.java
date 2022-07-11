package com.eyezah.cosmetics.mixin.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeymappingAccessor {
	@Accessor
	void setClickCount(int clickCount);

	@Accessor
	int getClickCount();

	@Accessor
	InputConstants.Key getKey();

	@Accessor
	static Map<String, KeyMapping> getALL() {
		throw new IllegalStateException("Mixin failed to load.");
	}
}
