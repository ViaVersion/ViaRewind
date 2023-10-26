package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model;

import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.rewriter.MetadataRewriter;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata.MetaType1_7_6_10;
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

public class VirtualHologramEntity {
	private final List<Metadata> metadataTracker = new ArrayList<>();
	private double locX, locY, locZ;

	private final UserConnection user;
	private final MetadataRewriter metadataRewriter;
	private final int entityId;

	private int[] entityIds = null;
	private State currentState = null;
	private boolean invisible = false;
	private String name = null;
	private float yaw, pitch;
	private float headYaw;
	private boolean small = false;
	private boolean marker = false;

	public VirtualHologramEntity(final UserConnection user, final MetadataRewriter metadataRewriter, final int entityId) {
		this.user = user;
		this.metadataRewriter = metadataRewriter;
		this.entityId = entityId;
	}

	public void updateReplacementPosition(double x, double y, double z) {
		if (x != this.locX || y != this.locY || z != this.locZ) {
			this.locX = x;
			this.locY = y;
			this.locZ = z;

			updateLocation(false);
		}
	}

	public void handleOriginalMovementPacket(double x, double y, double z) {
		if (x == 0.0 && y == 0.0 && z == 0.0) return;
		this.locX += x;
		this.locY += y;
		this.locZ += z;

		updateLocation(false);
	}

	public void setYawPitch(float yaw, float pitch) {
		if (this.yaw != yaw && this.pitch != pitch || this.headYaw != yaw) {
			this.yaw = yaw;
			this.headYaw = yaw;
			this.pitch = pitch;

			updateLocation(false);
		}
	}

	public void setHeadYaw(float yaw) {
		if (this.headYaw != yaw) {
			this.headYaw = yaw;

			updateLocation(false);
		}
	}

	public void updateMetadata(List<Metadata> metadataList) {
		for (Metadata metadata : metadataList) {
			metadataTracker.removeIf(m -> m.id() == metadata.id());
			metadataTracker.add(metadata);
		}
		updateState();
	}

	public void updateState() {
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
			teleportEntity(entityId, locX, locY, locZ, yaw, pitch);

			final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_HEAD_LOOK, user);

			entityHeadLook.write(Type.INT, entityId);
			entityHeadLook.write(Type.BYTE, (byte) ((headYaw / 360f) * 256));

			PacketUtil.sendPacket(entityHeadLook, Protocol1_7_6_10To1_8.class, true, true);
		} else if (currentState == State.HOLOGRAM) {
			if (remount) {
				PacketWrapper detach = PacketWrapper.create(ClientboundPackets1_7_2_5.ATTACH_ENTITY, null, user);
				detach.write(Type.INT, entityIds[1]);
				detach.write(Type.INT, -1);
				detach.write(Type.BOOLEAN, false);
				PacketUtil.sendPacket(detach, Protocol1_7_6_10To1_8.class, true, true);
			}

			// Don't ask me where this offset is coming from
			teleportEntity(entityIds[0], locX, (locY + (marker ? 54.85 : small ? 56 : 57) - 0.16), locZ, 0, 0); // Skull

			if (remount) {
				teleportEntity(entityIds[1], locX, locY + 56.75, locZ, 0, 0); // Horse

				PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_7_2_5.ATTACH_ENTITY, null, user);
				attach.write(Type.INT, entityIds[1]);
				attach.write(Type.INT, entityIds[0]);
				attach.write(Type.BOOLEAN, false);
				PacketUtil.sendPacket(attach, Protocol1_7_6_10To1_8.class, true, true);
			}
		}
	}

	protected void teleportEntity(final int entityId, final double x, final double y, final double z, final float yaw, final float pitch) {
		final PacketWrapper entityTeleport = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_TELEPORT, user);

		entityTeleport.write(Type.INT, entityId); // entity id
		entityTeleport.write(Type.INT, (int) (x * 32.0)); // x
		entityTeleport.write(Type.INT, (int) (y * 32.0)); // y
		entityTeleport.write(Type.INT, (int) (z * 32.0)); // z
		entityTeleport.write(Type.BYTE, (byte) ((yaw / 360f) * 256)); // yaw
		entityTeleport.write(Type.BYTE, (byte) ((pitch / 360f) * 256)); // pitch

		PacketUtil.sendPacket(entityTeleport, Protocol1_7_6_10To1_8.class, true, true);
	}

	protected void spawnEntity(final int entityId, final int type, final double locX, final double locY, final double locZ) {
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

		PacketUtil.sendPacket(spawnMob, Protocol1_7_6_10To1_8.class, true, true);
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
		for (Metadata metadata : metadataTracker) {
			if (metadata.id() < 0 || metadata.id() > 9) continue;
			metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
		}
		if (small) metadataList.add(new Metadata(12, MetaType1_8.Byte, (byte) 1));
		metadataRewriter.transform(EntityTypes1_10.EntityType.ZOMBIE, metadataList);

		metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
	}

	private void writeHologramMeta(PacketWrapper metadataPacket) {
		metadataPacket.write(Type.INT, entityIds[1]);

		List<Metadata> metadataList = new ArrayList<>();
		metadataList.add(new Metadata(MetaIndex1_7_6_10To1_8.ENTITY_AGEABLE_AGE.getIndex(), MetaType1_7_6_10.Int, -1700000));
		metadataList.add(new Metadata(MetaIndex1_7_6_10To1_8.ENTITY_LIVING_NAME_TAG.getIndex(), MetaType1_7_6_10.String, name));
		metadataList.add(new Metadata(MetaIndex1_7_6_10To1_8.ENTITY_LIVING_NAME_TAG_VISIBILITY.getIndex(), MetaType1_7_6_10.Byte, (byte) 1));

		metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
	}

	public void sendSpawnPacket() {
		if (entityIds != null) deleteEntity();

		if (currentState == State.ZOMBIE) {
			spawnEntity(entityId, 54, locX, locY, locZ);

			entityIds = new int[]{entityId};
		} else if (currentState == State.HOLOGRAM) {
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

		updateMetadata();
		updateLocation(true);
	}

	private int additionalEntityId() {
		return Integer.MAX_VALUE - 16000 - entityId;
	}

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

	public int getEntityId() {
		return entityId;
	}
}
