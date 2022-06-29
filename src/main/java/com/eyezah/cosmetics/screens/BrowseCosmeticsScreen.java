package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CosmeticType;
import com.eyezah.cosmetics.screens.widget.CosmeticSelection;
import com.eyezah.cosmetics.utils.TextComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class BrowseCosmeticsScreen extends SulphateScreen {
	protected BrowseCosmeticsScreen(@Nullable Screen parent, CosmeticType<?> type) {
		super(TextComponents.translatable("cosmetica.selection.select").append(TextComponents.translatable("cosmetica.entry." + switch (type.getUrlString()) {
			case "cape" -> "Cape";
			case "hat" -> "Hats";
			case "shoulderbuddy" -> "ShoulderBuddies";
			default -> "BackBling";
		})), parent);
		this.type = type.getUrlString();
	}

	private final String type;

	@Override
	protected void addWidgets() {
		this.addRenderableWidget(new CosmeticSelection(this.minecraft, this, this.type, this.font, s -> {}));
		this.addDone(this.height - 40);
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);

	}
}
