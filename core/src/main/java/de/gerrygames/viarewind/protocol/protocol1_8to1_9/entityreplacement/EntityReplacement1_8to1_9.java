package de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityReplacement1_8to1_9 implements EntityReplacement {
	protected final UserConnection user;

	protected EntityReplacement1_8to1_9(UserConnection user) {
		this.user = user;
	}

	protected void sendTeleportWithHead(int entityId, double locX, double locY, double locZ, float yaw, float pitch, float headYaw) {
		sendTeleport(entityId, locX, locY, locZ, yaw, pitch);
		sendHeadYaw(entityId, headYaw);
	}

	protected void sendTeleport(int entityId, double locX, double locY, double locZ, float yaw, float pitch) {
		PacketWrapper teleport = PacketWrapper.create(ClientboundPackets1_8.ENTITY_TELEPORT, null, user);
		teleport.write(Type.VAR_INT, entityId);
		teleport.write(Type.INT, (int) (locX * 32.0));
		teleport.write(Type.INT, (int) (locY * 32.0));
		teleport.write(Type.INT, (int) (locZ * 32.0));
		teleport.write(Type.BYTE, (byte) ((yaw / 360f) * 256));
		teleport.write(Type.BYTE, (byte) ((pitch / 360f) * 256));
		teleport.write(Type.BOOLEAN, true);

		PacketUtil.sendPacket(teleport, Protocol1_8TO1_9.class, true, true);
	}

	protected void sendHeadYaw(int entityId, float headYaw) {
		PacketWrapper head = PacketWrapper.create(ClientboundPackets1_8.ENTITY_HEAD_LOOK, null, user);
		head.write(Type.VAR_INT, entityId);
		head.write(Type.BYTE, (byte) ((headYaw / 360f) * 256));
		PacketUtil.sendPacket(head, Protocol1_8TO1_9.class, true, true);
	}

	protected void sendSpawn(int entityId, int type) {
		PacketWrapper spawn = PacketWrapper.create(ClientboundPackets1_8.SPAWN_MOB, null, user);
		spawn.write(Type.VAR_INT, entityId);
		spawn.write(Type.UNSIGNED_BYTE, (short) type);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		spawn.write(Type.SHORT, (short) 0);
		List<Metadata> list = new ArrayList<>();
		spawn.write(Types1_8.METADATA_LIST, list);

		PacketUtil.sendPacket(spawn, Protocol1_8TO1_9.class, true, true);
	}

	protected void sendSpawnEntity(int entityId, int type) {
		PacketWrapper spawn = PacketWrapper.create(ClientboundPackets1_8.SPAWN_ENTITY, null, user);
		spawn.write(Type.VAR_INT, entityId);
		spawn.write(Type.BYTE, (byte) type);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.INT, 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.BYTE, (byte) 0);
		spawn.write(Type.INT, 0);

		PacketUtil.sendPacket(spawn, Protocol1_8TO1_9.class, true, true);
	}
}
