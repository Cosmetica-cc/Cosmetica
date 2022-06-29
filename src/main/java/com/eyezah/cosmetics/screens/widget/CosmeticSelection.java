package com.eyezah.cosmetics.screens.widget;

import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
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
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class CosmeticSelection extends Selection<CosmeticSelection.Entry> {
	public CosmeticSelection(Minecraft minecraft, Screen parent, String cosmeticType, Font font, Consumer<String> onSelect) {
		super(minecraft, parent, font, 0, 15, 30, onSelect);
		this.cosmeticType = cosmeticType;
		this.addEntry(new Entry(this, "Thing", "SzN1OWVLUXVxZGIramc"));
		this.addEntry(new Entry(this, "Thing 2", "QVd1TGdDbWtQZ3M3VkE"));
	}

	private final String cosmeticType;

	public static class Entry extends Selection.Entry<CosmeticSelection.Entry> {
		public Entry(CosmeticSelection selection, String displayName, String cosmeticId) {
			super(selection, cosmeticId);
			this.displayName = displayName;
			this.texture = new ResourceLocation("cosmetica", "icon/" + CosmeticaSkinManager.pathify(cosmeticId));
			Minecraft.getInstance().getTextureManager().register(this.texture, new HttpTexture(
					Cosmetica.getConfigDirectory().resolve(".icon_cache").resolve(cosmeticId + ".png").toFile(),
					String.format("http://images.cosmetica.cc/?subject=%s&type=icon&id=%s", selection.cosmeticType, cosmeticId),
					new ResourceLocation("cosmetica", "textures/gui/loading.png"),
					false,
					null
			));
		}

		private final String displayName;
		private final ResourceLocation texture;

		@Override
		public void render(PoseStack poseStack, int x, int y, int k, int l, int m, int n, int o, boolean isHovered, float f) {
			x = Minecraft.getInstance().screen.width / 2 - 30;
			renderTexture(poseStack.last().pose(), this.texture, x - 15, x + 15, y - 2, y + 28, this.selection.getBlitOffset());
			Minecraft.getInstance().font.drawShadow(poseStack, this.displayName, (float) (Minecraft.getInstance().screen.width / 2), (float)(y + 8), 16777215, true);
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
