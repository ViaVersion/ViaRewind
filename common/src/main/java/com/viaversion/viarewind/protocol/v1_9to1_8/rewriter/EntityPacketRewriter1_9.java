/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
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
package com.viaversion.viarewind.protocol.v1_9to1_8.rewriter;

import com.viaversion.viarewind.api.minecraft.math.RelativeMoveUtil;
import com.viaversion.viarewind.api.rewriter.VREntityRewriter;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.data.EntityDataIndex1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.CooldownStorage;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.LevitationStorage;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.PlayerPositionTracker;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.EntityDataIndex1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import com.viaversion.viaversion.util.IdAndData;
import com.viaversion.viaversion.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityPacketRewriter1_9 extends VREntityRewriter<ClientboundPackets1_9, Protocol1_9To1_8> {

	private static final byte HAND_ACTIVE_BIT = 0;
	private static final byte STATUS_USE_BIT = 4;

	public EntityPacketRewriter1_9(Protocol1_9To1_8 protocol) {
		super(protocol);
	}

	@Override
	protected void registerPackets() {
		registerJoinGame1_8(ClientboundPackets1_9.LOGIN);
		registerRemoveEntities(ClientboundPackets1_9.REMOVE_ENTITIES);
		registerSetEntityData(ClientboundPackets1_9.SET_ENTITY_DATA, Types1_9.ENTITY_DATA_LIST, Types1_8.ENTITY_DATA_LIST);

		protocol.registerClientbound(ClientboundPackets1_9.ADD_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				read(Types.UUID); // Entity uuid
				map(Types.BYTE); // Entity type
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // X
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Y
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Z
				map(Types.BYTE); // Pitch
				map(Types.BYTE); // Yaw
				map(Types.INT); // Data

				// Track entity
				handler(getObjectTrackerHandler());
				handler(getObjectRewriter(EntityTypes1_9.ObjectType::findById));

				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					final int entityType = wrapper.get(Types.BYTE, 0);
					final EntityTypes1_9.EntityType type = EntityTypes1_9.getTypeFromId(entityType, true);

					// Cancel new entities which can't be handled properly
					if (type == EntityTypes1_9.EntityType.AREA_EFFECT_CLOUD || type == EntityTypes1_9.EntityType.SPECTRAL_ARROW || type == EntityTypes1_9.EntityType.DRAGON_FIREBALL) {
						wrapper.cancel();
						return;
					}

					if (type.is(EntityTypes1_9.EntityType.BOAT)) {
						byte yaw = wrapper.get(Types.BYTE, 1);
						yaw -= 64;
						wrapper.set(Types.BYTE, 1, yaw);

						int y = wrapper.get(Types.INT, 1);
						y += 10;
						wrapper.set(Types.INT, 1, y);
					}

					int data = wrapper.get(Types.INT, 3);

					if (type.isOrHasParent(EntityTypes1_9.EntityType.ARROW) && data != 0) {
						wrapper.set(Types.INT, 3, --data);
					}
					if (type.is(EntityTypes1_9.EntityType.FALLING_BLOCK)) {
						int blockId = data & 0xFFF;
						int blockData = data >> 12 & 0xF;
						IdAndData replace = protocol.getItemRewriter().handleBlock(blockId, blockData);
						if (replace != null) {
							wrapper.set(Types.INT, 3, replace.getId() | replace.getData() << 12);
						}
					}

					if (data > 0) {
						wrapper.passthrough(Types.SHORT); // Velocity x
						wrapper.passthrough(Types.SHORT); // Velocity y
						wrapper.passthrough(Types.SHORT); // Velocity z
					} else {
						final short velocityX = wrapper.read(Types.SHORT);
						final short velocityY = wrapper.read(Types.SHORT);
						final short velocityZ = wrapper.read(Types.SHORT);

						final PacketWrapper setEntityMotion = PacketWrapper.create(ClientboundPackets1_8.SET_ENTITY_MOTION, wrapper.user());
						setEntityMotion.write(Types.VAR_INT, entityId);
						setEntityMotion.write(Types.SHORT, velocityX);
						setEntityMotion.write(Types.SHORT, velocityY);
						setEntityMotion.write(Types.SHORT, velocityZ);
						setEntityMotion.scheduleSend(Protocol1_9To1_8.class);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ADD_EXPERIENCE_ORB, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // X
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Y
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Z
				map(Types.SHORT); // Count
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					wrapper.user().getEntityTracker(Protocol1_9To1_8.class).addEntity(entityId, EntityTypes1_9.EntityType.EXPERIENCE_ORB);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ADD_GLOBAL_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.BYTE); // Type
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // X
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Y
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Z
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					wrapper.user().getEntityTracker(Protocol1_9To1_8.class).addEntity(entityId, EntityTypes1_9.EntityType.LIGHTNING_BOLT);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ADD_MOB, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				read(Types.UUID); // Entity uuid
				map(Types.UNSIGNED_BYTE); // Entity type
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // X
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Y
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				map(Types.BYTE); // Head pitch
				map(Types.SHORT); // Velocity x
				map(Types.SHORT); // Velocity y
				map(Types.SHORT); // Velocity z
				map(Types1_9.ENTITY_DATA_LIST, Types1_8.ENTITY_DATA_LIST); // Entity data

				handler(getTrackerHandler(Types.UNSIGNED_BYTE, 0));
				handler(getMobSpawnRewriter(Types1_8.ENTITY_DATA_LIST));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ADD_PAINTING, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				read(Types.UUID); // Entity uuid
				map(Types.STRING); // Title
				map(Types.BLOCK_POSITION1_8); // Position
				map(Types.BYTE, Types.UNSIGNED_BYTE); // Direction
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					wrapper.user().getEntityTracker(Protocol1_9To1_8.class).addEntity(entityId, EntityTypes1_9.EntityType.PAINTING);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ADD_PLAYER, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.UUID); // Player uuid
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // X
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Y
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				create(Types.SHORT, (short) 0); // Current item
				map(Types1_9.ENTITY_DATA_LIST, Types1_8.ENTITY_DATA_LIST); // Entity data

				handler(getTrackerAndMetaHandler(Types1_8.ENTITY_DATA_LIST, EntityTypes1_9.EntityType.PLAYER));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_EVENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Entity id
				handler(wrapper -> {
					final byte status = wrapper.read(Types.BYTE);
					if (status > 23) { // Remove op permission level 0-4 (24-28)
						wrapper.cancel();
						return;
					}
					wrapper.write(Types.BYTE, status);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.MOVE_ENTITY_POS, wrapper -> {
			final int entityId = wrapper.passthrough(Types.VAR_INT);
			final int deltaX = wrapper.read(Types.SHORT);
			final int deltaY = wrapper.read(Types.SHORT);
			final int deltaZ = wrapper.read(Types.SHORT);

			final Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(wrapper.user(), entityId, deltaX, deltaY, deltaZ);

			wrapper.write(Types.BYTE, (byte) moves[0].blockX());
			wrapper.write(Types.BYTE, (byte) moves[0].blockY());
			wrapper.write(Types.BYTE, (byte) moves[0].blockZ());

			final boolean onGround = wrapper.passthrough(Types.BOOLEAN);

			if (moves.length > 1) {
				final PacketWrapper secondPacket = PacketWrapper.create(ClientboundPackets1_8.MOVE_ENTITY_POS, wrapper.user());
				secondPacket.write(Types.VAR_INT, entityId);
				secondPacket.write(Types.BYTE, (byte) moves[1].blockX());
				secondPacket.write(Types.BYTE, (byte) moves[1].blockY());
				secondPacket.write(Types.BYTE, (byte) moves[1].blockZ());
				secondPacket.write(Types.BOOLEAN, onGround);

				secondPacket.scheduleSend(Protocol1_9To1_8.class);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.MOVE_ENTITY_POS_ROT, wrapper -> {
			final int entityId = wrapper.passthrough(Types.VAR_INT);
			final int deltaX = wrapper.read(Types.SHORT);
			final int deltaY = wrapper.read(Types.SHORT);
			final int deltaZ = wrapper.read(Types.SHORT);

			final Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(wrapper.user(), entityId, deltaX, deltaY, deltaZ);

			wrapper.write(Types.BYTE, (byte) moves[0].blockX());
			wrapper.write(Types.BYTE, (byte) moves[0].blockY());
			wrapper.write(Types.BYTE, (byte) moves[0].blockZ());

			byte yaw = wrapper.passthrough(Types.BYTE);
			final byte pitch = wrapper.passthrough(Types.BYTE);
			final boolean onGround = wrapper.passthrough(Types.BOOLEAN);

			com.viaversion.viaversion.api.minecraft.entities.EntityType type = wrapper.user().getEntityTracker(Protocol1_9To1_8.class).entityType(entityId);
			if (type == EntityTypes1_9.EntityType.BOAT) {
				yaw -= 64;
				wrapper.set(Types.BYTE, 3, yaw);
			}

			if (moves.length > 1) {
				final PacketWrapper secondPacket = PacketWrapper.create(ClientboundPackets1_8.MOVE_ENTITY_POS_ROT, wrapper.user());
				secondPacket.write(Types.VAR_INT, entityId);
				secondPacket.write(Types.BYTE, (byte) moves[1].blockX());
				secondPacket.write(Types.BYTE, (byte) moves[1].blockY());
				secondPacket.write(Types.BYTE, (byte) moves[1].blockZ());
				secondPacket.write(Types.BYTE, yaw);
				secondPacket.write(Types.BYTE, pitch);
				secondPacket.write(Types.BOOLEAN, onGround);

				secondPacket.scheduleSend(Protocol1_9To1_8.class);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.MOVE_ENTITY_ROT, wrapper -> {
			final int entityId = wrapper.passthrough(Types.VAR_INT);
			final com.viaversion.viaversion.api.minecraft.entities.EntityType type = wrapper.user().getEntityTracker(Protocol1_9To1_8.class).entityType(entityId);
			if (type == EntityTypes1_9.EntityType.BOAT) {
				byte yaw = wrapper.read(Types.BYTE);
				yaw -= 64;
				wrapper.write(Types.BYTE, yaw);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.MOVE_VEHICLE, ClientboundPackets1_8.TELEPORT_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
					final int vehicle = tracker.getVehicle(tracker.clientEntityId());
					if (vehicle == -1) {
						wrapper.cancel();
					}
					wrapper.write(Types.VAR_INT, vehicle);
				});
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // X
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Y
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Z
				map(Types.FLOAT, Protocol1_9To1_8.DEGREES_TO_ANGLE); // Yaw
				map(Types.FLOAT, Protocol1_9To1_8.DEGREES_TO_ANGLE); // Pitch
				handler(wrapper -> {
					if (wrapper.isCancelled()) {
						return;
					}
					final PlayerPositionTracker storage = wrapper.user().get(PlayerPositionTracker.class);
					double x = wrapper.get(Types.INT, 0) / 32d;
					double y = wrapper.get(Types.INT, 1) / 32d;
					double z = wrapper.get(Types.INT, 2) / 32d;
					storage.setPos(x, y, z);
				});
				create(Types.BOOLEAN, true);
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					final EntityType type = wrapper.user().getEntityTracker(Protocol1_9To1_8.class).entityType(entityId);
					if (type == EntityTypes1_9.EntityType.BOAT) {
						byte yaw = wrapper.get(Types.BYTE, 1);
						yaw -= 64;
						wrapper.set(Types.BYTE, 0, yaw);
						int y = wrapper.get(Types.INT, 1);
						y += 10;
						wrapper.set(Types.INT, 1, y);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.REMOVE_MOB_EFFECT, wrapper -> {
			final int entityId = wrapper.passthrough(Types.VAR_INT);
			final int effectId = wrapper.passthrough(Types.BYTE);
			if (effectId > 23) { // Throw away new effects
				wrapper.cancel();
			}
			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
			if (effectId == 25 && entityId == tracker.clientEntityId()) {
				wrapper.user().get(LevitationStorage.class).setActive(false);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SET_ENTITY_LINK, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Attached entity id
				map(Types.INT); // Holding entity id
				create(Types.BOOLEAN, true); // Leash
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SET_EQUIPPED_ITEM, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				handler(wrapper -> {
					// todo check if this is correct for the own player
					int slot = wrapper.read(Types.VAR_INT);
					if (slot == 1) {
						wrapper.cancel();
					} else if (slot > 1) {
						slot -= 1;
					}
					wrapper.write(Types.SHORT, (short) slot);
				});
				map(Types.ITEM1_8); // Item
				handler(wrapper -> protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.get(Types.ITEM1_8, 0)));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SET_PASSENGERS, null, wrapper -> {
			wrapper.cancel();

			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
			final int vehicle = wrapper.read(Types.VAR_INT);
			final IntList oldPassengers = tracker.getPassengers(vehicle);

			final int count = wrapper.read(Types.VAR_INT);
			final IntList passengers = new IntArrayList();
			for (int i = 0; i < count; i++) {
				passengers.add(wrapper.read(Types.VAR_INT));
			}
			tracker.setPassengers(vehicle, passengers);

			if (!oldPassengers.isEmpty()) {
				for (Integer passenger : oldPassengers) {
					final PacketWrapper detach = PacketWrapper.create(ClientboundPackets1_8.SET_ENTITY_LINK, wrapper.user());
					detach.write(Types.INT, passenger); // Attached entity id
					detach.write(Types.INT, -1); // Holding entity id
					detach.write(Types.BOOLEAN, false); // Leash
					detach.scheduleSend(Protocol1_9To1_8.class);
				}
			}
			for (int i = 0; i < count; i++) {
				final int attachedEntityId = passengers.getInt(i);
				final int holdingEntityId = i == 0 ? vehicle : passengers.getInt(i - 1);

				final PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_8.SET_ENTITY_LINK, wrapper.user());
				attach.write(Types.INT, attachedEntityId);
				attach.write(Types.INT, holdingEntityId);
				attach.write(Types.BOOLEAN, false); // Leash
				attach.scheduleSend(Protocol1_9To1_8.class);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.TELEPORT_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // X
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Y
				map(Types.DOUBLE, Protocol1_9To1_8.DOUBLE_TO_INT_TIMES_32); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				map(Types.BOOLEAN); // On ground
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);

					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
					if (tracker.entityType(entityId) == EntityTypes1_9.EntityType.BOAT) {
						byte yaw = wrapper.get(Types.BYTE, 1);
						yaw -= 64;
						wrapper.set(Types.BYTE, 0, yaw);

						int y = wrapper.get(Types.INT, 1);
						y += 10;
						wrapper.set(Types.INT, 1, y);
					}
					tracker.resetEntityOffset(entityId);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.UPDATE_ATTRIBUTES, wrapper -> {
			final int entityId = wrapper.passthrough(Types.VAR_INT);

			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
			final boolean player = entityId == tracker.clientEntityId();

			// Throw away new attributes and track attack speed
			int removed = 0;
			final int size = wrapper.passthrough(Types.INT);
			for (int i = 0; i < size; i++) {
				final String key = wrapper.read(Types.STRING);
				final double value = wrapper.read(Types.DOUBLE);
				final int modifierSize = wrapper.read(Types.VAR_INT);

				final boolean valid = protocol.getItemRewriter().VALID_ATTRIBUTES.contains(key);
				if (valid) {
					wrapper.write(Types.STRING, key);
					wrapper.write(Types.DOUBLE, value);
					wrapper.write(Types.VAR_INT, modifierSize);
				}

				final List<Pair<Byte, Double>> modifiers = new ArrayList<>();
				for (int j = 0; j < modifierSize; j++) {
					final UUID modifierId = wrapper.read(Types.UUID); // UUID
					final double amount = wrapper.read(Types.DOUBLE); // Amount
					final byte operation = wrapper.read(Types.BYTE); // Operation
					if (valid) {
						wrapper.write(Types.UUID, modifierId);
						wrapper.write(Types.DOUBLE, amount);
						wrapper.write(Types.BYTE, operation);
					}
					modifiers.add(new Pair<>(operation, amount));
				}
				if (!valid) {
					if (player && key.equals("generic.attackSpeed")) {
						wrapper.user().get(CooldownStorage.class).setAttackSpeed(value, modifiers);
					}
					removed++;
				}
			}
			wrapper.set(Types.INT, 0, size - removed);
		});

		protocol.registerClientbound(ClientboundPackets1_9.UPDATE_MOB_EFFECT, wrapper -> {
			final int entityId = wrapper.passthrough(Types.VAR_INT);
			final int effectId = wrapper.passthrough(Types.BYTE);
			final byte amplifier = wrapper.passthrough(Types.BYTE);
			if (effectId > 23) { // Throw away new effects
				wrapper.cancel();
			}
			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
			if (effectId == 25 && entityId == tracker.clientEntityId()) {
				final LevitationStorage levitation = wrapper.user().get(LevitationStorage.class);
				levitation.setActive(true);
				levitation.setAmplifier(amplifier);
			}
		});
	}

	@Override
	protected void registerRewrites() {
		// Handle new entities
		mapEntityTypeWithData(EntityTypes1_9.EntityType.SHULKER, EntityTypes1_9.EntityType.MAGMA_CUBE).plainName();
		mapEntityTypeWithData(EntityTypes1_9.EntityType.SHULKER_BULLET, EntityTypes1_9.EntityType.WITCH).plainName();

		// Entity data rewrite
		filter().handler(this::handleEntityData);
	}

	private void handleEntityData(EntityDataHandlerEvent event, EntityData entityData) {
		final EntityTracker1_9 tracker = tracker(event.user());
		if (entityData.id() == EntityDataIndex1_9.ENTITY_STATUS.getIndex()) {
			tracker.getStatus().put(event.entityId(), (Byte) entityData.value());
		}
		final EntityDataIndex1_9 metaIndex = EntityDataIndex1_8.searchIndex(event.entityType(), entityData.id());
		if (metaIndex == null) {
			// Almost certainly bad data, remove it
			event.cancel();
			return;
		}
		if (metaIndex.getOldType() == null || metaIndex.getNewType() == null) {
			if (metaIndex == EntityDataIndex1_9.PLAYER_HAND) { // Player eating/aiming/drinking
				byte status = (byte) tracker.getStatus().getOrDefault(event.entityId(), 0);
				if ((((byte) entityData.value()) & 1 << HAND_ACTIVE_BIT) != 0) {
					status = (byte) (status | 1 << STATUS_USE_BIT);
				} else {
					status = (byte) (status & ~(1 << STATUS_USE_BIT));
				}
				event.createExtraData(new EntityData(EntityDataIndex1_9.ENTITY_STATUS.getIndex(), EntityDataTypes1_8.BYTE, status));
			}
			event.cancel();
			return;
		}

		entityData.setId(metaIndex.getIndex());
		entityData.setDataTypeUnsafe(metaIndex.getOldType());

		final Object value = entityData.getValue();
		switch (metaIndex.getNewType()) {
			case BYTE:
				if (metaIndex.getOldType() == EntityDataTypes1_8.BYTE) {
					entityData.setValue(value);
				}
				if (metaIndex.getOldType() == EntityDataTypes1_8.INT) {
					entityData.setValue(((Byte) value).intValue());
				}
				break;
			case OPTIONAL_UUID:
				if (metaIndex.getOldType() != EntityDataTypes1_8.STRING) {
					event.cancel();
					break;
				}
				final UUID owner = (UUID) value;
				entityData.setValue(owner != null ? owner.toString() : "");
				break;
			case OPTIONAL_BLOCK_STATE:
				event.cancel();
				event.createExtraData(new EntityData(metaIndex.getIndex(), EntityDataTypes1_8.SHORT, ((Integer) value).shortValue()));
				break;
			case VAR_INT:
				if (metaIndex.getOldType() == EntityDataTypes1_8.BYTE) {
					entityData.setValue(((Integer) value).byteValue());
				}
				if (metaIndex.getOldType() == EntityDataTypes1_8.SHORT) {
					entityData.setValue(((Integer) value).shortValue());
				}
				if (metaIndex.getOldType() == EntityDataTypes1_8.INT) {
					entityData.setValue(value);
				}
				break;
			case FLOAT:
			case STRING:
			case COMPONENT:
				entityData.setValue(value);
				break;
			case BOOLEAN:
				final boolean bool = (Boolean) value;
				if (metaIndex == EntityDataIndex1_9.ABSTRACT_AGEABLE_AGE) {
					entityData.setValue((byte) (bool ? -1 : 0));
				} else {
					entityData.setValue((byte) (bool ? 1 : 0));
				}
				break;
			case ITEM:
				entityData.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), (Item) value));
				break;
			case BLOCK_POSITION:
				final BlockPosition position = (BlockPosition) value;
				entityData.setValue(position);
				break;
			case ROTATIONS:
				final EulerAngle angle = (EulerAngle) value;
				entityData.setValue(angle);
				break;
			default:
				event.cancel();
				break;
		}
	}

	@Override
	public EntityTypes1_9.EntityType typeFromId(int type) {
		return EntityTypes1_9.getTypeFromId(type, false);
	}

	@Override
	public EntityTypes1_9.EntityType objectTypeFromId(int type) {
		return EntityTypes1_9.getTypeFromId(type, true);
	}
}
