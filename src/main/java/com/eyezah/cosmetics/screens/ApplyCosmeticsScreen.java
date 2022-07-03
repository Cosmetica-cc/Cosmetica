package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.Cape;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CustomCosmetic;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.CosmeticaSkinManager;
import com.eyezah.cosmetics.cosmetics.model.CosmeticStack;
import com.eyezah.cosmetics.screens.widget.SelectableFakePlayers;
import com.eyezah.cosmetics.screens.widget.TextWidget;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ApplyCosmeticsScreen<T extends CustomCosmetic, E> extends SulphateScreen {
	protected ApplyCosmeticsScreen(Screen parent, PlayerRenderScreen parentParent, CosmeticType<T> type, CosmeticStack<E> overrider, String id) {
		super(TextComponents.translatable("cosmetica.selection.apply").append(TextComponents.translatable("cosmetica.entry." + getTranslationPart(type))), parent);
		this.type = type;
		this.id = id;
		this.parentParent = parentParent;
		this.overrider = overrider;

		if (type == CosmeticType.CAPE) {
			this.item = (E) CosmeticaSkinManager.cloakId(id);

			Thread t = new Thread(() -> {
				Cape cape = Cape.fetch(id);

				if (cape == null) {
					Cosmetica.LOGGER.error("The cosmetica api crapped itself?");

					this.failed = true;

					if (this.minecraft != null) { // if somehow it hasn't initialised yet then don't bother reinitialising
						this.init(this.minecraft, this.width, this.height);
					}
				}
				else {
					CosmeticaSkinManager.processCape(cape);
				}
			});
			t.setName("Cosmetica Cape Getter / " + id);
			t.start();
		}

		this.setAnchorY(Anchor.TOP, () -> this.height - 50);
	}

	private final CosmeticType<T> type;
	private final CosmeticStack<E> overrider;
	private final String id;
	private E item;
	private final PlayerRenderScreen parentParent;
	private SelectableFakePlayers<E> selectableFakePlayers;
	private boolean failed = false;

	@Override
	protected void addWidgets() {
		if (this.failed) {
			Component text = TextComponents.translatable("cosmetica.selection.applyFailed");
			this.addRenderableWidget(new TextWidget(this.width / 2, this.height / 2, this.font.width(text), 20, true, text));
		}
		else {
			int width = 100;
			int separation = width + 30;
			int selectables = this.type == CosmeticType.HAT || this.type == CosmeticType.SHOULDER_BUDDY ? 2 : 1;

			// todo save rot
			this.parentParent.fakePlayer.yRotBody = this.type == CosmeticType.CAPE ? 200.0f : 0.0f;
			this.parentParent.fakePlayer.yRot = this.parentParent.fakePlayer.yRotBody;

			this.selectableFakePlayers = this.addRenderableWidget(new SelectableFakePlayers(
					this.width / 2 - (int) (0.5 * separation * (selectables - 1)),
					this.height / 2 + 30, width, 170, this.overrider, TextComponents.dummy()));
			this.selectableFakePlayers.setSeparation(separation);

			for (int i = 0; i < selectables; i++) {
				this.selectableFakePlayers.addFakePlayer(this.parentParent.fakePlayer, this.item);
			}
		}

		this.addButton(CommonComponents.GUI_DONE, b -> {
			this.minecraft.setScreen(this.parentParent);
		});
		this.addButton(CommonComponents.GUI_CANCEL, b -> this.onClose());
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, this.type == CosmeticType.CAPE ? this.width - mouseX : mouseX, mouseY, delta);
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
