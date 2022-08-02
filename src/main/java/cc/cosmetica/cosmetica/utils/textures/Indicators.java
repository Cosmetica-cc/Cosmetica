package cc.cosmetica.cosmetica.utils.textures;

import cc.cosmetica.api.CosmeticType;
import cc.cosmetica.api.Model;
import cc.cosmetica.cosmetica.utils.TextComponents;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Indicators {
	public static final ResourceLocation ANIMATED = new ResourceLocation("cosmetica", "textures/gui/icon/animated.png");
	public static final ResourceLocation LOCK_TO_TORSO = new ResourceLocation("cosmetica", "textures/gui/icon/lock_to_torso.png");
	public static final ResourceLocation MIRROR = new ResourceLocation("cosmetica", "textures/gui/icon/mirror.png");
	public static final ResourceLocation SW_CAPE = new ResourceLocation("cosmetica", "textures/gui/icon/show_with_cape.png");
	public static final ResourceLocation SW_CHESTPLATE = new ResourceLocation("cosmetica", "textures/gui/icon/show_with_chestplate.png");
	public static final ResourceLocation SW_HELMET = new ResourceLocation("cosmetica", "textures/gui/icon/show_with_helmet.png");
	public static final ResourceLocation SW_PARROT = new ResourceLocation("cosmetica", "textures/gui/icon/show_with_parrot.png");

	public static final Map<ResourceLocation, MutableComponent> TOOLTIPS = ImmutableMap.<ResourceLocation, MutableComponent>builder()
			.put(ANIMATED, TextComponents.translatable("cosmetica.indicators.animated"))
			.put(LOCK_TO_TORSO, TextComponents.translatable("cosmetica.indicators.lockToTorso"))
			.put(MIRROR, TextComponents.translatable("cosmetica.indicators.mirror"))
			.put(SW_CAPE, TextComponents.translatable("cosmetica.indicators.showWithCape"))
			.put(SW_CHESTPLATE, TextComponents.translatable("cosmetica.indicators.showWithChestplate"))
			.put(SW_HELMET, TextComponents.translatable("cosmetica.indicators.showWithHelmet"))
			.put(SW_PARROT, TextComponents.translatable("cosmetica.indicators.showWithParrot"))
			.build();

	public static List<ResourceLocation> getIcons(CosmeticType<?> type, int flags) {
		List<ResourceLocation> indicators = new ArrayList<>(3);

		// CosmeticType is a class not an enum so no, I cannot use switch here, Mr. Picky.
		// Why is it a class? So I can throw generics in it for use in various api methods. I would have totally made it an enum if I didn't need to do that.
		if (type == CosmeticType.CAPE) {
			if (flags > 0) {
				indicators.add(Indicators.ANIMATED);
			}
		}
		else if (type == CosmeticType.BACK_BLING) {
			if ((flags & Model.SHOW_BACK_BLING_WITH_CAPE) > 0) {
				indicators.add(Indicators.SW_CAPE);
			}

			if ((flags & Model.SHOW_BACK_BLING_WITH_CHESTPLATE) > 0) {
				indicators.add(Indicators.SW_CHESTPLATE);
			}
		}
		else if (type == CosmeticType.SHOULDER_BUDDY) {
			if ((flags & Model.SHOW_SHOULDER_BUDDY_WITH_PARROT) > 0) {
				indicators.add(Indicators.SW_PARROT);
			}

			if ((flags & Model.LOCK_SHOULDER_BUDDY_ORIENTATION) > 0) {
				indicators.add(Indicators.LOCK_TO_TORSO);
			}

			if ((flags & Model.DONT_MIRROR_SHOULDER_BUDDY) == 0) {
				indicators.add(Indicators.MIRROR);
			}
		}
		else {
			if ((flags & Model.SHOW_HAT_WITH_HELMET) > 0) {
				indicators.add(Indicators.SW_HELMET);
			}

			if ((flags & Model.LOCK_HAT_ORIENTATION) > 0) {
				indicators.add(Indicators.LOCK_TO_TORSO);
			}
		}

		return indicators;
	}
}
