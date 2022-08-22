package cc.cosmetica.cosmetica.compat;

import cc.cosmetica.cosmetica.screens.LoadingScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;

public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new LoadingScreen(parent, Minecraft.getInstance().options);
	}
}
