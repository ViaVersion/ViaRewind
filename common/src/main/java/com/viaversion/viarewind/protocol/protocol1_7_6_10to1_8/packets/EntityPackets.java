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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.VirtualHologramEntity;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerSessionStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.List;
import java.util.UUID;

public class EntityPackets {

	public static void register(Protocol1_7_6_10To1_8 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EQUIPMENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.SHORT); // slot
				map(Type.ITEM1_8, Types1_7_6_10.COMPRESSED_NBT_ITEM); // item

				// remap item
				handler(wrapper -> {
					final Item item = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					protocol.getItemRewriter().handleItemToClient(item);
					wrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
				});

				handler(wrapper -> {
					final short slot = wrapper.get(Type.SHORT, 0);
					final UUID uuid = wrapper.user().get(EntityTracker1_7_6_10.class).getPlayerUUID(wrapper.get(Type.INT, 0));
					if (uuid == null) return;

					final Item item = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					wrapper.user().get(PlayerSessionStorage.class).setPlayerEquipment(uuid, item, slot);

					final GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
					GameProfileStorage.GameProfile profile = storage.get(uuid);
					if (profile != null && profile.gamemode == 3) { // spectator mode didn't exist in 1.7.10
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.USE_BED, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.POSITION1_8, Types1_7_6_10.U_BYTE_POSITION); // position
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.COLLECT_ITEM, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // collected entity id
				map(Type.VAR_INT, Type.INT); // collector entity id

				handler(wrapper -> wrapper.user().get(EntityTracker1_7_6_10.class).removeEntity(wrapper.get(Type.INT, 0)));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_VELOCITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id

				map(Type.SHORT); // velocity x
				map(Type.SHORT); // velocity y
				map(Type.SHORT); // velocity z
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.DESTROY_ENTITIES, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT_ARRAY_PRIMITIVE, Types1_7_6_10.BYTE_INT_ARRAY); // entity ids

				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					for (int entityId : wrapper.get(Types1_7_6_10.BYTE_INT_ARRAY, 0)) {
						tracker.removeEntity(entityId);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_MOVEMENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.BYTE); // x
				map(Type.BYTE); // y
				map(Type.BYTE); // z
				read(Type.BOOLEAN); // on ground

				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getVirtualHologramMap().get(wrapper.get(Type.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int x = wrapper.get(Type.BYTE, 0);
						final int y = wrapper.get(Type.BYTE, 1);
						final int z = wrapper.get(Type.BYTE, 2);

						hologram.handleOriginalMovementPacket(x / 32.0, y / 32.0, z / 32.0);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_ROTATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.BYTE); // yaw
				map(Type.BYTE); // pitch
				read(Type.BOOLEAN); // on ground

				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getVirtualHologramMap().get(wrapper.get(Type.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int yaw = wrapper.get(Type.BYTE, 0);
						final int pitch = wrapper.get(Type.BYTE, 1);

						hologram.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION_AND_ROTATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.BYTE); // x
				map(Type.BYTE); // y
				map(Type.BYTE); // z
				map(Type.BYTE); // yaw
				map(Type.BYTE); // pitch
				read(Type.BOOLEAN); // on ground

				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getVirtualHologramMap().get(wrapper.get(Type.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int x = wrapper.get(Type.BYTE, 0);
						final int y = wrapper.get(Type.BYTE, 1);
						final int z = wrapper.get(Type.BYTE, 2);

						final int yaw = wrapper.get(Type.BYTE, 3);
						final int pitch = wrapper.get(Type.BYTE, 4);

						hologram.handleOriginalMovementPacket(x / 32.0, y / 32.0, z / 32.0);
						hologram.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_TELEPORT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				map(Type.BYTE); // yaw
				map(Type.BYTE); // pitch
				read(Type.BOOLEAN); // on ground
				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					final int entityId = wrapper.get(Type.INT, 0);
					final EntityTypes1_10.EntityType type = tracker.getEntityMap().get(entityId);

					if (type == EntityTypes1_10.EntityType.MINECART_ABSTRACT) { // TODO | Realign all entities?
						int y = wrapper.get(Type.INT, 2);
						y += 12;
						wrapper.set(Type.INT, 2, y);
					}

					final VirtualHologramEntity hologram = tracker.getVirtualHologramMap().get(entityId);
					if (hologram != null) {
						wrapper.cancel();
						final int x = wrapper.get(Type.INT, 1);
						final int y = wrapper.get(Type.INT, 2);
						final int z = wrapper.get(Type.INT, 3);

						final int yaw = wrapper.get(Type.BYTE, 0);
						final int pitch = wrapper.get(Type.BYTE, 1);

						hologram.updateReplacementPosition(x / 32.0, y / 32.0, z / 32.0);
						hologram.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_HEAD_LOOK, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.BYTE); // head yaw

				handler(wrapper -> {
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);

					final VirtualHologramEntity hologram = tracker.getVirtualHologramMap().get(wrapper.get(Type.INT, 0));
					if (hologram != null) {
						wrapper.cancel();
						final int yaw = wrapper.get(Type.BYTE, 0);

						hologram.setHeadYaw(yaw * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ATTACH_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // riding entity id
				map(Type.INT); // vehicle entity id
				map(Type.BOOLEAN); // leash state
				handler(packetWrapper -> {
					final boolean leash = packetWrapper.get(Type.BOOLEAN, 0);
					if (!leash) {
						final EntityTracker1_7_6_10 tracker = packetWrapper.user().get(EntityTracker1_7_6_10.class);

						final int passenger = packetWrapper.get(Type.INT, 0);
						final int vehicle = packetWrapper.get(Type.INT, 1);

						tracker.setPassenger(vehicle, passenger);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_METADATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST); // metadata
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.INT, 0);
					final List<Metadata> metadataList = wrapper.get(Types1_7_6_10.METADATA_LIST, 0);

					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					if (tracker.getEntityReplacementMap().containsKey(entityId)) {
						tracker.updateMetadata(entityId, metadataList);
						wrapper.cancel();
						return;
					}
					if (tracker.getEntityMap().containsKey(entityId)) {
						protocol.getMetadataRewriter().transform(tracker.getEntityMap().get(entityId), metadataList);
						if (metadataList.isEmpty()) {
							wrapper.cancel();
						}
					} else {
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EFFECT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.BYTE); // effect id
				map(Type.BYTE); // amplifier
				map(Type.VAR_INT, Type.SHORT); // duration
				read(Type.BYTE); // hide particles
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.REMOVE_ENTITY_EFFECT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				map(Type.BYTE); // effect id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_PROPERTIES, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT, Type.INT); // entity id
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.INT, 0);
					if (wrapper.user().get(EntityTracker1_7_6_10.class).getEntityReplacementMap().containsKey(entityId)) {
						wrapper.cancel();
						return;
					}

					final int amount = wrapper.passthrough(Type.INT);
					for (int i = 0; i < amount; i++) {
						wrapper.passthrough(Type.STRING); // id
						wrapper.passthrough(Type.DOUBLE); // value

						int modifierLength = wrapper.read(Type.VAR_INT);
						wrapper.write(Type.SHORT, (short) modifierLength);
						for (int j = 0; j < modifierLength; j++) {
							wrapper.passthrough(Type.UUID); // modifier uuid
							wrapper.passthrough(Type.DOUBLE); // modifier amount
							wrapper.passthrough(Type.BYTE); // modifier operation
						}
					}
				});

			}
		});

		protocol.cancelClientbound(ClientboundPackets1_8.UPDATE_ENTITY_NBT);
	}
}
