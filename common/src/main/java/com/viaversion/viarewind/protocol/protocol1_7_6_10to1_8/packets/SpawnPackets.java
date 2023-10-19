/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.api.rewriter.item.Replacement;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.List;
import java.util.UUID;

public class SpawnPackets {

	public static void register(Protocol1_7_6_10To1_8 protocol) {
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

					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					if (gameProfile != null && gameProfile.gamemode == 3) { // Spectator mode
						for (short i = 0; i < 5; i++) {
							final PacketWrapper entityEquipment = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_EQUIPMENT, wrapper.user());
							entityEquipment.write(Type.INT, entityId);
							entityEquipment.write(Type.SHORT, i);
							entityEquipment.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, i == 4 ? gameProfile.getSkull() : null);

							entityEquipment.scheduleSend(Protocol1_7_6_10To1_8.class, true);
						}
					}

					tracker.addPlayer(entityId, uuid);
				});
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				map(Type.BYTE); // yaw
				map(Type.BYTE); // pitch
				map(Type.SHORT); // item in hand id
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST); // metadata
				handler(wrapper -> {
					final List<Metadata> metadata = wrapper.get(Types1_7_6_10.METADATA_LIST, 0);
					protocol.getMetadataRewriter().transform(Entity1_10Types.EntityType.PLAYER, metadata);

					wrapper.set(Types1_7_6_10.METADATA_LIST, 0, metadata);
				});
				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					tracker.addEntity(wrapper.get(Type.VAR_INT, 0), Entity1_10Types.EntityType.PLAYER);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				map(Type.BYTE); // type id
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				map(Type.BYTE); // pitch
				map(Type.BYTE); // yaw
				map(Type.INT); // data

				handler(wrapper -> {
					final Entity1_10Types.EntityType type = Entity1_10Types.getTypeFromId(wrapper.get(Type.BYTE, 0), true);
					final int entityId = wrapper.get(Type.VAR_INT, 0);

					int x = wrapper.get(Type.INT, 0);
					int y = wrapper.get(Type.INT, 1);
					int z = wrapper.get(Type.INT, 2);

					byte pitch = wrapper.get(Type.BYTE, 1);
					byte yaw = wrapper.get(Type.BYTE, 2);

					int data = wrapper.get(Type.INT, 3);

					if (type == Entity1_10Types.ObjectType.ITEM_FRAME.getType()) {
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
					} else if (type == Entity1_10Types.ObjectType.ARMOR_STAND.getType()) {
						wrapper.cancel();
						// TODO | Tick Virtual Holograms
//						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
//						ArmorStandModel armorStand = new ArmorStandModel(wrapper.user(), protocol, entityId);
//						armorStand.updateReplacementPosition(x / 32.0, y / 32.0, z / 32.0);
//						armorStand.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
//						armorStand.setHeadYaw(yaw * 360f / 256);
//						tracker.addEntityReplacement(armorStand);
					}
					// TODO | Realign all entities

					wrapper.set(Type.INT, 0, x);
					wrapper.set(Type.INT, 1, y);
					wrapper.set(Type.INT, 2, z);
					wrapper.set(Type.BYTE, 2, yaw);

					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					tracker.addEntity(entityId, type);

					if (type != null && type.isOrHasParent(Entity1_10Types.EntityType.FALLING_BLOCK)) {
						int blockId = data & 0xFFF;
						int blockData = data >> 12 & 0xF;
						final Replacement replace = protocol.getItemRewriter().replace(blockId, blockData);
						if (replace != null) {
							blockId = replace.getId();
							blockData = replace.replaceData(blockData);
						}
						wrapper.set(Type.INT, 3, data = (blockId | blockData << 16));
					}

					if (data > 0) {
						wrapper.passthrough(Type.SHORT); // velocity x
						wrapper.passthrough(Type.SHORT); // velocity y
						wrapper.passthrough(Type.SHORT); // velocity z
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_MOB, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				map(Type.UNSIGNED_BYTE); // type id
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				map(Type.BYTE); // yaw
				map(Type.BYTE); // pitch
				map(Type.BYTE); // head yaw
				map(Type.SHORT); // velocity x
				map(Type.SHORT); // velocity y
				map(Type.SHORT); // velocity z
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST); // metadata
				handler(wrapper -> {
					final Entity1_10Types.EntityType type = Entity1_10Types.getTypeFromId(wrapper.get(Type.UNSIGNED_BYTE, 0), false);
					final int entityId = wrapper.get(Type.VAR_INT, 0);

					final int x = wrapper.get(Type.INT, 0);
					final int y = wrapper.get(Type.INT, 1);
					final int z = wrapper.get(Type.INT, 2);

					final byte pitch = wrapper.get(Type.BYTE, 1);
					final byte yaw = wrapper.get(Type.BYTE, 0);
					final byte headYaw = wrapper.get(Type.BYTE, 2);

					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					final List<Metadata> metadataList = wrapper.get(Types1_7_6_10.METADATA_LIST, 0);

					tracker.addEntity(entityId, type);
					protocol.getMetadataRewriter().transform(type, metadataList);

					if (tracker.isReplaced(type)) {
						final int newTypeId = tracker.replaceEntity(entityId, type);
						wrapper.set(Type.UNSIGNED_BYTE, 0, (short) newTypeId);

						tracker.updateMetadata(entityId, metadataList);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_PAINTING, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				map(Type.STRING); // title
				handler(wrapper -> {
					final Position position = wrapper.read(Type.POSITION);

					wrapper.write(Type.INT, position.x());
					wrapper.write(Type.INT, position.y());
					wrapper.write(Type.INT, position.z());
				});
				map(Type.UNSIGNED_BYTE, Type.INT); // rotation
				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					tracker.addEntity(wrapper.get(Type.VAR_INT, 0), Entity1_10Types.EntityType.PAINTING);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_EXPERIENCE_ORB, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				map(Type.SHORT); // count
				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					tracker.addEntity(wrapper.get(Type.VAR_INT, 0), Entity1_10Types.EntityType.EXPERIENCE_ORB);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_GLOBAL_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				map(Type.BYTE); // type id
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					tracker.addEntity(wrapper.get(Type.VAR_INT, 0), Entity1_10Types.EntityType.LIGHTNING);
				});
			}
		});
	}
}
