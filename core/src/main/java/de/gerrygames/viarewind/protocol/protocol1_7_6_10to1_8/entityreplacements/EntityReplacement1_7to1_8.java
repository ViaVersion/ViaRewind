package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.ArrayList;

public abstract class EntityReplacement1_7to1_8 implements EntityReplacement {
	protected final UserConnection user;

	public EntityReplacement1_7to1_8(UserConnection user) {
		this.user = user;
	}

	protected void sendTeleportWithHead(int entityId, double locX, double locY, double locZ, float yaw, float pitch, float headYaw) {
		sendTeleport(entityId, locX, locY, locZ, yaw, pitch);
		sendHeadYaw(entityId, headYaw);
	}

	protected void sendTeleport(int entityId, double locX, double locY, double locZ, float yaw, float pitch) {
		PacketWrapper teleport = PacketWrapper.create(ClientboundPackets1_7.ENTITY_TELEPORT, null, user);
		teleport.write(Type.INT, entityId);
		teleport.write(Type.INT, (int) (locX * 32.0));
		teleport.write(Type.INT, (int) (locY * 32.0));
		teleport.write(Type.INT, (int) (locZ * 32.0));
		teleport.write(Type.BYTE, (byte) ((yaw / 360f) * 256));
		teleport.write(Type.BYTE, (byte) ((pitch / 360f) * 256));

		PacketUtil.sendPacket(teleport, Protocol1_7_6_10TO1_8.class, true, true);
	}

	protected void sendHeadYaw(int entityId, float headYaw) {
		PacketWrapper head = PacketWrapper.create(ClientboundPackets1_7.ENTITY_HEAD_LOOK, null, user);
		head.write(Type.INT, entityId);
		head.write(Type.BYTE, (byte) ((headYaw / 360f) * 256));
		PacketUtil.sendPacket(head, Protocol1_7_6_10TO1_8.class, true, true);
	}

	protected void sendSpawn(int entityId, int type, double locX, double locY, double locZ) {
		PacketWrapper spawn = PacketWrapper.create(ClientboundPackets1_7.SPAWN_MOB, null, user);
		spawn.write(Type.VAR_INT, entityId);
		spawn.write(Type.UNSIGNED_BYTE, (short) type); // type
		spawn.write(Type.INT, (int) (locX * 32.0));
		spawn.write(Type.INT, (int) (locY * 32.0));
		spawn.write(Type.INT, (int) (locZ * 32.0));
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Types1_7_6_10.METADATA_LIST, new ArrayList<>());

		PacketUtil.sendPacket(spawn, Protocol1_7_6_10TO1_8.class, true, true);
	}
}
