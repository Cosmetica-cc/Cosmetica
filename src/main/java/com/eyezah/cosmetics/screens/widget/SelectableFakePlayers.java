package com.eyezah.cosmetics.screens.widget;

import com.eyezah.cosmetics.cosmetics.PlayerData;
import com.eyezah.cosmetics.screens.PlayerRenderScreen;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;

public class SelectableFakePlayers extends AbstractWidget {
	public SelectableFakePlayers(int x, int y, int w, int h, Component component) {
		super(x, y, w, h, component);
		this.separation = w + 4;
	}

	private final List<FakePlayer> players = new ArrayList<>();
	private int scale = 1;
	private int selected = -1;
	private int separation;
	private IntConsumer onSelect;

	public void setScale(int scale) {
		this.scale = scale;
	}

	public void setOnSelect(@Nullable IntConsumer onSelect) {
		this.onSelect = onSelect;
	}

	public int getSelected() {
		return this.selected;
	}

	public void setSeparation(int separation) {
		this.separation = separation;
	}

	public void createFakePlayer(UUID uuid, String name, PlayerData data) {
		this.players.add(new FakePlayer(Minecraft.getInstance(), uuid, name, data, data.slim()));
	}

	@Override
	public void updateNarration(NarrationElementOutput narration) {
		this.defaultButtonNarrationText(narration);
	}

	@Override
	public boolean mouseClicked(double clickX, double clickY, int i) {
		if (clickY >= this.y && clickY < (double)(this.y + this.height) && i == 0) {
			int x = this.x;
			int j = 0;

			for (FakePlayer player : this.players) {
				if (clickX >= x && clickX < x + this.width) {
					this.selected = j;
				}

				x += this.separation;
				j++;
			}

			return true;
		}

		return false;
	}

	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		int x = this.x;
		int i = 0;

		for (FakePlayer player : this.players) {
			if (this.selected == i) {
				final int x1 = x + this.width;
				final int y1 = y + this.height;

				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bb = tesselator.getBuilder();

				RenderSystem.disableTexture();
				RenderSystem.setShader(GameRenderer::getPositionShader);
				float shade = 1.0F;
				RenderSystem.setShaderColor(shade, shade, shade, 1.0F);
				bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bb.vertex(x, y1, 0.0D).endVertex();
				bb.vertex(x1, y1, 0.0D).endVertex();
				bb.vertex(x1, this.y, 0.0D).endVertex();
				bb.vertex(x, this.y, 0.0D).endVertex();

				tesselator.end();
				RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
				bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bb.vertex(x + 1, y1 - 1, 0.0D).endVertex();
				bb.vertex(x1 - 1, y1 - 1, 0.0D).endVertex();
				bb.vertex(x1 - 1, this.y + 1, 0.0D).endVertex();
				bb.vertex(x + 1, this.y + 1, 0.0D).endVertex();
				tesselator.end();
				RenderSystem.enableTexture();
			}

			PlayerRenderScreen.renderFakePlayerInMenu(x, this.y, this.scale, x - mouseX, (float)(this.y - 90) - mouseY, player);
			x += this.separation;
			i++;
		}
	}
}
