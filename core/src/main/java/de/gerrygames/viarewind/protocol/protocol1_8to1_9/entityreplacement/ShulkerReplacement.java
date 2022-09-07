package de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.ArrayList;
import java.util.List;

public class ShulkerReplacement extends EntityReplacement1_8to1_9 {
	private final int entityId;
	private final List<Metadata> datawatcher = new ArrayList<>();
	private double locX, locY, locZ;

	public ShulkerReplacement(int entityId, UserConnection user) {
		super(user);
		this.entityId = entityId;
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
	}

	public void setHeadYaw(float yaw) {
	}

	public void updateMetadata(List<Metadata> metadataList) {
		for (Metadata metadata : metadataList) {
			datawatcher.removeIf(m -> m.id() == metadata.id());
			datawatcher.add(metadata);
		}
		updateMetadata();
	}

	public void updateLocation() {
		sendTeleport(entityId, locX, locY, locZ, 0, 0);
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

		MetadataRewriter.transform(Entity1_10Types.EntityType.MAGMA_CUBE, metadataList);

		metadataPacket.write(Types1_8.METADATA_LIST, metadataList);

		PacketUtil.sendPacket(metadataPacket, Protocol1_8TO1_9.class);
	}

	@Override
	public void spawn() {
		sendSpawn(entityId, 62);
		// Old clients don't like empty metadata
	}

	@Override
	public void despawn() {
		PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7.DESTROY_ENTITIES, null, user);
		despawn.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});

		PacketUtil.sendPacket(despawn, Protocol1_8TO1_9.class, true, true);
	}

	public int getEntityId() {
		return this.entityId;
	}
}
