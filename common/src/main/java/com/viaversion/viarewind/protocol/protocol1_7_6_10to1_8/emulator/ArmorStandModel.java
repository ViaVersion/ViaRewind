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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.emulator;

import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.EntityModel1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata.MetaType1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viarewind.utils.math.AABB;
import com.viaversion.viarewind.utils.math.Vector3d;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public class ArmorStandModel extends EntityModel1_7_6_10 {
	private final int entityId;
	private final List<Metadata> datawatcher = new ArrayList<>();
	private int[] entityIds = null;
	private double locX, locY, locZ;
	private State currentState = null;
	private boolean invisible = false;
	private String name = null;
	private float yaw, pitch;
	private float headYaw;
	private boolean small = false;
	private boolean marker = false;

	public ArmorStandModel(UserConnection user, Protocol1_7_6_10To1_8 protocol, int entityId) {
		super(user, protocol);
		this.entityId = entityId;
	}

	@Override
	public int getEntityId() {
		return this.entityId;
	}

	@Override
	public void updateReplacementPosition(double x, double y, double z) {
		if (x != this.locX || y != this.locY || z != this.locZ) {
			this.locX = x;
			this.locY = y;
			this.locZ = z;
			updateLocation(false);
		}
	}

	@Override
	public void handleOriginalMovementPacket(double x, double y, double z) {
		if (x == 0.0 && y == 0.0 && z == 0.0) return;
		this.locX += x;
		this.locY += y;
		this.locZ += z;
		updateLocation(false);
	}

	@Override
	public void setYawPitch(float yaw, float pitch) {
		if (this.yaw != yaw && this.pitch != pitch || this.headYaw != yaw) {
			this.yaw = yaw;
			this.headYaw = yaw;
			this.pitch = pitch;
			updateLocation(false);
		}
	}

	@Override
	public void setHeadYaw(float yaw) {
		if (this.headYaw != yaw) {
			this.headYaw = yaw;
			updateLocation(false);
		}
	}

	@Override
	public void updateMetadata(List<Metadata> metadataList) {
		for (Metadata metadata : metadataList) {
			datawatcher.removeIf(m -> m.id() == metadata.id());
			datawatcher.add(metadata);
		}
		updateState();
	}

	public void updateState() {
		byte flags = 0;
		byte armorStandFlags = 0;
		for (Metadata metadata : datawatcher) {
			if (metadata.id() == 0 && metadata.metaType() == MetaType1_8.Byte) {
				flags = ((Number) metadata.getValue()).byteValue();
			} else if (metadata.id() == 2 && metadata.metaType() == MetaType1_8.String) {
				name = metadata.getValue().toString();
				if (name != null && name.isEmpty()) name = null;
			} else if (metadata.id() == 10 && metadata.metaType() == MetaType1_8.Byte) {
				armorStandFlags = ((Number) metadata.getValue()).byteValue();
			}
		}
		invisible = (flags & 0x20) != 0;
		small = (armorStandFlags & 0x01) != 0;
		marker = (armorStandFlags & 0x10) != 0;

		State prevState = currentState;
		if (invisible && name != null) {
			currentState = State.HOLOGRAM;
		} else {
			currentState = State.ZOMBIE;
		}

		if (currentState != prevState) {
			deleteEntity();
			sendSpawnPacket();
		} else {
			updateMetadata();
			updateLocation(false);
		}
	}

	public void updateLocation(boolean remount) {
		if (entityIds == null) return;

		if (currentState == State.ZOMBIE) {
			updateZombieLocation();
		} else if (currentState == State.HOLOGRAM) {
			updateHologramLocation(remount);
		}
	}

	private void updateZombieLocation() {
		teleportAndUpdate(entityId, locX, locY, locZ, yaw, pitch, headYaw);
	}

	private void updateHologramLocation(boolean remount) {
		if (remount) {
			PacketWrapper detach = PacketWrapper.create(ClientboundPackets1_7_2_5.ATTACH_ENTITY, null, user);
			detach.write(Type.INT, entityIds[1]);
			detach.write(Type.INT, -1);
			detach.write(Type.BOOLEAN, false);
			PacketUtil.sendPacket(detach, Protocol1_7_6_10To1_8.class, true, true);
		}

		// Don't ask me where this offset is coming from
		teleportEntity(entityIds[0], locX, (locY + (marker ? 54.85 : small ? 56 : 57)), locZ, 0, 0); // Skull

		if (remount) {
			teleportEntity(entityIds[1], locX, locY + 56.75, locZ, 0, 0); // Horse

			PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_7_2_5.ATTACH_ENTITY, null, user);
			attach.write(Type.INT, entityIds[1]);
			attach.write(Type.INT, entityIds[0]);
			attach.write(Type.BOOLEAN, false);
			PacketUtil.sendPacket(attach, Protocol1_7_6_10To1_8.class, true, true);
		}
	}

	public void updateMetadata() {
		if (entityIds == null) return;

		PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_METADATA, null, user);

		if (currentState == State.ZOMBIE) {
			writeZombieMeta(metadataPacket);
		} else if (currentState == State.HOLOGRAM) {
			writeHologramMeta(metadataPacket);
		} else {
			return;
		}

		PacketUtil.sendPacket(metadataPacket, Protocol1_7_6_10To1_8.class, true, true);
	}

	private void writeZombieMeta(PacketWrapper metadataPacket) {
		metadataPacket.write(Type.INT, entityIds[0]);

		List<Metadata> metadataList = new ArrayList<>();
		for (Metadata metadata : datawatcher) {
			if (metadata.id() < 0 || metadata.id() > 9) continue;
			metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
		}
		if (small) metadataList.add(new Metadata(12, MetaType1_8.Byte, (byte) 1));
		getProtocol().getMetadataRewriter().transform(EntityTypes1_10.EntityType.ZOMBIE, metadataList);

		metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
	}

	private void writeHologramMeta(PacketWrapper metadataPacket) {
		metadataPacket.write(Type.INT, entityIds[1]);

		List<Metadata> metadataList = new ArrayList<>();
		metadataList.add(new Metadata(12, MetaType1_7_6_10.Int, -1700000));
		metadataList.add(new Metadata(10, MetaType1_7_6_10.String, name));
		metadataList.add(new Metadata(11, MetaType1_7_6_10.Byte, (byte) 1));

		metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
	}

	@Override
	public void sendSpawnPacket() {
		if (entityIds != null) deleteEntity();

		if (currentState == State.ZOMBIE) {
			spawnZombie();
		} else if (currentState == State.HOLOGRAM) {
			spawnHologram();
		}

		updateMetadata();
		updateLocation(true);
	}

	private void spawnZombie() {
		spawnEntity(entityId, 54, locX, locY, locZ);

		entityIds = new int[]{entityId};
	}

	private void spawnHologram() {
		int[] entityIds = {entityId, additionalEntityId()};

		PacketWrapper spawnSkull = PacketWrapper.create(ClientboundPackets1_7_2_5.SPAWN_ENTITY, null, user);
		spawnSkull.write(Type.VAR_INT, entityIds[0]);
		spawnSkull.write(Type.BYTE, (byte) 66);
		spawnSkull.write(Type.INT, (int) (locX * 32.0));
		spawnSkull.write(Type.INT, (int) (locY * 32.0));
		spawnSkull.write(Type.INT, (int) (locZ * 32.0));
		spawnSkull.write(Type.BYTE, (byte) 0);
		spawnSkull.write(Type.BYTE, (byte) 0);
		spawnSkull.write(Type.INT, 0);
		PacketUtil.sendPacket(spawnSkull, Protocol1_7_6_10To1_8.class, true, true);

		spawnEntity(entityIds[1], 100, locX, locY, locZ); // Horse

		this.entityIds = entityIds;
	}

	private int additionalEntityId() {
		return Integer.MAX_VALUE - 16000 - entityId;
	}

	public AABB getBoundingBox() {
		double w = this.small ? 0.25 : 0.5;
		double h = this.small ? 0.9875 : 1.975;
		Vector3d min = new Vector3d(this.locX - w / 2, this.locY, this.locZ - w / 2);
		Vector3d max = new Vector3d(this.locX + w / 2, this.locY + h, this.locZ + w / 2);
		return new AABB(min, max);
	}

	@Override
	public void deleteEntity() {
		if (entityIds == null) return;
		PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7_2_5.DESTROY_ENTITIES, null, user);
		despawn.write(Type.BYTE, (byte) entityIds.length);
		for (int id : entityIds) {
			despawn.write(Type.INT, id);
		}
		entityIds = null;
		PacketUtil.sendPacket(despawn, Protocol1_7_6_10To1_8.class, true, true);
	}

	private enum State {
		HOLOGRAM, ZOMBIE
	}
}
