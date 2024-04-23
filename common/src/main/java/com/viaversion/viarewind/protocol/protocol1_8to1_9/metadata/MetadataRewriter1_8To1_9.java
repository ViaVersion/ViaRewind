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
package com.viaversion.viarewind.protocol.protocol1_8to1_9.metadata;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.rewriter.VREntityRewriter;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetaIndex;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEvent;
import com.viaversion.viaversion.util.IdAndData;

import java.util.UUID;
import java.util.logging.Level;

public class MetadataRewriter1_8To1_9 extends VREntityRewriter<ClientboundPackets1_9, Protocol1_8To1_9> {

	private static final byte HAND_ACTIVE_BIT = 0;
	private static final byte STATUS_USE_BIT = 4;

	public MetadataRewriter1_8To1_9(Protocol1_8To1_9 protocol) {
		super(protocol);
	}

	@Override
	protected void registerPackets() {
		registerRemoveEntities(ClientboundPackets1_9.DESTROY_ENTITIES);
		registerJoinGame1_8(ClientboundPackets1_9.JOIN_GAME, EntityType.ENTITY_HUMAN);
		registerMetadataRewriter(ClientboundPackets1_9.ENTITY_METADATA, Types1_9.METADATA_LIST, Types1_8.METADATA_LIST);

		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				read(Type.UUID); // Entity UUID
				map(Type.BYTE); // Entity type
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // X
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Y
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Z
				map(Type.BYTE); // Pitch
				map(Type.BYTE); // Yaw
				map(Type.INT); // Data

				// Track entity
				handler(getObjectTrackerHandler());
				handler(getObjectRewriter(id -> EntityTypes1_10.ObjectType.findById(id).orElse(null)));

				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					final int entityType = wrapper.get(Type.BYTE, 0);
					final EntityTypes1_10.EntityType type = EntityTypes1_10.getTypeFromId(entityType, true);

					// Cancel new entities which can't be handled properly
					if (type == EntityTypes1_10.EntityType.AREA_EFFECT_CLOUD || type == EntityTypes1_10.EntityType.SPECTRAL_ARROW || type == EntityTypes1_10.EntityType.DRAGON_FIREBALL) {
						wrapper.cancel();
						return;
					}

					if (type.is(EntityTypes1_10.EntityType.BOAT)) {
						byte yaw = wrapper.get(Type.BYTE, 1);
						yaw -= 64;
						wrapper.set(Type.BYTE, 1, yaw);

						int y = wrapper.get(Type.INT, 1);
						y += 10;
						wrapper.set(Type.INT, 1, y);
					}

					int data = wrapper.get(Type.INT, 3);

					if (type.isOrHasParent(EntityTypes1_10.EntityType.ARROW) && data != 0) {
						wrapper.set(Type.INT, 3, --data);
					}
					if (type.is(EntityTypes1_10.EntityType.FALLING_BLOCK)) {
						int blockId = data & 0xFFF;
						int blockData = data >> 12 & 0xF;
						IdAndData replace = protocol.getItemRewriter().handleBlock(blockId, blockData);
						if (replace != null) {
							wrapper.set(Type.INT, 3, replace.getId() | replace.getData() << 12);
						}
					}

					if (data > 0) {
						wrapper.passthrough(Type.SHORT); // Velocity x
						wrapper.passthrough(Type.SHORT); // Velocity y
						wrapper.passthrough(Type.SHORT); // Velocity z
					} else {
						final short velocityX = wrapper.read(Type.SHORT);
						final short velocityY = wrapper.read(Type.SHORT);
						final short velocityZ = wrapper.read(Type.SHORT);

						final PacketWrapper velocityPacket = PacketWrapper.create(ClientboundPackets1_8.ENTITY_VELOCITY, wrapper.user());
						velocityPacket.write(Type.VAR_INT, entityId);
						velocityPacket.write(Type.SHORT, velocityX);
						velocityPacket.write(Type.SHORT, velocityY);
						velocityPacket.write(Type.SHORT, velocityZ);
						velocityPacket.scheduleSend(Protocol1_8To1_9.class);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_EXPERIENCE_ORB, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // X
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Y
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Z
				map(Type.SHORT); // Count
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					wrapper.user().getEntityTracker(Protocol1_8To1_9.class).addEntity(entityId, EntityTypes1_10.EntityType.EXPERIENCE_ORB);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_GLOBAL_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				map(Type.BYTE); // Type
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // X
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Y
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Z
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					wrapper.user().getEntityTracker(Protocol1_8To1_9.class).addEntity(entityId, EntityTypes1_10.EntityType.LIGHTNING);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_MOB, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				read(Type.UUID); // Entity UUID
				map(Type.UNSIGNED_BYTE); // Entity type
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // X
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Y
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Z
				map(Type.BYTE); // Yaw
				map(Type.BYTE); // Pitch
				map(Type.BYTE); // Head pitch
				map(Type.SHORT); // Velocity x
				map(Type.SHORT); // Velocity y
				map(Type.SHORT); // Velocity z
				map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST); // Metadata

				handler(getTrackerHandler(Type.UNSIGNED_BYTE, 0));
				handler(getMobSpawnRewriter(Types1_8.METADATA_LIST));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_PAINTING, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				read(Type.UUID); // Entity UUID
				map(Type.STRING); // Title
				map(Type.POSITION1_8); // Position
				map(Type.BYTE, Type.UNSIGNED_BYTE); // Direction
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					wrapper.user().getEntityTracker(Protocol1_8To1_9.class).addEntity(entityId, EntityTypes1_10.EntityType.PAINTING);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_PLAYER, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				map(Type.UUID); // Player UUID
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // X
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Y
				map(Type.DOUBLE, Protocol1_8To1_9.DOUBLE_TO_INT_TIMES_32); // Z
				map(Type.BYTE); // Yaw
				map(Type.BYTE); // Pitch
				create(Type.SHORT, (short) 0); // Current item
				map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST); // Metadata

				handler(getTrackerAndMetaHandler(Types1_9.METADATA_LIST, EntityTypes1_10.EntityType.PLAYER));
			}
		});
	}

	@Override
	protected void registerRewrites() {
		// Handle new entities
		mapEntityTypeWithData(EntityType.SHULKER, EntityType.MAGMA_CUBE).plainName();
		mapEntityTypeWithData(EntityType.SHULKER_BULLET, EntityType.WITCH).plainName();

		// Metadata rewrite
		filter().handler((event, meta) -> {
			try {
				handleMetadata(event, meta);
			} catch (Exception e) {
				if (Via.getManager().isDebug()) {
					ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "An error occurred with entity metadata: " + meta, e);
				}
				event.cancel();
			}
		});
	}

	private void handleMetadata(MetaHandlerEvent event, Metadata metadata) {
		final EntityTracker1_9 tracker = tracker(event.user());
		if (metadata.id() == MetaIndex.ENTITY_STATUS.getIndex()) {
			tracker.getStatus().put(event.entityId(), (Byte) metadata.value());
		}
		final MetaIndex metaIndex = MetaIndex1_8to1_9.searchIndex(event.entityType(), metadata.id());
		if (metaIndex == null) {
			// Almost certainly bad data, remove it
			event.cancel();
			return;
		}
		if (metaIndex.getOldType() == null || metaIndex.getNewType() == null) {
			if (metaIndex == MetaIndex.PLAYER_HAND) { // Player eating/aiming/drinking
				int status = tracker.getStatus().getOrDefault(event.entityId(), 0);
				if ((((byte) metadata.value()) & 1 << HAND_ACTIVE_BIT) != 0) {
					status = (byte) (status | 1 << STATUS_USE_BIT);
				} else {
					status = (byte) (status & ~(1 << STATUS_USE_BIT));
				}
				event.createExtraMeta(new Metadata(MetaIndex.ENTITY_STATUS.getIndex(), MetaType1_8.Byte, status));
				return;
			}
			event.cancel();
			return;
		}

		metadata.setId(metaIndex.getIndex());
		metadata.setMetaTypeUnsafe(metaIndex.getOldType());

		final Object value = metadata.getValue();
		switch (metaIndex.getNewType()) {
			case Byte:
				if (metaIndex.getOldType() == MetaType1_8.Byte) {
					metadata.setValue(value);
				}
				if (metaIndex.getOldType() == MetaType1_8.Int) {
					metadata.setValue(((Byte) value).intValue());
				}
				break;
			case OptUUID:
				if (metaIndex.getOldType() != MetaType1_8.String) {
					event.cancel();
					break;
				}
				final UUID owner = (UUID) value;
				metadata.setValue(owner != null ? owner.toString() : "");
				break;
			case BlockID:
				event.cancel();
				event.createExtraMeta(new Metadata(metaIndex.getIndex(), MetaType1_8.Short, ((Integer) value).shortValue()));
				break;
			case VarInt:
				if (metaIndex.getOldType() == MetaType1_8.Byte) {
					metadata.setValue(((Integer) value).byteValue());
				}
				if (metaIndex.getOldType() == MetaType1_8.Short) {
					metadata.setValue(((Integer) value).shortValue());
				}
				if (metaIndex.getOldType() == MetaType1_8.Int) {
					metadata.setValue(value);
				}
				break;
			case Float:
			case String:
			case Chat:
				metadata.setValue(value);
				break;
			case Boolean:
				final boolean bool = (Boolean) value;
				if (metaIndex == MetaIndex.AGEABLE_CREATURE_AGE) {
					metadata.setValue((byte) (bool ? -1 : 0));
				} else {
					metadata.setValue((byte) (bool ? 1 : 0));
				}
				break;
			case Slot:
				metadata.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), (Item) value));
				break;
			case Position:
				final Vector vector = (Vector) value;
				metadata.setValue(vector);
				break;
			case Vector3F:
				final EulerAngle angle = (EulerAngle) value;
				metadata.setValue(angle);
				break;
			default:
				event.cancel();
				break;
		}
	}

	@Override
	public EntityType typeFromId(int type) {
		return EntityTypes1_10.getTypeFromId(type, false);
	}

	@Override
	public EntityType objectTypeFromId(int type) {
		return EntityTypes1_10.getTypeFromId(type, true);
	}
}
