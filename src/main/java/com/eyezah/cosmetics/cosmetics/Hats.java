package com.eyezah.cosmetics.cosmetics;

import cc.cosmetica.api.Model;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.cosmetics.model.OverriddenModel;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.screens.fakeplayer.MenuRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Hats<T extends Player> extends CustomLayer<T, PlayerModel<T>> implements MenuRenderLayer {
	private ModelManager modelManager;

	public Hats(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
		super(renderLayerParent);
		this.modelManager = Minecraft.getInstance().getModelManager();
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLight, T player, float f, float g, float pitch, float j, float k, float l) {
		if (player.isInvisible()) return;
		List<BakableModel> hats = overridden.getList(() -> Cosmetica.getPlayerData(player).hats());

		stack.pushPose();

		for (BakableModel modelData : hats) {
			if ((modelData.extraInfo() & Model.HIDE_HAT_UNDER_HELMET) == 0 && player.hasItemInSlot(EquipmentSlot.HEAD)) {
				continue; // disable hat flag
			}

			if ((modelData.extraInfo() & Model.LOCK_HAT_ORIENTATION) == 0) {
				doCoolRenderThings(modelData, this.getParentModel().getHead(), stack, multiBufferSource, packedLight, 0, 0.75f, 0);
			} else {
				doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLight, 0, 0.77f, 0);
			}

			stack.scale(1.001f, 1.001f, 1.001f); // stop multiple hats conflicting
		}

		stack.popPose();
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
		List<BakableModel> hats = overridden.getList(() -> player.getData().hats());

		stack.pushPose();

		for (BakableModel modelData : hats) {
			if ((modelData.extraInfo() & Model.LOCK_HAT_ORIENTATION) == 0) {
				doCoolRenderThings(modelData, this.getParentModel().getHead(), stack, bufferSource, packedLight, 0, 0.75f, 0);
			} else {
				doCoolRenderThings(modelData, this.getParentModel().body, stack, bufferSource, packedLight, 0, 0.77f, 0);
			}

			stack.scale(1.001f, 1.001f, 1.001f); // stop multiple hats conflicting
		}

		stack.popPose();
	}

	public static final OverriddenModel overridden = new OverriddenModel();
}