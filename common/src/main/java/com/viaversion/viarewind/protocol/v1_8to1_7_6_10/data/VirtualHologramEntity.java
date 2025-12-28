/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data;

import com.viaversion.viarewind.api.minecraft.entitydata.EntityDataTypes1_7_6_10;
import com.viaversion.viarewind.api.minecraft.math.AABB;
import com.viaversion.viarewind.api.minecraft.math.Vector3d;
import com.viaversion.viarewind.api.type.RewindTypes;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.rewriter.EntityPacketRewriter1_8;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.TrackedEntityImpl;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEventImpl;
import java.util.ArrayList;
import java.util.List;

public class VirtualHologramEntity {
    private final List<EntityData> entityDataTracker = new ArrayList<>();
    private final UserConnection user;
    private final int entityId;
    private double locX, locY, locZ;
    private int[] entityIds = null;
    private State currentState = null;
    private String name = null;
    private float yaw, pitch;
    private float headYaw;
    private boolean small = false;
    private boolean marker = false;
    private boolean sneaking = false;
    private boolean gravity = false;
    private byte alwaysShowNametag;

    public VirtualHologramEntity(final UserConnection user, final int entityId) {
        this.user = user;
        this.entityId = entityId;
    }

    public void setPosition(final double x, final double y, final double z) {
        if (x == this.locX && y == this.locY && z == this.locZ) return;
        this.locX = x;
        this.locY = y;
        this.locZ = z;

        updateLocation();
    }

    public void setRelativePosition(final double x, final double y, final double z) {
        if (x == 0.0 && y == 0.0 && z == 0.0) return;
        this.locX += x;
        this.locY += y;
        this.locZ += z;

        updateLocation();
    }

    public void setRotation(final float yaw, final float pitch) {
        if (this.yaw == yaw || this.headYaw == yaw || this.pitch == pitch) return;
        this.yaw = yaw;
        this.headYaw = yaw;
        this.pitch = pitch;

        updateLocation();
    }

    public void setHeadYaw(float yaw) {
        if (this.headYaw == yaw) return;
        this.headYaw = yaw;

        updateLocation();
    }

    public void syncState(final EntityPacketRewriter1_8 entityRewriter, final List<EntityData> entityDataList) {
        // Merge entity data updates into current tracker
        for (EntityData entityData : entityDataList) {
            entityDataTracker.removeIf(m -> m.id() == entityData.id());
            entityDataTracker.add(entityData);
        }

        // Filter armor stand data to calculate emulation
        byte flags = 0;
        byte armorStandFlags = 0;
        for (EntityData entityData : entityDataTracker) {
            if (entityData.id() == 0 && entityData.dataType() == EntityDataTypes1_8.BYTE) {
                flags = ((Number) entityData.getValue()).byteValue();
            } else if (entityData.id() == 2 && entityData.dataType() == EntityDataTypes1_8.STRING) {
                name = entityData.getValue().toString();
                if (name != null && name.isEmpty()) name = null;
            } else if (entityData.id() == 3 && entityData.dataType() == EntityDataTypes1_8.BYTE) {
                alwaysShowNametag = ((Number) entityData.getValue()).byteValue();
            } else if (entityData.id() == 10 && entityData.dataType() == EntityDataTypes1_8.BYTE) {
                armorStandFlags = ((Number) entityData.getValue()).byteValue();
            }
        }
        final boolean invisible = (flags & 0x20) != 0;
        sneaking = (flags & 0x02) != 0;
        small = (armorStandFlags & 0x01) != 0;
        marker = (armorStandFlags & 0x10) != 0;
        gravity = (armorStandFlags & 0x02) == 0;

        State prevState = currentState;
        if (invisible && name != null) {
            currentState = State.HOLOGRAM;
        } else if (gravity) {
            currentState = State.ZOMBIE;
        } else {
            currentState = State.ZOMBIE_NO_GRAVITY;
        }

        if (currentState != prevState) {
            deleteEntity();
            sendSpawnPacket(entityRewriter);
        } else {
            sendEntityDataUpdate(entityRewriter);
            updateLocation();
        }
    }

    private void updateLocation() {
        if (entityIds == null) {
            return;
        }
        if (currentState == State.ZOMBIE) {
            teleportEntity(entityId, locX, locY, locZ, yaw, pitch);

            final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_7_2_5.ROTATE_HEAD, user);

            entityHeadLook.write(Types.INT, entityId);
            entityHeadLook.write(Types.BYTE, (byte) ((headYaw / 360f) * 256));

            entityHeadLook.send(Protocol1_8To1_7_6_10.class);
        } else if (currentState == State.HOLOGRAM || currentState == State.ZOMBIE_NO_GRAVITY) {
            // Don't ask me where this offset is coming from
            teleportEntity(entityIds[1], locX, (locY + getOffset()), locZ, 0, 0); // Squid
        }
    }

    private double getOffset() {
        if (currentState == State.HOLOGRAM) {
            double baseOffset = 54.35;

            if (marker) {
                return baseOffset;
            } else if (small) {
                return baseOffset + 0.9875;
            } else {
                return baseOffset + (0.9875 * 2);
            }
        } else {
            return -0.4;
        }
    }

    protected void teleportEntity(final int entityId, final double x, final double y, final double z, final float yaw, final float pitch) {
        final PacketWrapper entityTeleport = PacketWrapper.create(ClientboundPackets1_7_2_5.TELEPORT_ENTITY, user);

        entityTeleport.write(Types.INT, entityId); // entity id
        entityTeleport.write(Types.INT, (int) (x * 32.0)); // x
        entityTeleport.write(Types.INT, (int) (y * 32.0)); // y
        entityTeleport.write(Types.INT, (int) (z * 32.0)); // z
        entityTeleport.write(Types.BYTE, (byte) ((yaw / 360f) * 256)); // yaw
        entityTeleport.write(Types.BYTE, (byte) ((pitch / 360f) * 256)); // pitch

        entityTeleport.send(Protocol1_8To1_7_6_10.class);
    }

    protected void spawnEntity(final int entityId, final int type, final double locX, final double locY, final double locZ, final List<EntityData> entityData) {
        final PacketWrapper addMob = PacketWrapper.create(ClientboundPackets1_7_2_5.ADD_MOB, user);

        addMob.write(Types.VAR_INT, entityId); // Entity id
        addMob.write(Types.UNSIGNED_BYTE, (short) type); // Entity type
        addMob.write(Types.INT, (int) (locX * 32.0)); // X
        addMob.write(Types.INT, (int) (locY * 32.0)); // Y
        addMob.write(Types.INT, (int) (locZ * 32.0)); // Z
        addMob.write(Types.BYTE, (byte) 0); // Yaw
        addMob.write(Types.BYTE, (byte) 0); // Pitch
        addMob.write(Types.BYTE, (byte) 0); // Head pitch
        addMob.write(Types.SHORT, (short) 0); // Velocity x
        addMob.write(Types.SHORT, (short) 0); // Velocity y
        addMob.write(Types.SHORT, (short) 0); // Velocity z
        addMob.write(RewindTypes.ENTITY_DATA_LIST1_7, entityData); // Entity data

        addMob.send(Protocol1_8To1_7_6_10.class);
    }

    public void sendEntityDataUpdate(final EntityPacketRewriter1_8 entityRewriter) {
        if (entityIds == null) {
            return;
        }
        final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_ENTITY_DATA, user);

        if (currentState == State.ZOMBIE || currentState == State.ZOMBIE_NO_GRAVITY) {
            writeZombieMeta(entityRewriter, setEntityData);
        } else if (currentState == State.HOLOGRAM) {
            writeHologramMeta(setEntityData);
        } else {
            return;
        }
        setEntityData.send(Protocol1_8To1_7_6_10.class);
    }

    private void writeZombieMeta(final EntityPacketRewriter1_8 entityRewriter, final PacketWrapper wrapper) {
        wrapper.write(Types.INT, entityIds[0]);

        // Filter entity data sent by the server and convert them together with our custom entity data
        final List<EntityData> entityDataList = new ArrayList<>();
        for (EntityData entityData : entityDataTracker) {
            // Remove non existent entityData
            if (entityData.id() < 0 || entityData.id() > 9) {
                continue;
            }
            entityDataList.add(new EntityData(entityData.id(), entityData.dataType(), entityData.getValue()));
        }
        if (small) {
            entityDataList.add(new EntityData(12, EntityDataTypes1_8.BYTE, (byte) 1));
        }

        // Push entity data from the server through entity data conversion 1.7->1.8
        for (EntityData entityData : entityDataList.toArray(new EntityData[0])) {
            final EntityDataHandlerEvent event = new EntityDataHandlerEventImpl(wrapper.user(), new TrackedEntityImpl(EntityTypes1_8.EntityType.ZOMBIE), -1, entityData, entityDataList);
            try {
                entityRewriter.handleEntityData(event, entityData);
            } catch (Exception e) {
                entityDataList.remove(entityData);
                break;
            }
            if (event.cancelled()) {
                entityDataList.remove(entityData);
                break;
            }
        }
        wrapper.write(RewindTypes.ENTITY_DATA_LIST1_7, entityDataList);
    }

    private void writeHologramMeta(PacketWrapper wrapper) {
        wrapper.write(Types.INT, entityIds[0]);

        // Directly write 1.7 entity data here since we are making them up
        final List<EntityData> entityDataList = new ArrayList<>();
        entityDataList.add(new EntityData(EntityDataIndex1_7_6_10.ENTITY_FLAGS.getIndex(), EntityDataTypes1_7_6_10.BYTE, (byte) (sneaking ? 2 : 0)));
        entityDataList.add(new EntityData(EntityDataIndex1_7_6_10.ABSTRACT_AGEABLE_AGE.getIndex(), EntityDataTypes1_7_6_10.INT, -1700000));
        entityDataList.add(new EntityData(EntityDataIndex1_7_6_10.LIVING_ENTITY_BASE_NAME_TAG.getIndex(), EntityDataTypes1_7_6_10.STRING, name));
        entityDataList.add(new EntityData(EntityDataIndex1_7_6_10.LIVING_ENTITY_BASE_NAME_TAG_VISIBILITY.getIndex(), EntityDataTypes1_7_6_10.BYTE, alwaysShowNametag));

        wrapper.write(RewindTypes.ENTITY_DATA_LIST1_7, entityDataList);
    }

    public void sendSpawnPacket(final EntityPacketRewriter1_8 entityRewriter) {
        if (entityIds != null) {
            deleteEntity();
        }
        if (currentState == State.ZOMBIE) {
            spawnEntity(entityId, EntityTypes1_8.EntityType.ZOMBIE.getId(), locX, locY, locZ, new ArrayList<>());

            entityIds = new int[]{entityId};
        } else if (currentState == State.ZOMBIE_NO_GRAVITY || currentState == State.HOLOGRAM) {
            final int[] entityIds = {entityId, additionalEntityId()};

            final List<EntityData> squidEntityData = new ArrayList<>();
            squidEntityData.add(new EntityData(0, EntityDataTypes1_8.BYTE, (byte) 32));

            if (currentState == State.HOLOGRAM) {
                spawnEntity(entityIds[0], EntityTypes1_8.EntityType.HORSE.getId(), locX, locY + (getOffset() + 0.68), locZ, new ArrayList<>());
            } else {
                spawnEntity(entityIds[0], EntityTypes1_8.EntityType.ZOMBIE.getId(), locX, locY, locZ, new ArrayList<>());
            }

            spawnEntity(entityIds[1], EntityTypes1_8.EntityType.SQUID.getId(), locX, locY + getOffset(), locZ, squidEntityData);

            this.entityIds = entityIds;
        }

        sendEntityDataUpdate(entityRewriter);
        if (entityIds == null) {
            return;
        }

        if (currentState == State.ZOMBIE) {
            updateLocation();
        } else {
            final PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_ENTITY_LINK, user);
            attach.write(Types.INT, entityIds[0]);
            attach.write(Types.INT, entityIds[1]);
            attach.write(Types.BOOLEAN, false);

            attach.send(Protocol1_8To1_7_6_10.class);
        }
    }

    public AABB getBoundingBox() {
        final double width = this.small ? 0.25 : 0.5;
        final double height = this.small ? 0.9875 : 1.975;

        final Vector3d min = new Vector3d(this.locX - width / 2, this.locY, this.locZ - width / 2);
        final Vector3d max = new Vector3d(this.locX + width / 2, this.locY + height, this.locZ + width / 2);

        return new AABB(min, max);
    }

    private int additionalEntityId() {
        return Integer.MAX_VALUE - 16000 - entityId;
    }

    public void deleteEntity() {
        if (entityIds == null) {
            return;
        }
        final PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7_2_5.REMOVE_ENTITIES, user);
        despawn.write(Types.BYTE, (byte) entityIds.length);
        for (int id : entityIds) {
            despawn.write(Types.INT, id);
        }
        entityIds = null;
        despawn.send(Protocol1_8To1_7_6_10.class);
    }

    private enum State {
        HOLOGRAM, ZOMBIE, ZOMBIE_NO_GRAVITY
    }
}
