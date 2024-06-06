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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.rewriter;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.rewriter.VREntityRewriter;
import com.viaversion.viarewind.api.type.RewindTypes;
import com.viaversion.viarewind.api.minecraft.entitydata.EntityDataTypes1_7_6_10;
import com.viaversion.viarewind.api.type.version.Types1_7_6_10;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data.EntityDataIndex1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data.VirtualHologramEntity;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.EntityTracker1_8;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.GameProfileStorage;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.PlayerSessionStorage;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.ScoreboardTracker;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.rewriter.entitydata.EntityDataHandlerEvent;
import com.viaversion.viaversion.util.IdAndData;

import java.util.List;
import java.util.UUID;

public class EntityPacketRewriter1_8 extends VREntityRewriter<ClientboundPackets1_8, Protocol1_8To1_7_6_10> {

	public EntityPacketRewriter1_8(Protocol1_8To1_7_6_10 protocol) {
		super(protocol, EntityDataTypes1_7_6_10.STRING, EntityDataTypes1_7_6_10.BYTE);
	}

	@Override
	protected void registerPackets() {
		protocol.registerClientbound(ClientboundPackets1_8.LOGIN, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Entity id
				map(Types.UNSIGNED_BYTE); // Game mode
				map(Types.BYTE); // Dimension
				map(Types.UNSIGNED_BYTE); // Difficulty
				map(Types.UNSIGNED_BYTE); // Max players
				map(Types.STRING); // Level type
				read(Types.BOOLEAN); // Reduced debug info

				handler(playerTrackerHandler());
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.INT, 0);

					if (ViaRewind.getConfig().isReplaceAdventureMode()) {
						if (wrapper.get(Types.UNSIGNED_BYTE, 0) == 2) { // adventure
							wrapper.set(Types.UNSIGNED_BYTE, 0, (short) 0); // survival
						}
					}

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					tracker.addPlayer(entityId, wrapper.user().getProtocolInfo().getUuid());
					tracker.setClientEntityGameMode(wrapper.get(Types.UNSIGNED_BYTE, 0));

					wrapper.user().get(ClientWorld.class).setEnvironment(wrapper.get(Types.BYTE, 0));

					// Reset on Velocity server change
					wrapper.user().put(new ScoreboardTracker(wrapper.user()));
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.REMOVE_ENTITIES, wrapper -> {
			final int[] entities = wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
			untrackEntities(wrapper.user(), entities);

			wrapper.cancel();

			// Split entity destroy packets into smaller packets because 1.8 can handle more entities at once then 1.7 can.
			final List<List<Integer>> parts = Lists.partition(Ints.asList(entities), Byte.MAX_VALUE);

			for (List<Integer> part : parts) {
				final PacketWrapper destroy = PacketWrapper.create(ClientboundPackets1_7_2_5.REMOVE_ENTITIES, wrapper.user());
				destroy.write(RewindTypes.INT_ARRAY, part.stream().mapToInt(Integer::intValue).toArray());
				destroy.scheduleSend(Protocol1_8To1_7_6_10.class);
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.SET_ENTITY_DATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types1_8.ENTITY_DATA_LIST, Types1_7_6_10.ENTITY_DATA_LIST); // Entity data
				handler(wrapper -> {
					final EntityTracker1_8 tracker = tracker(wrapper.user());
					final int entityId = wrapper.get(Types.INT, 0);
					final List<EntityData> entityData = wrapper.get(Types1_7_6_10.ENTITY_DATA_LIST, 0);
					if (tracker.getHolograms().containsKey(entityId)) {
						wrapper.cancel();
						tracker.getHolograms().get(entityId).syncState(EntityPacketRewriter1_8.this, entityData);
					} else {
						handleEntityData(entityId, entityData, wrapper.user());
					}
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.ADD_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.BYTE); // Type id
				map(Types.INT); // X
				map(Types.INT); // Y
				map(Types.INT); // Z
				map(Types.BYTE); // Pitch
				map(Types.BYTE); // Yaw
				map(Types.INT); // Data

				// Track entity
				handler(getObjectTrackerHandler());
				handler(getObjectRewriter(EntityTypes1_8.ObjectType::findById));

				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					final EntityTypes1_8.EntityType type = EntityTypes1_8.getTypeFromId(wrapper.get(Types.BYTE, 0), true);

					int x = wrapper.get(Types.INT, 0);
					int y = wrapper.get(Types.INT, 1);
					int z = wrapper.get(Types.INT, 2);

					byte pitch = wrapper.get(Types.BYTE, 1);
					byte yaw = wrapper.get(Types.BYTE, 2);

					int data = wrapper.get(Types.INT, 3);

					if (type == EntityTypes1_8.ObjectType.ITEM_FRAME.getType()) {
						yaw = switch (yaw) {
							case -128 -> {
								z += 32;
								yield 0;
							}
							case -64 -> {
								x -= 32;
								yield -64;
							}
							case 0 -> {
								z -= 32;
								yield -128;
							}
							case 64 -> {
								x += 32;
								yield 64;
							}
							default -> yaw;
						};
					} else if (type == EntityTypes1_8.ObjectType.ARMOR_STAND.getType()) {
						wrapper.cancel();

						final EntityTracker1_8 tracker = tracker(wrapper.user());
						final VirtualHologramEntity hologram = tracker.getHolograms().get(entityId);
						hologram.setPosition(x / 32.0, y / 32.0, z / 32.0);
						hologram.setRotation(yaw * 360f / 256, pitch * 360f / 256);
						hologram.setHeadYaw(yaw * 360f / 256);
					} else if (type != null && type.isOrHasParent(EntityTypes1_8.EntityType.FALLING_BLOCK)) {
						int blockId = data & 0xFFF;
						int blockData = data >> 12 & 0xF;
						final IdAndData replace = protocol.getItemRewriter().handleBlock(blockId, blockData);
						if (replace != null) {
							blockId = replace.getId();
							blockData = replace.getData();
						}
						wrapper.set(Types.INT, 3, data = (blockId | blockData << 16));
					}

					wrapper.set(Types.INT, 0, x);
					wrapper.set(Types.INT, 1, y);
					wrapper.set(Types.INT, 2, z);
					wrapper.set(Types.BYTE, 2, yaw);

					if (data > 0) {
						wrapper.passthrough(Types.SHORT); // Velocity x
						wrapper.passthrough(Types.SHORT); // Velocity y
						wrapper.passthrough(Types.SHORT); // Velocity z
					}
				});
			}
		});
		registerTracker(ClientboundPackets1_8.ADD_EXPERIENCE_ORB, EntityType.EXPERIENCE_ORB);
		registerTracker(ClientboundPackets1_8.ADD_GLOBAL_ENTITY, EntityType.LIGHTNING_BOLT);
		protocol.registerClientbound(ClientboundPackets1_8.ADD_PAINTING, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.STRING); // Title
				map(Types.BLOCK_POSITION1_8, RewindTypes.INT_POSITION); // Position
				map(Types.UNSIGNED_BYTE, Types.INT); // Rotation
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					final BlockPosition position = wrapper.get(RewindTypes.INT_POSITION, 0);
					final int rotation = wrapper.get(Types.INT, 0);
					int modX = 0;
					int modZ = 0;
					switch (rotation) {
						case 0:
							modZ = -1;
							break;
						case 1:
							modX = 1;
							break;
						case 2:
							modZ = 1;
							break;
						case 3:
							modX = -1;
							break;
					}
					wrapper.set(RewindTypes.INT_POSITION, 0, new BlockPosition(position.x() + modX, position.y(), position.z() + modZ));
					addTrackedEntity(wrapper, entityId, EntityTypes1_8.EntityType.PAINTING);
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.ADD_MOB, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.UNSIGNED_BYTE); // Entity type
				map(Types.INT); // X
				map(Types.INT); // Y
				map(Types.INT); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				map(Types.BYTE); // Head yaw
				map(Types.SHORT); // Velocity x
				map(Types.SHORT); // Velocity y
				map(Types.SHORT); // Velocity z
				map(Types1_8.ENTITY_DATA_LIST, Types1_7_6_10.ENTITY_DATA_LIST); // Entity data

				handler(getTrackerHandler(Types.UNSIGNED_BYTE, 0));

				// Handle holograms
				handler(wrapper -> {
					final short typeId = wrapper.get(Types.UNSIGNED_BYTE, 0);

					final EntityTypes1_8.EntityType type = EntityTypes1_8.getTypeFromId(typeId, false);
					if (type == EntityTypes1_8.EntityType.ARMOR_STAND) {
						wrapper.cancel();
						final int entityId = wrapper.get(Types.VAR_INT, 0);

						final int x = wrapper.get(Types.INT, 0);
						final int y = wrapper.get(Types.INT, 1);
						final int z = wrapper.get(Types.INT, 2);

						final byte pitch = wrapper.get(Types.BYTE, 1);
						final byte yaw = wrapper.get(Types.BYTE, 0);
						final byte headYaw = wrapper.get(Types.BYTE, 2);
						final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
						final VirtualHologramEntity hologram = tracker.getHolograms().get(entityId);

						hologram.setPosition(x / 32.0, y / 32.0, z / 32.0);
						hologram.setRotation(yaw * 360f / 256, pitch * 360f / 256);
						hologram.setHeadYaw(headYaw * 360f / 256);
						hologram.syncState(protocol().getEntityRewriter(), wrapper.get(Types1_7_6_10.ENTITY_DATA_LIST, 0));
					}
				});
				handler(getMobSpawnRewriter(Types1_7_6_10.ENTITY_DATA_LIST));
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.ADD_PLAYER, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				handler(wrapper -> {
					final UUID uuid = wrapper.read(Types.UUID);
					wrapper.write(Types.STRING, uuid.toString());

					final GameProfileStorage gameProfileStorage = wrapper.user().get(GameProfileStorage.class);

					GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
					if (gameProfile == null) {
						wrapper.write(Types.STRING, ""); // Name
						wrapper.write(Types.VAR_INT, 0); // Properties count
					} else {
						wrapper.write(Types.STRING, gameProfile.name.length() > 16 ? gameProfile.name.substring(0, 16) : gameProfile.name); // Name
						wrapper.write(Types.VAR_INT, gameProfile.properties.size()); // Properties count

						for (GameProfileStorage.Property property : gameProfile.properties) {
							wrapper.write(Types.STRING, property.name); // Property name
							wrapper.write(Types.STRING, property.value); // Property value
							wrapper.write(Types.STRING, property.signature == null ? "" : property.signature); // Property signature
						}
					}

					final int entityId = wrapper.get(Types.VAR_INT, 0);

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					if (gameProfile != null && gameProfile.gamemode == 3) { // Spectator mode
						for (short i = 0; i < 5; i++) {
							final PacketWrapper entityEquipment = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_EQUIPPED_ITEM, wrapper.user());
							entityEquipment.write(Types.INT, entityId);
							entityEquipment.write(Types.SHORT, i);
							entityEquipment.write(RewindTypes.COMPRESSED_NBT_ITEM, i == 4 ? gameProfile.getSkull() : null);

							entityEquipment.scheduleSend(Protocol1_8To1_7_6_10.class);
						}
					}

					tracker.addPlayer(entityId, uuid);
				});
				map(Types.INT); // X
				map(Types.INT); // Y
				map(Types.INT); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				map(Types.SHORT); // Current item
				map(Types1_8.ENTITY_DATA_LIST, Types1_7_6_10.ENTITY_DATA_LIST); // Entity data

				handler(getTrackerAndMetaHandler(Types1_7_6_10.ENTITY_DATA_LIST, EntityTypes1_8.EntityType.PLAYER));
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.SET_EQUIPPED_ITEM, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.SHORT); // Slot
				map(Types.ITEM1_8, RewindTypes.COMPRESSED_NBT_ITEM); // Item

				handler(wrapper -> protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.get(RewindTypes.COMPRESSED_NBT_ITEM, 0)));

				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					final int id = wrapper.get(Types.INT, 0);

					final int limit = tracker.clientEntityId() == id ? 3 : 4;
					if (wrapper.get(Types.SHORT, 0) > limit) {
						wrapper.cancel();
					}
				});

				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					final short slot = wrapper.get(Types.SHORT, 0);
					final UUID uuid = tracker.getPlayerUUID(wrapper.get(Types.INT, 0));
					if (uuid == null) {
						return;
					}
					final Item item = wrapper.get(RewindTypes.COMPRESSED_NBT_ITEM, 0);
					wrapper.user().get(PlayerSessionStorage.class).setPlayerEquipment(uuid, item, slot);

					final GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
					GameProfileStorage.GameProfile profile = storage.get(uuid);
					if (profile != null && profile.gamemode == 3) { // spectator mode didn't exist in 1.7.10
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_SLEEP, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.BLOCK_POSITION1_8, RewindTypes.U_BYTE_POSITION); // Position
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.TAKE_ITEM_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Collected Entity id
				map(Types.VAR_INT, Types.INT); // Collector Entity id

				handler(wrapper -> wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class).removeEntity(wrapper.get(Types.INT, 0)));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_ENTITY_MOTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MOVE_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MOVE_ENTITY_POS, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.BYTE); // X
				map(Types.BYTE); // Y
				map(Types.BYTE); // Z
				read(Types.BOOLEAN); // On ground

				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getHolograms().get(wrapper.get(Types.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int x = wrapper.get(Types.BYTE, 0);
						final int y = wrapper.get(Types.BYTE, 1);
						final int z = wrapper.get(Types.BYTE, 2);

						hologram.setRelativePosition(x / 32.0, y / 32.0, z / 32.0);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MOVE_ENTITY_ROT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				read(Types.BOOLEAN); // On ground

				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getHolograms().get(wrapper.get(Types.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int yaw = wrapper.get(Types.BYTE, 0);
						final int pitch = wrapper.get(Types.BYTE, 1);

						hologram.setRotation(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MOVE_ENTITY_POS_ROT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.BYTE); // X
				map(Types.BYTE); // Y
				map(Types.BYTE); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				read(Types.BOOLEAN); // On ground

				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getHolograms().get(wrapper.get(Types.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int x = wrapper.get(Types.BYTE, 0);
						final int y = wrapper.get(Types.BYTE, 1);
						final int z = wrapper.get(Types.BYTE, 2);

						final int yaw = wrapper.get(Types.BYTE, 3);
						final int pitch = wrapper.get(Types.BYTE, 4);

						hologram.setRelativePosition(x / 32.0, y / 32.0, z / 32.0);
						hologram.setRotation(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.TELEPORT_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.INT); // X
				map(Types.INT); // Y
				map(Types.INT); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				read(Types.BOOLEAN); // On ground
				handler(wrapper -> {
					final int entityId = wrapper.get(Types.INT, 0);

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					if (tracker.entityType(entityId) == EntityTypes1_8.EntityType.ABSTRACT_MINECART) { // TODO | Realign all entities?
						int y = wrapper.get(Types.INT, 2);
						y += 12;
						wrapper.set(Types.INT, 2, y);
					}

					final VirtualHologramEntity hologram = tracker.getHolograms().get(entityId);
					if (hologram != null) {
						wrapper.cancel();
						final int x = wrapper.get(Types.INT, 1);
						final int y = wrapper.get(Types.INT, 2);
						final int z = wrapper.get(Types.INT, 3);

						final int yaw = wrapper.get(Types.BYTE, 0);
						final int pitch = wrapper.get(Types.BYTE, 1);

						hologram.setPosition(x / 32.0, y / 32.0, z / 32.0);
						hologram.setRotation(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ROTATE_HEAD, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.BYTE); // Head yaw

				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getHolograms().get(wrapper.get(Types.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int yaw = wrapper.get(Types.BYTE, 0);

						hologram.setHeadYaw(yaw * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_ENTITY_LINK, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Riding Entity id
				map(Types.INT); // Vehicle Entity id
				map(Types.BOOLEAN); // Leash state
				handler(wrapper -> {
					final boolean leash = wrapper.get(Types.BOOLEAN, 0);
					if (!leash) {
						final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);

						final int passenger = wrapper.get(Types.INT, 0);
						final int vehicle = wrapper.get(Types.INT, 1);

						tracker.setPassenger(vehicle, passenger);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.UPDATE_MOB_EFFECT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.BYTE); // Effect id
				map(Types.BYTE); // Amplifier
				map(Types.VAR_INT, Types.SHORT); // Duration
				read(Types.BYTE); // Hide particles
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.REMOVE_MOB_EFFECT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				map(Types.BYTE); // Effect id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.UPDATE_ATTRIBUTES, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // Entity id
				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					if (tracker.getHolograms().containsKey(wrapper.get(Types.INT, 0))) { // Don't handle properties for hologram emulation
						wrapper.cancel();
						return;
					}
					final int amount = wrapper.passthrough(Types.INT);
					for (int i = 0; i < amount; i++) {
						wrapper.passthrough(Types.STRING); // id
						wrapper.passthrough(Types.DOUBLE); // value

						int modifierLength = wrapper.read(Types.VAR_INT);
						wrapper.write(Types.SHORT, (short) modifierLength);
						for (int j = 0; j < modifierLength; j++) {
							wrapper.passthrough(Types.UUID); // modifier uuid
							wrapper.passthrough(Types.DOUBLE); // modifier amount
							wrapper.passthrough(Types.BYTE); // modifier operation
						}
					}
				});

			}
		});

		protocol.cancelClientbound(ClientboundPackets1_8.UPDATE_ENTITY_NBT);
	}

	@Override
	protected void registerRewrites() {
		// Handle new entities
		mapEntityTypeWithData(EntityType.GUARDIAN, EntityType.SQUID).plainName();
		mapEntityTypeWithData(EntityType.ENDERMITE, EntityType.SQUID).plainName();
		mapEntityTypeWithData(EntityType.RABBIT, EntityType.CHICKEN).plainName();

		// Entity data rewrite
		filter().handler(this::handleEntityData);
	}

	public void handleEntityData(EntityDataHandlerEvent event, EntityData entityData) {
		final EntityDataIndex1_7_6_10 metaIndex = EntityDataIndex1_7_6_10.searchIndex(event.entityType(), entityData.id());
		if (metaIndex == null) {
			// Almost certainly bad data, remove it
			event.cancel();
			return;
		}
		if (metaIndex.getOldType() == null) {
			event.cancel();
			return;
		}
		final Object value = entityData.getValue();
		entityData.setTypeAndValue(metaIndex.getNewType(), value);
		entityData.setDataTypeUnsafe(metaIndex.getOldType());
		entityData.setId(metaIndex.getIndex());

		switch (metaIndex.getOldType()) {
			case INT:
				if (metaIndex.getNewType() == EntityDataTypes1_8.BYTE) {
					entityData.setValue(((Byte) value).intValue());
					if (metaIndex == EntityDataIndex1_7_6_10.ABSTRACT_AGEABLE_AGE) {
						if ((Integer) entityData.getValue() < 0) {
							entityData.setValue(-25000);
						}
					}
				}
				if (metaIndex.getNewType() == EntityDataTypes1_8.SHORT) {
					entityData.setValue(((Short) value).intValue());
				}
				if (metaIndex.getNewType() == EntityDataTypes1_8.INT) {
					entityData.setValue(value);
				}
				break;
			case BYTE:
				if (metaIndex.getNewType() == EntityDataTypes1_8.INT) {
					entityData.setValue(((Integer) value).byteValue());
				}
				if (metaIndex.getNewType() == EntityDataTypes1_8.BYTE) {
					if (metaIndex == EntityDataIndex1_7_6_10.ITEM_FRAME_ROTATION) {
						entityData.setValue(Integer.valueOf((Byte) value % 4).byteValue());
					} else {
						entityData.setValue(value);
					}
				}
				if (metaIndex == EntityDataIndex1_7_6_10.PLAYER_SKIN_FLAGS) {
					byte flags = (byte) value;
					boolean cape = (flags & 0x01) != 0;
					flags = (byte) (cape ? 0x00 : 0x02);
					entityData.setValue(flags);
				}
				break;
			case ITEM:
				entityData.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), (Item) value));
				break;
			case FLOAT:
			case STRING:
			case SHORT:
			case POSITION:
				entityData.setValue(value);
				break;
			default:
				event.cancel();
				break;
		}
	}

	@Override
	public EntityTypes1_8.EntityType typeFromId(int type) {
		return EntityTypes1_8.getTypeFromId(type, false);
	}

	@Override
	public EntityTypes1_8.EntityType objectTypeFromId(int type) {
		return EntityTypes1_8.getTypeFromId(type, true);
	}
}
