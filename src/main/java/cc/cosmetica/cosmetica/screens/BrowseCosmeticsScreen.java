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
import benzenestudios.sulphate.ExtendedScreen;
import cc.cosmetica.api.Cape;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CosmeticsPage;
import cc.cosmetica.api.CustomCosmetic;
import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.CosmeticaSkinManager;
import cc.cosmetica.cosmetica.cosmetics.model.CosmeticStack;
import cc.cosmetica.cosmetica.cosmetics.model.Models;
import cc.cosmetica.cosmetica.screens.widget.CosmeticSelection;
import cc.cosmetica.cosmetica.screens.widget.FetchingCosmetics;
import cc.cosmetica.cosmetica.screens.widget.SearchEditBox;
import cc.cosmetica.cosmetica.screens.widget.TextWidget;
import cc.cosmetica.cosmetica.utils.TextComponents;
import cc.cosmetica.cosmetica.utils.LoadState;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BrowseCosmeticsScreen<T extends CustomCosmetic, E> extends PlayerRenderScreen {
	protected BrowseCosmeticsScreen(@Nullable PlayerRenderScreen parent, CosmeticType<T> type, CosmeticStack<E> overrider) {
		super(TextComponents.translatable("cosmetica.selection.select").append(TextComponents.translatable("cosmetica.entry." + getTranslationPart(type))), parent, parent.fakePlayer);
		this.type = type;
		this.overrider = overrider;

		this.rightMouseGrabBuffer = 80;
		this.setTransitionProgress(1.0f);

		this.yRotBodyPrev = parent.fakePlayer.yRotBody;
		this.yRotPrev = parent.fakePlayer.yRot;

		parent.fakePlayer.yRotBody = this.type == CosmeticType.CAPE ? 200.0f : 0.0f;
		parent.fakePlayer.yRot = parent.fakePlayer.yRotBody;
	}

	private final CosmeticType<T> type;
	private final CosmeticStack<E> overrider;
	private String searchQuery = "";
	private LoadState state = LoadState.LOADING;
	@Nullable
	private CosmeticSelection<T> dataSelection; // null on initial load. Created in the fetcher
	private CosmeticSelection<T> viewSelection; // the display version for funny resize hack
	private FetchingCosmetics<CosmeticsPage<T>> currentFetcher;
	private int page = 1;
	private boolean nextPage;
	private SearchEditBox searchBox;
	private Button proceed;

	private float yRotBodyPrev;
	private float yRotPrev;

	private T lastSelected;
	private E lastSelectedButE;

	private static final Component SEARCH_ELLIPSIS = new TranslatableComponent("cosmetica.selection.search");
	private static final int SEARCH_Y = 32;

	@Override
	protected void addWidgets() {
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2);
		this.setRows(3);
		this.searchBox = null; // it doesn't exist unless we want it this screen
		this.proceed = null;

		switch (this.state) {
		case RELOADING:
			this.addMainGUI(true);
		case LOADING:
			this.currentFetcher = this.addWidget(new FetchingCosmetics<>(getTranslationPart(this.type), () -> ImmutableList.of(Cosmetica.api.getRecentCosmetics(this.type, this.page, 8, Optional.ofNullable(this.searchQuery))),
			(fetcher, results) -> {
				if (results.isEmpty()) {
					this.state = LoadState.FAILED;
				}
				else {
					this.dataSelection = new CosmeticSelection(this.minecraft, this, this.type.getUrlString(), this.font, s -> {});
					CosmeticsPage<T> page = results.get(0);

					for (T result : page.getCosmetics()) {
						this.dataSelection.addWithoutRegisteringTexture(result);
					}

					this.nextPage = page.hasNextPage();
				}

				// Change GUI widgets on the main thread to prevent CMEs. Load State LOADED must be set there too so that the GUI display changes all at once rather than one component at a time.
				RenderSystem.recordRenderCall(() -> {
					this.state = LoadState.LOADED;
					this.rebuildGUI();
				});
			}));
			this.currentFetcher.y = this.height / 2 - 20;
			this.currentFetcher.x = this.width / 2 - this.currentFetcher.getWidth() / 2;

			if (this.state == LoadState.LOADING) this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 20, 200, 20, CommonComponents.GUI_CANCEL, b -> this.onClose()));
			break;
		case LOADED:
			this.addMainGUI(false);
			break;
		case FAILED:
			this.addWidget((x, y, w, h, component) -> new TextWidget(x, y, w, h, true, component), TextComponents.translatable("cosmetica.selection.err"));
			this.addButton(TextComponents.translatable("cosmetica.okay"), b -> this.onClose());
			break;
		}

		this.initialPlayerLeft = this.width / 4 - 10;
		this.deltaPlayerLeft = 0;
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if (this.viewSelection != null) {
			this.viewSelection.mouseMoved(mouseX, mouseY);
		}
	}

	@Override
	public void onClose() {
		// return player to pre-browse rotations: the browse cosmetics screen has a different set of rotations
		this.fakePlayer.yRotBody = this.yRotBodyPrev;
		this.fakePlayer.yRot = this.yRotPrev;
		super.onClose();
	}

	// hack for keeping items on resizing
	private CosmeticSelection<T> createViewSelection() {
		this.viewSelection = new CosmeticSelection<T>(this.minecraft, this, this.type.getUrlString(), this.font, s -> {if (this.proceed != null) this.proceed.active = true;});
		this.viewSelection.copy(this.dataSelection);
		this.viewSelection.matchSelected(this.dataSelection);
		return this.viewSelection;
	}

	public void resize(Minecraft minecraft, int i, int j) {
		boolean wasFocusedOnSearch = this.searchBox != null && this.getFocused() == this.searchBox;
		//System.out.println(wasFocusedOnSearch);
		@Nullable Button lastProceed = this.proceed;
		if (this.viewSelection != null) this.dataSelection.matchSelected(this.viewSelection);

		if (this.searchBox == null)
			super.resize(minecraft, i, j);
		else {
			String query = this.searchBox.getValue();
			this.init(minecraft, i, j);
			if (this.searchBox != null) this.searchBox.setValue(query);
		}

		if (lastProceed != null && this.proceed != null) this.proceed.active = lastProceed.active;

		if (this.searchBox != null && wasFocusedOnSearch) {
			this.setFocused(this.searchBox);
			this.searchBox.setFocus(true);
		}
	}

	private void addMainGUI(boolean loadEdition) {
		// top
		this.searchBox = this.addRenderableWidget(new SearchEditBox(this.font, this.width / 2 - 100, SEARCH_Y, 200, 20, new TranslatableComponent("cosmetica.selection.search")));
		this.searchBox.setMaxLength(128);
		this.searchBox.setOnEnter(value -> {
			//this.setFocused(null);
			this.searchQuery = value;
			this.page = 1;
			this.state = LoadState.RELOADING;
			this.rebuildGUI();
		});

		this.addWidget(this.searchBox);

		if (loadEdition) {
			this.addRenderableOnly(this.createViewSelection());
		}
		else {
			this.addRenderableWidget(this.createViewSelection());
		}

		// bottom
		this.setAnchorY(Anchor.TOP, () -> this.height - 50);

		Button pageBack = this.addButton(100, 20, TextComponents.translatable("cosmetica.selection.pageBack"), b -> {
			this.page--;
			this.state = LoadState.RELOADING;
			this.rebuildGUI();
		});

		if (this.page == 1 || loadEdition) pageBack.active = false;

		Button clear = this.addButton(this.type == CosmeticType.SHOULDER_BUDDY || this.type == CosmeticType.BACK_BLING ? 150 : 100, 20, TextComponents.translatable("cosmetica.selection.remove").append(TextComponents.translatable("cosmetica.entry." + ApplyCosmeticsScreen.getTranslationPart(this.type))),
				b -> this.minecraft.setScreen(new ApplyCosmeticsScreen<>(this, (PlayerRenderScreen) this.parent, this.type, this.overrider, null, this.yRotBodyPrev, this.yRotPrev)));

		if (loadEdition) clear.active = false;

		Button pageForward = this.addButton(100, 20, TextComponents.translatable("cosmetica.selection.pageForward"), b -> {
			this.page++;
			this.state = LoadState.RELOADING;
			this.rebuildGUI();
		});

		if (!this.nextPage || loadEdition) pageForward.active = false;

		this.addButton(150, 20, CommonComponents.GUI_CANCEL, b -> this.onClose());
		this.proceed = this.addButton(150, 20, TextComponents.translatable("cosmetica.selection.proceed"), b -> this.minecraft.setScreen(new ApplyCosmeticsScreen<T, E>(this, (PlayerRenderScreen) this.parent, this.type, this.overrider, this.viewSelection.getSelectedCosmetic(), this.yRotBodyPrev, this.yRotPrev)));
		this.proceed.active = false;
	}

	private void rebuildGUI() {
		if (this.proceed != null) this.proceed.active = false;
		this.resize(this.minecraft, this.width, this.height);
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		for (Widget widget : List.copyOf(((ExtendedScreen) this).getWidgets())) { // renderables
			widget.render(matrices, mouseX, mouseY, delta);
		}

		drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xFFFFFF); // re-add title

		if (this.searchBox != null) {
			this.searchBox.render(matrices, mouseX, mouseY, delta);
			if (this.searchBox.isEmpty()) drawString(matrices, this.font, SEARCH_ELLIPSIS, this.width / 2 - 96, SEARCH_Y + 6, 10526880);
		}

		if (this.state == LoadState.RELOADING) {
			this.fillGradient(matrices, 0, 32 + 25, this.width, this.height - 65 + 4, -1072689136, -804253680);
		}

		if (this.state == LoadState.RELOADING || this.state == LoadState.LOADING) {
			this.currentFetcher.render(matrices, mouseX, mouseY, delta);
		}

		if (this.state != LoadState.LOADING && this.viewSelection != null) {
			T cosmetic = this.viewSelection.getSelectedCosmetic();

			if (cosmetic == null) {
				this.lastSelected = null;
				this.lastSelectedButE = null;
				CosmeticStack.strip();
			}
			else {
				if (cosmetic != this.lastSelected) {
					this.lastSelected = cosmetic;
					this.lastSelectedButE = getStack(cosmetic);
				}

				if (this.lastSelectedButE == null) {
					CosmeticStack.strip();
				}
				else {
					this.overrider.solo();
					this.overrider.push(this.lastSelectedButE);
				}
			}

			// paper doll on side
			this.updateSpin(mouseX, mouseY);
			this.fakePlayer.renderNametag = false;
			this.renderFakePlayer(mouseX, mouseY);
			this.fakePlayer.renderNametag = true;

			if (this.overrider.isSolo()) {
				this.overrider.pop();
			}

			CosmeticStack.normal();
		}
	}

	@Nullable
	private E getStack(T cosmetic) {
		if (cosmetic == null) {
			if (type == CosmeticType.CAPE) {
				return (E) CosmeticStack.NO_RESOURCE_LOCATION;
			}
			else {
				return (E) CosmeticStack.NO_BAKABLE_MODEL;
			}
		}
		if (cosmetic instanceof Cape cape) {
			E result = (E) CosmeticaSkinManager.textureId("cape", cosmetic.getId());
			CosmeticaSkinManager.processCape(cape);
			return result;
		}
		else if (cosmetic instanceof Model model) {
			return  (E) Models.createBakableModel(model);
		}
		else {
			throw new IllegalStateException("wtf (pls let valoeghese know that your game just said wtf)");
		}
	}

	@Override
	public void tick() {
		if (this.minecraft.level == null) {
			this.minecraft.getProfiler().push("textures");
			this.minecraft.getTextureManager().tick();
			this.minecraft.getProfiler().pop();
		}
	}

	private static String getTranslationPart(CosmeticType<?> type) {
		return switch (type.getUrlString()) {
			case "cape" -> "Capes";
			case "hat" -> "Hats";
			case "shoulderbuddy" -> "ShoulderBuddies";
			default -> "BackBlings";
		};
	}
}
