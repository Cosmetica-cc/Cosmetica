/*
 * Copyright 2022 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica.screens;

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.cosmetics.PlayerData;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayerRenderer;
import cc.cosmetica.cosmetica.screens.fakeplayer.MouseTracker;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class PlayerRenderScreen extends SulphateScreen {
	protected PlayerRenderScreen(Component title, @Nullable Screen parent, FakePlayer fakePlayer) {
		super(title, parent);

		this.fakePlayer = fakePlayer;
		this.rseNotif = new ResourceLocation("cosmetica", "textures/gui/icon/wtf.png");
	}

	protected final FakePlayer fakePlayer;
	// for the rotation
	private final MouseTracker mouseTracker = new MouseTracker();
	private boolean spinning = false;

	private int playerLeft = 0;
	private double transitionProgress = 0;

	protected int initialPlayerLeft;
	protected int deltaPlayerLeft;
	private long lastTimeMillis = System.currentTimeMillis();

	protected int rightMouseGrabBuffer = 51;
	protected int leftMouseGrabBuffer = 100000; // arbitrary big number

	private final ResourceLocation rseNotif;

	@Override
	public void tick() {
		this.fakePlayer.tickCount++;

		if (this.minecraft.level == null) {
			this.minecraft.getProfiler().push("textures");
			this.minecraft.getTextureManager().tick();
			this.minecraft.getProfiler().pop();
		}
	}

	protected void setTransitionProgress(double progress) {
		this.transitionProgress = progress;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		this.updateSpin(mouseX, mouseY);
		this.renderFakePlayer(mouseX, mouseY);
	}

	protected void renderRSENotif(PoseStack matrices, int mouseX, int mouseY) {
		Component regionEffectsMsg = null;
		PlayerData data = this.fakePlayer.getData();

		if (data.upsideDown()) {
			regionEffectsMsg = TextComponents.translatable("cosmetica.rsenotice.australian");
		}
		else if (!data.prefix().isEmpty()) {
			regionEffectsMsg = TextComponents.formattedTranslatable("cosmetica.rsenotice.prefix", data.prefix().trim());
		}
		else if (!data.suffix().isEmpty()) {
			regionEffectsMsg = TextComponents.formattedTranslatable("cosmetica.rsenotice.suffix", data.suffix().trim());
		}
		else if (Optional.ofNullable(data.rightShoulderBuddy()).map(b -> b.id().equals("-sheep")).orElse(false)
				|| Optional.ofNullable(data.leftShoulderBuddy()).map(b -> b.id().equals("-sheep")).orElse(false)) {
			regionEffectsMsg = TextComponents.translatable("cosmetica.rsenotice.kiwi");
		}

		if (regionEffectsMsg != null) {
			final int top = this.height / 2 - 60;
			final int left = this.playerLeft + 25;
			final int size = 16;
			Cosmetica.renderTexture(matrices.last().pose(), this.rseNotif, left, left + size, top, top + size, 0);

			if (mouseY >= top && mouseY <= top + size && mouseX >= left && mouseX <= left + size) {
				this.renderTooltip(matrices, this.font.split(regionEffectsMsg, Math.max(this.width / 2, 170)), mouseX, mouseY);
			}
		}
	}

	protected void updateSpin(int mouseX, int mouseY) {
		this.mouseTracker.update(mouseX, mouseY);
		this.playerLeft = this.initialPlayerLeft + (int) (this.transitionProgress * this.deltaPlayerLeft);

		if (this.mouseTracker.hasTrackingPosData()) {
			// track the mouse x since mouse down
			if (this.mouseTracker.wasMousePressed()) {
				this.spinning = mouseX > this.playerLeft - this.leftMouseGrabBuffer && mouseX < this.playerLeft + this.rightMouseGrabBuffer;
			}
			else if (!this.mouseTracker.isMouseDown()) {
				this.spinning = false;
			}

			if (this.spinning) {
				if (this.mouseTracker.wasMouseDown()) {
					this.fakePlayer.yRot -= this.mouseTracker.deltaMouseX();
					this.fakePlayer.yRotBody -= this.mouseTracker.deltaMouseX();

					if (this.fakePlayer.yRot > 180.0f) this.fakePlayer.yRot = -180.0f;
					if (this.fakePlayer.yRotBody > 180.0f) this.fakePlayer.yRotBody = -180.0f;

					if (this.fakePlayer.yRot < -180.0f) this.fakePlayer.yRot = 180.0f;
					if (this.fakePlayer.yRotBody < -180.0f) this.fakePlayer.yRotBody = 180.0f;
				}
			}
		}
	}

	protected void renderFakePlayer(int mouseX, int mouseY) {
		final int top = this.height / 2 + 55;

		renderFakePlayerInMenu(this.playerLeft, top, 30.0f, (float) this.playerLeft - mouseX, (float)(top - 90) - mouseY, this.fakePlayer);

		long currentTimeMillis = System.currentTimeMillis();
		double tickDelta = 0.02 * (double) (currentTimeMillis - this.lastTimeMillis); // to tick time
		this.lastTimeMillis = currentTimeMillis;

		// transition effect
		if (this.transitionProgress < 1) {
			this.transitionProgress += tickDelta * (0.1 - this.transitionProgress * 0.055); // times tick delta since the value in brackets is how much I want it to change each tick.

			if (this.transitionProgress > 1) {
				this.transitionProgress = 1;
			}
		}
	}

	protected double getTransitionProgress() {
		return this.transitionProgress;
	}

	public void onOpen() {
		this.lastTimeMillis = System.currentTimeMillis();
	}

	void refetchPlayerData() {
		new Thread(() -> {
			this.fakePlayer.setData(Cosmetica.getPlayerData(this.fakePlayer.getUUID(), this.fakePlayer.getName(), true));
		}).start();
	}

	void setPlayerData(PlayerData data) {
		this.fakePlayer.setData(data);
	}

	public static void renderFakePlayerInMenu(int left, int top, float extraScale, float lookX, float lookY, FakePlayer fakePlayer) {
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
		viewStack.scale(extraScale, extraScale, extraScale);
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
