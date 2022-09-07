package de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.ArrayList;
import java.util.List;

public class ShulkerBulletReplacement extends EntityReplacement1_8to1_9 {
	private final int entityId;
	private final List<Metadata> datawatcher = new ArrayList<>();
	private double locX, locY, locZ;
	private float yaw, pitch;
	private float headYaw;

	public ShulkerBulletReplacement(int entityId, UserConnection user) {
		super(user);
		this.entityId = entityId;
		spawn();
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
		if (this.yaw != yaw && this.pitch != pitch) {
			this.yaw = yaw;
			this.pitch = pitch;
			updateLocation();
		}
	}

	public void setHeadYaw(float yaw) {
		this.headYaw = yaw;
	}

	public void updateMetadata(List<Metadata> metadataList) {
	}

	public void updateLocation() {
		sendTeleportWithHead(entityId, locX, locY, locZ, yaw, pitch, headYaw);
	}

	@Override
	public void spawn() {
		sendSpawnEntity(entityId, 66);
	}

	@Override
	public void despawn() {
		PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_8.DESTROY_ENTITIES, null, user);
		despawn.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});

		PacketUtil.sendPacket(despawn, Protocol1_8TO1_9.class, true, true);
	}

	public int getEntityId() {
		return this.entityId;
	}
}
