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
package com.viaversion.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.*;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerPackets1_9 {

	public static void register(final Protocol1_8To1_9 protocol) {
		protocol.registerClientbound(ClientboundPackets1_9.BOSSBAR, null, wrapper -> {
			wrapper.cancel();
			final BossBarStorage bossbar = wrapper.user().get(BossBarStorage.class);

			final UUID uuid = wrapper.read(Type.UUID);
			final int action = wrapper.read(Type.VAR_INT);
			if (action == 0 /* add */) {
				final JsonElement title = wrapper.read(Type.COMPONENT);
				final float health = wrapper.read(Type.FLOAT);
				wrapper.read(Type.VAR_INT); // Color
				wrapper.read(Type.VAR_INT); // Division
				wrapper.read(Type.UNSIGNED_BYTE); // Flags

				bossbar.add(uuid, ChatUtil.jsonToLegacy(wrapper.user(), title), health);
			} else if (action == 1 /* remove */) {
				bossbar.remove(uuid);
			} else if (action == 2 /* update health */) {
				final float health = wrapper.read(Type.FLOAT);
				bossbar.updateHealth(uuid, health);
			} else if (action == 3 /* update title */) {
				final JsonElement title = wrapper.read(Type.COMPONENT);
				bossbar.updateTitle(uuid, ChatUtil.jsonToLegacy(wrapper.user(), title));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.TEAMS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING); // Team name
				map(Type.BYTE); // Mode
				handler(wrapper -> {
					final byte mode = wrapper.get(Type.BYTE, 0);
					if (mode == 0 /* create team */ || mode == 2 /* update team info */) {
						wrapper.passthrough(Type.STRING); // Display name
						wrapper.passthrough(Type.STRING); // Prefix
						wrapper.passthrough(Type.STRING); // Suffix
						wrapper.passthrough(Type.BYTE); // Friendly fire
						wrapper.passthrough(Type.STRING); // Name tag visibility
						wrapper.read(Type.STRING); // Collision rule
					}
				});
			}
		});

		protocol.cancelClientbound(ClientboundPackets1_9.COOLDOWN);

		protocol.registerClientbound(ClientboundPackets1_9.PLUGIN_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING); // Channel
				handlerSoftFail(wrapper -> {
					final String channel = wrapper.get(Type.STRING, 0);
					if (channel.equals("MC|TrList")) {
						wrapper.passthrough(Type.INT); // Window id

						int size;
						if (wrapper.isReadable(Type.BYTE, 0)) {
							size = wrapper.passthrough(Type.BYTE);
						} else {
							size = wrapper.passthrough(Type.UNSIGNED_BYTE);
						}

						final ItemRewriter<?> itemRewriter = protocol.getItemRewriter();

						for (int i = 0; i < size; i++) {
							wrapper.write(Type.ITEM1_8, itemRewriter.handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_8))); // Buy item 1
							wrapper.write(Type.ITEM1_8, itemRewriter.handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_8))); // Buy item 3

							boolean has3Items = wrapper.passthrough(Type.BOOLEAN);
							if (has3Items) {
								wrapper.write(Type.ITEM1_8, itemRewriter.handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_8))); // Buy item 2
							}

							wrapper.passthrough(Type.BOOLEAN); //Unavailable
							wrapper.passthrough(Type.INT); //Uses
							wrapper.passthrough(Type.INT); //Max Uses
						}
					} else if (channel.equals("MC|BOpen")) {
						wrapper.read(Type.VAR_INT);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.PLAYER_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.DOUBLE); // X
				map(Type.DOUBLE); // Feet y
				map(Type.DOUBLE); // Z
				map(Type.FLOAT); // Yaw
				map(Type.FLOAT); // Pitch
				map(Type.BYTE); // Relative arguments
				handler(wrapper -> {
					final PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);

					pos.setConfirmId(wrapper.read(Type.VAR_INT));

					byte flags = wrapper.get(Type.BYTE, 0);
					double x = wrapper.get(Type.DOUBLE, 0);
					double y = wrapper.get(Type.DOUBLE, 1);
					double z = wrapper.get(Type.DOUBLE, 2);
					float yaw = wrapper.get(Type.FLOAT, 0);
					float pitch = wrapper.get(Type.FLOAT, 1);

					wrapper.set(Type.BYTE, 0, (byte) 0);

					if (flags != 0) {
						if ((flags & 0x01) != 0) {
							x += pos.getPosX();
							wrapper.set(Type.DOUBLE, 0, x);
						}
						if ((flags & 0x02) != 0) {
							y += pos.getPosY();
							wrapper.set(Type.DOUBLE, 1, y);
						}
						if ((flags & 0x04) != 0) {
							z += pos.getPosZ();
							wrapper.set(Type.DOUBLE, 2, z);
						}
						if ((flags & 0x08) != 0) {
							yaw += pos.getYaw();
							wrapper.set(Type.FLOAT, 0, yaw);
						}
						if ((flags & 0x10) != 0) {
							pitch += pos.getPitch();
							wrapper.set(Type.FLOAT, 1, pitch);
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
				map(Type.INT); // Dimension
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).reset());
				handler(wrapper -> {
					final ClientWorld world = wrapper.user().get(ClientWorld.class);
					world.setEnvironment(wrapper.get(Type.INT, 0));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.CHAT_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING); // Message
				handler(wrapper -> {
					if (!ViaRewind.getConfig().isEnableOffhand()) {
						return;
					}
					final String msg = wrapper.get(Type.STRING, 0);
					if (msg.toLowerCase().trim().startsWith(ViaRewind.getConfig().getOffhandCommand())) {
						wrapper.cancel();
						final PacketWrapper swapItems = PacketWrapper.create(ServerboundPackets1_9.PLAYER_DIGGING, wrapper.user());
						swapItems.write(Type.VAR_INT, 6);
						swapItems.write(Type.POSITION1_8, new Position(0, 0, 0));
						swapItems.write(Type.BYTE, (byte) 255);

						swapItems.sendToServer(Protocol1_8To1_9.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.INTERACT_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Target
				map(Type.VAR_INT); // Type
				handler(wrapper -> {
					final int type = wrapper.get(Type.VAR_INT, 1);
					if (type == 2 /* attack */) {
						wrapper.passthrough(Type.FLOAT); // Target x
						wrapper.passthrough(Type.FLOAT); // Target y
						wrapper.passthrough(Type.FLOAT); // Target z
					}
					if (type == 2 /* attack */ || type == 0 /* interact */) {
						wrapper.write(Type.VAR_INT, 0); // Hand
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_MOVEMENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.BOOLEAN); // On ground
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
					if (tracker.isInsideVehicle(tracker.clientEntityId())) {
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.DOUBLE); // X
				map(Type.DOUBLE); // Feet y
				map(Type.DOUBLE); // Z
				map(Type.BOOLEAN); // On ground
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					final PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
					if (pos.getConfirmId() != -1) {
						return;
					}
					pos.setPos(wrapper.get(Type.DOUBLE, 0), wrapper.get(Type.DOUBLE, 1), wrapper.get(Type.DOUBLE, 2));
					pos.setOnGround(wrapper.get(Type.BOOLEAN, 0));
				});
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).updateLocation());
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_ROTATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
					if (pos.getConfirmId() != -1) return;
					pos.setYaw(wrapper.get(Type.FLOAT, 0));
					pos.setPitch(wrapper.get(Type.FLOAT, 1));
					pos.setOnGround(wrapper.get(Type.BOOLEAN, 0));
				});
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).updateLocation());
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_POSITION_AND_ROTATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);
				handler(wrapper -> {
					wrapper.user().get(PlayerPositionTracker.class).sendAnimations();

					double x = wrapper.get(Type.DOUBLE, 0);
					double y = wrapper.get(Type.DOUBLE, 1);
					double z = wrapper.get(Type.DOUBLE, 2);
					float yaw = wrapper.get(Type.FLOAT, 0);
					float pitch = wrapper.get(Type.FLOAT, 1);
					boolean onGround = wrapper.get(Type.BOOLEAN, 0);

					PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
					if (pos.getConfirmId() != -1) {
						if (pos.getPosX() == x && pos.getPosY() == y && pos.getPosZ() == z
								&& pos.getYaw() == yaw && pos.getPitch() == pitch) {
							PacketWrapper confirmTeleport = wrapper.create(0x00);
							confirmTeleport.write(Type.VAR_INT, pos.getConfirmId());
							confirmTeleport.sendToServer(Protocol1_8To1_9.class);

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

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_DIGGING, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT);
				map(Type.POSITION1_8);
				handler(wrapper -> {
					int state = wrapper.get(Type.VAR_INT, 0);
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

		protocol.registerServerbound(ServerboundPackets1_8.PLAYER_BLOCK_PLACEMENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8);
				map(Type.BYTE, Type.VAR_INT);
				read(Type.ITEM1_8);
				create(Type.VAR_INT, 0); //Main Hand
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				handler(wrapper -> {
					if (wrapper.get(Type.VAR_INT, 0) == -1) {
						wrapper.cancel();
						PacketWrapper useItem = PacketWrapper.create(0x1D, null, wrapper.user());
						useItem.write(Type.VAR_INT, 0);

						useItem.sendToServer(Protocol1_8To1_9.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.HELD_ITEM_CHANGE, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> wrapper.user().get(CooldownStorage.class).hit());
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.ANIMATION, new PacketHandlers() {
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
					delayedPacket.write(Type.VAR_INT, 0);  //Main Hand

					wrapper.user().get(PlayerPositionTracker.class).queueAnimation(delayedPacket);
				});
				handler(wrapper -> {
					wrapper.user().get(BlockPlaceDestroyTracker.class).updateMining();
					wrapper.user().get(CooldownStorage.class).hit();
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.ENTITY_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT);
				map(Type.VAR_INT);
				map(Type.VAR_INT);
				handler(wrapper -> {
					int action = wrapper.get(Type.VAR_INT, 1);
					if (action == 6) {
						wrapper.set(Type.VAR_INT, 1, 7);
					} else if (action == 0) {
						PlayerPositionTracker pos = wrapper.user().get(PlayerPositionTracker.class);
						if (!pos.isOnGround()) {
							PacketWrapper elytra = PacketWrapper.create(0x14, null, wrapper.user());
							elytra.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0));
							elytra.write(Type.VAR_INT, 8);
							elytra.write(Type.VAR_INT, 0);
							elytra.scheduleSendToServer(Protocol1_8To1_9.class);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.STEER_VEHICLE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
				handler(wrapper -> {
					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
					final int vehicle = tracker.getVehicle(tracker.clientEntityId());
					if (vehicle != -1 && tracker.entityType(vehicle) == EntityTypes1_10.EntityType.BOAT) {
						PacketWrapper steerBoat = PacketWrapper.create(0x11, null, wrapper.user());
						float left = wrapper.get(Type.FLOAT, 0);
						float forward = wrapper.get(Type.FLOAT, 1);
						steerBoat.write(Type.BOOLEAN, forward != 0.0f || left < 0.0f);
						steerBoat.write(Type.BOOLEAN, forward != 0.0f || left > 0.0f);
						steerBoat.scheduleSendToServer(Protocol1_8To1_9.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.UPDATE_SIGN, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8);
				handler(wrapper -> {
					for (int i = 0; i < 4; i++) {
						wrapper.write(Type.STRING, ChatUtil.jsonToLegacy(wrapper.user(), wrapper.read(Type.COMPONENT)));
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.TAB_COMPLETE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				create(Type.BOOLEAN, false);
				map(Type.OPTIONAL_POSITION1_8);
			}
		});
		protocol.registerServerbound(ServerboundPackets1_8.CLIENT_SETTINGS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				map(Type.BYTE);
				map(Type.BYTE, Type.VAR_INT);
				map(Type.BOOLEAN);
				map(Type.UNSIGNED_BYTE);
				create(Type.VAR_INT, 1);
				handler(wrapper -> {
					short flags = wrapper.get(Type.UNSIGNED_BYTE, 0);

					PacketWrapper updateSkin = PacketWrapper.create(0x1C, null, wrapper.user());
					updateSkin.write(Type.VAR_INT, wrapper.user().getEntityTracker(Protocol1_8To1_9.class).clientEntityId());

					ArrayList<Metadata> metadata = new ArrayList<>();
					metadata.add(new Metadata(10, MetaType1_8.Byte, (byte) flags));

					updateSkin.write(Types1_8.METADATA_LIST, metadata);

					updateSkin.scheduleSend(Protocol1_8To1_9.class);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.PLUGIN_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				handlerSoftFail(wrapper -> {
					String channel = wrapper.get(Type.STRING, 0);
					if (channel.equals("MC|BEdit") || channel.equals("MC|BSign")) {
						Item book = wrapper.passthrough(Type.ITEM);
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
						wrapper.set(Type.STRING, 0, "MC|AdvCmd");
					}
				});
			}
		});
	}
}
