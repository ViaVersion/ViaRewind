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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.ArrayList;
import java.util.List;

public class ShulkerModel extends EntityModel1_8To1_9 {
	private final int entityId;
	private final List<Metadata> datawatcher = new ArrayList<>();
	private double locX, locY, locZ;

	public ShulkerModel(UserConnection user, Protocol1_8To1_9 protocol, int entityId) {
		super(user, protocol);
		this.entityId = entityId;
		sendSpawnPacket();
	}

	@Override
	public void updateReplacementPosition(double x, double y, double z) {
		this.locX = x;
		this.locY = y;
		this.locZ = z;
		updateLocation();
	}

	@Override
	public void handleOriginalMovementPacket(double x, double y, double z) {
		this.locX += x;
		this.locY += y;
		this.locZ += z;
		updateLocation();
	}

	@Override
	public void setYawPitch(float yaw, float pitch) {
	}

	@Override
	public void setHeadYaw(float yaw) {
	}

	@Override
	public void updateMetadata(List<Metadata> metadataList) {
		for (Metadata metadata : metadataList) {
			datawatcher.removeIf(m -> m.id() == metadata.id());
			datawatcher.add(metadata);
		}
		updateMetadata();
	}

	public void updateLocation() {
		teleportEntity(entityId, locX, locY, locZ, 0, 0);
	}

	public void updateMetadata() {
		PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_8.ENTITY_METADATA, null, user);
		metadataPacket.write(Type.VAR_INT, entityId);

		List<Metadata> metadataList = new ArrayList<>();
		for (Metadata metadata : datawatcher) {
			if (metadata.id() == 11 || metadata.id() == 12 || metadata.id() == 13) continue;
			metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
		}
		metadataList.add(new Metadata(11, MetaType1_9.VarInt, 2));

		getProtocol().getMetadataRewriter().transform(user.get(EntityTracker.class), entityId, metadataList, EntityTypes1_10.EntityType.MAGMA_CUBE);

		metadataPacket.write(Types1_8.METADATA_LIST, metadataList);

		PacketUtil.sendPacket(metadataPacket, Protocol1_8To1_9.class);
	}

	@Override
	public void sendSpawnPacket() {
		sendSpawn(entityId, 62);
		// Old clients don't like empty metadata
	}

	@Override
	public void deleteEntity() {
		PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7_2_5.DESTROY_ENTITIES, null, user);
		despawn.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});

		PacketUtil.sendPacket(despawn, Protocol1_8To1_9.class, true, true);
	}

	@Override
	public int getEntityId() {
		return this.entityId;
	}
}
