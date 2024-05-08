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
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.data;

import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetaIndex;
import com.viaversion.viarewind.api.type.Types1_7_6_10;
import com.viaversion.viarewind.api.type.metadata.MetaType1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter1_7_6_10To1_8;
import com.viaversion.viarewind.utils.math.AABB;
import com.viaversion.viarewind.utils.math.Vector3d;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.TrackedEntityImpl;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEvent;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEventImpl;

import java.util.ArrayList;
import java.util.List;

public class VirtualHologramEntity {
	private final List<Metadata> metadataTracker = new ArrayList<>();
	private double locX, locY, locZ;

	private final UserConnection user;
	private final int entityId;

	private int[] entityIds = null;
	private State currentState = null;
	private String name = null;
	private float yaw, pitch;
	private float headYaw;
	private boolean small = false;
	private boolean marker = false;

	public VirtualHologramEntity(final UserConnection user, final int entityId) {
		this.user = user;
		this.entityId = entityId;
	}

	public void setPosition(final double x, final double y, final double z) throws Exception {
		if (x == this.locX && y == this.locY && z == this.locZ) return;
		this.locX = x;
		this.locY = y;
		this.locZ = z;

		updateLocation(false);
	}

	public void setRelativePosition(final double x, final double y, final double z) throws Exception {
		if (x == 0.0 && y == 0.0 && z == 0.0) return;
		this.locX += x;
		this.locY += y;
		this.locZ += z;

		updateLocation(false);
	}

	public void setRotation(final float yaw, final float pitch) throws Exception {
		if (this.yaw == yaw || this.headYaw == yaw || this.pitch == pitch) return;
		this.yaw = yaw;
		this.headYaw = yaw;
		this.pitch = pitch;

		updateLocation(false);
	}

	public void setHeadYaw(float yaw) throws Exception {
		if (this.headYaw == yaw) return;
		this.headYaw = yaw;

		updateLocation(false);
	}

	public void syncState(final MetadataRewriter1_7_6_10To1_8 entityRewriter, final List<Metadata> metadataList) throws Exception {
		// Merge metadata updates into current tracker
		for (Metadata metadata : metadataList) {
			metadataTracker.removeIf(m -> m.id() == metadata.id());
			metadataTracker.add(metadata);
		}

		// Filter armor stand data to calculate emulation
		byte flags = 0;
		byte armorStandFlags = 0;
		for (Metadata metadata : metadataTracker) {
			if (metadata.id() == 0 && metadata.metaType() == MetaType1_8.Byte) {
				flags = ((Number) metadata.getValue()).byteValue();
			} else if (metadata.id() == 2 && metadata.metaType() == MetaType1_8.String) {
				name = metadata.getValue().toString();
				if (name != null && name.isEmpty()) name = null;
			} else if (metadata.id() == 10 && metadata.metaType() == MetaType1_8.Byte) {
				armorStandFlags = ((Number) metadata.getValue()).byteValue();
			}
		}
		final boolean invisible = (flags & 0x20) != 0;
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
			sendSpawnPacket(entityRewriter);
		} else {
			sendMetadataUpdate(entityRewriter);
			updateLocation(false);
		}
	}

	private void updateLocation(final boolean remount) throws Exception {
		if (entityIds == null) {
			return;
		}
		if (currentState == State.ZOMBIE) {
			teleportEntity(entityId, locX, locY, locZ, yaw, pitch);

			final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_HEAD_LOOK, user);

			entityHeadLook.write(Type.INT, entityId);
			entityHeadLook.write(Type.BYTE, (byte) ((headYaw / 360f) * 256));

			entityHeadLook.send(Protocol1_7_6_10To1_8.class);
		} else if (currentState == State.HOLOGRAM) {
			if (remount) {
				PacketWrapper detach = PacketWrapper.create(ClientboundPackets1_7_2_5.ATTACH_ENTITY, user);
				detach.write(Type.INT, entityIds[1]);
				detach.write(Type.INT, -1);
				detach.write(Type.BOOLEAN, false);
				detach.send(Protocol1_7_6_10To1_8.class);
			}

			// Don't ask me where this offset is coming from
			teleportEntity(entityIds[0], locX, (locY + (marker ? 54.85 : small ? 56 : 57) - 0.16), locZ, 0, 0); // Skull

			if (remount) {
				teleportEntity(entityIds[1], locX, locY + 56.75, locZ, 0, 0); // Horse

				PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_7_2_5.ATTACH_ENTITY, null, user);
				attach.write(Type.INT, entityIds[1]);
				attach.write(Type.INT, entityIds[0]);
				attach.write(Type.BOOLEAN, false);
				attach.send(Protocol1_7_6_10To1_8.class);
			}
		}
	}

	protected void teleportEntity(final int entityId, final double x, final double y, final double z, final float yaw, final float pitch) throws Exception {
		final PacketWrapper entityTeleport = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_TELEPORT, user);

		entityTeleport.write(Type.INT, entityId); // entity id
		entityTeleport.write(Type.INT, (int) (x * 32.0)); // x
		entityTeleport.write(Type.INT, (int) (y * 32.0)); // y
		entityTeleport.write(Type.INT, (int) (z * 32.0)); // z
		entityTeleport.write(Type.BYTE, (byte) ((yaw / 360f) * 256)); // yaw
		entityTeleport.write(Type.BYTE, (byte) ((pitch / 360f) * 256)); // pitch

		entityTeleport.send(Protocol1_7_6_10To1_8.class);
	}

	protected void spawnEntity(final int entityId, final int type, final double locX, final double locY, final double locZ) throws Exception {
		final PacketWrapper spawnMob = PacketWrapper.create(ClientboundPackets1_7_2_5.SPAWN_MOB, null, user);

		spawnMob.write(Type.VAR_INT, entityId); // entity id
		spawnMob.write(Type.UNSIGNED_BYTE, (short) type); // type
		spawnMob.write(Type.INT, (int) (locX * 32.0)); // x
		spawnMob.write(Type.INT, (int) (locY * 32.0)); // y
		spawnMob.write(Type.INT, (int) (locZ * 32.0)); // z
		spawnMob.write(Type.BYTE, (byte) 0); // yaw
		spawnMob.write(Type.BYTE, (byte) 0); // pitch
		spawnMob.write(Type.BYTE, (byte) 0); // head pitch
		spawnMob.write(Type.SHORT, (short) 0); // velocity x
		spawnMob.write(Type.SHORT, (short) 0); // velocity y
		spawnMob.write(Type.SHORT, (short) 0); // velocity z
		spawnMob.write(Types1_7_6_10.METADATA_LIST, new ArrayList<>()); // metadata

		spawnMob.send(Protocol1_7_6_10To1_8.class);
	}

	public void sendMetadataUpdate(final MetadataRewriter1_7_6_10To1_8 entityRewriter) throws Exception {
		if (entityIds == null) {
			return;
		}
		final PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_METADATA, user);

		if (currentState == State.ZOMBIE) {
			writeZombieMeta(entityRewriter, metadataPacket);
		} else if (currentState == State.HOLOGRAM) {
			writeHologramMeta(metadataPacket);
		} else {
			return;
		}
		metadataPacket.send(Protocol1_7_6_10To1_8.class);
	}

	private void writeZombieMeta(final MetadataRewriter1_7_6_10To1_8 entityRewriter, PacketWrapper wrapper) {
		wrapper.write(Type.INT, entityIds[0]);

		// Filter metadata sent by the server and convert them together with our custom metadata
		final List<Metadata> metadataList = new ArrayList<>();
		for (Metadata metadata : metadataTracker) {
			// Remove non existent metadata
			if (metadata.id() < 0 || metadata.id() > 9) {
				continue;
			}
			metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
		}
		if (small) {
			metadataList.add(new Metadata(12, MetaType1_8.Byte, (byte) 1));
		}

		// Push metadata from the server through metadata conversion 1.7->1.8
		for (Metadata metadata : metadataList.toArray(new Metadata[0])) {
			final MetaHandlerEvent event = new MetaHandlerEventImpl(wrapper.user(), new TrackedEntityImpl(EntityTypes1_10.EntityType.ZOMBIE), -1, metadata, metadataList);
			try {
				entityRewriter.handleMetadata(event, metadata);
			} catch (Exception e) {
				metadataList.remove(metadata);
				break;
			}
			if (event.cancelled()) {
				metadataList.remove(metadata);
				break;
			}
		}
		wrapper.write(Types1_7_6_10.METADATA_LIST, metadataList);
	}

	private void writeHologramMeta(PacketWrapper wrapper) {
		wrapper.write(Type.INT, entityIds[1]);

		// Directly write 1.7 metadata here since we are making them up
		final List<Metadata> metadataList = new ArrayList<>();
		metadataList.add(new Metadata(MetaIndex.ENTITY_AGEABLE_AGE.getIndex(), MetaType1_7_6_10.Int, -1700000));
		metadataList.add(new Metadata(MetaIndex.ENTITY_LIVING_NAME_TAG.getIndex(), MetaType1_7_6_10.String, name));
		metadataList.add(new Metadata(MetaIndex.ENTITY_LIVING_NAME_TAG_VISIBILITY.getIndex(), MetaType1_7_6_10.Byte, (byte) 1));

		wrapper.write(Types1_7_6_10.METADATA_LIST, metadataList);
	}

	public void sendSpawnPacket(final MetadataRewriter1_7_6_10To1_8 entityRewriter) throws Exception {
		if (entityIds != null) {
			deleteEntity();
		}
		if (currentState == State.ZOMBIE) {
			spawnEntity(entityId, 54, locX, locY, locZ);

			entityIds = new int[]{entityId};
		} else if (currentState == State.HOLOGRAM) {
			final int[] entityIds = { entityId, additionalEntityId() };

			final PacketWrapper spawnSkull = PacketWrapper.create(ClientboundPackets1_7_2_5.SPAWN_ENTITY, user);
			spawnSkull.write(Type.VAR_INT, entityIds[0]);
			spawnSkull.write(Type.BYTE, (byte) 66);
			spawnSkull.write(Type.INT, (int) (locX * 32.0));
			spawnSkull.write(Type.INT, (int) (locY * 32.0));
			spawnSkull.write(Type.INT, (int) (locZ * 32.0));
			spawnSkull.write(Type.BYTE, (byte) 0);
			spawnSkull.write(Type.BYTE, (byte) 0);
			spawnSkull.write(Type.INT, 0);
			spawnSkull.send(Protocol1_7_6_10To1_8.class);

			spawnEntity(entityIds[1], 100, locX, locY, locZ); // Horse

			this.entityIds = entityIds;
		}

		sendMetadataUpdate(entityRewriter);
		updateLocation(true);
	}

	public AABB getBoundingBox() {
		final double width = this.small ? 0.25 : 0.5;
		final double height = this.small ? 0.9875 : 1.975;

		final Vector3d min = new Vector3d(this.locX - width / 2, this.locY, this.locZ - width / 2);
		final Vector3d max = new Vector3d(this.locX + width / 2, this.locY + height, this.locZ + width / 2);

		return new AABB(min, max);
	}

	private int additionalEntityId() {
		return Integer.MAX_VALUE - 16000 - entityId;
	}

	public void deleteEntity() throws Exception {
		if (entityIds == null) {
			return;
		}
		final PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7_2_5.DESTROY_ENTITIES, user);
		despawn.write(Type.BYTE, (byte) entityIds.length);
		for (int id : entityIds) {
			despawn.write(Type.INT, id);
		}
		entityIds = null;
		despawn.send(Protocol1_7_6_10To1_8.class);
	}

	private enum State {
		HOLOGRAM, ZOMBIE
	}

	public int getEntityId() {
		return entityId;
	}
}
