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

package cc.cosmetica.cosmetica.utils;

/**
 * Duck interface for the {@linkplain net.minecraft.client.gui.screens.TitleScreen title screen} to add extended functionality.
 */
public interface ExtendedTitleScreen {
	/**
	 * Set whether to 'flashbang' (fade from white) on title screen. On by default. Does not exist past 1.17.
	 * @param flashbang whether to flashbang on the title screen.
	 */
	void setFlashbang(boolean flashbang);
}
