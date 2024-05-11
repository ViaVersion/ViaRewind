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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

public class WorldBorderEmulator extends StoredObject {
	private double x, z;
	private double oldDiameter, newDiameter;

	private long lerpTime;
	private long lerpStartTime;

	private boolean init = false;

	public WorldBorderEmulator(UserConnection user) {
		super(user);
	}

	public void init(double x, double z, double oldDiameter, double newDiameter, long lerpTime) {
		this.x = x;
		this.z = z;

		this.oldDiameter = oldDiameter;
		this.newDiameter = newDiameter;

		this.lerpTime = lerpTime;

		init = true;
	}

	public void setCenter(double x, double z) {
		this.x = x;
		this.z = z;
	}

	public void lerpSize(double oldDiameter, double newDiameter, long lerpTime) {
		this.oldDiameter = oldDiameter;
		this.newDiameter = newDiameter;
		this.lerpTime = lerpTime;
		this.lerpStartTime = System.currentTimeMillis();
	}

	public void setSize(double size) {
		this.oldDiameter = size;
		this.newDiameter = size;

		this.lerpTime = 0;
	}

	public double getSize() {
		if (lerpTime == 0) {
			return newDiameter;
		}

		double percent = ((double) (System.currentTimeMillis() - lerpStartTime) / (double) (lerpTime));

		// Clamp value
		if (percent > 1.0D) percent = 1.0d;
		else if (percent < 0.0D) percent = 0.0d;

		return oldDiameter + (newDiameter - oldDiameter) * percent;
	}

	public double getX() {
		return x;
	}

	public double getZ() {
		return z;
	}

	public boolean isInit() {
		return init;
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
