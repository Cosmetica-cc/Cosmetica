package com.eyezah.cosmetics.mixin.screen;

import com.eyezah.cosmetics.screens.LoadingScreen;
import com.eyezah.cosmetics.utils.ExtendedScreen;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
	protected OptionsScreenMixin(Component component) {
		super(component);
	}

	@Shadow
	@Final
	private Options options;

	@Inject(at=@At("RETURN"), method="init")
	private void onInit(CallbackInfo info) {
		for (GuiEventListener eventListener: ((ExtendedScreen) this).getChildren()) {
			if (eventListener instanceof AbstractWidget widget) {
				if (widget.getMessage() instanceof TranslatableComponent whyIsJavaLikeThis) {
					if (whyIsJavaLikeThis.getKey().equals("options.skinCustomisation")) {
						this.removeWidget(eventListener);
						break;
					}
				}
			}
		}
		this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, new TranslatableComponent("cosmetica.cosmetics"),
				button -> this.minecraft.setScreen(new LoadingScreen(this, this.options))));
	}
}
