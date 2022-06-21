package com.eyezah.cosmetics.screens;

import benzenestudios.sulphate.SulphateScreen;
import cc.cosmetica.api.CapeServer;
import com.eyezah.cosmetics.utils.CapeServerOption;
import com.eyezah.cosmetics.utils.TextComponents;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CapeServerSettingsScreen extends SulphateScreen {
	protected CapeServerSettingsScreen(Screen parent, Map<String, CapeServer> settings) {
		super(TextComponents.translatable("cosmetica.capeServerSettings"), parent);
		this.settings = new ArrayList<>(settings.entrySet());
		Collections.sort(this.settings, Comparator.comparingInt(a -> a.getValue().getCheckOrder()));
	}

	private final List<Map.Entry<String, CapeServer>> settings;

	@Override
	protected void addWidgets() {
		ButtonList list = new ButtonList(this.minecraft, this);

		this.settings.forEach(entry ->
			list.addButton(200, TextComponents.literal(entry.getValue().getName() + ": ").append(TextComponents.translatable("cosmetica.capeDisplay." + entry.getValue().getDisplay().toString().toLowerCase(Locale.ROOT))), b -> {})
		);

		this.addRenderableWidget(list);

		this.addDone();
	}
}
