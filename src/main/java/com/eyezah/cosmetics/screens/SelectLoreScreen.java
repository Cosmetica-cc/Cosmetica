package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.LoreType;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.utils.ExtendedScreen;
import com.eyezah.cosmetics.utils.TextComponents;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectLoreScreen extends SulphateScreen {
	protected SelectLoreScreen(@Nullable Screen parent, String lore) {
		super(TextComponents.translatable("cosmetica.entry.Lore"), parent);
		this.baseTitle = TextComponents.translatable("cosmetica.entry.Lore");
		if (!lore.isEmpty() && lore.charAt(0) == '\u00A7') this.colour = lore.substring(0, 2);
		this.lore = this.colour.isEmpty() ? lore : lore.substring(2);
		this.originalLore = this.lore;
	}

	private List<String> titles = ImmutableList.of();
	private List<String> pronouns = ImmutableList.of();
	private boolean auth;
	private FetchingCosmetics fetching;
	private SelectionList list;
	private final MutableComponent baseTitle;
	private String lore;
	private String colour = "";
	private final String originalLore;
	private boolean showingPronouns;
	private boolean setPronouns;
	private int pronounsSelected;

	@Override
	protected void addWidgets() {
		if (this.fetching == null) {
			this.fetching = this.addWidget((x, y, w, h, component) -> new FetchingCosmetics<>("Lore", () -> ImmutableList.of(Cosmetica.api.getLoreList(LoreType.TITLES), Cosmetica.api.getLoreList(LoreType.PRONOUNS)), (obj, titles) -> {
				if (obj == this.fetching) {
					if (titles.isEmpty()) {
						this.auth = false;
					}
					else {
						this.titles = titles.get(0);
						this.pronouns = titles.get(1);
						this.auth = true;
					}

					this.minecraft.tell(() -> {if (this.minecraft.screen == this) this.init(this.minecraft, this.width, this.height);});
				}
			}), TextComponents.dummy());

			this.addButton(CommonComponents.GUI_CANCEL, b -> this.onClose());
		}
		else if (this.auth) {
			SelectionList list = new SelectionList(this.minecraft, this, this.font, 0, s -> {
				if (this.showingPronouns) {
					if (this.pronounsSelected == 0) {
						this.setPronouns = true;
						this.lore = s;
						this.pronounsSelected = 1;
					}
					else if (this.pronounsSelected < 4) {
						this.setPronouns = true;
						this.lore += "/" + s;
						this.pronounsSelected++;
					}
				}
				else {
					this.setPronouns = false;
					this.lore = s;
				}
				this.updateTitle();
			});

			if (this.showingPronouns) {
				this.pronounsSelected = 0;
				this.pronouns.forEach(list::addItem);
			}
			else {
				this.titles.forEach(list::addItem);
				list.selectItem(this.lore);
				list.recenter();
			}

			if (this.list != null) {
				list.setSelected(this.list.getSelected());
			}

			this.list = list; // update

			this.updateTitle();
			this.addRenderableWidget(this.list);
			this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 50, 100, 20, TextComponents.translatable(this.showingPronouns ? "cosmetica.selection.lore.titles" : "cosmetica.selection.lore.pronouns"), b -> {
				this.showingPronouns = !this.showingPronouns;
				this.list = null;
				this.init(this.minecraft, this.width, this.height);
			}));

			this.addRenderableWidget(new Button(this.width / 2, this.height - 50, 100, 20, TextComponents.translatable("cosmetica.selection.lore.clear"), b -> {
				this.lore = "";
				this.pronounsSelected = 0;
				this.updateTitle();
			}));

			this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 30, 200, 20, CommonComponents.GUI_DONE, b -> {
				if (this.lore.equals(this.originalLore)) {
					this.onClose();
				}
				else {
					this.minecraft.setScreen(new UpdatingCosmeticsScreen<>(this.parent, () -> Cosmetica.api.setLore(this.setPronouns ? LoreType.PRONOUNS : LoreType.TITLES, this.lore)));
				}
			}));
		}
		else {
			this.addWidget((x, y, w, h, component) -> new TextWidget(x, y, w, h, true, component), TextComponents.translatable("cosmetica.selection.lore.err"));
			this.addButton(TextComponents.translatable("cosmetica.okay"), b -> this.onClose());
		}
	}

	private void updateTitle() {
		((ExtendedScreen) this).setTitle(this.lore.isEmpty() ? this.baseTitle : this.baseTitle.copy().append(TextComponents.literal(": " + this.lore)));
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xFFFFFF); // re-add title
	}
}
