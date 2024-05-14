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

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data.VirtualHologramEntity;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntArrayMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectArrayMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EntityTracker1_8 extends EntityTrackerBase {

	private final Int2ObjectMap<VirtualHologramEntity> holograms = new Int2ObjectArrayMap<>();
	private final Int2IntMap vehicles = new Int2IntArrayMap();
	private final Int2ObjectMap<UUID> entityIdToUUID = new Int2ObjectArrayMap<>();
	private final Object2IntMap<UUID> entityUUIDToId = new Object2IntOpenHashMap<>();

	public int spectatingClientEntityId = -1;
	private int clientEntityGameMode;

	public EntityTracker1_8(UserConnection connection) {
		super(connection, EntityTypes1_8.EntityType.PLAYER);
	}

	@Override
	public void addEntity(int id, EntityType type) {
		super.addEntity(id, type);
		if (type == EntityTypes1_8.EntityType.ARMOR_STAND) {
			holograms.put(id, new VirtualHologramEntity(user(), id));
		}
	}

	@Override
	public void removeEntity(int entityId) {
		super.removeEntity(entityId);

		if (entityIdToUUID.containsKey(entityId)) {
			final UUID playerId = entityIdToUUID.remove(entityId);

			entityUUIDToId.removeInt(playerId);
			user().get(PlayerSessionStorage.class).getPlayerEquipment().remove(playerId);
		}
	}

	@Override
	public void clearEntities() {
		super.clearEntities();
		vehicles.clear();
	}

	@Override
	public void setClientEntityId(int entityId) {
		if (this.spectatingClientEntityId == this.clientEntityId()) {
			this.spectatingClientEntityId = entityId;
		}
		super.setClientEntityId(entityId);
	}

	public void addPlayer(final int entityId, final UUID uuid) {
		entityUUIDToId.put(uuid, entityId);
		entityIdToUUID.put(entityId, uuid);
	}

	public UUID getPlayerUUID(final int entityId) {
		return entityIdToUUID.get(entityId);
	}

	public int getPlayerEntityId(final UUID uuid) {
		return entityUUIDToId.getOrDefault(uuid, -1);
	}

	public int getVehicle(final int passengerId) {
		for (Map.Entry<Integer, Integer> vehicle : vehicles.entrySet()) {
			if (vehicle.getValue() == passengerId) {
				return vehicle.getValue();
			}
		}
		return -1;
	}

	public int getPassenger(int vehicleId) {
		return vehicles.getOrDefault(vehicleId, -1);
	}

	protected void startSneaking() {
		try {
			final PacketWrapper entityAction = PacketWrapper.create(ServerboundPackets1_7_2_5.PLAYER_COMMAND, user());
			entityAction.write(Types.VAR_INT, this.clientEntityId()); // Entity id
			entityAction.write(Types.VAR_INT, 0); // Action id
			entityAction.write(Types.VAR_INT, 0); // Jump boost

			entityAction.sendToServer(Protocol1_8To1_7_6_10.class);
		} catch (Exception e) {
			ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send sneak packet", e);
		}
	}

	public void setPassenger(final int vehicleId, final int passengerId) {
		if (vehicleId == this.spectatingClientEntityId && this.spectatingClientEntityId != this.clientEntityId()) {
			startSneaking();
			setSpectating(this.clientEntityId());
		}

		if (vehicleId == -1) {
			vehicles.remove(getVehicle(passengerId));
		} else if (passengerId == -1) {
			vehicles.remove(vehicleId);
		} else {
			vehicles.put(vehicleId, passengerId);
		}
	}

	protected void attachEntity(final int target) {
		try {
			final PacketWrapper attachEntity = PacketWrapper.create(ClientboundPackets1_8.SET_ENTITY_LINK, user());
			attachEntity.write(Types.INT, this.clientEntityId()); // vehicle id
			attachEntity.write(Types.INT, target); // passenger id
			attachEntity.write(Types.BOOLEAN, false); // leash

			attachEntity.scheduleSend(Protocol1_8To1_7_6_10.class);
		} catch (Exception e) {
			ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send attach packet", e);
		}
	}

	public void setSpectating(int spectating) {
		if (spectating != this.clientEntityId() && getPassenger(spectating) != -1) {
			startSneaking();
			setSpectating(this.clientEntityId());
			return;
		}
		if (this.spectatingClientEntityId != spectating && this.spectatingClientEntityId != this.clientEntityId()) {
			attachEntity(-1);
		}
		this.spectatingClientEntityId = spectating;
		if (spectating != this.clientEntityId()) {
			attachEntity(this.spectatingClientEntityId);
		}
	}

	public Int2ObjectMap<VirtualHologramEntity> getHolograms() {
		return holograms;
	}

	public boolean isSpectator() {
		return clientEntityGameMode == 3;
	}

	public void setClientEntityGameMode(int clientEntityGameMode) {
		this.clientEntityGameMode = clientEntityGameMode;
	}
}
