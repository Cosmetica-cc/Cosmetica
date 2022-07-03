package com.eyezah.cosmetics.screens.widget;

import com.eyezah.cosmetics.cosmetics.model.CosmeticStack;
import com.eyezah.cosmetics.screens.PlayerRenderScreen;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class SelectableFakePlayers<T> extends AbstractWidget {
	public SelectableFakePlayers(int x, int y, int w, int h, CosmeticStack<T> override, Component component) {
		super(x, y, w, -1 * h, component);
		this.separation = w + 4;
		this.overrider = override;
	}

	private final List<Tuple<FakePlayer, T>> players = new ArrayList<>();
	private float scale = 30.0f;
	private int selected = -1;
	private int separation;
	private IntConsumer onSelect;
	private final CosmeticStack<T> overrider;

	public void setScale(float scale) {
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

	public void addFakePlayer(FakePlayer player, T override) {
		this.players.add(new Tuple<>(player, override));
	}

	@Override
	public void updateNarration(NarrationElementOutput narration) {
		this.defaultButtonNarrationText(narration);
	}

	@Override
	public boolean mouseClicked(double clickX, double clickY, int i) {
		if (this.players.size() < 2) return false;

		if (clickY < this.y && clickY >= (double)(this.y + this.height) && i == 0) {
			int x = this.x - this.width / 2;
			int j = 0;

			for (var player : this.players) {
				if (clickX >= x && clickX < x + this.width) {
					this.selected = j;
					return true;
				}

				x += this.separation;
				j++;
			}
		}

		return false;
	}

	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		int x = this.x;
		int i = 0;

		for (var player : this.players) {
			if (this.selected == i) {
				final int x1 = x + (this.width / 2);
				final int y0 = this.y + this.height + 8;
				final int y1 = this.y + 8;
				final int x0 = x - (this.width / 2);

				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bb = tesselator.getBuilder();

				RenderSystem.disableTexture();
				RenderSystem.setShader(GameRenderer::getPositionShader);
				float shade = 1.0F;
				RenderSystem.setShaderColor(shade, shade, shade, 1.0F);
				bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bb.vertex(x0, y1, 0.0D).endVertex();
				bb.vertex(x1, y1, 0.0D).endVertex();
				bb.vertex(x1, y0, 0.0D).endVertex();
				bb.vertex(x0, y0, 0.0D).endVertex();

				tesselator.end();
				RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
				bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bb.vertex(x0 + 1, y1 - 1, 0.0D).endVertex();
				bb.vertex(x1 - 1, y1 - 1, 0.0D).endVertex();
				bb.vertex(x1 - 1, y0 + 1, 0.0D).endVertex();
				bb.vertex(x0 + 1, y0 + 1, 0.0D).endVertex();
				tesselator.end();
				RenderSystem.enableTexture();
			}

			this.overrider.push(player.getB());
			PlayerRenderScreen.renderFakePlayerInMenu(x, this.y, this.scale, x - mouseX, (float)(this.y - 90) - mouseY, player.getA());
			this.overrider.pop();

			x += this.separation;
			i++;
		}
	}
}
