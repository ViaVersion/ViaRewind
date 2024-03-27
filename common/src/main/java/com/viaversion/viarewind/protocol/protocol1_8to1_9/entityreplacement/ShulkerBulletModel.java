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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ShulkerBulletModel extends EntityModel1_8To1_9 {
	private final int entityId;
	private final List<Metadata> datawatcher = new ArrayList<>();
	private double locX, locY, locZ;
	private float yaw, pitch;
	private float headYaw;

	public ShulkerBulletModel(UserConnection user, Protocol1_8To1_9 protocol, int entityId) {
		super(user, protocol);
		this.entityId = entityId;
		sendSpawnPacket();
	}

	@Override
	public void updateReplacementPosition(double x, double y, double z) {
		if (x != this.locX || y != this.locY || z != this.locZ) {
			this.locX = x;
			this.locY = y;
			this.locZ = z;
			updateLocation();
		}
	}

	@Override
	public void handleOriginalMovementPacket(double x, double y, double z) {
		if (x == 0.0 && y == 0.0 && z == 0.0) return;
		this.locX += x;
		this.locY += y;
		this.locZ += z;
		updateLocation();
	}

	@Override
	public void setYawPitch(float yaw, float pitch) {
		if (this.yaw != yaw && this.pitch != pitch) {
			this.yaw = yaw;
			this.pitch = pitch;
			updateLocation();
		}
	}

	@Override
	public void setHeadYaw(float yaw) {
		this.headYaw = yaw;
	}

	@Override
	public void updateMetadata(List<Metadata> metadataList) {
	}

	public void updateLocation() {
		sendTeleportWithHead(entityId, locX, locY, locZ, yaw, pitch, headYaw);
	}

	@Override
	public void sendSpawnPacket() {
		sendSpawnEntity(entityId, 66);
	}

	@Override
	public void deleteEntity() {
		PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_8.DESTROY_ENTITIES, null, user);
		despawn.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});

		PacketUtil.sendPacket(despawn, Protocol1_8To1_9.class, true, true);
	}

	@Override
	public int getEntityId() {
		return this.entityId;
	}
}
