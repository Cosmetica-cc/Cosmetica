/*
 * Copyright 2022, 2023 EyezahMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cosmetica.cosmetica.utils.textures;

import cc.cosmetica.cosmetica.Cosmetica;
import cc.cosmetica.cosmetica.mixin.textures.NativeImageAccessorMixin;
import cc.cosmetica.cosmetica.utils.DebugMode;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class CosmeticIconTexture extends DirectAnimatedTexture {
	private CosmeticIconTexture(ResourceLocation debugPath, int aspectRatio, Supplier<NativeImage> image, int animationDelay) {
		super(debugPath, aspectRatio, image, animationDelay);
	}

	public final Set<String> indicators = new HashSet<>();

	public static void load(TextureManager manager, ResourceLocation texture, Path path, String url, int animationDelay) {
		// load placeholder
		manager.register(texture, new SimpleTexture(ResourceLocation.fromNamespaceAndPath("cosmetica", "textures/gui/loading.png")));

		// load image
		Thread resourceLoader = new Thread(() -> {
			HttpURLConnection httpURLConnection = null;
			DebugMode.log("Downloading Cosmetica Icon texture from {} to {}", url, path);
			URI uRI = URI.create(url);

			NativeImage image;
			try {
				httpURLConnection = (HttpURLConnection)uRI.toURL().openConnection(Minecraft.getInstance().getProxy());
				httpURLConnection.setDoInput(true);
				httpURLConnection.setDoOutput(false);
				httpURLConnection.connect();
				int i = httpURLConnection.getResponseCode();
				if (i / 100 != 2) {
					throw new IOException("Failed to open " + uRI + ", HTTP error code: " + i);
				}

				byte[] bs = httpURLConnection.getInputStream().readAllBytes();

				try {
					FileUtil.createDirectoriesSafe(path.getParent());
					Files.write(path, bs, new OpenOption[0]);
				} catch (IOException var13) {
					Cosmetica.LOGGER.warn("Failed to cache texture {} in {}", url, path);
				}

				image = NativeImage.read(bs);
			} catch (IOException e) {
				Cosmetica.LOGGER.error("Downloading Icon from " + url, e);
				return;
			} finally {
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
			}

			RenderSystem.recordRenderCall(() -> {
				manager.register(texture, new CosmeticIconTexture(
						ResourceLocation.fromNamespaceAndPath("cosmetica", "dynamic_icon_texture"),
						1,
						() -> image,
						animationDelay));
			});
		});
		resourceLoader.setName("Cosmetic Icon Downloader");
		resourceLoader.setDaemon(true);
		resourceLoader.start();
	}
}
