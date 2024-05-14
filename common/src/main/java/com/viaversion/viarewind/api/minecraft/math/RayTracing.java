/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viarewind.api.minecraft.math;

public class RayTracing {

	public static Vector3d trace(Ray3d ray, AABB aabb, double distance) {
		Vector3d invDir = new Vector3d(1f / ray.dir.x, 1f / ray.dir.y, 1f / ray.dir.z);

		boolean signDirX = invDir.x < 0;
		boolean signDirY = invDir.y < 0;
		boolean signDirZ = invDir.z < 0;

		Vector3d bbox = signDirX ? aabb.max : aabb.min;
		double tmin = (bbox.x - ray.start.x) * invDir.x;
		bbox = signDirX ? aabb.min : aabb.max;
		double tmax = (bbox.x - ray.start.x) * invDir.x;
		bbox = signDirY ? aabb.max : aabb.min;
		double tymin = (bbox.y - ray.start.y) * invDir.y;
		bbox = signDirY ? aabb.min : aabb.max;
		double tymax = (bbox.y - ray.start.y) * invDir.y;

		if (tmin > tymax || tymin > tmax) return null;

		if (tymin > tmin) tmin = tymin;

		if (tymax < tmax) tmax = tymax;

		bbox = signDirZ ? aabb.max : aabb.min;
		double tzmin = (bbox.z - ray.start.z) * invDir.z;
		bbox = signDirZ ? aabb.min : aabb.max;
		double tzmax = (bbox.z - ray.start.z) * invDir.z;

		if (tmin > tzmax || tzmin > tmax) return null;

		if (tzmin > tmin) tmin = tzmin;

		if (tzmax < tmax) tmax = tzmax;

		return tmin <= distance && tmax > 0 ? ray.start.clone().add(ray.dir.clone().normalize().multiply(tmin)) : null;
	}

}
