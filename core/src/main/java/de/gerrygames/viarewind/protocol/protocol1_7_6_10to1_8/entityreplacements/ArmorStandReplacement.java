package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.MetaType1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.math.AABB;
import de.gerrygames.viarewind.utils.math.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class ArmorStandReplacement extends EntityReplacement1_7to1_8 {
	private final int entityId;
	private final List<Metadata> datawatcher = new ArrayList<>();
	private int[] entityIds = null;
	private double locX, locY, locZ;
	private State currentState = null;
	private boolean invisible = false;
	private boolean nameTagVisible = false;
	private String name = null;
	private float yaw, pitch;
	private float headYaw;
	private boolean small = false;
	private boolean marker = false;

	public int getEntityId() {
		return this.entityId;
	}

	private enum State {
		HOLOGRAM, ZOMBIE
	}

	public ArmorStandReplacement(int entityId, UserConnection user) {
		super(user);
		this.entityId = entityId;
	}

	public void setLocation(double x, double y, double z) {
		if (x != this.locX || y != this.locY || z != this.locZ) {
			this.locX = x;
			this.locY = y;
			this.locZ = z;
			updateLocation();
		}
	}

	public void relMove(double x, double y, double z) {
		if (x == 0.0 && y == 0.0 && z == 0.0) return;
		this.locX += x;
		this.locY += y;
		this.locZ += z;
		updateLocation();
	}

	public void setYawPitch(float yaw, float pitch) {
		if (this.yaw != yaw && this.pitch != pitch || this.headYaw != yaw) {
			this.yaw = yaw;
			this.headYaw = yaw;
			this.pitch = pitch;
			updateLocation();
		}
	}

	public void setHeadYaw(float yaw) {
		if (this.headYaw != yaw) {
			this.headYaw = yaw;
			updateLocation();
		}
	}

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
				if (name != null && name.equals("")) name = null;
			} else if (metadata.id() == 10 && metadata.metaType() == MetaType1_8.Byte) {
				armorStandFlags = ((Number) metadata.getValue()).byteValue();
			} else if (metadata.id() == 3 && metadata.metaType() == MetaType1_8.Byte) {
				nameTagVisible = ((Number) metadata.getValue()).byteValue() != 0;
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
			despawn();
			spawn();
		} else {
			updateMetadata();
			updateLocation();
		}
	}

	public void updateLocation() {
		if (entityIds == null) return;

		if (currentState == State.ZOMBIE) {
			updateZombieLocation();
		} else if (currentState == State.HOLOGRAM) {
			updateHologramLocation();
		}
	}

	private void updateZombieLocation() {
		sendTeleportWithHead(entityId, locX, locY, locZ, yaw, pitch, headYaw);
	}

	private void updateHologramLocation() {
		PacketWrapper detach = PacketWrapper.create(ClientboundPackets1_7.ATTACH_ENTITY, null, user);
		detach.write(Type.INT, entityIds[1]);
		detach.write(Type.INT, -1);
		detach.write(Type.BOOLEAN, false);
		PacketUtil.sendPacket(detach, Protocol1_7_6_10TO1_8.class, true, true);

		// Don't ask me where this offset is coming from
		sendTeleport(entityIds[0], locX, (locY + (marker ? 54.85 : small ? 56 : 57)), locZ, 0, 0); // Skull
		sendTeleport(entityIds[1], locX, locY + 56.75, locZ, 0, 0); // Horse

		PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_7.ATTACH_ENTITY, null, user);
		attach.write(Type.INT, entityIds[1]);
		attach.write(Type.INT, entityIds[0]);
		attach.write(Type.BOOLEAN, false);
		PacketUtil.sendPacket(attach, Protocol1_7_6_10TO1_8.class, true, true);
	}

	public void updateMetadata() {
		if (entityIds == null) return;

		PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_7.ENTITY_METADATA, null, user);

		if (currentState == State.ZOMBIE) {
			writeZombieMeta(metadataPacket);
		} else if (currentState == State.HOLOGRAM) {
			writeHologramMeta(metadataPacket);
		} else {
			return;
		}

		PacketUtil.sendPacket(metadataPacket, Protocol1_7_6_10TO1_8.class, true, true);
	}

	private void writeZombieMeta(PacketWrapper metadataPacket) {
		metadataPacket.write(Type.INT, entityIds[0]);

		List<Metadata> metadataList = new ArrayList<>();
		for (Metadata metadata : datawatcher) {
			if (metadata.id() < 0 || metadata.id() > 9) continue;
			metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
		}
		if (small) metadataList.add(new Metadata(12, MetaType1_8.Byte, (byte) 1));
		MetadataRewriter.transform(Entity1_10Types.EntityType.ZOMBIE, metadataList);

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

	public void spawn() {
		if (entityIds != null) despawn();

		if (currentState == State.ZOMBIE) {
			spawnZombie();
		} else if (currentState == State.HOLOGRAM) {
			spawnHologram();
		}

		updateMetadata();
		updateLocation();
	}

	private void spawnZombie() {
		sendSpawn(entityId, 54, locX, locY, locZ);

		entityIds = new int[] {entityId};
	}

	private void spawnHologram() {
		int[] entityIds = new int[] {entityId, additionalEntityId()};

		sendSpawn(entityIds[0], 66, locX, locY, locZ); // Skull
		sendSpawn(entityIds[1], 100, locX, locY, locZ); // Horse

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

	public void despawn() {
		if (entityIds == null) return;
		PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7.DESTROY_ENTITIES, null, user);
		despawn.write(Type.BYTE, (byte) entityIds.length);
		for (int id : entityIds) {
			despawn.write(Type.INT, id);
		}
		entityIds = null;
		PacketUtil.sendPacket(despawn, Protocol1_7_6_10TO1_8.class, true, true);
	}
}
