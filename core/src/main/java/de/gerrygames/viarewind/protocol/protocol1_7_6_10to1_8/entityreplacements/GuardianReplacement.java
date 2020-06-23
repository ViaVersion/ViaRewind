package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import lombok.Getter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public class GuardianReplacement implements EntityReplacement {
	@Getter
	private int entityId;
	private List<Metadata> datawatcher = new ArrayList<>();
	private double locX, locY, locZ;
	private float yaw, pitch;
	private float headYaw;
	private UserConnection user;

	public GuardianReplacement(int entityId, UserConnection user) {
		this.entityId = entityId;
		this.user = user;
		spawn();
	}

	public void setLocation(double x, double y, double z) {
		this.locX = x;
		this.locY = y;
		this.locZ = z;
		updateLocation();
	}

	public void relMove(double x, double y, double z) {
		this.locX += x;
		this.locY += y;
		this.locZ += z;
		updateLocation();
	}

	public void setYawPitch(float yaw, float pitch) {
		if (this.yaw!=yaw || this.pitch!=pitch) {
			this.yaw = yaw;
			this.pitch = pitch;
			updateLocation();
		}
	}

	public void setHeadYaw(float yaw) {
		if (this.headYaw!=yaw) {
			this.headYaw = yaw;
			updateLocation();
		}
	}

	public void updateMetadata(List<Metadata> metadataList) {
		for (Metadata metadata : metadataList) {
			datawatcher.removeIf(m -> m.getId()==metadata.getId());
			datawatcher.add(metadata);
		}
		updateMetadata();
	}

	public void updateLocation() {
		PacketWrapper teleport = new PacketWrapper(0x18, null, user);
		teleport.write(Type.INT, entityId);
		teleport.write(Type.INT, (int) (locX * 32.0));
		teleport.write(Type.INT, (int) (locY * 32.0));
		teleport.write(Type.INT, (int) (locZ * 32.0));
		teleport.write(Type.BYTE, (byte)((yaw / 360f) * 256));
		teleport.write(Type.BYTE, (byte)((pitch / 360f) * 256));

		PacketWrapper head = new PacketWrapper(0x19, null, user);
		head.write(Type.INT, entityId);
		head.write(Type.BYTE, (byte)((headYaw / 360f) * 256));

		PacketUtil.sendPacket(teleport, Protocol1_7_6_10TO1_8.class, true, true);
		PacketUtil.sendPacket(head, Protocol1_7_6_10TO1_8.class, true, true);
	}

	public void updateMetadata() {
		PacketWrapper metadataPacket = new PacketWrapper(0x1C, null, user);
		metadataPacket.write(Type.INT, entityId);

		List<Metadata> metadataList = new ArrayList<>();
		for (Metadata metadata : datawatcher) {
			if (metadata.getId()==16 || metadata.getId()==17) continue;
			metadataList.add(new Metadata(metadata.getId(), metadata.getMetaType(), metadata.getValue()));
		}

		MetadataRewriter.transform(Entity1_10Types.EntityType.SQUID, metadataList);

		metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);

		PacketUtil.sendPacket(metadataPacket, Protocol1_7_6_10TO1_8.class);
	}

	@Override
	public void spawn() {
		PacketWrapper spawn = new PacketWrapper(0x0F, null, user);
		spawn.write(Type.VAR_INT, entityId);
		spawn.write(Type.UNSIGNED_BYTE, (short) 94);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Types1_7_6_10.METADATA_LIST, new ArrayList<>());

		PacketUtil.sendPacket(spawn, Protocol1_7_6_10TO1_8.class, true, true);
	}

	@Override
	public void despawn() {
		PacketWrapper despawn = new PacketWrapper(0x13, null, user);
		despawn.write(Types1_7_6_10.INT_ARRAY, new int[] {entityId});

		PacketUtil.sendPacket(despawn, Protocol1_7_6_10TO1_8.class, true, true);
	}
}
