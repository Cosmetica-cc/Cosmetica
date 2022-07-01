package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CustomCosmetic;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ApplyCosmeticsScreen<T extends CustomCosmetic> extends SulphateScreen {
	protected ApplyCosmeticsScreen(@Nullable Screen parent, CosmeticType<T> type, String id) {
		super(TextComponents.translatable("cosmetica.selection.apply").append("cosmetica.entry." + getTranslationPart(type)), parent);
	}

	@Override
	protected void addWidgets() {

	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
	}

	static String getTranslationPart(CosmeticType<?> type) {
		return switch (type.getUrlString()) {
			case "cape" -> "Cape";
			case "hat" -> "Hats";
			case "shoulderbuddy" -> "ShoulderBuddies";
			default -> "BackBling";
		};
	}
}
