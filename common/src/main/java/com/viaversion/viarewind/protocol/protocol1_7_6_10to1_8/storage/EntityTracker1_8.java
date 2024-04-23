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
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.rewriter.ReplacementEntityTracker;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.VirtualHologramEntity;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.data.MetadataRewriter;
import com.viaversion.viarewind.api.type.metadata.MetaType1_7_6_10;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EntityTracker1_8 extends ReplacementEntityTracker {

	protected final MetadataRewriter metadataRewriter;

	private final Map<Integer, Integer> vehicleMap = new ConcurrentHashMap<>();
	private final Map<Integer, VirtualHologramEntity> virtualHologramMap = new HashMap<>();

	private final Map<Integer, UUID> playersByEntityId = new HashMap<>();
	private final Map<UUID, Integer> playersByUniqueId = new HashMap<>();

	public int spectatingPlayerId = -1;

	public EntityTracker1_8(UserConnection user, final MetadataRewriter metadataRewriter) {
		super(user, ProtocolVersion.v1_8, MetaType1_7_6_10.Byte, MetaType1_7_6_10.String);
		this.metadataRewriter = metadataRewriter;

		registerEntity(EntityTypes1_10.EntityType.GUARDIAN, EntityTypes1_10.EntityType.SQUID, "Guardian");
		registerEntity(EntityTypes1_10.EntityType.ENDERMITE, EntityTypes1_10.EntityType.SQUID, "Endermite");
		registerEntity(EntityTypes1_10.EntityType.RABBIT, EntityTypes1_10.EntityType.CHICKEN, "Rabbit");
	}

	public void trackHologram(final int entityId, final VirtualHologramEntity hologram) {
		addEntity(entityId, EntityTypes1_10.EntityType.ARMOR_STAND);
		getEntityReplacementMap().put(entityId, EntityTypes1_10.EntityType.ARMOR_STAND);

		virtualHologramMap.put(entityId, hologram);
	}

	@Override
	public void updateMetadata(int entityId, List<Metadata> metadata) throws Exception {
		if (virtualHologramMap.containsKey(entityId)) {
			virtualHologramMap.get(entityId).updateMetadata(metadata);
			return;
		}

		super.updateMetadata(entityId, metadata);
	}

	@Override
	public void removeEntity(int entityId) {
		super.removeEntity(entityId);

		if (playersByEntityId.containsKey(entityId)) {
			final UUID playerId = playersByEntityId.remove(entityId);

			playersByUniqueId.remove(playerId);
			getUser().get(PlayerSessionStorage.class).getPlayerEquipment().remove(playerId);
		}
	}

	@Override
	public void clear() {
		super.clear();

		vehicleMap.clear();
	}

	@Override
	public void setClientEntityId(int entityId) {
		if (this.spectatingPlayerId == this.getPlayerId()) {
			this.spectatingPlayerId = entityId;
		}
		super.setClientEntityId(entityId);
	}

	public void addPlayer(final Integer entityId, final UUID uuid) {
		playersByUniqueId.put(uuid, entityId);
		playersByEntityId.put(entityId, uuid);
	}

	public UUID getPlayerUUID(final int entityId) {
		return playersByEntityId.get(entityId);
	}

	public int getPlayerEntityId(final UUID uuid) {
		return playersByUniqueId.getOrDefault(uuid, -1);
	}

	public int getVehicle(final int passengerId) {
		for (Map.Entry<Integer, Integer> vehicle : vehicleMap.entrySet()) {
			if (vehicle.getValue() == passengerId) {
				return vehicle.getValue();
			}
		}
		return -1;
	}

	public int getPassenger(int vehicleId) {
		return vehicleMap.getOrDefault(vehicleId, -1);
	}

	protected void startSneaking() {
		try {
			final PacketWrapper entityAction = PacketWrapper.create(ServerboundPackets1_7_2_5.ENTITY_ACTION, getUser());
			entityAction.write(Type.VAR_INT, this.getPlayerId()); // entity id
			entityAction.write(Type.VAR_INT, 0); // action id, start sneaking
			entityAction.write(Type.VAR_INT, 0); // jump boost

			entityAction.sendToServer(Protocol1_7_6_10To1_8.class);
		} catch (Exception e) {
			ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send sneak packet", e);
		}
	}

	public void setPassenger(int vehicleId, int passengerId) {
		if (vehicleId == this.spectatingPlayerId && this.spectatingPlayerId != this.getPlayerId()) {
			startSneaking();
			setSpectating(this.getPlayerId());
		}

		if (vehicleId == -1) {
			vehicleMap.remove(getVehicle(passengerId));
		} else if (passengerId == -1) {
			vehicleMap.remove(vehicleId);
		} else {
			vehicleMap.put(vehicleId, passengerId);
		}
	}

	protected void attachEntity(final int target) {
		try {
			final PacketWrapper attachEntity = PacketWrapper.create(ClientboundPackets1_8.ATTACH_ENTITY, getUser());
			attachEntity.write(Type.INT, this.getPlayerId()); // vehicle id
			attachEntity.write(Type.INT, target); // passenger id
			attachEntity.write(Type.BOOLEAN, false); // leash

			attachEntity.scheduleSend(Protocol1_7_6_10To1_8.class, true);
		} catch (Exception e) {
			ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send attach packet", e);
		}
	}

	public void setSpectating(int spectating) {
		if (spectating != this.getPlayerId() && getPassenger(spectating) != -1) {
			startSneaking();
			setSpectating(this.getPlayerId());
			return;
		}
		if (this.spectatingPlayerId != spectating && this.spectatingPlayerId != this.getPlayerId()) {
			attachEntity(-1);
		}
		this.spectatingPlayerId = spectating;
		if (spectating != this.getPlayerId()) {
			attachEntity(this.spectatingPlayerId);
		}
	}

	public Map<Integer, VirtualHologramEntity> getVirtualHologramMap() {
		return virtualHologramMap;
	}
}
