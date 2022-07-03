package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.Cape;
import cc.cosmetica.api.CosmeticPosition;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CustomCosmetic;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.eyezah.cosmetics.cosmetics.model.CosmeticStack;
import com.eyezah.cosmetics.screens.widget.SelectableFakePlayers;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class ApplyCosmeticsScreen<T extends CustomCosmetic, E> extends SulphateScreen {
	protected ApplyCosmeticsScreen(Screen parent, PlayerRenderScreen parentParent, CosmeticType<T> type, CosmeticStack<E> overrider, T cosmetic) {
		super(TextComponents.translatable("cosmetica.selection.apply").append(TextComponents.translatable("cosmetica.entry." + getTranslationPart(type))), parent);
		this.type = type;
		this.id = cosmetic.getId();
		this.parentParent = parentParent;
		this.overrider = overrider;

		if (cosmetic instanceof Cape cape) {
			this.item = (E) CosmeticaSkinManager.cloakId(this.id);
			CosmeticaSkinManager.processCape(cape);
		}

		this.setAnchorY(Anchor.TOP, () -> this.height - 50);
		this.lmao = this.parentParent.fakePlayer.yRotBody;
		this.xd = this.parentParent.fakePlayer.yRot;
	}

	private final CosmeticType<T> type;
	private final CosmeticStack<E> overrider;
	private final String id;
	private E item;
	private final PlayerRenderScreen parentParent;
	private SelectableFakePlayers<E> selectableFakePlayers;
	private final float lmao;
	private final float xd;

	@Override
	protected void addWidgets() {
		int width = 100;
		int separation = width + 30;
		int selectables = this.type == CosmeticType.HAT || this.type == CosmeticType.SHOULDER_BUDDY ? 2 : 1;

		this.parentParent.fakePlayer.yRotBody = this.type == CosmeticType.CAPE ? 200.0f : 0.0f;
		this.parentParent.fakePlayer.yRot = this.parentParent.fakePlayer.yRotBody;

		this.selectableFakePlayers = this.addRenderableWidget(new SelectableFakePlayers(
				this.width / 2 - (int) (0.5 * separation * (selectables - 1)),
				this.height / 2 + 30, width, 170, this.overrider, TextComponents.dummy()));
		this.selectableFakePlayers.setSeparation(separation);

		for (int i = 0; i < selectables; i++) {
			this.selectableFakePlayers.addFakePlayer(this.parentParent.fakePlayer, this.item);
		}

		this.addButton(CommonComponents.GUI_DONE, b -> {
			this.parentParent.fakePlayer.yRotBody = this.lmao;
			this.parentParent.fakePlayer.yRot = this.xd;

			this.minecraft.setScreen(new UpdatingCosmeticsScreen<>(this.parentParent, () -> Cosmetica.api.setCosmetic(CosmeticPosition.CAPE, this.id)));
		});
		this.addButton(CommonComponents.GUI_CANCEL, b -> this.onClose());
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, this.type == CosmeticType.CAPE ? this.width - mouseX : mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		this.parentParent.fakePlayer.yRotBody = this.lmao;
		this.parentParent.fakePlayer.yRot = this.xd;
		super.onClose();
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
