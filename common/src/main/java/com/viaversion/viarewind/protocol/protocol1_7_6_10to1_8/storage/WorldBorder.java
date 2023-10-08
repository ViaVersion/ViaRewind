/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

public class WorldBorder extends StoredObject {

	private double x, z;
	private double oldDiameter, newDiameter;
	private long lerpTime;
	private long lerpStartTime;
	private int portalTeleportBoundary;
	private int warningTime, warningBlocks;
	private boolean init = false;

	public WorldBorder(UserConnection user) {
		super(user);
	}

	public boolean isInit() {
		return init;
	}

	public void init(double x, double z, double oldDiameter, double newDiameter, long lerpTime, int portalTeleportBoundary, int warningTime, int warningBlocks) {
		this.x = x;
		this.z = z;
		this.oldDiameter = oldDiameter;
		this.newDiameter = newDiameter;
		this.lerpTime = lerpTime;
		this.portalTeleportBoundary = portalTeleportBoundary;
		this.warningTime = warningTime;
		this.warningBlocks = warningBlocks;
		init = true;
	}

	public double getX() {
		return x;
	}

	public double getZ() {
		return z;
	}

	public void setCenter(double x, double z) {
		this.x = x;
		this.z = z;
	}

	public double getOldDiameter() {
		return oldDiameter;
	}

	public double getNewDiameter() {
		return newDiameter;
	}

	public long getLerpTime() {
		return lerpTime;
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
		if (lerpTime == 0) return newDiameter;

		long time = System.currentTimeMillis() - lerpStartTime;
		double percent = ((double) (time) / (double) (lerpTime));
		if (percent > 1.0d) percent = 1.0d;
		else if (percent < 0.0d) percent = 0.0d;

		return oldDiameter + (newDiameter - oldDiameter) * percent;
	}

	public int getPortalTeleportBoundary() {
		return portalTeleportBoundary;
	}

	public void setPortalTeleportBoundary(int portalTeleportBoundary) {
		this.portalTeleportBoundary = portalTeleportBoundary;
	}

	public int getWarningTime() {
		return warningTime;
	}

	public void setWarningTime(int warningTime) {
		this.warningTime = warningTime;
	}

	public int getWarningBlocks() {
		return warningBlocks;
	}

	public void setWarningBlocks(int warningBlocks) {
		this.warningBlocks = warningBlocks;
	}

	public enum Side {
		NORTH(0, -1),
		EAST(1, 0),
		SOUTH(0, 1),
		WEST(-1, 0),
		;

		public final int modX;
		public final int modZ;

		Side(int modX, int modZ) {
			this.modX = modX;
			this.modZ = modZ;
		}
	}
}
