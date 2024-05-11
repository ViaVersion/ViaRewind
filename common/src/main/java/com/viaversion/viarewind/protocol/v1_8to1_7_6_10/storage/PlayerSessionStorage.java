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
import com.viaversion.viaversion.api.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSessionStorage extends StoredObject {

	// Player abilities
	public boolean sprinting, allowFly, flying, invincible, creative;
	public float flySpeed, walkSpeed;

	// Player position, rotation and data
	private double posX, posY, posZ;
	public double receivedPosY;
	public float yaw, pitch;
	public boolean onGround;

	// Player inventory
	private final Map<UUID, Item[]> playerEquipment = new HashMap<>();

	public PlayerSessionStorage(UserConnection user) {
		super(user);
	}

	public byte combineAbilities() {
		byte flags = 0;

		if (invincible) flags |= 8;
		if (allowFly) flags |= 4;
		if (flying) flags |= 2;
		if (creative) flags |= 1;

		return flags;
	}

	public double getPosX() {
		return posX;
	}

	public double getPosY() {
		return posY;
	}

	public double getPosZ() {
		return posZ;
	}

	public void setPos(double x, double y, double z) {
		posX = x;
		posY = y;
		posZ = z;
	}

	public Item getPlayerEquipment(final UUID uuid, final int slot) {
		final Item[] items = playerEquipment.get(uuid);
		if (items == null || slot < 0 || slot >= items.length) {
			return null;
		}

		return items[slot];
	}

	public void setPlayerEquipment(final UUID uuid, final Item equipment, final int slot) {
		final Item[] items = playerEquipment.computeIfAbsent(uuid, it -> new Item[5]);
		if (slot < 0 || slot >= items.length) {
			return;
		}

		items[slot] = equipment;
	}

	public Map<UUID, Item[]> getPlayerEquipment() {
		return playerEquipment;
	}
}
