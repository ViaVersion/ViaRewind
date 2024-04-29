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

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.TitleRenderProvider;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.*;
import com.viaversion.viarewind.api.type.Types1_7_6_10;
import com.viaversion.viarewind.utils.ChatUtil;
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

public class PlayerPackets1_8 {

	public static void register(Protocol1_7_6_10To1_8 protocol) {
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
				handler(wrapper -> {
					if (ViaRewind.getConfig().isReplaceAdventureMode()) {
						if (wrapper.get(Type.UNSIGNED_BYTE, 1) == 2) {
							wrapper.set(Type.UNSIGNED_BYTE, 1, (short) 0);
						}
					}

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					tracker.setClientEntityGameMode(wrapper.get(Type.UNSIGNED_BYTE, 1));

					final Environment dimension = Environment.getEnvironmentById(wrapper.get(Type.INT, 0));

					final ClientWorld world = wrapper.user().get(ClientWorld.class);
					if (world.getEnvironment() != dimension) {
						// Clear entities on dimension change and re-track player
						world.setEnvironment(dimension.id());
						tracker.clearEntities();
						tracker.addEntity(tracker.clientEntityId(), EntityTypes1_10.EntityType.PLAYER);
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

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					if (tracker.spectatingClientEntityId != tracker.clientEntityId()) {
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

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					if (gameMode == 3 || tracker.isSpectator()) {
						UUID myId = wrapper.user().getProtocolInfo().getUuid();
						Item[] equipment = new Item[4];
						if (gameMode == 3) {
							GameProfileStorage.GameProfile profile = wrapper.user().get(GameProfileStorage.class).get(myId);
							equipment[3] = profile == null ? null : profile.getSkull();
						} else {
							for (int i = 0; i < equipment.length; i++) {
								equipment[i] =  wrapper.user().get(PlayerSessionStorage.class).getPlayerEquipment(myId, i);
							}
						}

						for (int i = 0; i < equipment.length; i++) {
							PacketWrapper setSlot = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_SLOT, wrapper.user());
							setSlot.write(Type.BYTE, (byte) 0);
							setSlot.write(Type.SHORT, (short) (8 - i));
							setSlot.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[i]);
							setSlot.scheduleSend(Protocol1_7_6_10To1_8.class);
						}
					}

					if (gameMode == 2 && ViaRewind.getConfig().isReplaceAdventureMode()) {
						gameMode = 0;
						wrapper.set(Type.FLOAT, 0, 0.0f);
					}
					tracker.setClientEntityGameMode(gameMode);
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
				handler(wrapper -> {
					wrapper.cancel();
					int action = wrapper.read(Type.VAR_INT);
					int count = wrapper.read(Type.VAR_INT);
					GameProfileStorage gameProfileStorage = wrapper.user().get(GameProfileStorage.class);
					for (int i = 0; i < count; i++) {
						UUID uuid = wrapper.read(Type.UUID);
						if (action == 0) {
							String name = wrapper.read(Type.STRING);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) gameProfile = gameProfileStorage.put(uuid, name);

							int propertyCount = wrapper.read(Type.VAR_INT);
							while (propertyCount-- > 0) {
								String propertyName = wrapper.read(Type.STRING);
								String propertyValue = wrapper.read(Type.STRING);
								String propertySignature = wrapper.read(Type.OPTIONAL_STRING);
								gameProfile.properties.add(new GameProfileStorage.Property(propertyName, propertyValue, propertySignature));
							}

							int gamemode = wrapper.read(Type.VAR_INT);
							int ping = wrapper.read(Type.VAR_INT);
							gameProfile.ping = ping;
							gameProfile.gamemode = gamemode;
							JsonElement displayName = wrapper.read(Type.OPTIONAL_COMPONENT);
							if (displayName != null) {
								gameProfile.setDisplayName(ChatUtil.jsonToLegacy(wrapper.user(), displayName));
							}

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							packet.scheduleSend(Protocol1_7_6_10To1_8.class);
						} else if (action == 1) {
							int gamemode = wrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.gamemode == gamemode) continue;

							if (gamemode == 3 || gameProfile.gamemode == 3) {
								EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
								int entityId = tracker.getPlayerEntityId(uuid);
								boolean isOwnPlayer = entityId == tracker.clientEntityId();
								if (entityId != -1) {
									// Weirdly, PlayerEntity has only 4 slots instead of 5
									Item[] equipment = new Item[isOwnPlayer ? 4 : 5];
									if (gamemode == 3) {
										equipment[isOwnPlayer ? 3 : 4] = gameProfile.getSkull();
									} else {
										for (int j = 0; j < equipment.length; j++) {
											equipment[j] = wrapper.user().get(PlayerSessionStorage.class).getPlayerEquipment(uuid, j);
										}
									}

									for (short slot = 0; slot < equipment.length; slot++) {
										PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_EQUIPMENT, wrapper.user());
										equipmentPacket.write(Type.INT, entityId);
										equipmentPacket.write(Type.SHORT, slot);
										equipmentPacket.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[slot]);
										equipmentPacket.scheduleSend(Protocol1_7_6_10To1_8.class);
									}
								}
							}

							gameProfile.gamemode = gamemode;
						} else if (action == 2) {
							int ping = wrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_7_6_10To1_8.class);

							gameProfile.ping = ping;

							packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							packet.scheduleSend(Protocol1_7_6_10To1_8.class);
						} else if (action == 3) {
							JsonElement displayNameComponent = wrapper.read(Type.OPTIONAL_COMPONENT);
							String displayName = displayNameComponent != null ? ChatUtil.jsonToLegacy(wrapper.user(), displayNameComponent) : null;

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.displayName == null && displayName == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_7_6_10To1_8.class);

							if (gameProfile.displayName == null && displayName != null || gameProfile.displayName != null && displayName == null || !gameProfile.displayName.equals(displayName)) {
								gameProfile.setDisplayName(displayName);
							}

							packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_7_6_10To1_8.class);
						} else if (action == 4) {
							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.remove(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_7_6_10To1_8.class);
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
				handler(wrapper -> {
					byte flags = wrapper.get(Type.BYTE, 0);
					float flySpeed = wrapper.get(Type.FLOAT, 0);
					float walkSpeed = wrapper.get(Type.FLOAT, 1);
					PlayerSessionStorage abilities = wrapper.user().get(PlayerSessionStorage.class);
					abilities.invincible = (flags & 8) == 8;
					abilities.allowFly = (flags & 4) == 4;
					abilities.flying = (flags & 2) == 2;
					abilities.creative = (flags & 1) == 1;
					abilities.flySpeed = flySpeed;
					abilities.walkSpeed = walkSpeed;

					if (abilities.sprinting && abilities.flying) {
						wrapper.set(Type.FLOAT, 0, abilities.flySpeed * 2.0f);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLUGIN_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				handlerSoftFail(wrapper -> {
					String channel = wrapper.get(Type.STRING, 0);
					if (channel.equals("MC|TrList")) {
						wrapper.passthrough(Type.INT);  //Window Id

						int size;
						if (wrapper.isReadable(Type.BYTE, 0)) {
							size = wrapper.passthrough(Type.BYTE);
						} else {
							size = wrapper.passthrough(Type.UNSIGNED_BYTE);
						}

						for (int i = 0; i < size; i++) {
							Item item = protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_8));
							wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 1

							item = protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_8));
							wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 3

							boolean has3Items = wrapper.passthrough(Type.BOOLEAN);
							if (has3Items) {
								item = protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Type.ITEM1_8));
								wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 2
							}

							wrapper.passthrough(Type.BOOLEAN); //Unavailable
							wrapper.read(Type.INT); //Uses
							wrapper.read(Type.INT); //Max Uses
						}
					} else if (channel.equals("MC|Brand")) {
						wrapper.write(Type.REMAINING_BYTES, wrapper.read(Type.STRING).getBytes(StandardCharsets.UTF_8));
					}

					wrapper.cancel();
					wrapper.setPacketType(null);
					ByteBuf newPacketBuf = Unpooled.buffer();
					wrapper.writeToBuffer(newPacketBuf);
					PacketWrapper newWrapper = PacketWrapper.create(ClientboundPackets1_7_2_5.PLUGIN_MESSAGE, newPacketBuf, wrapper.user());
					newWrapper.passthrough(Type.STRING);
					if (newPacketBuf.readableBytes() <= Short.MAX_VALUE) {
						newWrapper.write(Type.SHORT, (short) newPacketBuf.readableBytes());
						newWrapper.send(Protocol1_7_6_10To1_8.class);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.CAMERA, null, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					wrapper.cancel();
					final int entityId = wrapper.read(Type.VAR_INT);

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					if (tracker.spectatingClientEntityId != entityId) {
						tracker.setSpectating(entityId);
					}
				});
			}
		});

		//Title
		protocol.registerClientbound(ClientboundPackets1_8.TITLE, null, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					wrapper.cancel();
					TitleRenderProvider titleRenderProvider = Via.getManager().getProviders().get(TitleRenderProvider.class);
					if (titleRenderProvider == null) return;
					int action = wrapper.read(Type.VAR_INT);
					UUID uuid = wrapper.user().getProtocolInfo().getUuid();
					switch (action) {
						case 0:
							titleRenderProvider.setTitle(uuid, wrapper.read(Type.STRING));
							break;
						case 1:
							titleRenderProvider.setSubTitle(uuid, wrapper.read(Type.STRING));
							break;
						case 2:
							titleRenderProvider.setTimings(uuid, wrapper.read(Type.INT), wrapper.read(Type.INT), wrapper.read(Type.INT));
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
				handler(wrapper -> {
					ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
					try {
						// Url
						Type.STRING.write(buf, wrapper.read(Type.STRING));

						wrapper.write(Type.SHORT_BYTE_ARRAY, Type.REMAINING_BYTES.read(buf));
					} finally {
						buf.release();
					}
				});
				read(Type.STRING); // Hash
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CHAT_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					String msg = wrapper.get(Type.STRING, 0);
					if (tracker.isSpectator() && msg.toLowerCase().startsWith("/stp ")) { // TODO add setting
						String username = msg.split(" ")[1];
						GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
						GameProfileStorage.GameProfile profile = storage.get(username, true);
						if (profile != null && profile.uuid != null) {
							wrapper.cancel();

							PacketWrapper teleportPacket = PacketWrapper.create(0x18, null, wrapper.user());
							teleportPacket.write(Type.UUID, profile.uuid);

							teleportPacket.send(Protocol1_7_6_10To1_8.class);
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
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
					final PlayerSessionStorage position = wrapper.user().get(PlayerSessionStorage.class);

					if (tracker.getHolograms().containsKey(entityId)) {
						final AABB boundingBox = tracker.getHolograms().get(entityId).getBoundingBox();

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
				handler(wrapper -> {
					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					playerSession.onGround = wrapper.get(Type.BOOLEAN, 0);
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
				handler(wrapper -> {
					double x = wrapper.get(Type.DOUBLE, 0);
					double feetY = wrapper.get(Type.DOUBLE, 1);
					double z = wrapper.get(Type.DOUBLE, 2);

					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);

					playerSession.onGround = wrapper.get(Type.BOOLEAN, 0);
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
				handler(wrapper -> {
					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					playerSession.yaw = wrapper.get(Type.FLOAT, 0);
					playerSession.pitch = wrapper.get(Type.FLOAT, 1);
					playerSession.onGround = wrapper.get(Type.BOOLEAN, 0);
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
				handler(wrapper -> {
					double x = wrapper.get(Type.DOUBLE, 0);
					double feetY = wrapper.get(Type.DOUBLE, 1);
					double z = wrapper.get(Type.DOUBLE, 2);

					float yaw = wrapper.get(Type.FLOAT, 0);
					float pitch = wrapper.get(Type.FLOAT, 1);

					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);

					playerSession.onGround = wrapper.get(Type.BOOLEAN, 0);
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
				handler(wrapper -> {
					int x = wrapper.read(Type.INT);
					int y = wrapper.read(Type.UNSIGNED_BYTE);
					int z = wrapper.read(Type.INT);
					wrapper.write(Type.POSITION1_8, new Position(x, y, z));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_BLOCK_PLACEMENT, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					int x = wrapper.read(Type.INT);
					int y = wrapper.read(Type.UNSIGNED_BYTE);
					int z = wrapper.read(Type.INT);
					wrapper.write(Type.POSITION1_8, new Position(x, y, z));

					wrapper.passthrough(Type.BYTE);  //Direction
					Item item = wrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
					item = protocol.getItemRewriter().handleItemToServer(wrapper.user(), item);
					wrapper.write(Type.ITEM1_8, item);

					for (int i = 0; i < 3; i++) {
						wrapper.passthrough(Type.BYTE);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.ANIMATION, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					int entityId = wrapper.read(Type.INT);
					int animation = wrapper.read(Type.BYTE);  //Animation
					if (animation == 1) return;
					wrapper.cancel();
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
					PacketWrapper entityAction = PacketWrapper.create(0x0B, null, wrapper.user());
					entityAction.write(Type.VAR_INT, entityId);
					entityAction.write(Type.VAR_INT, animation);
					entityAction.write(Type.VAR_INT, 0);
					entityAction.send(Protocol1_7_6_10To1_8.class);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.ENTITY_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT, Type.VAR_INT);  //Entity Id
				handler(wrapper -> wrapper.write(Type.VAR_INT, wrapper.read(Type.BYTE) - 1));  //Action Id
				map(Type.INT, Type.VAR_INT);  //Action Parameter
				handler(wrapper -> {
					int action = wrapper.get(Type.VAR_INT, 1);
					if (action == 3 || action == 4) {
						PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
						playerSession.sprinting = action == 3;
						PacketWrapper abilitiesPacket = PacketWrapper.create(0x39, null, wrapper.user());
						abilitiesPacket.write(Type.BYTE, playerSession.combineAbilities());
						abilitiesPacket.write(Type.FLOAT, playerSession.sprinting ? playerSession.flySpeed * 2.0f : playerSession.flySpeed);
						abilitiesPacket.write(Type.FLOAT, playerSession.walkSpeed);
						abilitiesPacket.send(Protocol1_7_6_10To1_8.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.STEER_VEHICLE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.FLOAT);  //Sideways
				map(Type.FLOAT);  //Forwards
				handler(wrapper -> {
					boolean jump = wrapper.read(Type.BOOLEAN);
					boolean unmount = wrapper.read(Type.BOOLEAN);
					short flags = 0;
					if (jump) flags += 0x01;
					if (unmount) flags += 0x02;
					wrapper.write(Type.UNSIGNED_BYTE, flags);

					if (unmount) {
						EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_7_6_10To1_8.class);
						if (tracker.spectatingClientEntityId != tracker.clientEntityId()) {
							PacketWrapper sneakPacket = PacketWrapper.create(0x0B, null, wrapper.user());
							sneakPacket.write(Type.VAR_INT, tracker.clientEntityId());
							sneakPacket.write(Type.VAR_INT, 0);  //Start sneaking
							sneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							PacketWrapper unsneakPacket = PacketWrapper.create(0x0B, null, wrapper.user());
							unsneakPacket.write(Type.VAR_INT, tracker.clientEntityId());
							unsneakPacket.write(Type.VAR_INT, 1);  //Stop sneaking
							unsneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							sneakPacket.scheduleSendToServer(Protocol1_7_6_10To1_8.class);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.UPDATE_SIGN, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					int x = wrapper.read(Type.INT);
					int y = wrapper.read(Type.SHORT);
					int z = wrapper.read(Type.INT);
					wrapper.write(Type.POSITION1_8, new Position(x, y, z));
					for (int i = 0; i < 4; i++) {
						final String line = wrapper.read(Type.STRING);

						wrapper.write(Type.COMPONENT, ComponentUtil.legacyToJson(line));
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
				handler(wrapper -> {
					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					if (playerSession.allowFly) {
						byte flags = wrapper.get(Type.BYTE, 0);
						playerSession.flying = (flags & 2) == 2;
					}
					wrapper.set(Type.FLOAT, 0, playerSession.flySpeed);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.TAB_COMPLETE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				create(Type.OPTIONAL_POSITION1_8, null);
				handler(wrapper -> {
					String msg = wrapper.get(Type.STRING, 0);
					if (msg.toLowerCase().startsWith("/stp ")) {
						wrapper.cancel();
						String[] args = msg.split(" ");
						if (args.length <= 2) {
							String prefix = args.length == 1 ? "" : args[1];
							GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
							List<GameProfileStorage.GameProfile> profiles = storage.getAllWithPrefix(prefix, true);

							PacketWrapper tabComplete = PacketWrapper.create(0x3A, null, wrapper.user());
							tabComplete.write(Type.VAR_INT, profiles.size());
							for (GameProfileStorage.GameProfile profile : profiles) {
								tabComplete.write(Type.STRING, profile.name);
							}

							tabComplete.scheduleSend(Protocol1_7_6_10To1_8.class);
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
				handler(wrapper -> {
					boolean cape = wrapper.read(Type.BOOLEAN);
					wrapper.write(Type.UNSIGNED_BYTE, (short) (cape ? 127 : 126));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLUGIN_MESSAGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				read(Type.SHORT); // Length
				handler(wrapper -> {
					String channel = wrapper.get(Type.STRING, 0);

					switch (channel) {
						case "MC|TrSel": {
							wrapper.passthrough(Type.INT);
							wrapper.read(Type.REMAINING_BYTES); // unused data ???
							break;
						}
						case "MC|ItemName": {
							byte[] data = wrapper.read(Type.REMAINING_BYTES);
							String name = new String(data, StandardCharsets.UTF_8);

							wrapper.write(Type.STRING, name);

							InventoryTracker windowTracker = wrapper.user().get(InventoryTracker.class);
							PacketWrapper updateCost = PacketWrapper.create(0x31, null, wrapper.user());
							updateCost.write(Type.UNSIGNED_BYTE, windowTracker.anvilId);
							updateCost.write(Type.SHORT, (short) 0);
							updateCost.write(Type.SHORT, windowTracker.levelCost);

							updateCost.send(Protocol1_7_6_10To1_8.class);
							break;
						}
						case "MC|BEdit":
						case "MC|BSign": {
							Item book = wrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
							CompoundTag tag = book.tag();
							if (tag != null && tag.contains("pages")) {
								ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
								for (int i = 0; i < pages.size(); i++) {
									StringTag page = pages.get(i);
									String value = page.getValue();
									value = ComponentUtil.legacyToJsonString(value);
									page.setValue(value);
								}
							}
							wrapper.write(Type.ITEM1_8, book);
							break;
						}
						case "MC|Brand": {
							wrapper.write(Type.STRING, new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8));
							break;
						}
					}
				});
			}
		});

	}
}
