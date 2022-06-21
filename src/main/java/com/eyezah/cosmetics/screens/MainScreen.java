package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CapeServer;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.impl.CosmeticaWebAPI;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.Map;

public class MainScreen extends SulphateScreen {
	public MainScreen(Screen parentScreen, UserSettings settings) {
		super(TextComponents.translatable("cosmetica.cosmeticaMainMenu"), parentScreen);

		this.cosmeticaOptions = new ServerOptions(settings.doShoulderBuddies(), settings.doHats(), settings.doBackBlings(), settings.hasPerRegionEffects(), settings.doLore());
		this.capeServerSettings = settings.getCapeServerSettings();
		this.setAnchorX(Anchor.LEFT, () -> this.width / 2);
	}

	private final ServerOptions cosmeticaOptions;
	private final Map<String, CapeServer> capeServerSettings;

	@Override
	protected void addWidgets() {
		this.addButton(150, 20, TextComponents.translatable("cosmetica.customizeCosmetics"), button ->
			this.minecraft.setScreen(new CustomiseCosmeticsScreen(this))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.capeServerSettings"), button ->
			this.minecraft.setScreen(new CapeServerSettingsScreen(this, this.capeServerSettings))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.cosmeticaSettings"), button ->
			this.minecraft.setScreen(new CosmeticaSettingsScreen(this, this.cosmeticaOptions))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.openWebPanel"), button -> {
			try {
				Minecraft.getInstance().keyboardHandler.setClipboard(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
				Util.getPlatform().openUri(Cosmetica.websiteHost + "/manage?" + ((CosmeticaWebAPI)Cosmetica.api).getMasterToken());
			} catch (Exception e) {
				throw new RuntimeException("bruh", e);
			}
		});

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
