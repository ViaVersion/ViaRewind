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

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.TitleRenderProvider;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.*;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viarewind.utils.math.AABB;
import com.viaversion.viarewind.utils.math.Ray3d;
import com.viaversion.viarewind.utils.math.RayTracing;
import com.viaversion.viarewind.utils.math.Vector3d;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.util.ComponentUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class PlayerPackets {

	public static void register(Protocol1_7_6_10To1_8 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.JOIN_GAME, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // entity id
				map(Type.UNSIGNED_BYTE);// game mode
				map(Type.BYTE); // dimension
				map(Type.UNSIGNED_BYTE); // difficulty
				map(Type.UNSIGNED_BYTE); // max players
				map(Type.STRING); // level type
				read(Type.BOOLEAN); // reduced debug info

				handler(wrapper -> {
					if (ViaRewind.getConfig().isReplaceAdventureMode()) {
						if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 2) { // adventure
							wrapper.set(Type.UNSIGNED_BYTE, 0, (short) 0); // survival
						}
					}

					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					tracker.setClientEntityId(wrapper.get(Type.INT, 0));
					tracker.addPlayer(wrapper.get(Type.INT, 0), wrapper.user().getProtocolInfo().getUuid());

					wrapper.user().get(PlayerSessionStorage.class).gameMode = wrapper.get(Type.UNSIGNED_BYTE, 0);
					wrapper.user().get(ClientWorld.class).setEnvironment(wrapper.get(Type.BYTE, 0));

					// Reset on Velocity server change
					wrapper.user().put(new Scoreboard(wrapper.user()));
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.CHAT_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.COMPONENT); // chat message
				handler(wrapper -> {
					final int position = wrapper.read(Type.BYTE);
					if (position == 2) { // above hotbar
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8, Types1_7_6_10.INT_POSITION); // spawn position
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.UPDATE_HEALTH, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.FLOAT); // health
				map(Type.VAR_INT, Type.SHORT); // food
				map(Type.FLOAT); // food saturation
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.RESPAWN, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // dimension
				map(Type.UNSIGNED_BYTE); // difficulty
				map(Type.UNSIGNED_BYTE); // game mode
				map(Type.STRING); // level type
				handler(wrapper -> {
					if (ViaRewind.getConfig().isReplaceAdventureMode()) {
						if (wrapper.get(Type.UNSIGNED_BYTE, 1) == 2) {
							wrapper.set(Type.UNSIGNED_BYTE, 1, (short) 0);
						}
					}

					wrapper.user().get(PlayerSessionStorage.class).gameMode = wrapper.get(Type.UNSIGNED_BYTE, 1);

					final ClientWorld world = wrapper.user().get(ClientWorld.class);
					final Environment dimension = Environment.getEnvironmentById(wrapper.get(Type.INT, 0));

					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					if (world.getEnvironment() != dimension) {
						world.setEnvironment(dimension.id());

						// Reset on dimension change
						tracker.clear();
						tracker.getEntityMap().put(tracker.getPlayerId(), EntityTypes1_10.EntityType.ENTITY_HUMAN);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.DOUBLE); // x
				map(Type.DOUBLE); // y
				map(Type.DOUBLE); // z
				map(Type.FLOAT); // yaw
				map(Type.FLOAT); // pitch
				handler(wrapper -> {
					final double x = wrapper.get(Type.DOUBLE, 0);
					double y = wrapper.get(Type.DOUBLE, 1);
					final double z = wrapper.get(Type.DOUBLE, 2);

					final float yaw = wrapper.get(Type.FLOAT, 0);
					final float pitch = wrapper.get(Type.FLOAT, 1);

					final PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					final int flags = wrapper.read(Type.BYTE);

					// x, y, and z
					if ((flags & 0x01) == 0x01) wrapper.set(Type.DOUBLE, 0, x + playerSession.getPosX());
					if ((flags & 0x02) == 0x02) y += playerSession.getPosY();

					playerSession.receivedPosY = y;
					wrapper.set(Type.DOUBLE, 1, y + 1.62F);

					if ((flags & 0x04) == 0x04) wrapper.set(Type.DOUBLE, 2, z + playerSession.getPosZ());

					// yaw and pitch
					if ((flags & 0x08) == 0x08) wrapper.set(Type.FLOAT, 0, yaw + playerSession.yaw);
					if ((flags & 0x10) == 0x10) wrapper.set(Type.FLOAT, 1, pitch + playerSession.pitch);

					wrapper.write(Type.BOOLEAN, playerSession.onGround);

					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					if (tracker.spectatingPlayerId != tracker.getPlayerId()) {
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_EXPERIENCE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.FLOAT); // experience bar
				map(Type.VAR_INT, Type.SHORT); // level
				map(Type.VAR_INT, Type.SHORT); // total experience
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.GAME_EVENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE); // reason
				map(Type.FLOAT); // value
				handler(wrapper -> {
					if (wrapper.get(Type.UNSIGNED_BYTE, 0) != 3) return; // Change game mode
					int gameMode = wrapper.get(Type.FLOAT, 0).intValue();

					final PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					if (gameMode == 3 || playerSession.gameMode == 3) {
						UUID myId = wrapper.user().getProtocolInfo().getUuid();
						Item[] equipment = new Item[4];
						if (gameMode == 3) {
							GameProfileStorage.GameProfile profile = wrapper.user().get(GameProfileStorage.class).get(myId);
							equipment[3] = profile == null ? null : profile.getSkull();
						} else {
							for (int i = 0; i < equipment.length; i++) {
								equipment[i] = playerSession.getPlayerEquipment(myId, i);
							}
						}

						for (int i = 0; i < equipment.length; i++) {
							PacketWrapper setSlot = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_SLOT, wrapper.user());
							setSlot.write(Type.BYTE, (byte) 0);
							setSlot.write(Type.SHORT, (short) (8 - i));
							setSlot.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[i]);
							PacketUtil.sendPacket(setSlot, Protocol1_7_6_10To1_8.class);
						}
					}

					if (gameMode == 2 && ViaRewind.getConfig().isReplaceAdventureMode()) {
						gameMode = 0;
						wrapper.set(Type.FLOAT, 0, 0.0f);
					}
					wrapper.user().get(PlayerSessionStorage.class).gameMode = gameMode;
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.OPEN_SIGN_EDITOR, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8, Types1_7_6_10.INT_POSITION); // position
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_INFO, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					packetWrapper.cancel();
					int action = packetWrapper.read(Type.VAR_INT);
					int count = packetWrapper.read(Type.VAR_INT);
					GameProfileStorage gameProfileStorage = packetWrapper.user().get(GameProfileStorage.class);
					for (int i = 0; i < count; i++) {
						UUID uuid = packetWrapper.read(Type.UUID);
						if (action == 0) {
							String name = packetWrapper.read(Type.STRING);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) gameProfile = gameProfileStorage.put(uuid, name);

							int propertyCount = packetWrapper.read(Type.VAR_INT);
							while (propertyCount-- > 0) {
								String propertyName = packetWrapper.read(Type.STRING);
								String propertyValue = packetWrapper.read(Type.STRING);
								String propertySignature = packetWrapper.read(Type.OPTIONAL_STRING);
								gameProfile.properties.add(new GameProfileStorage.Property(propertyName, propertyValue, propertySignature));
							}

							int gamemode = packetWrapper.read(Type.VAR_INT);
							int ping = packetWrapper.read(Type.VAR_INT);
							gameProfile.ping = ping;
							gameProfile.gamemode = gamemode;
							JsonElement displayName = packetWrapper.read(Type.OPTIONAL_COMPONENT);
							if (displayName != null) {
								gameProfile.setDisplayName(ChatUtil.jsonToLegacy(displayName));
							}

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10To1_8.class);
						} else if (action == 1) {
							int gamemode = packetWrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.gamemode == gamemode) continue;

							if (gamemode == 3 || gameProfile.gamemode == 3) {
								EntityTracker1_7_6_10 tracker = packetWrapper.user().get(EntityTracker1_7_6_10.class);
								int entityId = tracker.getPlayerEntityId(uuid);
								boolean isOwnPlayer = entityId == tracker.getPlayerId();
								if (entityId != -1) {
									// Weirdly, PlayerEntity has only 4 slots instead of 5
									Item[] equipment = new Item[isOwnPlayer ? 4 : 5];
									if (gamemode == 3) {
										equipment[isOwnPlayer ? 3 : 4] = gameProfile.getSkull();
									} else {
										for (int j = 0; j < equipment.length; j++) {
											equipment[j] = packetWrapper.user().get(PlayerSessionStorage.class).getPlayerEquipment(uuid, j);
										}
									}

									for (short slot = 0; slot < equipment.length; slot++) {
										PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_EQUIPMENT, packetWrapper.user());
										equipmentPacket.write(Type.INT, entityId);
										equipmentPacket.write(Type.SHORT, slot);
										equipmentPacket.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[slot]);
										PacketUtil.sendPacket(equipmentPacket, Protocol1_7_6_10To1_8.class);
									}
								}
							}

							gameProfile.gamemode = gamemode;
						} else if (action == 2) {
							int ping = packetWrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10To1_8.class);

							gameProfile.ping = ping;

							packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10To1_8.class);
						} else if (action == 3) {
							JsonElement displayNameComponent = packetWrapper.read(Type.OPTIONAL_COMPONENT);
							String displayName = displayNameComponent != null ? ChatUtil.jsonToLegacy(displayNameComponent) : null;

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.displayName == null && displayName == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10To1_8.class);

							if (gameProfile.displayName == null && displayName != null || gameProfile.displayName != null && displayName == null || !gameProfile.displayName.equals(displayName)) {
								gameProfile.setDisplayName(displayName);
							}

							packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10To1_8.class);
						} else if (action == 4) {
							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.remove(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10To1_8.class);
						}
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_ABILITIES, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(packetWrapper -> {
					byte flags = packetWrapper.get(Type.BYTE, 0);
					float flySpeed = packetWrapper.get(Type.FLOAT, 0);
					float walkSpeed = packetWrapper.get(Type.FLOAT, 1);
					PlayerSessionStorage abilities = packetWrapper.user().get(PlayerSessionStorage.class);
					abilities.invincible = (flags & 8) == 8;
					abilities.allowFly = (flags & 4) == 4;
					abilities.flying = (flags & 2) == 2;
					abilities.creative = (flags & 1) == 1;
					abilities.flySpeed = flySpeed;
					abilities.walkSpeed = walkSpeed;

					if (abilities.sprinting && abilities.flying) {
						packetWrapper.set(Type.FLOAT, 0, abilities.flySpeed * 2.0f);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLUGIN_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				handler(packetWrapper -> {
					String channel = packetWrapper.get(Type.STRING, 0);
					if (channel.equalsIgnoreCase("MC|TrList")) {
						packetWrapper.passthrough(Type.INT);  //Window Id

						int size;
						if (packetWrapper.isReadable(Type.BYTE, 0)) {
							size = packetWrapper.passthrough(Type.BYTE);
						} else {
							size = packetWrapper.passthrough(Type.UNSIGNED_BYTE);
						}

						for (int i = 0; i < size; i++) {
							Item item = protocol.getItemRewriter().handleItemToClient(packetWrapper.read(Type.ITEM1_8));
							packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 1

							item = protocol.getItemRewriter().handleItemToClient(packetWrapper.read(Type.ITEM1_8));
							packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 3

							boolean has3Items = packetWrapper.passthrough(Type.BOOLEAN);
							if (has3Items) {
								item = protocol.getItemRewriter().handleItemToClient(packetWrapper.read(Type.ITEM1_8));
								packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 2
							}

							packetWrapper.passthrough(Type.BOOLEAN); //Unavailable
							packetWrapper.read(Type.INT); //Uses
							packetWrapper.read(Type.INT); //Max Uses
						}
					} else if (channel.equalsIgnoreCase("MC|Brand")) {
						packetWrapper.write(Type.REMAINING_BYTES, packetWrapper.read(Type.STRING).getBytes(StandardCharsets.UTF_8));
					}

					packetWrapper.cancel();
					packetWrapper.setPacketType(null);
					ByteBuf newPacketBuf = Unpooled.buffer();
					packetWrapper.writeToBuffer(newPacketBuf);
					PacketWrapper newWrapper = PacketWrapper.create(ClientboundPackets1_7_2_5.PLUGIN_MESSAGE, newPacketBuf, packetWrapper.user());
					newWrapper.passthrough(Type.STRING);
					if (newPacketBuf.readableBytes() <= Short.MAX_VALUE) {
						newWrapper.write(Type.SHORT, (short) newPacketBuf.readableBytes());
						newWrapper.send(Protocol1_7_6_10To1_8.class);
					}
				});
			}
		});

		//Camera
		protocol.registerClientbound(ClientboundPackets1_8.CAMERA, null, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					packetWrapper.cancel();

					EntityTracker1_7_6_10 tracker = packetWrapper.user().get(EntityTracker1_7_6_10.class);

					int entityId = packetWrapper.read(Type.VAR_INT);
					int spectating = tracker.spectatingPlayerId;

					if (spectating != entityId) {
						tracker.setSpectating(entityId);
					}
				});
			}
		});

		//Title
		protocol.registerClientbound(ClientboundPackets1_8.TITLE, null, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					packetWrapper.cancel();
					TitleRenderProvider titleRenderProvider = Via.getManager().getProviders().get(TitleRenderProvider.class);
					if (titleRenderProvider == null) return;
					int action = packetWrapper.read(Type.VAR_INT);
					UUID uuid = packetWrapper.user().getProtocolInfo().getUuid();
					switch (action) {
						case 0:
							titleRenderProvider.setTitle(uuid, packetWrapper.read(Type.STRING));
							break;
						case 1:
							titleRenderProvider.setSubTitle(uuid, packetWrapper.read(Type.STRING));
							break;
						case 2:
							titleRenderProvider.setTimings(uuid, packetWrapper.read(Type.INT), packetWrapper.read(Type.INT), packetWrapper.read(Type.INT));
							break;
						case 3:
							titleRenderProvider.clear(uuid);
							break;
						case 4:
							titleRenderProvider.reset(uuid);
							break;
					}
				});
			}
		});

		//Player List Header And Footer
		protocol.cancelClientbound(ClientboundPackets1_8.TAB_LIST);

		//Resource Pack Send
		protocol.registerClientbound(ClientboundPackets1_8.RESOURCE_PACK, ClientboundPackets1_7_2_5.PLUGIN_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				create(Type.STRING, "MC|RPack");
				handler(packetWrapper -> {
					ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
					try {
						// Url
						Type.STRING.write(buf, packetWrapper.read(Type.STRING));

						packetWrapper.write(Type.SHORT_BYTE_ARRAY, Type.REMAINING_BYTES.read(buf));
					} finally {
						buf.release();
					}
				});
				read(Type.STRING); // Hash
			}
		});

		/*  INCOMING  */

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CHAT_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				handler(packetWrapper -> {
					String msg = packetWrapper.get(Type.STRING, 0);
					int gamemode = packetWrapper.user().get(PlayerSessionStorage.class).gameMode;
					if (gamemode == 3 && msg.toLowerCase().startsWith("/stp ")) {
						String username = msg.split(" ")[1];
						GameProfileStorage storage = packetWrapper.user().get(GameProfileStorage.class);
						GameProfileStorage.GameProfile profile = storage.get(username, true);
						if (profile != null && profile.uuid != null) {
							packetWrapper.cancel();

							PacketWrapper teleportPacket = PacketWrapper.create(0x18, null, packetWrapper.user());
							teleportPacket.write(Type.UUID, profile.uuid);

							PacketUtil.sendToServer(teleportPacket, Protocol1_7_6_10To1_8.class, true, true);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.INTERACT_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT, Type.VAR_INT);
				map(Type.BYTE, Type.VAR_INT);
				handler(wrapper -> {
					int mode = wrapper.get(Type.VAR_INT, 1);
					if (mode != 0) {
						return;
					}
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					final EntityTracker1_7_6_10 tracker = wrapper.user().get(EntityTracker1_7_6_10.class);
					final PlayerSessionStorage position = wrapper.user().get(PlayerSessionStorage.class);

					if (tracker.getVirtualHologramMap().containsKey(entityId)) {
						final AABB boundingBox = tracker.getVirtualHologramMap().get(entityId).getBoundingBox();

						Vector3d pos = new Vector3d(position.getPosX(), position.getPosY() + 1.8, position.getPosZ());
						double yaw = Math.toRadians(position.yaw);
						double pitch = Math.toRadians(position.pitch);

						Vector3d dir = new Vector3d(-Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
						Ray3d ray = new Ray3d(pos, dir);
						Vector3d intersection = RayTracing.trace(ray, boundingBox, 5.0);

						if (intersection == null) {
							return;
						}
						intersection.substract(boundingBox.getMin());

						mode = 2;
						wrapper.set(Type.VAR_INT, 1, mode);
						wrapper.write(Type.FLOAT, (float) intersection.getX());
						wrapper.write(Type.FLOAT, (float) intersection.getY());
						wrapper.write(Type.FLOAT, (float) intersection.getZ());
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_MOVEMENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
					PlayerSessionStorage playerSession = packetWrapper.user().get(PlayerSessionStorage.class);
					playerSession.onGround = packetWrapper.get(Type.BOOLEAN, 0);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				read(Type.DOUBLE);
				map(Type.DOUBLE);  //Z
				map(Type.BOOLEAN);  //OnGround
				handler(packetWrapper -> {
					double x = packetWrapper.get(Type.DOUBLE, 0);
					double feetY = packetWrapper.get(Type.DOUBLE, 1);
					double z = packetWrapper.get(Type.DOUBLE, 2);

					PlayerSessionStorage playerSession = packetWrapper.user().get(PlayerSessionStorage.class);

					playerSession.onGround = packetWrapper.get(Type.BOOLEAN, 0);
					playerSession.setPos(x, feetY, z);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_ROTATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
					PlayerSessionStorage playerSession = packetWrapper.user().get(PlayerSessionStorage.class);
					playerSession.yaw = packetWrapper.get(Type.FLOAT, 0);
					playerSession.pitch = packetWrapper.get(Type.FLOAT, 1);
					playerSession.onGround = packetWrapper.get(Type.BOOLEAN, 0);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_POSITION_AND_ROTATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				read(Type.DOUBLE);
				map(Type.DOUBLE);  //Z
				map(Type.FLOAT);  //Yaw
				map(Type.FLOAT);  //Pitch
				map(Type.BOOLEAN);  //OnGround
				handler(packetWrapper -> {
					double x = packetWrapper.get(Type.DOUBLE, 0);
					double feetY = packetWrapper.get(Type.DOUBLE, 1);
					double z = packetWrapper.get(Type.DOUBLE, 2);

					float yaw = packetWrapper.get(Type.FLOAT, 0);
					float pitch = packetWrapper.get(Type.FLOAT, 1);

					PlayerSessionStorage playerSession = packetWrapper.user().get(PlayerSessionStorage.class);

					playerSession.onGround = packetWrapper.get(Type.BOOLEAN, 0);
					playerSession.setPos(x, feetY, z);
					playerSession.yaw = yaw;
					playerSession.pitch = pitch;
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_DIGGING, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT);  //Status
				handler(packetWrapper -> {
					int x = packetWrapper.read(Type.INT);
					int y = packetWrapper.read(Type.UNSIGNED_BYTE);
					int z = packetWrapper.read(Type.INT);
					packetWrapper.write(Type.POSITION1_8, new Position(x, y, z));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_BLOCK_PLACEMENT, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					int x = packetWrapper.read(Type.INT);
					int y = packetWrapper.read(Type.UNSIGNED_BYTE);
					int z = packetWrapper.read(Type.INT);
					packetWrapper.write(Type.POSITION1_8, new Position(x, y, z));

					packetWrapper.passthrough(Type.BYTE);  //Direction
					Item item = packetWrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
					item = protocol.getItemRewriter().handleItemToServer(item);
					packetWrapper.write(Type.ITEM1_8, item);

					for (int i = 0; i < 3; i++) {
						packetWrapper.passthrough(Type.BYTE);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.ANIMATION, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					int entityId = packetWrapper.read(Type.INT);
					int animation = packetWrapper.read(Type.BYTE);  //Animation
					if (animation == 1) return;
					packetWrapper.cancel();
					//1.7 vanilla client is not sending this packet with animation!=1
					switch (animation) {
						case 104:
							animation = 0;
							break;
						case 105:
							animation = 1;
							break;
						case 3:
							animation = 2;
							break;
						default:
							return;
					}
					PacketWrapper entityAction = PacketWrapper.create(0x0B, null, packetWrapper.user());
					entityAction.write(Type.VAR_INT, entityId);
					entityAction.write(Type.VAR_INT, animation);
					entityAction.write(Type.VAR_INT, 0);
					PacketUtil.sendPacket(entityAction, Protocol1_7_6_10To1_8.class, true, true);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.ENTITY_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT, Type.VAR_INT);  //Entity Id
				handler(packetWrapper -> packetWrapper.write(Type.VAR_INT, packetWrapper.read(Type.BYTE) - 1));  //Action Id
				map(Type.INT, Type.VAR_INT);  //Action Parameter
				handler(packetWrapper -> {
					int action = packetWrapper.get(Type.VAR_INT, 1);
					if (action == 3 || action == 4) {
						PlayerSessionStorage playerSession = packetWrapper.user().get(PlayerSessionStorage.class);
						playerSession.sprinting = action == 3;
						PacketWrapper abilitiesPacket = PacketWrapper.create(0x39, null, packetWrapper.user());
						abilitiesPacket.write(Type.BYTE, playerSession.combineAbilities());
						abilitiesPacket.write(Type.FLOAT, playerSession.sprinting ? playerSession.flySpeed * 2.0f : playerSession.flySpeed);
						abilitiesPacket.write(Type.FLOAT, playerSession.walkSpeed);
						PacketUtil.sendPacket(abilitiesPacket, Protocol1_7_6_10To1_8.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.STEER_VEHICLE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.FLOAT);  //Sideways
				map(Type.FLOAT);  //Forwards
				handler(packetWrapper -> {
					boolean jump = packetWrapper.read(Type.BOOLEAN);
					boolean unmount = packetWrapper.read(Type.BOOLEAN);
					short flags = 0;
					if (jump) flags += 0x01;
					if (unmount) flags += 0x02;
					packetWrapper.write(Type.UNSIGNED_BYTE, flags);

					if (unmount) {
						EntityTracker1_7_6_10 tracker = packetWrapper.user().get(EntityTracker1_7_6_10.class);
						if (tracker.spectatingPlayerId != tracker.getPlayerId()) {
							PacketWrapper sneakPacket = PacketWrapper.create(0x0B, null, packetWrapper.user());
							sneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
							sneakPacket.write(Type.VAR_INT, 0);  //Start sneaking
							sneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							PacketWrapper unsneakPacket = PacketWrapper.create(0x0B, null, packetWrapper.user());
							unsneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
							unsneakPacket.write(Type.VAR_INT, 1);  //Stop sneaking
							unsneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10To1_8.class);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.UPDATE_SIGN, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					int x = packetWrapper.read(Type.INT);
					int y = packetWrapper.read(Type.SHORT);
					int z = packetWrapper.read(Type.INT);
					packetWrapper.write(Type.POSITION1_8, new Position(x, y, z));
					for (int i = 0; i < 4; i++) {
						final String line = packetWrapper.read(Type.STRING);

						packetWrapper.write(Type.COMPONENT, ComponentUtil.legacyToJson(line));
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_ABILITIES, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(packetWrapper -> {
					PlayerSessionStorage playerSession = packetWrapper.user().get(PlayerSessionStorage.class);
					if (playerSession.allowFly) {
						byte flags = packetWrapper.get(Type.BYTE, 0);
						playerSession.flying = (flags & 2) == 2;
					}
					packetWrapper.set(Type.FLOAT, 0, playerSession.flySpeed);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.TAB_COMPLETE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				create(Type.OPTIONAL_POSITION1_8, null);
				handler(packetWrapper -> {
					String msg = packetWrapper.get(Type.STRING, 0);
					if (msg.toLowerCase().startsWith("/stp ")) {
						packetWrapper.cancel();
						String[] args = msg.split(" ");
						if (args.length <= 2) {
							String prefix = args.length == 1 ? "" : args[1];
							GameProfileStorage storage = packetWrapper.user().get(GameProfileStorage.class);
							List<GameProfileStorage.GameProfile> profiles = storage.getAllWithPrefix(prefix, true);

							PacketWrapper tabComplete = PacketWrapper.create(0x3A, null, packetWrapper.user());
							tabComplete.write(Type.VAR_INT, profiles.size());
							for (GameProfileStorage.GameProfile profile : profiles) {
								tabComplete.write(Type.STRING, profile.name);
							}

							PacketUtil.sendPacket(tabComplete, Protocol1_7_6_10To1_8.class);
						}
					}
				});
			}
		});

		//Client Settings
		protocol.registerServerbound(ServerboundPackets1_7_2_5.CLIENT_SETTINGS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				read(Type.BYTE);
				handler(packetWrapper -> {
					boolean cape = packetWrapper.read(Type.BOOLEAN);
					packetWrapper.write(Type.UNSIGNED_BYTE, (short) (cape ? 127 : 126));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLUGIN_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				read(Type.SHORT); // Length
				handler(packetWrapper -> {
					String channel = packetWrapper.get(Type.STRING, 0);

					switch (channel) {
						case "MC|TrSel": {
							packetWrapper.passthrough(Type.INT);
							packetWrapper.read(Type.REMAINING_BYTES); // unused data ???
							break;
						}
						case "MC|ItemName": {
							byte[] data = packetWrapper.read(Type.REMAINING_BYTES);
							String name = new String(data, StandardCharsets.UTF_8);

							packetWrapper.write(Type.STRING, name);

							InventoryTracker windowTracker = packetWrapper.user().get(InventoryTracker.class);
							PacketWrapper updateCost = PacketWrapper.create(0x31, null, packetWrapper.user());
							updateCost.write(Type.UNSIGNED_BYTE, windowTracker.anvilId);
							updateCost.write(Type.SHORT, (short) 0);
							updateCost.write(Type.SHORT, windowTracker.levelCost);

							PacketUtil.sendPacket(updateCost, Protocol1_7_6_10To1_8.class, true, true);
							break;
						}
						case "MC|BEdit":
						case "MC|BSign": {
							Item book = packetWrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
							CompoundTag tag = book.tag();
							if (tag != null && tag.contains("pages")) {
								ListTag pages = tag.get("pages");
								for (int i = 0; i < pages.size(); i++) {
									StringTag page = pages.get(i);
									String value = page.getValue();
									value = ComponentUtil.legacyToJsonString(value);
									page.setValue(value);
								}
							}
							packetWrapper.write(Type.ITEM1_8, book);
							break;
						}
						case "MC|Brand": {
							packetWrapper.write(Type.STRING, new String(packetWrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8));
							break;
						}
					}
				});
			}
		});

	}
}
