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

package cc.cosmetica.cosmetica.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.Cape;
import cc.cosmetica.api.CosmeticPosition;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CustomCosmetic;
import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.cosmetics.model.Models;
import cc.cosmetica.cosmetica.screens.fakeplayer.MouseTracker;
import cc.cosmetica.cosmetica.screens.widget.SelectableFakePlayers;
import cc.cosmetica.cosmetica.screens.widget.TextWidget;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ApplyCosmeticsScreen<T extends CustomCosmetic, E> extends SulphateScreen {
	protected ApplyCosmeticsScreen(Screen parent, PlayerRenderScreen parentParent, CosmeticType<T> type, CosmeticStack<E> overrider, @Nullable T cosmetic, float yRotBody, float yRot) {
		super(TextComponents.translatable(cosmetic == null ? "cosmetica.selection.remove" : "cosmetica.selection.apply").append(TextComponents.translatable("cosmetica.entry." + getTranslationPart(type))), parent);
		this.type = type;
		this.id = cosmetic == null ? "none" : cosmetic.getId();
		this.parentParent = parentParent;
		this.overrider = overrider;

		if (cosmetic == null) {
			if (type == CosmeticType.CAPE) {
				this.item = (E) CosmeticStack.NO_RESOURCE_LOCATION;
			}
			else {
				this.item = (E) CosmeticStack.NO_BAKABLE_MODEL;
			}
		}
		else if (cosmetic instanceof Cape) {
			this.item = (E) CosmeticaSkinManager.textureId("cape", this.id);
			CosmeticaSkinManager.processCape((Cape) cosmetic);
		}
		else if (cosmetic instanceof Model) {
			Model model = (Model) cosmetic;
			this.item = (E) Models.createBakableModel(model);
			this.failed = this.item == null;
		}
		else {
			throw new IllegalStateException("wtf (pls let valoeghese know that your game just said wtf)");
		}

		this.setAnchorY(Anchor.TOP, () -> this.height - 55);
		this.lmao = yRotBody;
		this.xd = yRot;
	}

	private final CosmeticType<T> type;
	private final CosmeticStack<E> overrider;
	private final String id;
	private E item;
	private final PlayerRenderScreen parentParent;
	private SelectableFakePlayers<E> selectableFakePlayers;
	private final float lmao;
	private final float xd;
	private boolean failed = false;
	private Button done;
	private MouseTracker mouseTracker = new MouseTracker();
	private boolean spinning;

	@Override
	protected void addWidgets() {
		if (this.failed) {
			Component text = TextComponents.translatable("cosmetica.selection.applyFailed");
			int tWidth = this.font.width(text);
			this.addRenderableWidget(new TextWidget((this.width + tWidth) / 2, this.height / 2, tWidth, 20, true, text));

			// todo "Retry" button?
			this.addButton(TextComponents.translatable("cosmetica.okay"), b -> this.onClose());
		}
		else {
			int width = 100;
			int separation = width + 30;
			int selectables = (this.type == CosmeticType.HAT && this.parentParent.fakePlayer.getData().hats().size() > (this.item == CosmeticStack.NO_BAKABLE_MODEL ? 1 : 0)) || this.type == CosmeticType.SHOULDER_BUDDY ? 2 : 1;

			this.selectableFakePlayers = this.addRenderableWidget(new SelectableFakePlayers(
					this.width / 2 - (int) (0.5 * separation * (selectables - 1)),
					this.height / 2 + 30, width, 170, this.overrider, TextComponents.dummy()));
			this.selectableFakePlayers.setSeparation(separation);
			this.selectableFakePlayers.active = false; // we gonna be usin' select buttons

			for (int i = 0; i < selectables; i++) {
				this.selectableFakePlayers.addFakePlayer(this.parentParent.fakePlayer, this.item);
			}

			this.done = this.addButton(CommonComponents.GUI_DONE, b -> {
				this.parentParent.fakePlayer.yRotBody = this.lmao;
				this.parentParent.fakePlayer.yRot = this.xd;

				this.minecraft.setScreen(new UpdatingCosmeticsScreen<>(this.parentParent, () -> Cosmetica.api.setCosmetic(this.positionOf(this.selectableFakePlayers.getSelected()), this.id)));
			});

			if (selectables > 1) {
				this.selectableFakePlayers.createSelectButtons(this::addRenderableWidget);
				this.selectableFakePlayers.setOnSelect(i -> this.done.active = true);
				this.done.active = false;
			}

			this.addButton(CommonComponents.GUI_CANCEL, b -> this.onClose());
		}
	}

	private CosmeticPosition positionOf(int selected) {
		if (this.type == CosmeticType.HAT) {
			return selected > 0 ? CosmeticPosition.SECOND_HAT : CosmeticPosition.HAT; // hat by default unless specifically second
		}
		else if (this.type == CosmeticType.SHOULDER_BUDDY) {
			return selected == 0 ? CosmeticPosition.RIGHT_SHOULDER_BUDDY : CosmeticPosition.LEFT_SHOULDER_BUDDY;
		}
		else if (this.type == CosmeticType.CAPE) {
			return CosmeticPosition.CAPE;
		}
		else {
			return CosmeticPosition.BACK_BLING;
		}
	}

	@Override
	public void tick() {
		this.parentParent.fakePlayer.tickCount++;

		if (this.minecraft.level == null) {
			this.minecraft.getProfiler().push("textures");
			this.minecraft.getTextureManager().tick();
			this.minecraft.getProfiler().pop();
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int bn) {
		boolean result = super.mouseClicked(mouseX, mouseY, bn);

		this.spinning = false;
		this.mouseTracker.setMouseDown(true);

		if (!result) {
			if (mouseY <= this.height - 60 && mouseY >= 20) {
				this.spinning = true;
			}
		}

		return result;
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		this.mouseTracker.setMouseDown(false);
		return super.mouseReleased(d, e, i);
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		this.mouseTracker.updatePosition(mouseX, mouseY);

		if (!this.mouseTracker.isMouseDown() && this.mouseTracker.hasTrackingPosData()) {
			this.spinning = false;
		}

		if (this.spinning) {
			if (this.mouseTracker.wasMouseDown()) {
				this.parentParent.fakePlayer.yRot -= this.mouseTracker.deltaMouseX();
				this.parentParent.fakePlayer.yRotBody -= this.mouseTracker.deltaMouseX();

				if (this.parentParent.fakePlayer.yRot > 180.0f) this.parentParent.fakePlayer.yRot = -180.0f;
				if (this.parentParent.fakePlayer.yRotBody > 180.0f) this.parentParent.fakePlayer.yRotBody = -180.0f;

				if (this.parentParent.fakePlayer.yRot < -180.0f) this.parentParent.fakePlayer.yRot = 180.0f;
				if (this.parentParent.fakePlayer.yRotBody < -180.0f) this.parentParent.fakePlayer.yRotBody = 180.0f;
			}
		}

		this.mouseTracker.pushMouseDown();
	}

	static String getTranslationPart(CosmeticType<?> type) {
		switch (type.getUrlString()) {
		case "cape":
			return "Cape";
		case "hat":
			return "Hat";
		case "shoulderbuddy":
			return "ShoulderBuddy";
		default:
			return "BackBling";
		}
	}
}
