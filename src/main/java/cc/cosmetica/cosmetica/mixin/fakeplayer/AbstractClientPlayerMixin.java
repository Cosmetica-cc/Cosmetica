package cc.cosmetica.cosmetica.mixin.fakeplayer;

import cc.cosmetica.cosmetica.screens.fakeplayer.Playerish;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements Playerish {
	public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile, ProfilePublicKey ppk) {
		super(level, blockPos, f, gameProfile, ppk);
	}

	@Override
	public int getLifetime() {
		return this.tickCount;
	}

	@Override
	public int getPseudoId() {
		return this.getId();
	}

	@Override
	public boolean isSneaking() {
		return this.isCrouching();
	}
}
