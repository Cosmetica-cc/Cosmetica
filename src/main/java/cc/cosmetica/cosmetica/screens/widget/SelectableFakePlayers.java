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

package cc.cosmetica.cosmetica.screens.widget;

import benzenestudios.sulphate.ClassicButton;
import cc.cosmetica.cosmetica.cosmetics.ShoulderBuddies;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.screens.fakeplayer.FakePlayer;
import cc.cosmetica.cosmetica.utils.TextComponents;
import cc.cosmetica.cosmetica.screens.PlayerRenderScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
	public void updateWidgetNarration(NarrationElementOutput narration) {
		this.defaultButtonNarrationText(narration);
	}

	public void createSelectButtons(Consumer<Button> widgetAdder) {
		int x = this.getX() - this.width / 2;
		int j = 0;

		for (var player : this.players) {
			final int itemNo = j;
			widgetAdder.accept(new ClassicButton(x, this.getY() + 10, this.width, 20,
					TextComponents.translatable("cosmetica.selection.apply.select"), b -> {
						this.selected = itemNo;
						if (this.onSelect != null) this.onSelect.accept(itemNo);
					}
			));
			x += this.separation;
			j++;
		}
	}

	@Override
	public boolean mouseClicked(double clickX, double clickY, int i) {
		if (!this.active) return false;
		if (this.players.size() < 2) return false;

		if (clickY < this.getY() && clickY >= (double)(this.getY() + this.height) && i == 0) {
			int x = this.getX() - this.width / 2;
			int j = 0;

			for (var player : this.players) {
				if (clickX >= x && clickX < x + this.width) {
					this.selected = j;
					if (this.onSelect != null) this.onSelect.accept(j);
					return true;
				}

				x += this.separation;
				j++;
			}
		}

		return false;
	}

	@Override
	public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		int x = this.getX();
		int j = 0;
		CosmeticStack<T> overrider = this.overrider;

		for (var player : this.players) {
			if (this.selected == j) {
				final int x1 = x + (this.width / 2);
				final int y0 = this.getY() + this.height + 8;
				final int y1 = this.getY() + 8;
				final int x0 = x - (this.width / 2);

				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bb = tesselator.getBuilder();

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
			}

			this.overrider.setIndex(j); // to make sure it's all -1 at the end we use this.overrider for index setting but really it doesn't matter because it's only for hats anyway
			overrider.push(player.getB());
			PlayerRenderScreen.renderFakePlayerInMenu(x, this.getY(), this.scale, x - mouseX, (float)(this.getY() - 90) - mouseY, player.getA());
			overrider.pop();

			x += this.separation;
			j++;

			if (overrider == ShoulderBuddies.RIGHT_OVERRIDDEN) overrider = (CosmeticStack<T>) ShoulderBuddies.LEFT_OVERRIDDEN;
		}

		this.overrider.setIndex(-1);
	}
}
