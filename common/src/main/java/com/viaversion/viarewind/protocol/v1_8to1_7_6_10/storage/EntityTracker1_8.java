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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data.VirtualHologramEntity;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntArrayMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectArrayMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.viaversion.viarewind.api.minecraft.entitydata.EntityDataTypes1_7_6_10;
import com.viaversion.viarewind.api.type.RewindTypes;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;

public class EntityTracker1_8 extends EntityTrackerBase {

    private final Int2ObjectMap<VirtualHologramEntity> holograms = new Int2ObjectArrayMap<>();
    private final Int2IntMap extraHologramIds = new Int2IntArrayMap();
    private final Int2IntMap vehicles = new Int2IntArrayMap();
    private final Int2ObjectMap<UUID> entityIdToUUID = new Int2ObjectArrayMap<>();
    private final Object2IntMap<UUID> entityUUIDToId = new Object2IntOpenHashMap<>();
    private final Int2IntMap playerNametagHiderEntities = new Int2IntArrayMap();

    private final List<EntityData> entityData = new ArrayList<>();

    public Integer spectatingClientEntityId;
    private int clientEntityGameMode;

    public EntityTracker1_8(UserConnection connection) {
        super(connection, EntityTypes1_8.EntityType.PLAYER);
    }

    @Override
    public void addEntity(int id, EntityType type) {
        super.addEntity(id, type);
        if (type == EntityTypes1_8.EntityType.ARMOR_STAND) {
            holograms.put(id, new VirtualHologramEntity(user(), id));
        }
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);

        final VirtualHologramEntity hologram = holograms.get(entityId);
        if (hologram != null) {
            hologram.deleteEntity();
            holograms.remove(entityId);
        }

        if (playerNametagHiderEntities.containsKey(entityId)) {
            despawnNametagHiderEntity(entityId);
        }

        if (entityIdToUUID.containsKey(entityId)) {
            final UUID playerId = entityIdToUUID.remove(entityId);

            entityUUIDToId.removeInt(playerId);
            user().get(PlayerSessionStorage.class).getPlayerEquipment().remove(playerId);
        }
    }

    @Override
    public void clearEntities() {
        super.clearEntities();
        holograms.clear();
        extraHologramIds.clear();
        vehicles.clear();
        playerNametagHiderEntities.clear();
    }

    @Override
    public void setClientEntityId(int entityId) {
        if (Objects.equals(this.spectatingClientEntityId, clientEntityIdOrNull())) {
            this.spectatingClientEntityId = entityId;
        }
        super.setClientEntityId(entityId);
    }

    public Integer clientEntityIdOrNull() {
        return this.hasClientEntityId() ? this.clientEntityId() : null;
    }

    public void addPlayer(final int entityId, final UUID uuid) {
        entityUUIDToId.put(uuid, entityId);
        entityIdToUUID.put(entityId, uuid);
    }

    public UUID getPlayerUUID(final int entityId) {
        return entityIdToUUID.get(entityId);
    }

    public int getPlayerEntityId(final UUID uuid) {
        return entityUUIDToId.getOrDefault(uuid, -1);
    }

    public int getVehicle(final int passengerId) {
        for (Map.Entry<Integer, Integer> vehicle : vehicles.int2IntEntrySet()) {
            if (vehicle.getValue() == passengerId) {
                return vehicle.getValue();
            }
        }
        return -1;
    }

    public int getPassenger(int vehicleId) {
        return vehicles.getOrDefault(vehicleId, -1);
    }

    protected void startSneaking() {
        try {
            final PacketWrapper entityAction = PacketWrapper.create(ServerboundPackets1_7_2_5.PLAYER_COMMAND, user());
            entityAction.write(Types.VAR_INT, this.clientEntityId()); // Entity id
            entityAction.write(Types.VAR_INT, 0); // Action id
            entityAction.write(Types.VAR_INT, 0); // Jump boost

            entityAction.sendToServer(Protocol1_8To1_7_6_10.class);
        } catch (Exception e) {
            ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send sneak packet", e);
        }
    }

    public void setPassenger(final int vehicleId, final int passengerId) {
        if (vehicleId == this.spectatingClientEntityId && this.spectatingClientEntityId != this.clientEntityId()) {
            startSneaking();
            setSpectating(this.clientEntityId());
        }

        if (vehicleId == -1) {
            vehicles.remove(getVehicle(passengerId));
        } else if (passengerId == -1) {
            vehicles.remove(vehicleId);
        } else {
            vehicles.put(vehicleId, passengerId);
        }

        // Re-evaluate nametag visibility when a player entity's passenger changes
        if (vehicleId != -1 && entityIdToUUID.containsKey(vehicleId)) {
            checkNametagVisibility(vehicleId);
        }
    }

    protected void attachEntity(final int target) {
        try {
            final PacketWrapper attachEntity = PacketWrapper.create(ClientboundPackets1_8.SET_ENTITY_LINK, user());
            attachEntity.write(Types.INT, this.clientEntityId()); // vehicle id
            attachEntity.write(Types.INT, target); // passenger id
            attachEntity.write(Types.BOOLEAN, false); // leash

            attachEntity.scheduleSend(Protocol1_8To1_7_6_10.class);
        } catch (Exception e) {
            ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send attach packet", e);
        }
    }

    public void setSpectating(int spectating) {
        if (spectating != this.clientEntityId() && getPassenger(spectating) != -1) {
            startSneaking();
            setSpectating(this.clientEntityId());
            return;
        }
        if (this.spectatingClientEntityId != spectating && this.spectatingClientEntityId != this.clientEntityId()) {
            attachEntity(-1);
        }
        this.spectatingClientEntityId = spectating;
        if (spectating != this.clientEntityId()) {
            attachEntity(this.spectatingClientEntityId);
        }
    }

    public void checkNametagVisibility(final int entityId) {
        if (!entityIdToUUID.containsKey(entityId)) {
            return;
        }
        final boolean shouldHide = isPlayerNametagHidden(entityId);
        final boolean hasServerPassenger = getPassenger(entityId) != -1;
        final boolean hasSkull = playerNametagHiderEntities.containsKey(entityId);

        if (shouldHide && !hasServerPassenger && !hasSkull) {
            spawnNametagHiderEntity(entityId);
        } else if ((!shouldHide || hasServerPassenger) && hasSkull) {
            despawnNametagHiderEntity(entityId);
        }
    }

    public void checkNametagVisbility(final String username) {
        final GameProfileStorage profileStorage = user().get(GameProfileStorage.class);
        final GameProfileStorage.GameProfile profile = profileStorage.get(username, false);
        if (profile == null) {
            return;
        }

        final int entityId = getPlayerEntityId(profile.uuid);
        if (entityId == -1) {
            return;
        }

        checkNametagVisibility(entityId);
    }

    private boolean isPlayerNametagHidden(final int entityId) {
        final UUID uuid = entityIdToUUID.get(entityId);
        if (uuid == null) {
            return false;
        }
        final GameProfileStorage profileStorage = user().get(GameProfileStorage.class);
        final GameProfileStorage.GameProfile profile = profileStorage.get(uuid);
        if (profile == null) {
            return false;
        }
        return user().get(ScoreboardTracker.class).isNametagHidden(profile.name);
    }

    private int getNametagHiderEntityId(final int playerEntityId) {
        return Integer.MAX_VALUE - 32000 - playerEntityId;
    }

    private void spawnNametagHiderEntity(final int playerEntityId) {
        final int entityId = getNametagHiderEntityId(playerEntityId);
        playerNametagHiderEntities.put(playerEntityId, entityId);

        final List<EntityData> mobData = new ArrayList<>();
        mobData.add(new EntityData(0, EntityDataTypes1_7_6_10.BYTE, (byte) 0x20));
        mobData.add(new EntityData(16, EntityDataTypes1_7_6_10.BYTE, (byte) 0));

        final PacketWrapper spawnMob = PacketWrapper.create(ClientboundPackets1_7_2_5.ADD_MOB, user());
        spawnMob.write(Types.VAR_INT, entityId);
        spawnMob.write(Types.UNSIGNED_BYTE, (short) EntityTypes1_8.EntityType.MAGMA_CUBE.getId());
        spawnMob.write(Types.INT, 0); // X
        spawnMob.write(Types.INT, 0); // Y
        spawnMob.write(Types.INT, 0); // Z
        spawnMob.write(Types.BYTE, (byte) 0); // Yaw
        spawnMob.write(Types.BYTE, (byte) 0); // Pitch
        spawnMob.write(Types.BYTE, (byte) 0); // Head yaw
        spawnMob.write(Types.SHORT, (short) 0); // Velocity x
        spawnMob.write(Types.SHORT, (short) 0); // Velocity y
        spawnMob.write(Types.SHORT, (short) 0); // Velocity z
        spawnMob.write(RewindTypes.ENTITY_DATA_LIST1_7, mobData);
        spawnMob.scheduleSend(Protocol1_8To1_7_6_10.class);

        final PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_ENTITY_LINK, user());
        attach.write(Types.INT, entityId);
        attach.write(Types.INT, playerEntityId);
        attach.write(Types.BOOLEAN, false);
        attach.scheduleSend(Protocol1_8To1_7_6_10.class);
    }

    private void despawnNametagHiderEntity(final int playerEntityId) {
        if (!playerNametagHiderEntities.containsKey(playerEntityId)) return;
        final int mobId = playerNametagHiderEntities.remove(playerEntityId);

        final PacketWrapper despawn = PacketWrapper.create(ClientboundPackets1_7_2_5.REMOVE_ENTITIES, user());
        despawn.write(Types.BYTE, (byte) 1);
        despawn.write(Types.INT, mobId);
        despawn.scheduleSend(Protocol1_8To1_7_6_10.class);
    }

    public Int2ObjectMap<VirtualHologramEntity> getHolograms() {
        return holograms;
    }

    public void setExtraHologramId(final int entityId, final int extraId) {
        extraHologramIds.put(extraId, entityId);
    }

    public void removeExtraHologramId(int extraId) {
        extraHologramIds.remove(extraId);
    }

    public int getHologramIdWithExtra(final int id) {
        if (holograms.containsKey(id)) {
            return id;
        }
        return extraHologramIds.getOrDefault(id, -1);
    }

    public boolean isSpectator() {
        return clientEntityGameMode == 3;
    }

    public void setClientEntityGameMode(int clientEntityGameMode) {
        this.clientEntityGameMode = clientEntityGameMode;
    }

    public void updateEntityData(List<EntityData> entityData) {
        this.entityData.removeIf(first -> entityData.stream().anyMatch(second -> first.id() == second.id()));
        for (final EntityData data : entityData) {
            final Object value = data.value();
            if (value instanceof Item item) {
                this.entityData.add(new EntityData(data.id(), data.dataType(), item.copy()));
            } else {
                this.entityData.add(new EntityData(data.id(), data.dataType(), value));
            }
        }
    }

    public List<EntityData> getEntityData() {
        return entityData;
    }

}
