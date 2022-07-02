package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CustomCosmetic;
import com.eyezah.cosmetics.cosmetics.model.CosmeticStack;
import com.eyezah.cosmetics.screens.widget.SelectableFakePlayers;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;

public class ApplyCosmeticsScreen<T extends CustomCosmetic> extends SulphateScreen {
	protected ApplyCosmeticsScreen(Screen parent, PlayerRenderScreen parentParent, CosmeticType<T> type, CosmeticStack<T> overrider, String id) {
		super(TextComponents.translatable("cosmetica.selection.apply").append(TextComponents.translatable("cosmetica.entry." + getTranslationPart(type))), parent);
		this.type = type;
		this.id = id;
		this.parentParent = parentParent;
		this.overrider = overrider;
	}

	private final CosmeticType<T> type;
	private final CosmeticStack<T> overrider;
	private final String id;
	private final PlayerRenderScreen parentParent;
	private SelectableFakePlayers selectableFakePlayers;

	@Override
	protected void addWidgets() {
		int width = 100;
		int separation = width + 30;
		int selectables = this.type == CosmeticType.HAT || this.type == CosmeticType.SHOULDER_BUDDY ? 2 : 1;

		this.parentParent.fakePlayer.yRotBody = this.type == CosmeticType.CAPE ? (float) Math.PI : 0;
		this.parentParent.fakePlayer.yRotHead = this.parentParent.fakePlayer.yRotBody;

		this.selectableFakePlayers = this.addRenderableWidget(new SelectableFakePlayers(
				this.width / 2 - (int) (0.5 * separation * (selectables - 1)),
				this.height / 2 + 30, width, -170, this.overrider, TextComponents.dummy()));
		this.selectableFakePlayers.setSeparation(separation);

		for (int i = 0; i < selectables; i++) {
			this.selectableFakePlayers.addFakePlayer(this.parentParent.fakePlayer, this.id);
		}
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
