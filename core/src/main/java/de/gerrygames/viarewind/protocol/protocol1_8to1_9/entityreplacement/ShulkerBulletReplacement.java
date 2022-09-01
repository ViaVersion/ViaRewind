package de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.ArrayList;
import java.util.List;

public class ShulkerBulletReplacement implements EntityReplacement {
	private final int entityId;
	private final List<Metadata> datawatcher = new ArrayList<>();
	private double locX, locY, locZ;
	private float yaw, pitch;
	private float headYaw;
	private final UserConnection user;

	public ShulkerBulletReplacement(int entityId, UserConnection user) {
		this.entityId = entityId;
		this.user = user;
		spawn();
	}

	public void setLocation(double x, double y, double z) {
		if (x!=this.locX || y!=this.locY || z!=this.locZ) {
			this.locX = x;
			this.locY = y;
			this.locZ = z;
			updateLocation();
		}
	}

	public void relMove(double x, double y, double z) {
		if (x==0.0 && y==0.0 && z==0.0) return;
		this.locX += x;
		this.locY += y;
		this.locZ += z;
		updateLocation();
	}

	public void setYawPitch(float yaw, float pitch) {
		if (this.yaw!=yaw && this.pitch!=pitch) {
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
		PacketWrapper teleport = PacketWrapper.create(0x18, null, user);
		teleport.write(Type.VAR_INT, entityId);
		teleport.write(Type.INT, (int) (locX * 32.0));
		teleport.write(Type.INT, (int) (locY * 32.0));
		teleport.write(Type.INT, (int) (locZ * 32.0));
		teleport.write(Type.BYTE, (byte) ((yaw / 360f) * 256));
		teleport.write(Type.BYTE, (byte) ((pitch / 360f) * 256));
		teleport.write(Type.BOOLEAN, true);

		PacketWrapper head = PacketWrapper.create(0x19, null, user);
		head.write(Type.VAR_INT, entityId);
		head.write(Type.BYTE, (byte)((headYaw / 360f) * 256));

		PacketUtil.sendPacket(teleport, Protocol1_8TO1_9.class, true, true);
		PacketUtil.sendPacket(head, Protocol1_8TO1_9.class, true, true);
	}

	@Override
	public void spawn() {
		PacketWrapper spawn = PacketWrapper.create(0x0E, null, user);
		spawn.write(Type.VAR_INT, entityId);
		spawn.write(Type.BYTE, (byte) 66);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.INT, 0);

		PacketUtil.sendPacket(spawn, Protocol1_8TO1_9.class, true, true);
	}

	@Override
	public void despawn() {
		PacketWrapper despawn = PacketWrapper.create(0x13, null, user);
		despawn.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[] {entityId});

		PacketUtil.sendPacket(despawn, Protocol1_8TO1_9.class, true, true);
	}

	public int getEntityId() {
		return this.entityId;
	}
}
