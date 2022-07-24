package com.eyezah.cosmetics.mixin.fakeplayer;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(at = @At("RETURN"), method = "pick")
	private void cinder(float yawProbably, CallbackInfo info) {
		Entity entity = this.minecraft.getCameraEntity();

		if (entity != null) {
			if (this.minecraft.level != null) {
				this.minecraft.getProfiler().push("snipe");
				Cosmetica.farPickPlayer = null;

				double maxDist = 6.0f;
				Cosmetica.farPickHitResult = entity.pick(maxDist, yawProbably, false);
				Vec3 eyePosition = entity.getEyePosition(yawProbably);

				double maxDistSqr = maxDist;
				maxDistSqr *= maxDistSqr;

				if (Cosmetica.farPickHitResult != null) {
					maxDistSqr = Cosmetica.farPickHitResult.getLocation().distanceToSqr(eyePosition);
				}

				Vec3 view = entity.getViewVector(1.0F);
				Vec3 castTowards = eyePosition.add(view.x * maxDist, view.y * maxDist, view.z * maxDist);

				final float inflation = 1.0F;
				AABB selectionBoundingBox = entity.getBoundingBox().expandTowards(view.scale(maxDist)).inflate(inflation, inflation, inflation);
				EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, eyePosition, castTowards, selectionBoundingBox, e -> !e.isSpectator() && e.isPickable(), maxDistSqr);

				if (entityHitResult != null) {
					Entity entity2 = entityHitResult.getEntity();
					Vec3 resultLocation = entityHitResult.getLocation();
					double distance = eyePosition.distanceToSqr(resultLocation);

					if (distance < maxDistSqr || Cosmetica.farPickHitResult == null) {
						Cosmetica.farPickHitResult = entityHitResult;

						if (entity2 instanceof Player player) { // vanilla crosshair pick: entity2 instanceof LivingEntity || entity2 instanceof ItemFrame
							Cosmetica.farPickPlayer = player;
						}
					}
				}

				this.minecraft.getProfiler().pop();
			}
		}
	}
}
