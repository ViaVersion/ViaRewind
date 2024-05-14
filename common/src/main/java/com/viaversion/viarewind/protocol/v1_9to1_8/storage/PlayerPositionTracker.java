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
package com.viaversion.viarewind.protocol.v1_9to1_8.storage;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerPositionTracker implements StorableObject {

	private final Queue<PacketWrapper> animations = new ConcurrentLinkedQueue<>();
	private double posX, posY, posZ;
	private float yaw, pitch;
	private boolean onGround;
	private int confirmId = -1;

	public void setPos(double x, double y, double z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}

	public void sendAnimations() {
		PacketWrapper wrapper;
		while ((wrapper = animations.poll()) != null) {
			wrapper.sendToServer(Protocol1_9To1_8.class);
		}
	}

	public void queueAnimation(final PacketWrapper wrapper) {
		animations.add(wrapper);
	}

	public void setYaw(float yaw) {
		this.yaw = yaw % 360f;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch % 360f;
	}

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getPosZ() {
		return this.posZ;
	}

	public float getYaw() {
		return this.yaw;
	}

	public float getPitch() {
		return this.pitch;
	}

	public boolean isOnGround() {
		return this.onGround;
	}

	public int getConfirmId() {
		return this.confirmId;
	}

	public void setOnGround(boolean onGround) {
		this.onGround = onGround;
	}

	public void setConfirmId(int confirmId) {
		this.confirmId = confirmId;
	}

}
