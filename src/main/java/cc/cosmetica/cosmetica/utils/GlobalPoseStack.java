package cc.cosmetica.cosmetica.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

/**
 * @reason easier porting between 1.16.5- and 1.17+
 */
@SuppressWarnings("deprecated")
public class GlobalPoseStack extends PoseStack {
	@Override
	public void pushPose() {
		RenderSystem.pushMatrix();
	}

	@Override
	public void popPose() {
		RenderSystem.popMatrix();
	}

	@Override
	public void translate(double d, double e, double f) {
		RenderSystem.translated(d, e, f);
	}

	@Override
	public void scale(float f, float g, float h) {
		RenderSystem.scalef(f, g, h);
	}

	@Override
	public void mulPose(Quaternion quaternion) {
		throw new UnsupportedOperationException("Cannot mulPose on the global pose stack.");
	}

	@Override
	public Pose last() {
		throw new UnsupportedOperationException("Cannot get last pose on the global pose stack.");
	}

	@Override
	public boolean clear() {
		throw new UnsupportedOperationException("Cannot clear the global pose stack.");
	}
}
