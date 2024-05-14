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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.fastutil.ints.*;

import java.util.List;
import java.util.Map;

public class EntityTracker1_9 extends EntityTrackerBase {

	private final Int2ObjectMap<IntList> vehicles = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<Vector> offsets = new Int2ObjectOpenHashMap<>();
	private final Int2IntMap status = new Int2IntOpenHashMap();

	public EntityTracker1_9(UserConnection connection) {
		super(connection, EntityTypes1_9.EntityType.PLAYER);
	}

	@Override
	public void removeEntity(int id) {
		vehicles.remove(id);
		offsets.remove(id);
		status.remove(id);

		vehicles.forEach((vehicle, passengers) -> passengers.rem(id));
		vehicles.int2ObjectEntrySet().removeIf(entry -> entry.getValue().isEmpty());
		super.removeEntity(id);
	}

	public void resetEntityOffset(final int id) {
		offsets.remove(id);
	}

	public Vector getEntityOffset(final int id) {
		return offsets.get(id);
	}

	public void setEntityOffset(final int id, Vector offset) {
		offsets.put(id, offset);
	}

	public IntList getPassengers(final int id) {
		return vehicles.getOrDefault(id, new IntArrayList());
	}

	public void setPassengers(final int id, final IntList passengers) {
		vehicles.put(id, passengers);
	}

	public boolean isInsideVehicle(final int id) {
		for (List<Integer> vehicle : vehicles.values()) {
			if (vehicle.contains(id)) return true;
		}
		return false;
	}

	public int getVehicle(final int passenger) {
		for (Map.Entry<Integer, IntList> vehicle : vehicles.int2ObjectEntrySet()) {
			if (vehicle.getValue().contains(passenger)) return vehicle.getKey();
		}
		return -1;
	}

	public Int2IntMap getStatus() {
		return status;
	}
}
