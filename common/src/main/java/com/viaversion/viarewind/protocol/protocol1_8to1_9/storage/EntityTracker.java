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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.api.minecraft.EntityModel;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ClientEntityIdChangeListener {
	protected final Protocol1_8To1_9 protocol;
	private final Map<Integer, List<Integer>> vehicleMap = new ConcurrentHashMap<>();
	private final Map<Integer, EntityTypes1_10.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
	private final Map<Integer, List<Metadata>> metadataBuffer = new ConcurrentHashMap<>();
	private final Map<Integer, EntityModel> entityReplacements = new ConcurrentHashMap<>();
	private final Map<Integer, Vector> entityOffsets = new ConcurrentHashMap<>();
	private int playerId;
	private int playerGamemode = 0;

	public EntityTracker(UserConnection user, Protocol1_8To1_9 protocol) {
		super(user);

		this.protocol = protocol;
	}

	public void setPlayerId(int entityId) {
		playerId = entityId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getPlayerGamemode() {
		return playerGamemode;
	}

	public void setPlayerGamemode(int playerGamemode) {
		this.playerGamemode = playerGamemode;
	}

	public void removeEntity(int entityId) {
		vehicleMap.remove(entityId);
		vehicleMap.forEach((vehicle, passengers) -> passengers.remove((Integer) entityId));
		vehicleMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
		clientEntityTypes.remove(entityId);
		entityOffsets.remove(entityId);
		if (entityReplacements.containsKey(entityId)) {
			entityReplacements.remove(entityId).deleteEntity();
		}
	}

	public void resetEntityOffset(int entityId) {
		entityOffsets.remove(entityId);
	}

	public Vector getEntityOffset(int entityId) {
		return entityOffsets.get(entityId);
	}

	public void addToEntityOffset(int entityId, short relX, short relY, short relZ) {
		entityOffsets.compute(entityId, (key, offset) -> {
			if (offset == null) {
				return new Vector(relX, relY, relZ);
			} else {
				return new Vector(offset.blockX() + relX, offset.blockY() + relY, offset.blockZ() + relZ);
			}
		});
	}

	public void setEntityOffset(int entityId, short relX, short relY, short relZ) {
		entityOffsets.compute(entityId, (key, offset) -> new Vector(relX, relY, relZ));
	}

	public void setEntityOffset(int entityId, Vector offset) {
		entityOffsets.put(entityId, offset);
	}

	public List<Integer> getPassengers(int entityId) {
		return vehicleMap.getOrDefault(entityId, new ArrayList<>());
	}

	public void setPassengers(int entityId, List<Integer> passengers) {
		vehicleMap.put(entityId, passengers);
	}

	public void addEntityReplacement(EntityModel entityModel) {
		entityReplacements.put(entityModel.getEntityId(), entityModel);
	}

	public EntityModel getEntityReplacement(int entityId) {
		return entityReplacements.get(entityId);
	}

	public Map<Integer, EntityTypes1_10.EntityType> getClientEntityTypes() {
		return this.clientEntityTypes;
	}

	public void addMetadataToBuffer(int entityID, List<Metadata> metadataList) {
		if (this.metadataBuffer.containsKey(entityID)) {
			this.metadataBuffer.get(entityID).addAll(metadataList);
		} else if (!metadataList.isEmpty()) {
			this.metadataBuffer.put(entityID, metadataList);
		}
	}

	public List<Metadata> getBufferedMetadata(int entityId) {
		return metadataBuffer.get(entityId);
	}

	public boolean isInsideVehicle(int entityId) {
		for (List<Integer> vehicle : vehicleMap.values()) {
			if (vehicle.contains(entityId)) return true;
		}
		return false;
	}

	public int getVehicle(int passenger) {
		for (Map.Entry<Integer, List<Integer>> vehicle : vehicleMap.entrySet()) {
			if (vehicle.getValue().contains(passenger)) return vehicle.getKey();
		}
		return -1;
	}

	public boolean isPassenger(int vehicle, int passenger) {
		return vehicleMap.containsKey(vehicle) && vehicleMap.get(vehicle).contains(passenger);
	}

	public void sendMetadataBuffer(int entityId) {
		if (!this.metadataBuffer.containsKey(entityId)) return;
		if (entityReplacements.containsKey(entityId)) {
			entityReplacements.get(entityId).updateMetadata(this.metadataBuffer.remove(entityId));
		} else {
			PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_8.ENTITY_METADATA, this.getUser());
			wrapper.write(Type.VAR_INT, entityId);
			wrapper.write(Types1_8.METADATA_LIST, this.metadataBuffer.get(entityId));
			protocol.getMetadataRewriter().transform(this.getClientEntityTypes().get(entityId), this.metadataBuffer.get(entityId));
			if (!this.metadataBuffer.get(entityId).isEmpty()) {
				try {
					wrapper.send(Protocol1_8To1_9.class);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			this.metadataBuffer.remove(entityId);
		}
	}

	@Override
	public void setClientEntityId(int playerEntityId) {
		clientEntityTypes.remove(this.playerId);
		this.playerId = playerEntityId;
		clientEntityTypes.put(this.playerId, EntityTypes1_10.EntityType.ENTITY_HUMAN);
	}
}
