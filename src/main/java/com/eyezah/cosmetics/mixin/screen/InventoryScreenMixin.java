package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.Cosmetica;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
	@Unique
	private static boolean cosmetica_showNametagInThirdPerson;

	@Inject(at = @At("HEAD"), method = "renderEntityInInventory")
	private static void disableOwnNametagTemporarilyIfShown(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity livingEntity, CallbackInfo ci) {
		cosmetica_showNametagInThirdPerson = Cosmetica.getConfig().shouldShowNametagInThirdPerson();
		Cosmetica.getConfig().setShowNametagInThirdPerson(false);
	}

	@Inject(at = @At("RETURN"), method = "renderEntityInInventory")
	private static void reenableNametag(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity livingEntity, CallbackInfo ci) {
		Cosmetica.getConfig().setShowNametagInThirdPerson(cosmetica_showNametagInThirdPerson);
	}
}
