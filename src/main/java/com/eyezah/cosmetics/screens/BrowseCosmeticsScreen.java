package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.Anchor;
import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.CosmeticsPage;
import cc.cosmetica.api.CustomCosmetic;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.screens.widget.CosmeticSelection;
import com.eyezah.cosmetics.screens.widget.FetchingCosmetics;
import com.eyezah.cosmetics.screens.widget.TextWidget;
import com.eyezah.cosmetics.utils.LoadState;
import com.eyezah.cosmetics.utils.TextComponents;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BrowseCosmeticsScreen<T extends CustomCosmetic> extends SulphateScreen {
	protected BrowseCosmeticsScreen(@Nullable Screen parent, CosmeticType<T> type) {
		super(TextComponents.translatable("cosmetica.selection.select").append(TextComponents.translatable("cosmetica.entry." + getTranslationPart(type))), parent);
		this.type = type;
	}

	private final CosmeticType<T> type;
	private LoadState state = LoadState.LOADING;
	@Nullable
	private CosmeticSelection selection; // null on initial load. Created in the fetcher
	private FetchingCosmetics<CosmeticsPage<T>> currentFetcher;
	private int page = 1;
	private boolean nextPage;

	@Override
	protected void addWidgets() {
		this.setAnchorY(Anchor.CENTRE, () -> this.height / 2);
		this.setRows(3);

		switch (this.state) {
		case RELOADING:
			this.addRenderableOnly(this.selection);
			this.addMainButtons(true);
		case LOADING:
			this.currentFetcher = this.addRenderableWidget(new FetchingCosmetics<>(getTranslationPart(this.type), () -> ImmutableList.of(Cosmetica.api.getRecentCosmetics(this.type, this.page, 8, Optional.empty())),
			(fetcher, results) -> {
				if (results.isEmpty()) {
					this.state = LoadState.FAILED;
				}
				else {
					this.selection = new CosmeticSelection(this.minecraft, this, this.type.getUrlString(), this.font, s -> {});
					CosmeticsPage<T> page = results.get(0);

					for (T result : page.getCosmetics()) {
						this.selection.add(result.getName(), result.getId());
					}

					this.nextPage = page.hasNextPage();
					this.state = LoadState.LOADED;
				}

				this.rebuildGUI();
			}));
			this.currentFetcher.y = this.height / 2 - 20;
			this.currentFetcher.x = this.width / 2 - this.currentFetcher.getWidth() / 2;

			if (this.state == LoadState.LOADING) this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + 20, 200, 20, CommonComponents.GUI_CANCEL, b -> this.onClose()));
			break;
		case LOADED:
			this.addMainButtons(false);
			break;
		case FAILED:
			this.addWidget((x, y, w, h, component) -> new TextWidget(x, y, w, h, true, component), TextComponents.translatable("cosmetica.selection.err"));
			this.addButton(TextComponents.translatable("cosmetica.okay"), b -> this.onClose());
			break;
		}
	}

	private void addMainButtons(boolean loadEdition) {
		this.setAnchorY(Anchor.TOP, () -> this.height - 50);
		this.addRenderableWidget(this.selection);

		Button pageBack = this.addButton(100, 20, TextComponents.translatable("cosmetica.selection.pageBack"), b -> {
			this.page--;
			this.state = LoadState.RELOADING;
			this.rebuildGUI();
		});

		if (this.page == 1 || loadEdition) pageBack.active = false;

		Button clear = this.addButton(100, 20, TextComponents.translatable("cosmetica.selection.clear").append(TextComponents.translatable("cosmetica.entry." + getTranslationPart(this.type))), b -> {});

		if (loadEdition) clear.active = false;

		Button pageForward = this.addButton(100, 20, TextComponents.translatable("cosmetica.selection.pageForward"), b -> {
			this.page++;
			this.state = LoadState.RELOADING;
			this.rebuildGUI();
		});

		if (!this.nextPage || loadEdition) pageForward.active = false;

		this.addButton(150, 20, CommonComponents.GUI_CANCEL, b -> this.onClose());
		this.addButton(150, 20, this.type == CosmeticType.SHOULDER_BUDDY || this.type == CosmeticType.HAT ? TextComponents.translatable("cosmetica.selection.proceed") : CommonComponents.GUI_DONE, b -> this.onClose());
	}

	private void rebuildGUI() {
		this.init(this.minecraft, this.width, this.height);
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xFFFFFF); // re-add title

		if (this.state == LoadState.RELOADING) {
			this.fillGradient(matrices, 0, 32, this.width, this.height - 65 + 4, -1072689136, -804253680);
		}

		if (this.state == LoadState.RELOADING || this.state == LoadState.LOADING) {
			this.currentFetcher.render(matrices, mouseX, mouseY, delta);
		}
	}

	@Override
	public void tick() {
		this.minecraft.getTextureManager().tick();
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
