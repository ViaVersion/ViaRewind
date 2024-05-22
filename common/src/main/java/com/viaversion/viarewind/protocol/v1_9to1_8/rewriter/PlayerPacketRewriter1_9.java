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

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.*;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerPacketRewriter1_9 {

	public static void register(final Protocol1_9To1_8 protocol) {
		protocol.registerClientbound(ClientboundPackets1_9.BOSS_EVENT, null, wrapper -> {
			wrapper.cancel();
			final BossBarStorage bossbar = wrapper.user().get(BossBarStorage.class);

			final UUID uuid = wrapper.read(Types.UUID);
			final int action = wrapper.read(Types.VAR_INT);
			if (action == 0 /* add */) {
				final JsonElement title = wrapper.read(Types.COMPONENT);
				final float health = wrapper.read(Types.FLOAT);
				wrapper.read(Types.VAR_INT); // Color
				wrapper.read(Types.VAR_INT); // Division
				wrapper.read(Types.UNSIGNED_BYTE); // Flags

				bossbar.add(uuid, ChatUtil.jsonToLegacy(wrapper.user(), title), health);
			} else if (action == 1 /* remove */) {
				bossbar.remove(uuid);
			} else if (action == 2 /* update health */) {
				final float health = wrapper.read(Types.FLOAT);
				bossbar.updateHealth(uuid, health);
			} else if (action == 3 /* update title */) {
				final JsonElement title = wrapper.read(Types.COMPONENT);
				bossbar.updateTitle(uuid, ChatUtil.jsonToLegacy(wrapper.user(), title));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SET_PLAYER_TEAM, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING); // Team name
				map(Types.BYTE); // Mode
				handler(wrapper -> {
					final byte mode = wrapper.get(Types.BYTE, 0);
					if (mode == 0 /* create team */ || mode == 2 /* update team info */) {
						wrapper.passthrough(Types.STRING); // Display name
						wrapper.passthrough(Types.STRING); // Prefix
						wrapper.passthrough(Types.STRING); // Suffix
						wrapper.passthrough(Types.BYTE); // Friendly fire
						wrapper.passthrough(Types.STRING); // Name tag visibility
						wrapper.read(Types.STRING); // Collision rule
					}
				});
			}
		});

		protocol.cancelClientbound(ClientboundPackets1_9.COOLDOWN);

		protocol.registerClientbound(ClientboundPackets1_9.CUSTOM_PAYLOAD, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING); // Channel
				handlerSoftFail(wrapper -> {
					final String channel = wrapper.get(Types.STRING, 0);
					if (channel.equals("MC|TrList")) {
						wrapper.passthrough(Types.INT); // Window id

						int size;
						if (wrapper.isReadable(Types.BYTE, 0)) {
							size = wrapper.passthrough(Types.BYTE);
						} else {
							size = wrapper.passthrough(Types.UNSIGNED_BYTE);
						}

						final ItemRewriter<?> itemRewriter = protocol.getItemRewriter();

						for (int i = 0; i < size; i++) {
							wrapper.write(Types.ITEM1_8, itemRewriter.handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_8))); // Buy item 1
							wrapper.write(Types.ITEM1_8, itemRewriter.handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_8))); // Buy item 3

							boolean has3Items = wrapper.passthrough(Types.BOOLEAN);
							if (has3Items) {
								wrapper.write(Types.ITEM1_8, itemRewriter.handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_8))); // Buy item 2
							}

							wrapper.passthrough(Types.BOOLEAN); //Unavailable
							wrapper.passthrough(Types.INT); //Uses
							wrapper.passthrough(Types.INT); //Max Uses
						}
					} else if (channel.equals("MC|BOpen")) {
						wrapper.read(Types.VAR_INT);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.PLAYER_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.DOUBLE); // X
				map(Types.DOUBLE); // Feet y
				map(Types.DOUBLE); // Z
				map(Types.FLOAT); // Yaw
				map(Types.FLOAT); // Pitch
				map(Types.BYTE); // Relative arguments
				handler(wrapper -> {
					final PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);

					pos.setConfirmId(wrapper.read(Types.VAR_INT));

					byte flags = wrapper.get(Types.BYTE, 0);
					double x = wrapper.get(Types.DOUBLE, 0);
					double y = wrapper.get(Types.DOUBLE, 1);
					double z = wrapper.get(Types.DOUBLE, 2);
					float yaw = wrapper.get(Types.FLOAT, 0);
					float pitch = wrapper.get(Types.FLOAT, 1);

					wrapper.set(Types.BYTE, 0, (byte) 0);

					if (flags != 0) {
						if ((flags & 0x01) != 0) {
							x += pos.getPosX();
							wrapper.set(Types.DOUBLE, 0, x);
						}
						if ((flags & 0x02) != 0) {
							y += pos.getPosY();
							wrapper.set(Types.DOUBLE, 1, y);
						}
						if ((flags & 0x04) != 0) {
							z += pos.getPosZ();
							wrapper.set(Types.DOUBLE, 2, z);
						}
						if ((flags & 0x08) != 0) {
							yaw += pos.getYaw();
							wrapper.set(Types.FLOAT, 0, yaw);
						}
						if ((flags & 0x10) != 0) {
							pitch += pos.getPitch();
							wrapper.set(Types.FLOAT, 1, pitch);
						}
					}

					pos.setPos(x, y, z);
					pos.setYaw(yaw);
					pos.setPitch(pitch);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.RESPAWN, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Dimension
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).reset());
				handler(wrapper -> {
					final ClientWorld world = wrapper.user().get(ClientWorld.class);
					world.setEnvironment(wrapper.get(Types.INT, 0));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.CHAT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING); // Message
				handler(wrapper -> {
					if (!ViaRewind.getConfig().isEnableOffhand()) {
						return;
					}
					final String msg = wrapper.get(Types.STRING, 0);
					if (msg.toLowerCase().trim().startsWith(ViaRewind.getConfig().getOffhandCommand())) {
						wrapper.cancel();
						final PacketWrapper swapItems = PacketWrapper.create(ServerboundPackets1_9.PLAYER_ACTION, wrapper.user());
						swapItems.write(Types.VAR_INT, 6);
						swapItems.write(Types.BLOCK_POSITION1_8, new BlockPosition(0, 0, 0));
						swapItems.write(Types.BYTE, (byte) 255);

						swapItems.sendToServer(Protocol1_9To1_8.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.INTERACT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Target
				map(Types.VAR_INT); // Type
				handler(wrapper -> {
					final int type = wrapper.get(Types.VAR_INT, 1);
					if (type == 2 /* attack */) {
						wrapper.passthrough(Types.FLOAT); // Target x
						wrapper.passthrough(Types.FLOAT); // Target y
						wrapper.passthrough(Types.FLOAT); // Target z
					}
					if (type == 2 /* attack */ || type == 0 /* interact */) {
						wrapper.write(Types.VAR_INT, 0); // Hand
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.MOVE_PLAYER_STATUS_ONLY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BOOLEAN); // On ground
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
					if (tracker.isInsideVehicle(tracker.clientEntityId())) {
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.MOVE_PLAYER_POS, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.DOUBLE); // X
				map(Types.DOUBLE); // Feet y
				map(Types.DOUBLE); // Z
				map(Types.BOOLEAN); // On ground
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					final PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
					if (pos.getConfirmId() != -1) {
						return;
					}
					pos.setPos(wrapper.get(Types.DOUBLE, 0), wrapper.get(Types.DOUBLE, 1), wrapper.get(Types.DOUBLE, 2));
					pos.setOnGround(wrapper.get(Types.BOOLEAN, 0));
				});
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).updateLocation());
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.MOVE_PLAYER_ROT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.FLOAT);
				map(Types.FLOAT);
				map(Types.BOOLEAN);
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
					if (pos.getConfirmId() != -1) return;
					pos.setYaw(wrapper.get(Types.FLOAT, 0));
					pos.setPitch(wrapper.get(Types.FLOAT, 1));
					pos.setOnGround(wrapper.get(Types.BOOLEAN, 0));
				});
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).updateLocation());
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.MOVE_PLAYER_POS_ROT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.DOUBLE);
				map(Types.DOUBLE);
				map(Types.DOUBLE);
				map(Types.FLOAT);
				map(Types.FLOAT);
				map(Types.BOOLEAN);
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					double x = wrapper.get(Types.DOUBLE, 0);
					double y = wrapper.get(Types.DOUBLE, 1);
					double z = wrapper.get(Types.DOUBLE, 2);
					float yaw = wrapper.get(Types.FLOAT, 0);
					float pitch = wrapper.get(Types.FLOAT, 1);
					boolean onGround = wrapper.get(Types.BOOLEAN, 0);

					PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
					if (pos.getConfirmId() != -1) {
						if (pos.getPosX() == x && pos.getPosY() == y && pos.getPosZ() == z
								&& pos.getYaw() == yaw && pos.getPitch() == pitch) {
							PacketWrapper confirmTeleport = wrapper.create(0x00);
							confirmTeleport.write(Types.VAR_INT, pos.getConfirmId());
							confirmTeleport.sendToServer(Protocol1_9To1_8.class);

							pos.setConfirmId(-1);
						}
					} else {
						pos.setPos(x, y, z);
						pos.setYaw(yaw);
						pos.setPitch(pitch);
						pos.setOnGround(onGround);
						wrapper.user().get(BossBarStorage.class).updateLocation();
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT);
				map(Types.BLOCK_POSITION1_8);
				handler(wrapper -> {
					int state = wrapper.get(Types.VAR_INT, 0);
					if (state == 0) {
						wrapper.user().get(BlockPlaceDestroyTracker.class).setMining();
					} else if (state == 2) {
						BlockPlaceDestroyTracker tracker = wrapper.user().get(BlockPlaceDestroyTracker.class);
						tracker.setMining();
						tracker.setLastMining(System.currentTimeMillis() + 100);
						wrapper.user().get(CooldownStorage.class).setLastHit(0);
					} else if (state == 1) {
						BlockPlaceDestroyTracker tracker = wrapper.user().get(BlockPlaceDestroyTracker.class);
						tracker.setMining();
						tracker.setLastMining(0);
						wrapper.user().get(CooldownStorage.class).hit();
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.USE_ITEM_ON, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8);
				map(Types.BYTE, Types.VAR_INT);
				read(Types.ITEM1_8);
				create(Types.VAR_INT, 0); //Main Hand
				map(Types.BYTE, Types.UNSIGNED_BYTE);
				map(Types.BYTE, Types.UNSIGNED_BYTE);
				map(Types.BYTE, Types.UNSIGNED_BYTE);
				handler(wrapper -> {
					if (wrapper.get(Types.VAR_INT, 0) == -1) {
						wrapper.cancel();
						PacketWrapper useItem = PacketWrapper.create(0x1D, null, wrapper.user());
						useItem.write(Types.VAR_INT, 0);

						useItem.sendToServer(Protocol1_9To1_8.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.SET_CARRIED_ITEM, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> wrapper.user().get(CooldownStorage.class).hit());
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.SWING, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					wrapper.cancel();

					/* We have to add ArmAnimation to a queue to be sent on PacketPlayInFlying. In 1.9,
					 * PacketPlayInArmAnimation is sent after PacketPlayInUseEntity, not before like it used to be.
					 * However, all packets are sent before PacketPlayInFlying. We'd just do a normal delay, but
					 * it would cause the packet to be sent after PacketPlayInFlying, potentially false flagging
					 * anticheats that check for this behavior from clients. Since all packets are sent before
					 * PacketPlayInFlying, if we queue it to be sent right before PacketPlayInFlying is processed,
					 * we can be certain it will be sent after PacketPlayInUseEntity */
					wrapper.cancel();
					final PacketWrapper delayedPacket = PacketWrapper.create(0x1A, null, wrapper.user());
					delayedPacket.write(Types.VAR_INT, 0);  //Main Hand

					wrapper.user().get(PlayerPositionTracker.class).queueAnimation(delayedPacket);
				});
				handler(wrapper -> {
					wrapper.user().get(BlockPlaceDestroyTracker.class).updateMining();
					wrapper.user().get(CooldownStorage.class).hit();
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_COMMAND, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT);
				map(Types.VAR_INT);
				map(Types.VAR_INT);
				handler(wrapper -> {
					int action = wrapper.get(Types.VAR_INT, 1);
					if (action == 6) {
						wrapper.set(Types.VAR_INT, 1, 7);
					} else if (action == 0) {
						PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
						if (!pos.isOnGround()) {
							PacketWrapper elytra = PacketWrapper.create(0x14, null, wrapper.user());
							elytra.write(Types.VAR_INT, wrapper.get(Types.VAR_INT, 0));
							elytra.write(Types.VAR_INT, 8);
							elytra.write(Types.VAR_INT, 0);
							elytra.scheduleSendToServer(Protocol1_9To1_8.class);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_INPUT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.FLOAT);
				map(Types.FLOAT);
				map(Types.UNSIGNED_BYTE);
				handler(wrapper -> {
					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
					final int vehicle = tracker.getVehicle(tracker.clientEntityId());
					if (vehicle != -1 && tracker.entityType(vehicle) == EntityTypes1_9.EntityType.BOAT) {
						PacketWrapper steerBoat = PacketWrapper.create(0x11, null, wrapper.user());
						float left = wrapper.get(Types.FLOAT, 0);
						float forward = wrapper.get(Types.FLOAT, 1);
						steerBoat.write(Types.BOOLEAN, forward != 0.0f || left < 0.0f);
						steerBoat.write(Types.BOOLEAN, forward != 0.0f || left > 0.0f);
						steerBoat.scheduleSendToServer(Protocol1_9To1_8.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.SIGN_UPDATE, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8);
				handler(wrapper -> {
					for (int i = 0; i < 4; i++) {
						wrapper.write(Types.STRING, ChatUtil.jsonToLegacy(wrapper.user(), wrapper.read(Types.COMPONENT)));
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.COMMAND_SUGGESTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				create(Types.BOOLEAN, false);
				map(Types.OPTIONAL_POSITION1_8);
			}
		});
		protocol.registerServerbound(ServerboundPackets1_8.CLIENT_INFORMATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				map(Types.BYTE);
				map(Types.BYTE, Types.VAR_INT);
				map(Types.BOOLEAN);
				map(Types.UNSIGNED_BYTE);
				create(Types.VAR_INT, 1);
				handler(wrapper -> {
					short flags = wrapper.get(Types.UNSIGNED_BYTE, 0);

					PacketWrapper updateSkin = PacketWrapper.create(0x1C, null, wrapper.user());
					updateSkin.write(Types.VAR_INT, wrapper.user().getEntityTracker(Protocol1_9To1_8.class).clientEntityId());

					ArrayList<EntityData> metadata = new ArrayList<>();
					metadata.add(new EntityData(10, EntityDataTypes1_8.BYTE, (byte) flags));

					updateSkin.write(Types1_8.ENTITY_DATA_LIST, metadata);

					updateSkin.scheduleSend(Protocol1_9To1_8.class);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.CUSTOM_PAYLOAD, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				handlerSoftFail(wrapper -> {
					String channel = wrapper.get(Types.STRING, 0);
					if (channel.equals("MC|BEdit") || channel.equals("MC|BSign")) {
						Item book = wrapper.passthrough(Types.ITEM1_8);
						book.setIdentifier(386);
						CompoundTag tag = book.tag();
						if (tag.contains("pages")) {
							ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
							if (pages.size() > ViaRewind.getConfig().getMaxBookPages()) {
								wrapper.user().disconnect("Too many book pages");
								return;
							}
							for (int i = 0; i < pages.size(); i++) {
								StringTag page = pages.get(i);
								String value = page.getValue();
								if (value.length() > ViaRewind.getConfig().getMaxBookPageSize()) {
									wrapper.user().disconnect("Book page too large");
									return;
								}
								value = ChatUtil.jsonToLegacy(wrapper.user(), value);
								page.setValue(value);
							}
						}
					} else if (channel.equals("MC|AdvCdm")) {
						wrapper.set(Types.STRING, 0, "MC|AdvCmd");
					}
				});
			}
		});
	}
}
