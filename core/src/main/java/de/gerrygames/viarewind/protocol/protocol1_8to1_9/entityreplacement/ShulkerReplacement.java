package de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import lombok.Getter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;

import java.util.ArrayList;
import java.util.List;

public class ShulkerReplacement implements EntityReplacement {
	@Getter
	private int entityId;
	private List<Metadata> datawatcher = new ArrayList<>();
	private double locX, locY, locZ;
	private UserConnection user;

	public ShulkerReplacement(int entityId, UserConnection user) {
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

	public void setYawPitch(float yaw, float pitch) { }

	public void setHeadYaw(float yaw) { }

	public void updateMetadata(List<Metadata> metadataList) {
		for (Metadata metadata : metadataList) {
			datawatcher.removeIf(m -> m.getId()==metadata.getId());
			datawatcher.add(metadata);
		}
		updateMetadata();
	}

	public void updateLocation() {
		PacketWrapper teleport = new PacketWrapper(0x18, null, user);
		teleport.write(Type.VAR_INT, entityId);
		teleport.write(Type.INT, (int) (locX * 32.0));
		teleport.write(Type.INT, (int) (locY * 32.0));
		teleport.write(Type.INT, (int) (locZ * 32.0));
		teleport.write(Type.BYTE, (byte) 0);
		teleport.write(Type.BYTE, (byte) 0);
		teleport.write(Type.BOOLEAN, true);

		PacketUtil.sendPacket(teleport, Protocol1_8TO1_9.class, true, true);
	}

	public void updateMetadata() {
		PacketWrapper metadataPacket = new PacketWrapper(0x1C, null, user);
		metadataPacket.write(Type.VAR_INT, entityId);

		List<Metadata> metadataList = new ArrayList<>();
		for (Metadata metadata : datawatcher) {
			if (metadata.getId()==11 || metadata.getId()==12 || metadata.getId()==13) continue;
			metadataList.add(new Metadata(metadata.getId(), metadata.getMetaType(), metadata.getValue()));
		}
		metadataList.add(new Metadata(11, MetaType1_9.VarInt, 2));

		MetadataRewriter.transform(Entity1_10Types.EntityType.MAGMA_CUBE, metadataList);

		metadataPacket.write(Types1_8.METADATA_LIST, metadataList);

		PacketUtil.sendPacket(metadataPacket, Protocol1_8TO1_9.class);
	}

	@Override
	public void spawn() {
		PacketWrapper spawn = new PacketWrapper(0x0F, null, user);
		spawn.write(Type.VAR_INT, entityId);
		spawn.write(Type.UNSIGNED_BYTE, (short) 62);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Types1_8.METADATA_LIST, new ArrayList<>());

		PacketUtil.sendPacket(spawn, Protocol1_8TO1_9.class, true, true);
	}

	@Override
	public void despawn() {
		PacketWrapper despawn = new PacketWrapper(0x13, null, user);
		despawn.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[] {entityId});

		PacketUtil.sendPacket(despawn, Protocol1_8TO1_9.class, true, true);
	}
}
