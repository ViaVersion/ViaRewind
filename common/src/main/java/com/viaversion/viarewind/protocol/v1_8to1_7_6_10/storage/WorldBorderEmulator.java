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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.MathUtil;

public final class WorldBorderEmulator extends StoredObject {

    private double x, z;
    private double oldDiameter, newDiameter;

    private long deltaTime;
    private long deltaStartTime;

    public WorldBorderEmulator(UserConnection user, final double x, final double z, final double oldDiameter, final double newDiameter, final long deltaTime) {
        super(user);

        this.x = x;
        this.z = z;

        this.oldDiameter = oldDiameter;
        this.newDiameter = newDiameter;

        this.deltaTime = deltaTime;
    }

    public void setCenter(final double x, final double z) {
        this.x = x;
        this.z = z;
    }

    public void updateDeltaTime(final double oldDiameter, final double newDiameter, final long deltaTime) {
        this.oldDiameter = oldDiameter;
        this.newDiameter = newDiameter;
        this.deltaTime = deltaTime;
        this.deltaStartTime = System.currentTimeMillis();
    }

    public double getSize() {
        if (deltaTime == 0) {
            return newDiameter;
        }

        final double percent = ((double) (System.currentTimeMillis() - deltaStartTime) / (double) (deltaTime));
        return oldDiameter + (newDiameter - oldDiameter) * MathUtil.clamp(percent, 0D, 1D);
    }

    public void setSize(final double size) {
        this.oldDiameter = size;
        this.newDiameter = size;

        this.deltaTime = 0;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public enum Side {
        NORTH(0, -1),
        EAST(1, 0),
        SOUTH(0, 1),
        WEST(-1, 0);

        public final int modX;
        public final int modZ;

        Side(int modX, int modZ) {
            this.modX = modX;
            this.modZ = modZ;
        }
    }
}

