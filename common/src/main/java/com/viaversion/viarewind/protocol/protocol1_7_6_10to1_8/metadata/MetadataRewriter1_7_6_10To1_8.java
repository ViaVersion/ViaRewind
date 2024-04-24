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
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.metadata;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.rewriter.VREntityRewriter;
import com.viaversion.viarewind.api.type.Types1_7_6_10;
import com.viaversion.viarewind.api.type.metadata.MetaType1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.emulation.VirtualHologramEntity;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.Scoreboard;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEvent;
import com.viaversion.viaversion.util.IdAndData;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class MetadataRewriter1_7_6_10To1_8 extends VREntityRewriter<ClientboundPackets1_8, Protocol1_7_6_10To1_8> {

	public MetadataRewriter1_7_6_10To1_8(Protocol1_7_6_10To1_8 protocol) {
		super(protocol, MetaType1_7_6_10.String, MetaType1_7_6_10.Byte);
	}

	@Override
	protected void registerPackets() {
		protocol.registerClientbound(ClientboundPackets1_8.JOIN_GAME, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // Entity id
				map(Type.UNSIGNED_BYTE); // Game mode
				map(Type.BYTE); // Dimension
				map(Type.UNSIGNED_BYTE); // Difficulty
				map(Type.UNSIGNED_BYTE); // Max players
				map(Type.STRING); // Level type
				read(Type.BOOLEAN); // Reduced debug info

				handler(playerTrackerHandler());
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.INT, 0);

					if (ViaRewind.getConfig().isReplaceAdventureMode()) {
						if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 2) { // adventure
							wrapper.set(Type.UNSIGNED_BYTE, 0, (short) 0); // survival
						}
					}

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					tracker.addPlayer(entityId, wrapper.user().getProtocolInfo().getUuid());
					tracker.setClientEntityGameMode(wrapper.get(Type.UNSIGNED_BYTE, 0));

					wrapper.user().get(ClientWorld.class).setEnvironment(wrapper.get(Type.BYTE, 0));

					// Reset on Velocity server change
					wrapper.user().put(new Scoreboard(wrapper.user()));
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.DESTROY_ENTITIES, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT_ARRAY_PRIMITIVE, Types1_7_6_10.BYTE_INT_ARRAY); // Entity ids
				handler(wrapper -> {
					final int[] entities = wrapper.get(Types1_7_6_10.BYTE_INT_ARRAY, 0);
					untrackEntities(wrapper.user(), entities);
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_METADATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // Entity id
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST); // Metadata
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.INT, 0);
					final List<Metadata> metadata = wrapper.get(Types1_7_6_10.METADATA_LIST, 0);
					handleMetadata(entityId, metadata, wrapper.user());
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				map(Type.BYTE); // Type id
				map(Type.INT); // X
				map(Type.INT); // Y
				map(Type.INT); // Z
				map(Type.BYTE); // Pitch
				map(Type.BYTE); // Yaw
				map(Type.INT); // Data

				// Track entity
				handler(getObjectTrackerHandler());
				handler(getObjectRewriter(id -> EntityTypes1_10.ObjectType.findById(id).orElse(null)));

				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					final EntityTypes1_10.EntityType type = EntityTypes1_10.getTypeFromId(wrapper.get(Type.BYTE, 0), true);

					int x = wrapper.get(Type.INT, 0);
					int y = wrapper.get(Type.INT, 1);
					int z = wrapper.get(Type.INT, 2);

					byte pitch = wrapper.get(Type.BYTE, 1);
					byte yaw = wrapper.get(Type.BYTE, 2);

					int data = wrapper.get(Type.INT, 3);

					if (type == EntityTypes1_10.ObjectType.ITEM_FRAME.getType()) {
						switch (yaw) {
							case -128:
								z += 32;
								yaw = 0;
								break;
							case -64:
								x -= 32;
								yaw = -64;
								break;
							case 0:
								z -= 32;
								yaw = -128;
								break;
							case 64:
								x += 32;
								yaw = 64;
								break;
						}
					} else if (type == EntityTypes1_10.ObjectType.ARMOR_STAND.getType()) {
						wrapper.cancel();

						final EntityTracker1_8 tracker = tracker(wrapper.user());

						final VirtualHologramEntity hologram = tracker.getHolograms().get(entityId);
						hologram.updateReplacementPosition(x / 32.0, y / 32.0, z / 32.0);
						hologram.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
						hologram.setHeadYaw(yaw * 360f / 256);
					} else if (type != null && type.isOrHasParent(EntityTypes1_10.EntityType.FALLING_BLOCK)) {
						int blockId = data & 0xFFF;
						int blockData = data >> 12 & 0xF;
						final IdAndData replace = protocol.getItemRewriter().handleBlock(blockId, blockData);
						if (replace != null) {
							blockId = replace.getId();
							blockData = replace.getData();
						}
						wrapper.set(Type.INT, 3, data = (blockId | blockData << 16));
					}

					wrapper.set(Type.INT, 0, x);
					wrapper.set(Type.INT, 1, y);
					wrapper.set(Type.INT, 2, z);
					wrapper.set(Type.BYTE, 2, yaw);

					if (data > 0) {
						wrapper.passthrough(Type.SHORT); // Velocity x
						wrapper.passthrough(Type.SHORT); // Velocity y
						wrapper.passthrough(Type.SHORT); // Velocity z
					}
				});
			}
		});
		registerTracker(ClientboundPackets1_8.SPAWN_EXPERIENCE_ORB, EntityType.EXPERIENCE_ORB);
		registerTracker(ClientboundPackets1_8.SPAWN_GLOBAL_ENTITY, EntityType.LIGHTNING);
		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_PAINTING, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				map(Type.STRING); // Title
				map(Type.POSITION1_8, Types1_7_6_10.INT_POSITION); // Position
				map(Type.UNSIGNED_BYTE, Type.INT); // Rotation
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					final Position position = wrapper.get(Types1_7_6_10.INT_POSITION, 0);
					final int rotation = wrapper.get(Type.INT, 0);
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
					wrapper.set(Types1_7_6_10.INT_POSITION, 0, new Position(position.x() + modX, position.y(), position.z() + modZ));
					addTrackedEntity(wrapper, entityId, EntityTypes1_10.EntityType.PAINTING);
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_MOB, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				map(Type.UNSIGNED_BYTE); // Entity type
				map(Type.INT); // X
				map(Type.INT); // Y
				map(Type.INT); // Z
				map(Type.BYTE); // Yaw
				map(Type.BYTE); // Pitch
				map(Type.BYTE); // Head yaw
				map(Type.SHORT); // Velocity x
				map(Type.SHORT); // Velocity y
				map(Type.SHORT); // Velocity z
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST); // Metadata

				handler(getTrackerHandler(Type.UNSIGNED_BYTE, 0));
				handler(getMobSpawnRewriter(Types1_7_6_10.METADATA_LIST));

				// Handle holograms
				handler(wrapper -> {
					final short typeId = wrapper.get(Type.UNSIGNED_BYTE, 0);

					final EntityTypes1_10.EntityType type = EntityTypes1_10.getTypeFromId(typeId, false);
					if (type == EntityTypes1_10.EntityType.ARMOR_STAND) {
						wrapper.cancel();
						final int entityId = wrapper.get(Type.VAR_INT, 0);

						final int x = wrapper.get(Type.INT, 0);
						final int y = wrapper.get(Type.INT, 1);
						final int z = wrapper.get(Type.INT, 2);

						final byte pitch = wrapper.get(Type.BYTE, 1);
						final byte yaw = wrapper.get(Type.BYTE, 0);
						final byte headYaw = wrapper.get(Type.BYTE, 2);
						final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
						final VirtualHologramEntity hologram = tracker.getHolograms().get(entityId);

						hologram.updateReplacementPosition(x / 32.0, y / 32.0, z / 32.0);
						hologram.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
						hologram.setHeadYaw(headYaw * 360f / 256);
					}
				});
			}
		});
		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_PLAYER, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				handler(wrapper -> {
					final UUID uuid = wrapper.read(Type.UUID);
					wrapper.write(Type.STRING, uuid.toString()); // map to string

					final GameProfileStorage gameProfileStorage = wrapper.user().get(GameProfileStorage.class);

					GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
					if (gameProfile == null) {
						wrapper.write(Type.STRING, ""); // name
						wrapper.write(Type.VAR_INT, 0); // properties count
					} else {
						wrapper.write(Type.STRING, gameProfile.name.length() > 16 ? gameProfile.name.substring(0, 16) : gameProfile.name); // name
						wrapper.write(Type.VAR_INT, gameProfile.properties.size()); // properties count

						for (GameProfileStorage.Property property : gameProfile.properties) {
							wrapper.write(Type.STRING, property.name); // property name
							wrapper.write(Type.STRING, property.value); // property value
							wrapper.write(Type.STRING, property.signature == null ? "" : property.signature); // property signature
						}
					}

					final int entityId = wrapper.get(Type.VAR_INT, 0);

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					if (gameProfile != null && gameProfile.gamemode == 3) { // Spectator mode
						for (short i = 0; i < 5; i++) {
							final PacketWrapper entityEquipment = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_EQUIPMENT, wrapper.user());
							entityEquipment.write(Type.INT, entityId);
							entityEquipment.write(Type.SHORT, i);
							entityEquipment.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, i == 4 ? gameProfile.getSkull() : null);

							entityEquipment.scheduleSend(Protocol1_7_6_10To1_8.class);
						}
					}

					tracker.addPlayer(entityId, uuid);
				});
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				map(Type.BYTE); // yaw
				map(Type.BYTE); // pitch
				map(Type.SHORT); // Current item
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST); // metadata

				handler(getTrackerAndMetaHandler(Types1_7_6_10.METADATA_LIST, EntityTypes1_10.EntityType.PLAYER));
			}
		});
	}

	@Override
	protected void registerRewrites() {
		// Handle new entities
		mapEntityTypeWithData(EntityType.GUARDIAN, EntityType.SQUID).plainName();
		mapEntityTypeWithData(EntityType.ENDERMITE, EntityType.SQUID).plainName();
		mapEntityTypeWithData(EntityType.RABBIT, EntityType.CHICKEN).plainName();

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
		final MetaIndex metaIndex = MetaIndex.searchIndex(event.entityType(), metadata.id());
		if (metaIndex == null) {
			// Almost certainly bad data, remove it
			event.cancel();
			return;
		}
		if (metaIndex.getOldType() == null) {
			event.cancel();
			return;
		}
		final Object value = metadata.getValue();
		metadata.setTypeAndValue(metaIndex.getNewType(), value);
		metadata.setMetaTypeUnsafe(metaIndex.getOldType());
		metadata.setId(metaIndex.getIndex());

		switch (metaIndex.getOldType()) {
			case Int:
				if (metaIndex.getNewType() == MetaType1_8.Byte) {
					metadata.setValue(((Byte) value).intValue());
					if (metaIndex == MetaIndex.ENTITY_AGEABLE_AGE) {
						if ((Integer) metadata.getValue() < 0) {
							metadata.setValue(-25000);
						}
					}
				}
				if (metaIndex.getNewType() == MetaType1_8.Short) {
					metadata.setValue(((Short) value).intValue());
				}
				if (metaIndex.getNewType() == MetaType1_8.Int) {
					metadata.setValue(value);
				}
				break;
			case Byte:
				if (metaIndex.getNewType() == MetaType1_8.Int) {
					metadata.setValue(((Integer) value).byteValue());
				}
				if (metaIndex.getNewType() == MetaType1_8.Byte) {
					if (metaIndex == MetaIndex.ITEM_FRAME_ROTATION) {
						metadata.setValue(Integer.valueOf((Byte) value % 4).byteValue());
					} else {
						metadata.setValue(value);
					}
				}
				if (metaIndex == MetaIndex.HUMAN_SKIN_FLAGS) {
					byte flags = (byte) value;
					boolean cape = (flags & 0x01) != 0;
					flags = (byte) (cape ? 0x00 : 0x02);
					metadata.setValue(flags);
				}
				break;
			case Slot:
				metadata.setValue(protocol.getItemRewriter().handleItemToClient(event.user(), (Item) value));
				break;
			default:
				event.cancel();
				break;
		}
	}

	@Override
	public EntityTypes1_10.EntityType typeFromId(int type) {
		return EntityTypes1_10.getTypeFromId(type, false);
	}

	@Override
	public EntityTypes1_10.EntityType objectTypeFromId(int type) {
		return EntityTypes1_10.getTypeFromId(type, true);
	}
}
