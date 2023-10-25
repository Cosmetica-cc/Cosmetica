/*
 * Copyright 2022, 2023 EyezahMC
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

import cc.cosmetica.api.CustomCape;
import cc.cosmetica.api.CustomCosmetic;
import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import cc.cosmetica.cosmetica.utils.TextComponents;
import cc.cosmetica.cosmetica.utils.textures.CosmeticIconTexture;
import cc.cosmetica.cosmetica.utils.textures.Indicators;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class CosmeticSelection<T extends CustomCosmetic> extends Selection<CosmeticSelection.Entry<T>> {
	public CosmeticSelection(Minecraft minecraft, Screen parent, String cosmeticType, Font font, Consumer<String> onSelect) {
		super(minecraft, parent, font, 0, 25, 60, onSelect);
		this.cosmeticType = cosmeticType;
	}

	private final String cosmeticType;
	private final Map<String, Entry> byId = new HashMap<>();

	private static final int OFFSET_X = 45;

	public void add(T cosmetic) {
		this._add(cosmetic, true);
	}

	public void addWithoutRegisteringTexture(T cosmetic) {
		this._add(cosmetic, false);
	}

	public void copy(CosmeticSelection<T> other) {
		synchronized (minecraft.getTextureManager()) { // can this be removed now?
			for (Entry<T> entry : other.children()) {
				this._add(entry.cosmetic, true);
			}
		}
	}

	protected final void _add(T cosmetic, boolean register) {
		Entry<T> entry = new Entry<>(this, cosmetic, register);
		this.addEntry(entry);
		this.byId.put(cosmetic.getId(), entry);
	}

	@Override
	protected CosmeticSelection.Entry findEntry(CosmeticSelection.Entry key) {
		return this.byId.get(key.item);
	}

	@Nullable
	public T getSelectedCosmetic() {
		@Nullable Entry<T> selected = this.getSelected();
		return selected == null ? null : selected.cosmetic;
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for (Entry entry : this.byId.values()) {
			entry.mouseMoved(mouseX, mouseY);
		}
	}

	@Override
	protected void renderList(PoseStack poseStack, int xOffset, int yOffset, int k, int l, float f) {
		// stolen proudly from vanilla
		int itemCount = this.getItemCount();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();

		for(int i = 0; i < itemCount; ++i) {
			int rowTop = this.getRowTop(i);
			int rowBottom = rowTop + this.itemHeight;

			if (rowBottom >= this.y0 && rowTop <= this.y1) {
				int y0 = yOffset + i * this.itemHeight + this.headerHeight;
				int dy = this.itemHeight - 4;
				Entry entry = this.getEntry(i);
				int rowWidth = this.getRowWidth();
				int x0;
				if (this.isSelectedItem(i)) {
					x0 = this.x0 + this.width / 2 - rowWidth / 2 + OFFSET_X;
					int x1 = this.x0 + this.width / 2 + rowWidth / 2;

					RenderSystem.disableTexture();
					RenderSystem.setShader(GameRenderer::getPositionShader);
					float g = this.isFocused() ? 1.0F : 0.5F;

					// white ""outline"" (square 1)
					RenderSystem.setShaderColor(g, g, g, 1.0F);
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
					bufferBuilder.vertex(x0, (y0 + dy + 2), 0.0D).endVertex();
					bufferBuilder.vertex(x1, (y0 + dy + 2), 0.0D).endVertex();
					bufferBuilder.vertex(x1, (y0 - 2), 0.0D).endVertex();
					bufferBuilder.vertex(x0, (y0 - 2), 0.0D).endVertex();
					tesselator.end();

					// blacc
					RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
					bufferBuilder.vertex((x0 + 1), (y0 + dy + 1), 0.0D).endVertex();
					bufferBuilder.vertex((x1 - 1), (y0 + dy + 1), 0.0D).endVertex();
					bufferBuilder.vertex((x1 - 1), (y0 - 1), 0.0D).endVertex();
					bufferBuilder.vertex((x0 + 1), (y0 - 1), 0.0D).endVertex();
					tesselator.end();
					RenderSystem.enableTexture();
				}

				x0 = this.getRowLeft();
				entry.render(poseStack, i, rowTop, x0, rowWidth, dy, k, l, Objects.equals(this.getHovered(), entry), f);
			}
		}

	}

	protected static class Entry<T extends CustomCosmetic> extends Selection.Entry<CosmeticSelection.Entry<T>> {
		public Entry(CosmeticSelection selection, T cosmetic, boolean register) {
			super(selection, cosmetic.getId());
			String cosmeticId = cosmetic.getId();

			this.displayName = cosmetic.getName();
			this.cosmetic = cosmetic;
			this.texture = new ResourceLocation("cosmetica", "icon/" + CosmeticaSkinManager.pathify(cosmeticId));
			this.indicators = Indicators.getIcons(
					cosmetic.getType(),
					cosmetic instanceof Model model ? model.flags() : ((CustomCape) cosmetic).getFrameDelay()
			);

			// so we can add off-thread to the data version then duplicate later on thread when we make the view version
			if (register) { // yes please do load icon each time (it removes it from memory?)
				if (RenderSystem.isOnRenderThreadOrInit()) {
					Minecraft.getInstance().getTextureManager().register(this.texture, new CosmeticIconTexture(
							Cosmetica.getCacheDirectory().resolve(".icon_cache").resolve(cosmeticId.substring(0, 2)).resolve(cosmeticId + "-" + this.cosmetic.getType().getUrlString() + ".png").toFile(),
							String.format("http://images.cosmetica.cc/?subject=%s&type=icon&id=%s", selection.cosmeticType, cosmeticId)
					));

					textureRegistered = true;
				} else {
					Cosmetica.LOGGER.warn("Tried to register cosmetic icon texture on thread \"{}\". Avoiding crashes by delaying registration!", Thread.currentThread().getName());

					RenderSystem.recordRenderCall(() -> {
						Minecraft.getInstance().getTextureManager().register(this.texture, new CosmeticIconTexture(
								Cosmetica.getCacheDirectory().resolve(".icon_cache").resolve(cosmeticId.substring(0, 2)).resolve(cosmeticId + "-" + this.cosmetic.getType().getUrlString() + ".png").toFile(),
								String.format("http://images.cosmetica.cc/?subject=%s&type=icon&id=%s", selection.cosmeticType, cosmeticId)
						));

						textureRegistered = true;
					});
				}
			}

			this.screen = selection.parent;
			this.selection = selection;
		}

		private final String displayName;
		private final T cosmetic;
		private final ResourceLocation texture;
		private volatile boolean textureRegistered = false; // volatile but I think it's only modified from render thread anyway
		private final List<ResourceLocation> indicators;
		private final Screen screen;
		@Nullable
		private ResourceLocation hoveredIndicator;
		private int indicatorStartY = 100000000; // arbitrary big number
		private int mouseX;
		private int mouseY;
		private final CosmeticSelection selection;

		@Override
		public void render(PoseStack poseStack, int x, int y, int k, int l, int m, int n, int o, boolean isHovered, float f) {
			this.indicatorStartY = y + 16;
			x = Minecraft.getInstance().screen.width / 2 - 60;
			final int textY = y;
			y += 28;

			Matrix4f pose = poseStack.last().pose();

			if (textureRegistered) Cosmetica.renderTexture(pose, this.texture, x - 25, x + 25, y - 25, y + 25, this.selection.getBlitOffset(), 1.0f);
			this.selection.font.drawShadow(poseStack, this.displayName, (float) (x + 30), (float)(textY + 6), 16777215, true);
			
			int indicatorX = x + 30;
			
			for (ResourceLocation location : this.indicators) {
				Cosmetica.renderTexture(pose, location, indicatorX, indicatorX + 10, this.indicatorStartY, this.indicatorStartY + 10, this.selection.getBlitOffset(), 1.0f);
				indicatorX += 14; // move x down
			}

			if (this.hoveredIndicator != null) {
				this.screen.renderTooltip(poseStack, Indicators.TOOLTIPS.get(this.hoveredIndicator), mouseX, mouseY + 8);
			}
		}

		@Override
		public void mouseMoved(double mouseX, double mouseY) {
			this.mouseX = (int) mouseX;
			this.mouseY = (int) mouseY;

			int indicatorStartX = Minecraft.getInstance().screen.width / 2 - 60 + 30;

			for (ResourceLocation location : this.indicators) {
				if (mouseX >= indicatorStartX && mouseX <= indicatorStartX + 10 && mouseY >= this.indicatorStartY && mouseY <= this.indicatorStartY + 10) {
					this.hoveredIndicator = location;
					return;
				}

				indicatorStartX += 14;
			}

			this.hoveredIndicator = null;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int i) {
			if (mouseX >= this.selection.x0 + this.selection.width / 2 - this.selection.getRowWidth() / 2 + OFFSET_X) {
				return super.mouseClicked(mouseX, mouseY, i);
			} else {
				return false;
			}
		}

		@Override
		public Component getNarration() {
			return TextComponents.formattedTranslatable("narrator.select", new Object[]{this.displayName});
		}
	}
}
