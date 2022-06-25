package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.SulphateScreen;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayerRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class PlayerRenderScreen extends SulphateScreen {
	protected PlayerRenderScreen(Component title, @Nullable Screen parent, FakePlayer fakePlayer) {
		super(title, parent);

		this.fakePlayer = fakePlayer;
	}

	protected final FakePlayer fakePlayer;

	// for the rotation
	private float lastMouseX = 0;
	private boolean wasMouseDown = false;

	private int playerLeft = 0;
	private double transitionProgress = 0;
	private double nextTransitionProgress = 0;

	protected int initialPlayerLeft;
	protected int deltaPlayerLeft;

	@Override
	public void tick() {
		this.fakePlayer.tickCount++;

		if (this.minecraft.level == null) {
			this.minecraft.getProfiler().push("textures");
			this.minecraft.getTextureManager().tick();
			this.minecraft.getProfiler().pop();
		}

		// transition effect
		if (this.transitionProgress < 1) {
			this.transitionProgress = this.nextTransitionProgress;

			if (this.nextTransitionProgress < 1) {
				this.nextTransitionProgress += (0.08 - this.transitionProgress * 0.05);
			}
			else {
				this.nextTransitionProgress = 1;
			}
		}
		else {
			this.transitionProgress = 1;
			this.nextTransitionProgress = 1;
		}
	}

	protected void setTransitionProgress(double progress) {
		this.transitionProgress = progress;
		this.nextTransitionProgress = progress;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		this.playerLeft = this.initialPlayerLeft + (int) (Mth.lerp(delta, this.transitionProgress, this.nextTransitionProgress) * this.deltaPlayerLeft);

		if (GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
			if (mouseX < this.playerLeft + 40 && (!(this instanceof CustomiseCosmeticsScreen) || mouseX > this.playerLeft - 40) || this.wasMouseDown) {
				if (this.wasMouseDown) {
					this.fakePlayer.yRot -= (mouseX - this.lastMouseX);
					this.fakePlayer.yRotBody -= (mouseX - this.lastMouseX);

					if (this.fakePlayer.yRot > 180.0f) this.fakePlayer.yRot = -180.0f;
					if (this.fakePlayer.yRotBody > 180.0f) this.fakePlayer.yRotBody = -180.0f;

					if (this.fakePlayer.yRot < -180.0f) this.fakePlayer.yRot = 180.0f;
					if (this.fakePlayer.yRotBody < -180.0f) this.fakePlayer.yRotBody = 180.0f;
				}

				this.wasMouseDown = true;
				this.lastMouseX = mouseX;
			}
		}
		else {
			this.wasMouseDown = false;
		}

		final int top = this.height / 2 + 55;
		renderFakePlayerInMenu(this.playerLeft, top, 30, (float) this.playerLeft - mouseX, (float)(top - 90) - mouseY, this.fakePlayer);
	}

	protected double getTransitionProgress() {
		return this.transitionProgress;
	}

	void refetchPlayerData() {
		new Thread(() -> {
			this.fakePlayer.setData(Cosmetica.getPlayerData(this.fakePlayer.getUUID(), this.fakePlayer.getName(), true));
		}).start();
	}

	void setPlayerData(PlayerData data) {
		this.fakePlayer.setData(data);
	}

	public static void renderFakePlayerInMenu(int left, int top, int extraScale, float lookX, float lookY, FakePlayer fakePlayer) {
		float h = (float)Math.atan(lookX / 40.0F);
		float l = (float)Math.atan(lookY / 40.0F);
		PoseStack stack = RenderSystem.getModelViewStack();

		stack.pushPose();
		stack.translate(left, top, 1050.0D);
		stack.scale(2.0F, 2.0F, -1.0F);
		RenderSystem.applyModelViewMatrix();

		// view
		PoseStack viewStack = new PoseStack();
		viewStack.translate(0.0D, 0.0D, 1000.0D);
		viewStack.scale((float)extraScale, (float)extraScale, (float)extraScale);
		Quaternion zRotation = Vector3f.ZP.rotationDegrees(180.0F);
		Quaternion xRotation = Vector3f.XP.rotationDegrees(l * 20.0F);
		zRotation.mul(xRotation);
		viewStack.mulPose(zRotation);

		float rotationBody = 180.0F + h * 20.0F;
		float rotationMain = 180.0F + h * 40.0F;
		fakePlayer.yRotBody += rotationBody;
		fakePlayer.yRot += rotationMain;
		fakePlayer.xRot = -l * 20.0F;
		fakePlayer.yRotHead = fakePlayer.getYRot(0);
		Lighting.setupForEntityInInventory();

		xRotation.conj();
		FakePlayerRenderer.cameraOrientation = xRotation;
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		RenderSystem.runAsFancy(() -> {
			FakePlayerRenderer.render(viewStack, fakePlayer, bufferSource, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, 15728880);
		});
		bufferSource.endBatch();

		fakePlayer.yRotBody -= rotationBody;
		fakePlayer.yRot -= rotationMain;

		stack.popPose();
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
	}
}
