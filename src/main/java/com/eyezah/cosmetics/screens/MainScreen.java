package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CapeDisplay;
import cc.cosmetica.api.CapeServer;
import cc.cosmetica.api.UserSettings;
import cc.cosmetica.impl.CosmeticaWebAPI;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayerRenderer;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainScreen extends SulphateScreen {
	public MainScreen(Screen parentScreen, UserSettings settings, PlayerData data) {
		super(TextComponents.translatable("cosmetica.cosmeticaMainMenu"), parentScreen);

		this.cosmeticaOptions = new ServerOptions(settings.doShoulderBuddies(), settings.doHats(), settings.doBackBlings(), settings.hasPerRegionEffects(), settings.doLore());
		this.capeServerSettings = Cosmetica.map(settings.getCapeServerSettings(), CapeServer::getDisplay);
		this.capeServerSettingsForButtons = new ArrayList<>(settings.getCapeServerSettings().entrySet());
		Collections.sort(this.capeServerSettingsForButtons, Comparator.comparingInt(a -> a.getValue().getCheckOrder()));;

		this.setAnchorX(Anchor.LEFT, () -> this.width / 2);
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2 - 20);

		this.fakePlayer = new FakePlayer(Minecraft.getInstance(), UUID.fromString(Cosmetica.dashifyUUID(Minecraft.getInstance().getUser().getUuid())), Minecraft.getInstance().getUser().getName(), data, true);
	}

	private final ServerOptions cosmeticaOptions;
	private Map<String, CapeDisplay> capeServerSettings;
	private List<Map.Entry<String, CapeServer>> capeServerSettingsForButtons;
	private final FakePlayer fakePlayer;

	@Override
	protected void addWidgets() {
		this.addButton(150, 20, TextComponents.translatable("cosmetica.customizeCosmetics"), button ->
			this.minecraft.setScreen(new CustomiseCosmeticsScreen(this))
		);

		this.addButton(150, 20, TextComponents.translatable("cosmetica.capeServerSettings"), button ->
			this.minecraft.setScreen(new CapeServerSettingsScreen(this, this.capeServerSettings, this.capeServerSettingsForButtons))
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

		this.addButton(150, 20, TextComponents.translatable("options.skinCustomisation"), button ->
			this.minecraft.setScreen(new SkinCustomizationScreen(this, Minecraft.getInstance().options))
		);

		this.addDone();
	}

	void setCapeServerSettings(Map<String, CapeDisplay> settings) {
		this.capeServerSettings = settings;
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
		else {
			final int left = this.width / 2 - 5 * this.width / 16;
			final int top = this.height / 2 - this.height / 4 + 10;
			RenderSystem.getModelViewStack().pushPose();
			RenderSystem.getModelViewStack().scale(2.0f, 2.0f, 2.0f);
			renderFakePlayerInInventory(left, top, 30, (float)(left)*2 - mouseX, (float)(top - 50)*2 - mouseY, this.fakePlayer);
			RenderSystem.getModelViewStack().popPose();
		}
	}

	public static void renderFakePlayerInInventory(int i, int j, int k, float f, float g, FakePlayer fakePlayer) {
		float h = (float)Math.atan(f / 40.0F);
		float l = (float)Math.atan(g / 40.0F);
		PoseStack poseStack = RenderSystem.getModelViewStack();
		poseStack.pushPose();
		poseStack.translate(i, j, 1050.0D);
		poseStack.scale(1.0F, 1.0F, -1.0F);
		RenderSystem.applyModelViewMatrix();
		PoseStack poseStack2 = new PoseStack();
		poseStack2.translate(0.0D, 0.0D, 1000.0D);
		poseStack2.scale((float)k, (float)k, (float)k);
		Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
		Quaternion quaternion2 = Vector3f.XP.rotationDegrees(l * 20.0F);
		quaternion.mul(quaternion2);
		poseStack2.mulPose(quaternion);
		float m = fakePlayer.getYRotBody(0);
		float n = fakePlayer.getYRot(0);
		float o = fakePlayer.getXRot(0);
		float p = fakePlayer.getYRotHead(0);

		fakePlayer.yRotBody = 180.0F + h * 20.0F;
		fakePlayer.yRot = (180.0F + h * 40.0F);
		fakePlayer.xRot = (-l * 20.0F);
		fakePlayer.yRotHead = fakePlayer.getYRot(0);
		fakePlayer.yRotHead = fakePlayer.getYRot(0);
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		quaternion2.conj();
		entityRenderDispatcher.overrideCameraOrientation(quaternion2);
		entityRenderDispatcher.setRenderShadow(false);
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderSystem.runAsFancy(() -> {
			FakePlayerRenderer.render(poseStack2, fakePlayer, bufferSource, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, 15728880);
		});
		bufferSource.endBatch();
		entityRenderDispatcher.setRenderShadow(true);
		fakePlayer.yRotBody = m;
		fakePlayer.yRot = (n);
		fakePlayer.xRot = (o);
		fakePlayer.yRotHead = p;

		poseStack.popPose();
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
	}
}
