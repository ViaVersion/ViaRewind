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

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.TitleRenderProvider;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.*;
import com.viaversion.viarewind.api.type.RewindTypes;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viarewind.api.minecraft.math.AABB;
import com.viaversion.viarewind.api.minecraft.math.Ray3d;
import com.viaversion.viarewind.api.minecraft.math.RayTracing;
import com.viaversion.viarewind.api.minecraft.math.Vector3d;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.util.ComponentUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class PlayerPacketRewriter1_8 {

	public static void register(Protocol1_8To1_7_6_10 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.CHAT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.COMPONENT); // chat message
				handler(wrapper -> {
					final int position = wrapper.read(Types.BYTE);
					if (position == 2) { // above hotbar
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_DEFAULT_SPAWN_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8, RewindTypes.INT_POSITION); // spawn position
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_HEALTH, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.FLOAT); // health
				map(Types.VAR_INT, Types.SHORT); // food
				map(Types.FLOAT); // food saturation
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.RESPAWN, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // dimension
				map(Types.UNSIGNED_BYTE); // difficulty
				map(Types.UNSIGNED_BYTE); // game mode
				handler(wrapper -> {
					if (ViaRewind.getConfig().isReplaceAdventureMode()) {
						if (wrapper.get(Types.UNSIGNED_BYTE, 1) == 2) {
							wrapper.set(Types.UNSIGNED_BYTE, 1, (short) 0);
						}
					}

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					tracker.setClientEntityGameMode(wrapper.get(Types.UNSIGNED_BYTE, 1));

					final Environment dimension = Environment.getEnvironmentById(wrapper.get(Types.INT, 0));

					final ClientWorld world = wrapper.user().get(ClientWorld.class);
					if (world.getEnvironment() != dimension) {
						// Clear entities on dimension change and re-track player
						world.setEnvironment(dimension.id());
						tracker.clearEntities();
						tracker.addEntity(tracker.clientEntityId(), EntityTypes1_8.EntityType.PLAYER);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_POSITION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.DOUBLE); // x
				map(Types.DOUBLE); // y
				map(Types.DOUBLE); // z
				map(Types.FLOAT); // yaw
				map(Types.FLOAT); // pitch
				handler(wrapper -> {
					final double x = wrapper.get(Types.DOUBLE, 0);
					double y = wrapper.get(Types.DOUBLE, 1);
					final double z = wrapper.get(Types.DOUBLE, 2);

					final float yaw = wrapper.get(Types.FLOAT, 0);
					final float pitch = wrapper.get(Types.FLOAT, 1);

					final PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					final int flags = wrapper.read(Types.BYTE);

					// x, y, and z
					if ((flags & 0x01) == 0x01) wrapper.set(Types.DOUBLE, 0, x + playerSession.getPosX());
					if ((flags & 0x02) == 0x02) y += playerSession.getPosY();

					playerSession.receivedPosY = y;
					wrapper.set(Types.DOUBLE, 1, y + 1.62F);

					if ((flags & 0x04) == 0x04) wrapper.set(Types.DOUBLE, 2, z + playerSession.getPosZ());

					// yaw and pitch
					if ((flags & 0x08) == 0x08) wrapper.set(Types.FLOAT, 0, yaw + playerSession.yaw);
					if ((flags & 0x10) == 0x10) wrapper.set(Types.FLOAT, 1, pitch + playerSession.pitch);

					wrapper.write(Types.BOOLEAN, playerSession.onGround);

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					if (tracker.spectatingClientEntityId != tracker.clientEntityId()) {
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_EXPERIENCE, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.FLOAT); // experience bar
				map(Types.VAR_INT, Types.SHORT); // level
				map(Types.VAR_INT, Types.SHORT); // total experience
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.GAME_EVENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.UNSIGNED_BYTE); // reason
				map(Types.FLOAT); // value
				handler(wrapper -> {
					if (wrapper.get(Types.UNSIGNED_BYTE, 0) != 3) return; // Change game mode
					int gameMode = wrapper.get(Types.FLOAT, 0).intValue();

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
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
							PacketWrapper setSlot = PacketWrapper.create(ClientboundPackets1_7_2_5.CONTAINER_SET_SLOT, wrapper.user());
							setSlot.write(Types.BYTE, (byte) 0);
							setSlot.write(Types.SHORT, (short) (8 - i));
							setSlot.write(RewindTypes.COMPRESSED_NBT_ITEM, equipment[i]);
							setSlot.scheduleSend(Protocol1_8To1_7_6_10.class);
						}
					}

					if (gameMode == 2 && ViaRewind.getConfig().isReplaceAdventureMode()) {
						gameMode = 0;
						wrapper.set(Types.FLOAT, 0, 0.0f);
					}
					tracker.setClientEntityGameMode(gameMode);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.OPEN_SIGN_EDITOR, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8, RewindTypes.INT_POSITION); // position
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_INFO, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					wrapper.cancel();
					int action = wrapper.read(Types.VAR_INT);
					int count = wrapper.read(Types.VAR_INT);
					GameProfileStorage gameProfileStorage = wrapper.user().get(GameProfileStorage.class);
					for (int i = 0; i < count; i++) {
						UUID uuid = wrapper.read(Types.UUID);
						if (action == 0) {
							String name = wrapper.read(Types.STRING);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) gameProfile = gameProfileStorage.put(uuid, name);

							int propertyCount = wrapper.read(Types.VAR_INT);
							while (propertyCount-- > 0) {
								String propertyName = wrapper.read(Types.STRING);
								String propertyValue = wrapper.read(Types.STRING);
								String propertySignature = wrapper.read(Types.OPTIONAL_STRING);
								gameProfile.properties.add(new GameProfileStorage.Property(propertyName, propertyValue, propertySignature));
							}

							int gamemode = wrapper.read(Types.VAR_INT);
							int ping = wrapper.read(Types.VAR_INT);
							gameProfile.ping = ping;
							gameProfile.gamemode = gamemode;
							JsonElement displayName = wrapper.read(Types.OPTIONAL_COMPONENT);
							if (displayName != null) {
								gameProfile.setDisplayName(ChatUtil.jsonToLegacy(wrapper.user(), displayName));
							}

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Types.STRING, gameProfile.getDisplayName());
							packet.write(Types.BOOLEAN, true);
							packet.write(Types.SHORT, (short) ping);
							packet.scheduleSend(Protocol1_8To1_7_6_10.class);
						} else if (action == 1) {
							int gamemode = wrapper.read(Types.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.gamemode == gamemode) continue;

							if (gamemode == 3 || gameProfile.gamemode == 3) {
								EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
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
										PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_EQUIPPED_ITEM, wrapper.user());
										equipmentPacket.write(Types.INT, entityId);
										equipmentPacket.write(Types.SHORT, slot);
										equipmentPacket.write(RewindTypes.COMPRESSED_NBT_ITEM, equipment[slot]);
										equipmentPacket.scheduleSend(Protocol1_8To1_7_6_10.class);
									}
								}
							}

							gameProfile.gamemode = gamemode;
						} else if (action == 2) {
							int ping = wrapper.read(Types.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Types.STRING, gameProfile.getDisplayName());
							packet.write(Types.BOOLEAN, false);
							packet.write(Types.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_8To1_7_6_10.class);

							gameProfile.ping = ping;

							packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Types.STRING, gameProfile.getDisplayName());
							packet.write(Types.BOOLEAN, true);
							packet.write(Types.SHORT, (short) ping);
							packet.scheduleSend(Protocol1_8To1_7_6_10.class);
						} else if (action == 3) {
							JsonElement displayNameComponent = wrapper.read(Types.OPTIONAL_COMPONENT);
							String displayName = displayNameComponent != null ? ChatUtil.jsonToLegacy(wrapper.user(), displayNameComponent) : null;

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.displayName == null && displayName == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Types.STRING, gameProfile.getDisplayName());
							packet.write(Types.BOOLEAN, false);
							packet.write(Types.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_8To1_7_6_10.class);

							if (gameProfile.displayName == null && displayName != null || gameProfile.displayName != null && displayName == null || !gameProfile.displayName.equals(displayName)) {
								gameProfile.setDisplayName(displayName);
							}

							packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Types.STRING, gameProfile.getDisplayName());
							packet.write(Types.BOOLEAN, true);
							packet.write(Types.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_8To1_7_6_10.class);
						} else if (action == 4) {
							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.remove(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_7_2_5.PLAYER_INFO, null, wrapper.user());
							packet.write(Types.STRING, gameProfile.getDisplayName());
							packet.write(Types.BOOLEAN, false);
							packet.write(Types.SHORT, (short) gameProfile.ping);
							packet.scheduleSend(Protocol1_8To1_7_6_10.class);
						}
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_ABILITIES, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BYTE);
				map(Types.FLOAT);
				map(Types.FLOAT);
				handler(wrapper -> {
					byte flags = wrapper.get(Types.BYTE, 0);
					float flySpeed = wrapper.get(Types.FLOAT, 0);
					float walkSpeed = wrapper.get(Types.FLOAT, 1);
					PlayerSessionStorage abilities = wrapper.user().get(PlayerSessionStorage.class);
					abilities.invincible = (flags & 8) == 8;
					abilities.allowFly = (flags & 4) == 4;
					abilities.flying = (flags & 2) == 2;
					abilities.creative = (flags & 1) == 1;
					abilities.flySpeed = flySpeed;
					abilities.walkSpeed = walkSpeed;

					if (abilities.sprinting && abilities.flying) {
						wrapper.set(Types.FLOAT, 0, abilities.flySpeed * 2.0f);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.CUSTOM_PAYLOAD, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				handlerSoftFail(wrapper -> {
					String channel = wrapper.get(Types.STRING, 0);
					if (channel.equals("MC|TrList")) {
						wrapper.passthrough(Types.INT);  //Window Id

						int size;
						if (wrapper.isReadable(Types.BYTE, 0)) {
							size = wrapper.passthrough(Types.BYTE);
						} else {
							size = wrapper.passthrough(Types.UNSIGNED_BYTE);
						}

						for (int i = 0; i < size; i++) {
							Item item = protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_8));
							wrapper.write(RewindTypes.COMPRESSED_NBT_ITEM, item); //Buy Item 1

							item = protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_8));
							wrapper.write(RewindTypes.COMPRESSED_NBT_ITEM, item); //Buy Item 3

							boolean has3Items = wrapper.passthrough(Types.BOOLEAN);
							if (has3Items) {
								item = protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_8));
								wrapper.write(RewindTypes.COMPRESSED_NBT_ITEM, item); //Buy Item 2
							}

							wrapper.passthrough(Types.BOOLEAN); //Unavailable
							wrapper.read(Types.INT); //Uses
							wrapper.read(Types.INT); //Max Uses
						}
					} else if (channel.equals("MC|Brand")) {
						wrapper.write(Types.REMAINING_BYTES, wrapper.read(Types.STRING).getBytes(StandardCharsets.UTF_8));
					}

					wrapper.cancel();
					wrapper.setPacketType(null);
					ByteBuf newPacketBuf = Unpooled.buffer();
					wrapper.writeToBuffer(newPacketBuf);
					PacketWrapper newWrapper = PacketWrapper.create(ClientboundPackets1_7_2_5.CUSTOM_PAYLOAD, newPacketBuf, wrapper.user());
					newWrapper.passthrough(Types.STRING);
					if (newPacketBuf.readableBytes() <= Short.MAX_VALUE) {
						newWrapper.write(Types.SHORT, (short) newPacketBuf.readableBytes());
						newWrapper.send(Protocol1_8To1_7_6_10.class);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_CAMERA, null, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					wrapper.cancel();
					final int entityId = wrapper.read(Types.VAR_INT);

					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					if (tracker.spectatingClientEntityId != entityId) {
						tracker.setSpectating(entityId);
					}
				});
			}
		});

		//Title
		protocol.registerClientbound(ClientboundPackets1_8.SET_TITLES, null, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					wrapper.cancel();
					TitleRenderProvider titleRenderProvider = Via.getManager().getProviders().get(TitleRenderProvider.class);
					if (titleRenderProvider == null) return;
					int action = wrapper.read(Types.VAR_INT);
					UUID uuid = wrapper.user().getProtocolInfo().getUuid();
					switch (action) {
						case 0:
							titleRenderProvider.setTitle(uuid, wrapper.read(Types.STRING));
							break;
						case 1:
							titleRenderProvider.setSubTitle(uuid, wrapper.read(Types.STRING));
							break;
						case 2:
							titleRenderProvider.setTimings(uuid, wrapper.read(Types.INT), wrapper.read(Types.INT), wrapper.read(Types.INT));
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
		protocol.registerClientbound(ClientboundPackets1_8.RESOURCE_PACK, ClientboundPackets1_7_2_5.CUSTOM_PAYLOAD, new PacketHandlers() {
			@Override
			public void register() {
				create(Types.STRING, "MC|RPack");
				handler(wrapper -> {
					ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
					try {
						// Url
						Types.STRING.write(buf, wrapper.read(Types.STRING));

						wrapper.write(Types.SHORT_BYTE_ARRAY, Types.REMAINING_BYTES.read(buf));
					} finally {
						buf.release();
					}
				});
				read(Types.STRING); // Hash
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CHAT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				handler(wrapper -> {
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
					String msg = wrapper.get(Types.STRING, 0);
					if (tracker.isSpectator() && msg.toLowerCase().startsWith("/stp ")) { // TODO add setting
						String username = msg.split(" ")[1];
						GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
						GameProfileStorage.GameProfile profile = storage.get(username, true);
						if (profile != null && profile.uuid != null) {
							wrapper.cancel();

							PacketWrapper teleportPacket = PacketWrapper.create(0x18, null, wrapper.user());
							teleportPacket.write(Types.UUID, profile.uuid);

							teleportPacket.send(Protocol1_8To1_7_6_10.class);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.INTERACT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT, Types.VAR_INT);
				map(Types.BYTE, Types.VAR_INT);
				handler(wrapper -> {
					int mode = wrapper.get(Types.VAR_INT, 1);
					if (mode != 0) {
						return;
					}
					final int entityId = wrapper.get(Types.VAR_INT, 0);
					final EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
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
						wrapper.set(Types.VAR_INT, 1, mode);
						wrapper.write(Types.FLOAT, (float) intersection.getX());
						wrapper.write(Types.FLOAT, (float) intersection.getY());
						wrapper.write(Types.FLOAT, (float) intersection.getZ());
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.MOVE_PLAYER_STATUS_ONLY, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BOOLEAN);
				handler(wrapper -> {
					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					playerSession.onGround = wrapper.get(Types.BOOLEAN, 0);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.MOVE_PLAYER_POS, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.DOUBLE);  //X
				map(Types.DOUBLE);  //Y
				read(Types.DOUBLE);
				map(Types.DOUBLE);  //Z
				map(Types.BOOLEAN);  //OnGround
				handler(wrapper -> {
					double x = wrapper.get(Types.DOUBLE, 0);
					double feetY = wrapper.get(Types.DOUBLE, 1);
					double z = wrapper.get(Types.DOUBLE, 2);

					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);

					playerSession.onGround = wrapper.get(Types.BOOLEAN, 0);
					playerSession.setPos(x, feetY, z);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.MOVE_PLAYER_ROT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.FLOAT);
				map(Types.FLOAT);
				map(Types.BOOLEAN);
				handler(wrapper -> {
					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					playerSession.yaw = wrapper.get(Types.FLOAT, 0);
					playerSession.pitch = wrapper.get(Types.FLOAT, 1);
					playerSession.onGround = wrapper.get(Types.BOOLEAN, 0);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.MOVE_PLAYER_POS_ROT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.DOUBLE);  //X
				map(Types.DOUBLE);  //Y
				read(Types.DOUBLE);
				map(Types.DOUBLE);  //Z
				map(Types.FLOAT);  //Yaw
				map(Types.FLOAT);  //Pitch
				map(Types.BOOLEAN);  //OnGround
				handler(wrapper -> {
					double x = wrapper.get(Types.DOUBLE, 0);
					double feetY = wrapper.get(Types.DOUBLE, 1);
					double z = wrapper.get(Types.DOUBLE, 2);

					float yaw = wrapper.get(Types.FLOAT, 0);
					float pitch = wrapper.get(Types.FLOAT, 1);

					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);

					playerSession.onGround = wrapper.get(Types.BOOLEAN, 0);
					playerSession.setPos(x, feetY, z);
					playerSession.yaw = yaw;
					playerSession.pitch = pitch;
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT);  //Status
				handler(wrapper -> {
					int x = wrapper.read(Types.INT);
					int y = wrapper.read(Types.UNSIGNED_BYTE);
					int z = wrapper.read(Types.INT);
					wrapper.write(Types.BLOCK_POSITION1_8, new BlockPosition(x, y, z));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.USE_ITEM_ON, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					int x = wrapper.read(Types.INT);
					int y = wrapper.read(Types.UNSIGNED_BYTE);
					int z = wrapper.read(Types.INT);
					wrapper.write(Types.BLOCK_POSITION1_8, new BlockPosition(x, y, z));

					wrapper.passthrough(Types.BYTE);  //Direction
					Item item = wrapper.read(RewindTypes.COMPRESSED_NBT_ITEM);
					item = protocol.getItemRewriter().handleItemToServer(wrapper.user(), item);
					wrapper.write(Types.ITEM1_8, item);

					for (int i = 0; i < 3; i++) {
						wrapper.passthrough(Types.BYTE);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.SWING, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					int entityId = wrapper.read(Types.INT);
					int animation = wrapper.read(Types.BYTE);  //Animation
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
					entityAction.write(Types.VAR_INT, entityId);
					entityAction.write(Types.VAR_INT, animation);
					entityAction.write(Types.VAR_INT, 0);
					entityAction.send(Protocol1_8To1_7_6_10.class);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_COMMAND, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT, Types.VAR_INT);  //Entity Id
				handler(wrapper -> wrapper.write(Types.VAR_INT, wrapper.read(Types.BYTE) - 1));  //Action Id
				map(Types.INT, Types.VAR_INT);  //Action Parameter
				handler(wrapper -> {
					int action = wrapper.get(Types.VAR_INT, 1);
					if (action == 3 || action == 4) {
						PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
						playerSession.sprinting = action == 3;
						PacketWrapper abilitiesPacket = PacketWrapper.create(0x39, null, wrapper.user());
						abilitiesPacket.write(Types.BYTE, playerSession.combineAbilities());
						abilitiesPacket.write(Types.FLOAT, playerSession.sprinting ? playerSession.flySpeed * 2.0f : playerSession.flySpeed);
						abilitiesPacket.write(Types.FLOAT, playerSession.walkSpeed);
						abilitiesPacket.scheduleSend(Protocol1_8To1_7_6_10.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_INPUT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.FLOAT);  //Sideways
				map(Types.FLOAT);  //Forwards
				handler(wrapper -> {
					boolean jump = wrapper.read(Types.BOOLEAN);
					boolean unmount = wrapper.read(Types.BOOLEAN);
					short flags = 0;
					if (jump) flags += 0x01;
					if (unmount) flags += 0x02;
					wrapper.write(Types.UNSIGNED_BYTE, flags);

					if (unmount) {
						EntityTracker1_8 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_7_6_10.class);
						if (tracker.spectatingClientEntityId != tracker.clientEntityId()) {
							PacketWrapper sneakPacket = PacketWrapper.create(0x0B, null, wrapper.user());
							sneakPacket.write(Types.VAR_INT, tracker.clientEntityId());
							sneakPacket.write(Types.VAR_INT, 0);  //Start sneaking
							sneakPacket.write(Types.VAR_INT, 0);  //Action Parameter

							PacketWrapper unsneakPacket = PacketWrapper.create(0x0B, null, wrapper.user());
							unsneakPacket.write(Types.VAR_INT, tracker.clientEntityId());
							unsneakPacket.write(Types.VAR_INT, 1);  //Stop sneaking
							unsneakPacket.write(Types.VAR_INT, 0);  //Action Parameter

							sneakPacket.scheduleSendToServer(Protocol1_8To1_7_6_10.class);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.SIGN_UPDATE, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					int x = wrapper.read(Types.INT);
					int y = wrapper.read(Types.SHORT);
					int z = wrapper.read(Types.INT);
					wrapper.write(Types.BLOCK_POSITION1_8, new BlockPosition(x, y, z));
					for (int i = 0; i < 4; i++) {
						final String line = wrapper.read(Types.STRING);

						wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(line));
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.PLAYER_ABILITIES, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BYTE);
				map(Types.FLOAT);
				map(Types.FLOAT);
				handler(wrapper -> {
					PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					if (playerSession.allowFly) {
						byte flags = wrapper.get(Types.BYTE, 0);
						playerSession.flying = (flags & 2) == 2;
					}
					wrapper.set(Types.FLOAT, 0, playerSession.flySpeed);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.COMMAND_SUGGESTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				create(Types.OPTIONAL_POSITION1_8, null);
				handler(wrapper -> {
					String msg = wrapper.get(Types.STRING, 0);
					if (msg.toLowerCase().startsWith("/stp ")) {
						wrapper.cancel();
						String[] args = msg.split(" ");
						if (args.length <= 2) {
							String prefix = args.length == 1 ? "" : args[1];
							GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
							List<GameProfileStorage.GameProfile> profiles = storage.getAllWithPrefix(prefix, true);

							PacketWrapper tabComplete = PacketWrapper.create(0x3A, null, wrapper.user());
							tabComplete.write(Types.VAR_INT, profiles.size());
							for (GameProfileStorage.GameProfile profile : profiles) {
								tabComplete.write(Types.STRING, profile.name);
							}

							tabComplete.scheduleSend(Protocol1_8To1_7_6_10.class);
						}
					}
				});
			}
		});

		//Client Settings
		protocol.registerServerbound(ServerboundPackets1_7_2_5.CLIENT_INFORMATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				map(Types.BYTE);
				map(Types.BYTE);
				map(Types.BOOLEAN);
				read(Types.BYTE);
				handler(wrapper -> {
					boolean cape = wrapper.read(Types.BOOLEAN);
					wrapper.write(Types.UNSIGNED_BYTE, (short) (cape ? 127 : 126));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CUSTOM_PAYLOAD, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING);
				read(Types.SHORT); // Length
				handler(wrapper -> {
					String channel = wrapper.get(Types.STRING, 0);

					switch (channel) {
						case "MC|TrSel": {
							wrapper.passthrough(Types.INT);
							wrapper.read(Types.REMAINING_BYTES); // unused data ???
							break;
						}
						case "MC|ItemName": {
							byte[] data = wrapper.read(Types.REMAINING_BYTES);
							String name = new String(data, StandardCharsets.UTF_8);

							wrapper.write(Types.STRING, name);

							InventoryTracker windowTracker = wrapper.user().get(InventoryTracker.class);
							PacketWrapper updateCost = PacketWrapper.create(0x31, null, wrapper.user());
							updateCost.write(Types.UNSIGNED_BYTE, windowTracker.anvilId);
							updateCost.write(Types.SHORT, (short) 0);
							updateCost.write(Types.SHORT, windowTracker.levelCost);

							updateCost.send(Protocol1_8To1_7_6_10.class);
							break;
						}
						case "MC|BEdit":
						case "MC|BSign": {
							Item book = wrapper.read(RewindTypes.COMPRESSED_NBT_ITEM);
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
							wrapper.write(Types.ITEM1_8, book);
							break;
						}
						case "MC|Brand": {
							wrapper.write(Types.STRING, new String(wrapper.read(Types.REMAINING_BYTES), StandardCharsets.UTF_8));
							break;
						}
					}
				});
			}
		});

	}
}
