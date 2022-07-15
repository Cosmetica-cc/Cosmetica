package com.eyezah.cosmetics.screens.widget;

import cc.cosmetica.api.CustomCape;
import cc.cosmetica.api.CustomCosmetic;
import cc.cosmetica.api.Model;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.eyezah.cosmetics.utils.textures.CosmeticIconTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CosmeticSelection<T extends CustomCosmetic> extends Selection<CosmeticSelection.Entry<T>> {
	public CosmeticSelection(Minecraft minecraft, Screen parent, String cosmeticType, Font font, Consumer<String> onSelect) {
		super(minecraft, parent, font, 0, 25, 50, onSelect);
		this.cosmeticType = cosmeticType;
	}

	private final String cosmeticType;
	private final Map<String, Entry> byId = new HashMap<>();

	public void add(T cosmetic) {
		this._add(cosmetic, true);
	}

	public void addWithoutRegisteringTexture(T cosmetic) {
		this._add(cosmetic, false);
	}

	public void copy(CosmeticSelection<T> other) {
		synchronized (minecraft.getTextureManager()) {
			for (Entry<T> entry : other.byId.values()) {
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

	public T getSelectedCosmetic() {
		return this.getSelected().cosmetic;
	}

	protected static class Entry<T extends CustomCosmetic> extends Selection.Entry<CosmeticSelection.Entry<T>> {
		public Entry(CosmeticSelection selection, T cosmetic, boolean register) {
			super(selection, cosmetic.getId());
			String cosmeticId = cosmetic.getId();

			this.displayName = cosmetic.getName();
			this.cosmetic = cosmetic;
			this.texture = new ResourceLocation("cosmetica", "icon/" + CosmeticaSkinManager.pathify(cosmeticId));

			// so we can add off-thread to the data version then duplicate later on thread when we make the view version
			if (register) { // ~~don't~~ yes please do load icon twice
				if (RenderSystem.isOnRenderThreadOrInit()) {
					Minecraft.getInstance().getTextureManager().register(this.texture, new CosmeticIconTexture(
							Cosmetica.getConfigDirectory().resolve(".icon_cache").resolve(cosmeticId.substring(0, 2)).resolve(cosmeticId + ".png").toFile(),
							String.format("http://images.cosmetica.cc/?subject=%s&type=icon&id=%s", selection.cosmeticType, cosmeticId),
							cosmetic.getType(),
							cosmetic instanceof Model model ? model.flags() : ((CustomCape) cosmetic).getFrameDelay()
					));

					textureRegistered = true;
				} else {
					Cosmetica.LOGGER.warn("Tried to register cosmetic icon texture on thread \"{}\". Avoiding crashes by delaying registration!", Thread.currentThread().getName());

					RenderSystem.recordRenderCall(() -> {
						Minecraft.getInstance().getTextureManager().register(this.texture, new CosmeticIconTexture(
								Cosmetica.getConfigDirectory().resolve(".icon_cache").resolve(cosmeticId.substring(0, 2)).resolve(cosmeticId + ".png").toFile(),
								String.format("http://images.cosmetica.cc/?subject=%s&type=icon&id=%s", selection.cosmeticType, cosmeticId),
								cosmetic.getType(),
								cosmetic instanceof Model model ? model.flags() : ((CustomCape) cosmetic).getFrameDelay()
						));

						textureRegistered = true;
					});
				}
			}
		}

		private final String displayName;
		private final T cosmetic;
		private final ResourceLocation texture;
		private volatile boolean textureRegistered = false; // volatile but I think it's only modified from render thread anyway

		@Override
		public void render(PoseStack poseStack, int x, int y, int k, int l, int m, int n, int o, boolean isHovered, float f) {
			x = Minecraft.getInstance().screen.width / 2 - 60;
			final int textY = y;
			y += 20;
			if (textureRegistered) renderTexture(poseStack.last().pose(), this.texture, x - 25, x + 25, y - 25, y + 25, this.selection.getBlitOffset());
			this.selection.font.drawShadow(poseStack, this.displayName, (float) (x + 30), (float)(textY + 6), 16777215, true);
		}
		
		private static void renderTexture(Matrix4f matrix4f, ResourceLocation texture, int x0, int x1, int y0, int y1, int z) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, texture);
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(matrix4f, (float)x0, (float)y1, (float)z).uv(0, 1).endVertex();
			bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z).uv(1, 1).endVertex();
			bufferBuilder.vertex(matrix4f, (float)x1, (float)y0, (float)z).uv(1, 0).endVertex();
			bufferBuilder.vertex(matrix4f, (float)x0, (float)y0, (float)z).uv(0, 0).endVertex();
			bufferBuilder.end();
			BufferUploader.end(bufferBuilder);
		}

		@Override
		public Component getNarration() {
			return new TranslatableComponent("narrator.select", new Object[]{this.displayName});
		}
	}
}
