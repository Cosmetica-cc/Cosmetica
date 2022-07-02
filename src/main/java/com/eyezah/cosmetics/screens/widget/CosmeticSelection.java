package com.eyezah.cosmetics.screens.widget;

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

public class CosmeticSelection extends Selection<CosmeticSelection.Entry> {
	public CosmeticSelection(Minecraft minecraft, Screen parent, String cosmeticType, Font font, Consumer<String> onSelect) {
		super(minecraft, parent, font, 0, 25, 46, onSelect);
		this.cosmeticType = cosmeticType;
	}

	private final String cosmeticType;
	private final Map<String, Entry> byId = new HashMap<>();

	public void add(String name, String id) {
		Entry entry = new Entry(this, name, id);
		this.addEntry(entry);
		this.byId.put(id, entry);
	}

	public void copy(CosmeticSelection other) {
		for (Entry entry : other.byId.values()) {
			this.add(entry.displayName, entry.item);
		}
	}

	@Override
	protected CosmeticSelection.Entry findEntry(CosmeticSelection.Entry key) {
		return this.byId.get(key.item);
	}

	public String getSelectedId() {
		return this.getSelected().item;
	}

	public static class Entry extends Selection.Entry<CosmeticSelection.Entry> {
		public Entry(CosmeticSelection selection, String displayName, String cosmeticId) {
			super(selection, cosmeticId);
			this.displayName = displayName;
			this.texture = new ResourceLocation("cosmetica", "icon/" + CosmeticaSkinManager.pathify(cosmeticId));
			Minecraft.getInstance().getTextureManager().register(this.texture, new CosmeticIconTexture(
					Cosmetica.getConfigDirectory().resolve(".icon_cache").resolve(cosmeticId.substring(0, 2)).resolve(cosmeticId + ".png").toFile(),
					String.format("http://images.cosmetica.cc/?subject=%s&type=icon&id=%s", selection.cosmeticType, cosmeticId)
			));
		}

		private final String displayName;
		private final ResourceLocation texture;

		@Override
		public void render(PoseStack poseStack, int x, int y, int k, int l, int m, int n, int o, boolean isHovered, float f) {
			x = Minecraft.getInstance().screen.width / 2 - 60;
			renderTexture(poseStack.last().pose(), this.texture, x - 25, x + 25, y - 4, y + 46, this.selection.getBlitOffset());
			this.selection.font.drawShadow(poseStack, this.displayName, (float) (x + 30), (float)(y + 6), 16777215, true);
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
