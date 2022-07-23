package com.eyezah.cosmetics.cosmetics;

import cc.cosmetica.api.Model;
import com.eyezah.cosmetics.Cosmetica;
import com.eyezah.cosmetics.cosmetics.model.BakableModel;
import com.eyezah.cosmetics.cosmetics.model.CosmeticStack;
import com.eyezah.cosmetics.screens.fakeplayer.FakePlayer;
import com.eyezah.cosmetics.screens.fakeplayer.MenuRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.Items;

public class BackBling<T extends AbstractClientPlayer> extends CustomLayer<T, PlayerModel<T>> implements MenuRenderLayer {
	public BackBling(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
		super(renderLayerParent);;
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource multiBufferSource, int packedLightProbably, T player, float f, float g, float pitch, float j, float k, float l) {
		if (player.isInvisible()) return;
		BakableModel modelData = OVERRIDDEN.get(() -> Cosmetica.getPlayerData(player).backBling());

		if (modelData == null) return; // if it has a model
		if (((player.isCapeLoaded() && player.isModelPartShown(PlayerModelPart.CAPE) && player.getCloakTextureLocation() != null) || player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA))
				&& (modelData.extraInfo() & Model.SHOW_BACK_BLING_WITH_CAPE) == 0) return; // if wearing cape/elytra and show bb w cape is not set
		else if (player.hasItemInSlot(EquipmentSlot.CHEST) && (modelData.extraInfo() & Model.SHOW_BACK_BLING_WITH_CHESTPLATE) == 0) return; // if wearing chestplate and show bb w chestplate is not set

		stack.pushPose();
		doCoolRenderThings(modelData, this.getParentModel().body, stack, multiBufferSource, packedLightProbably, 0, -0.1f - (0.15f/6.0f), 0.1f + (0.4f/16.0f));
		stack.popPose();
	}

	@Override
	public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, FakePlayer player, float o, float n, float delta, float bob, float yRotDiff, float xRot) {
		BakableModel modelData = OVERRIDDEN.get(() -> player.getData().backBling());

		if (modelData == null) return; // if it has a model
		if (player.getData().cape() != null && (modelData.extraInfo() & Model.SHOW_BACK_BLING_WITH_CAPE) == 0) return; // if wearing cape and show bb w cape is not set

		stack.pushPose();
		doCoolRenderThings(modelData, this.getParentModel().body, stack, bufferSource, packedLight, 0, -0.1f - (0.15f/6.0f), 0.1f);
		stack.popPose();
	}

	public static final CosmeticStack<BakableModel> OVERRIDDEN = new CosmeticStack();
}
