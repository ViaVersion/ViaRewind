/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
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

import com.viaversion.viaversion.api.minecraft.Vector3d;

public final class RayTracing {

    public static Vector3d trace(final Box ray, final Box box, final double distance) {
        final Vector3d invDir = new Vector3d(1f / ray.end().x(), 1f / ray.end().y(), 1f / ray.end().z());

        final boolean signDirX = invDir.x() < 0;
        final boolean signDirY = invDir.y() < 0;
        final boolean signDirZ = invDir.z() < 0;

        Vector3d boundingBox = signDirX ? box.end() : box.start();
        double tmin = (boundingBox.x() - ray.start().x()) * invDir.x();
        boundingBox = signDirX ? box.start() : box.end();
        double tmax = (boundingBox.x() - ray.start().x()) * invDir.x();
        boundingBox = signDirY ? box.end() : box.start();
        final double tymin = (boundingBox.y() - ray.start().y()) * invDir.y();
        boundingBox = signDirY ? box.start() : box.end();
        final double tymax = (boundingBox.y() - ray.start().y()) * invDir.y();

        if (tmin > tymax || tymin > tmax) {
            return null;
        }

        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }

        boundingBox = signDirZ ? box.end() : box.start();
        double tzmin = (boundingBox.z() - ray.start().z()) * invDir.z();
        boundingBox = signDirZ ? box.start() : box.end();
        double tzmax = (boundingBox.z() - ray.start().z()) * invDir.z();

        if (tmin > tzmax || tzmin > tmax) {
            return null;
        }

        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }

        if (tmin <= distance && tmax > 0) {
            // ray.start().clone().add(ray.dir().clone().normalize().multiply(tmin))
        }

        return null;
    }

}
