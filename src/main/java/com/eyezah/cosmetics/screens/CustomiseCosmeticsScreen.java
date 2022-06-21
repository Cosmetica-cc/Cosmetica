package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public class CustomiseCosmeticsScreen extends SulphateScreen {
	protected CustomiseCosmeticsScreen(Screen parentScreen) {
		super(TextComponents.translatable("cosmetica.customizeCosmetics"), parentScreen);

		this.setAnchorX(Anchor.LEFT, () -> this.width / 2);
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2 - 40);
	}

	@Override
	protected void addWidgets() {
		this.addDone();
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		if (this.minecraft.player != null) {
			final int left = this.width / 2 - 5 * this.width / 16;
			final int top = this.height / 2 - this.height / 4 + 10;
			RenderSystem.getModelViewStack().pushPose();
			RenderSystem.getModelViewStack().scale(2.0f, 2.0f, 2.0f);
			InventoryScreen.renderEntityInInventory(left, top, 30, (float)(left)*2 - mouseX, (float)(top - 50)*2 - mouseY, this.minecraft.player);
			RenderSystem.getModelViewStack().popPose();
		}
	}
}
