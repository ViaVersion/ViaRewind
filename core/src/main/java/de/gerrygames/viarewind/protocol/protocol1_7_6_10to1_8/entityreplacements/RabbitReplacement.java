package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.ArrayList;
import java.util.List;

public class RabbitReplacement implements EntityReplacement {
    private int entityId;
    private List<Metadata> datawatcher = new ArrayList<>();
    private double locX, locY, locZ;
    private float yaw, pitch;
    private float headYaw;
    private UserConnection user;

    public RabbitReplacement(int entityId, UserConnection user) {
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
        if (this.yaw != yaw || this.pitch != pitch) {
            this.yaw = yaw;
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
        updateMetadata();
    }

    public void updateLocation() {
        PacketWrapper teleport = PacketWrapper.create(ClientboundPackets1_7.ENTITY_TELEPORT, null, user);
        teleport.write(Type.INT, entityId);
        teleport.write(Type.INT, (int) (locX * 32.0));
        teleport.write(Type.INT, (int) (locY * 32.0));
        teleport.write(Type.INT, (int) (locZ * 32.0));
        teleport.write(Type.BYTE, (byte) ((yaw / 360f) * 256));
        teleport.write(Type.BYTE, (byte) ((pitch / 360f) * 256));

        PacketWrapper head = PacketWrapper.create(ClientboundPackets1_7.ENTITY_HEAD_LOOK, null, user);
        head.write(Type.INT, entityId);
        head.write(Type.BYTE, (byte) ((headYaw / 360f) * 256));

        PacketUtil.sendPacket(teleport, Protocol1_7_6_10TO1_8.class, true, true);
        PacketUtil.sendPacket(head, Protocol1_7_6_10TO1_8.class, true, true);
    }

    public void updateMetadata() {
        PacketWrapper metadataPacket = PacketWrapper.create(ClientboundPackets1_7.ENTITY_METADATA, null, user);
        metadataPacket.write(Type.INT, entityId);

        List<Metadata> metadataList = new ArrayList<>();
        for (Metadata metadata : datawatcher) {
            metadataList.add(new Metadata(metadata.id(), metadata.metaType(), metadata.getValue()));
        }

        MetadataRewriter.transform(Entity1_10Types.EntityType.CHICKEN, metadataList);

        metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);

        PacketUtil.sendPacket(metadataPacket, Protocol1_7_6_10TO1_8.class, true, true);
    }

    @Override
    public void spawn() {
        PacketWrapper spawn = PacketWrapper.create(ClientboundPackets1_7.SPAWN_MOB, null, user);
        spawn.write(Type.VAR_INT, entityId);
        spawn.write(Type.UNSIGNED_BYTE, (short) 93); // chicken
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
        PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7.DESTROY_ENTITIES, null, user);
        despawn.write(Types1_7_6_10.INT_ARRAY, new int[]{entityId});

        PacketUtil.sendPacket(despawn, Protocol1_7_6_10TO1_8.class, true, true);
    }

    public int getEntityId() {
        return this.entityId;
    }
}
