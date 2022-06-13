package com.eyezah.cosmetics.mixin;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.ThreadPool;
import com.eyezah.cosmetics.utils.Response;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level {
	@Shadow @Final private Minecraft minecraft;

	protected MixinClientLevel(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
		super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l, i);
	}

	@Inject(at = @At("RETURN"), method = "tick")
	private void onClientTick(BooleanSupplier hasTimeLeft, CallbackInfo info) {
		if (this.getGameTime() % 600 == 0) { // every 30 seconds in africa
			Cosmetica.runOffthread(() -> {
				if (this.minecraft.isLocalServer()) {
					Cosmetica.safari(new InetSocketAddress("127.0.0.1", 25565), false);
				}
				if (this.minecraft.getConnection().getConnection().getRemoteAddress() instanceof InetSocketAddress ip) {
					Cosmetica.safari(ip, false);
				}
			}, ThreadPool.GENERAL_THREADS);
		}
	}
}
