package cc.cosmetica.cosmetica.screens.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TextWidget extends AbstractWidget {
	public TextWidget(int x, int y, int width, int height, boolean centered, Component component) {
		super(x, y, width, height, component);
		this.centered = centered;
	}

	private boolean centered;

	public boolean mouseClicked(double d, double e, int i) {
		return false;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;

		int colour = this.active ? 16777215 : 10526880;

		if (this.centered) {
			drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, colour | Mth.ceil(this.alpha * 255.0F) << 24);
		}
		else {
			drawString(poseStack, font, this.getMessage(), this.x, this.y + (this.height - 8) / 2, colour | Mth.ceil(this.alpha * 255.0F) << 24);
		}
	}
}