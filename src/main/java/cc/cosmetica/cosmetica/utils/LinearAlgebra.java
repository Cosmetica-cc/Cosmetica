/*
 * Copyright 2022 EyezahMC
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

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LinearAlgebra {
	public static final Vector3f XP = new Vector3f(1, 0, 0);
	public static final Vector3f XN = new Vector3f(-1, 0, 0);

	public static final Vector3f YP = new Vector3f(0, 1, 0);
	public static final Vector3f YN = new Vector3f(0, -1, 0);

	public static final Vector3f ZP = new Vector3f(0, 0, 1);
	public static final Vector3f ZN = new Vector3f(0, 0, -1);

	public static Quaternionf quaternion(Vector3f axis, float radians) {
		return new Quaternionf(new AxisAngle4f(radians, axis));
	}
}
