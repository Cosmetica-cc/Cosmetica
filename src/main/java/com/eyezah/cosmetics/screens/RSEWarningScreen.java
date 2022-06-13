package com.eyezah.cosmetics.screens;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.utils.Debug;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class RSEWarningScreen extends Screen {
	public RSEWarningScreen(@Nullable Screen parent) {
		super(new TextComponent("Region-Specific Effects Notice"));
		this.parent = parent;
	}

	@Nullable
	private final Screen parent;

	@Override
	protected void init() {
		int y = 2 * this.height / 3;

		this.addRenderableWidget(new Button(this.width / 2 + 20, y, 120, 20, new TranslatableComponent("cosmetica.options.yes"), bn -> setRSEAndClose(true)));

		this.addRenderableWidget(new Button(this.width / 2 - 140, y, 120, 20, new TranslatableComponent("cosmetica.options.no"), bn -> setRSEAndClose(false)));
	}

	private void setRSEAndClose(boolean enabled) {
		Thread requestThread = new Thread(() -> {
			if (Cosmetica.api.isAuthenticated()) {
				Cosmetica.api.updateUserSettings(ImmutableMap.of("doregioneffects", enabled)).ifSuccessfulOrElse(j -> Debug.info("Received successful response for RSE update."), Cosmetica.logErr("Error while setting region specific effects!"));
			}
			else {
				Cosmetica.LOGGER.warn("Could not update RSE because you are not authenticated!");
			}
		});

		requestThread.start();
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(PoseStack stack, int i, int j, float f) {
		this.renderBackground(stack);
		super.render(stack, i, j, f);

		stack.pushPose();
		stack.scale(1.5f, 1.5f, 0);
		drawCenteredString(stack, this.font, new TranslatableComponent("cosmetica.rsewarning.title"), this.width / 3, this.height / 3 - 30, 0xDADADA);
		stack.popPose();
		drawCenteredString(stack, this.font, new TranslatableComponent("cosmetica.rsewarning.description1"), this.width / 2, this.height / 2 - 10, 0xDADADA);
		drawCenteredString(stack, this.font, new TranslatableComponent("cosmetica.rsewarning.description2"), this.width / 2, this.height / 2, 0xDADADA);
		drawCenteredString(stack, this.font, new TranslatableComponent("cosmetica.rsewarning.description3"), this.width / 2, this.height / 2 + 10, 0xDADADA);
	}

	// only set this true if we are online
	public static boolean appearNextScreenChange = false;
}
