package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ServerboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements.ArmorStandReplacement;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider.TitleRenderProvider;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.*;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.Utils;
import de.gerrygames.viarewind.utils.math.AABB;
import de.gerrygames.viarewind.utils.math.Ray3d;
import de.gerrygames.viarewind.utils.math.RayTracing;
import de.gerrygames.viarewind.utils.math.Vector3d;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class PlayerPackets {

	public static void register(Protocol1_7_6_10TO1_8 protocol) {

		/*  OUTGOING  */

		protocol.registerClientbound(ClientboundPackets1_8.JOIN_GAME, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);  //Entiy Id
				map(Type.UNSIGNED_BYTE);  //Gamemode
				map(Type.BYTE);  //Dimension
				map(Type.UNSIGNED_BYTE);  //Difficulty
				map(Type.UNSIGNED_BYTE);  //Max players
				map(Type.STRING);  //Level Type
				map(Type.BOOLEAN, Type.NOTHING);//Reduced Debug Info
				handler(packetWrapper -> {
					if (!ViaRewind.getConfig().isReplaceAdventureMode()) return;
					if (packetWrapper.get(Type.UNSIGNED_BYTE, 0) == 2) {
						packetWrapper.set(Type.UNSIGNED_BYTE, 0, (short) 0);
					}
				});
				handler(packetWrapper -> {
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					tracker.setGamemode(packetWrapper.get(Type.UNSIGNED_BYTE, 0));
					tracker.setPlayerId(packetWrapper.get(Type.INT, 0));
					tracker.getClientEntityTypes().put(tracker.getPlayerId(), Entity1_10Types.EntityType.ENTITY_HUMAN);
					tracker.setDimension(packetWrapper.get(Type.BYTE, 0));
				});
				handler(packetWrapper -> {
					ClientWorld world = packetWrapper.user().get(ClientWorld.class);
					world.setEnvironment(packetWrapper.get(Type.BYTE, 0));
				});
				handler(wrapper -> {
					// Reset on Velocity server change
					wrapper.user().put(new Scoreboard(wrapper.user()));
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.CHAT_MESSAGE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.COMPONENT);  //Chat Message
				handler(packetWrapper -> {
					int position = packetWrapper.read(Type.BYTE);
					if (position == 2) packetWrapper.cancel();
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_POSITION, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					Position position = packetWrapper.read(Type.POSITION);
					packetWrapper.write(Type.INT, position.getX());
					packetWrapper.write(Type.INT, (int) position.getY());
					packetWrapper.write(Type.INT, position.getZ());
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.UPDATE_HEALTH, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);  //Health
				map(Type.VAR_INT, Type.SHORT);  //Food
				map(Type.FLOAT);  //Food Saturation
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.RESPAWN, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.STRING);
				handler(packetWrapper -> {
					if (!ViaRewind.getConfig().isReplaceAdventureMode()) return;
					if (packetWrapper.get(Type.UNSIGNED_BYTE, 1) == 2) {
						packetWrapper.set(Type.UNSIGNED_BYTE, 1, (short) 0);
					}
				});
				handler(packetWrapper -> {
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					tracker.setGamemode(packetWrapper.get(Type.UNSIGNED_BYTE, 1));
					if (tracker.getDimension() != packetWrapper.get(Type.INT, 0)) {
						tracker.setDimension(packetWrapper.get(Type.INT, 0));
						tracker.clearEntities();
						tracker.getClientEntityTypes().put(tracker.getPlayerId(), Entity1_10Types.EntityType.ENTITY_HUMAN);
					}
				});
				handler(packetWrapper -> {
					ClientWorld world = packetWrapper.user().get(ClientWorld.class);
					world.setEnvironment(packetWrapper.get(Type.INT, 0));
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_POSITION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //x
				map(Type.DOUBLE);  //y
				map(Type.DOUBLE);  //z
				map(Type.FLOAT);  //yaw
				map(Type.FLOAT);  //pitch
				handler(packetWrapper -> {
					PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);
					playerPosition.setPositionPacketReceived(true);

					int flags = packetWrapper.read(Type.BYTE);
					if ((flags & 0x01) == 0x01) {
						double x = packetWrapper.get(Type.DOUBLE, 0);
						x += playerPosition.getPosX();
						packetWrapper.set(Type.DOUBLE, 0, x);
					}
					double y = packetWrapper.get(Type.DOUBLE, 1);
					if ((flags & 0x02) == 0x02) {
						y += playerPosition.getPosY();
					}
					playerPosition.setReceivedPosY(y);
					y += (double) 1.62F;
					packetWrapper.set(Type.DOUBLE, 1, y);
					if ((flags & 0x04) == 0x04) {
						double z = packetWrapper.get(Type.DOUBLE, 2);
						z += playerPosition.getPosZ();
						packetWrapper.set(Type.DOUBLE, 2, z);
					}
					if ((flags & 0x08) == 0x08) {
						float yaw = packetWrapper.get(Type.FLOAT, 0);
						yaw += playerPosition.getYaw();
						packetWrapper.set(Type.FLOAT, 0, yaw);
					}
					if ((flags & 0x10) == 0x10) {
						float pitch = packetWrapper.get(Type.FLOAT, 1);
						pitch += playerPosition.getPitch();
						packetWrapper.set(Type.FLOAT, 1, pitch);
					}
				});
				handler(packetWrapper -> {
					PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);
					packetWrapper.write(Type.BOOLEAN, playerPosition.isOnGround());
				});
				handler(packetWrapper -> {
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					if (tracker.getSpectating() != tracker.getPlayerId()) {
						packetWrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_EXPERIENCE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);  //Experience bar
				map(Type.VAR_INT, Type.SHORT);  //Level
				map(Type.VAR_INT, Type.SHORT);  //Total Experience
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.GAME_EVENT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.FLOAT);
				handler(packetWrapper -> {
					int mode = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					if (mode != 3) return;
					int gamemode = packetWrapper.get(Type.FLOAT, 0).intValue();
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					if (gamemode == 3 || tracker.getGamemode() == 3) {
						UUID uuid = packetWrapper.user().getProtocolInfo().getUuid();
						Item[] equipment;
						if (gamemode == 3) {
							GameProfileStorage.GameProfile profile = packetWrapper.user().get(GameProfileStorage.class).get(uuid);
							equipment = new Item[5];
							equipment[4] = profile.getSkull();
						} else {
							equipment = tracker.getPlayerEquipment(uuid);
							if (equipment == null) equipment = new Item[5];
						}

						for (int i = 1; i < 5; i++) {
							PacketWrapper setSlot = PacketWrapper.create(0x2F, null, packetWrapper.user());
							setSlot.write(Type.BYTE, (byte) 0);
							setSlot.write(Type.SHORT, (short) (9 - i));
							setSlot.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[i]);
							PacketUtil.sendPacket(setSlot, Protocol1_7_6_10TO1_8.class);
						}
					}
				});
				handler(packetWrapper -> {
					int mode = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					if (mode == 3) {
						int gamemode = packetWrapper.get(Type.FLOAT, 0).intValue();
						if (gamemode == 2 && ViaRewind.getConfig().isReplaceAdventureMode()) {
							gamemode = 0;
							packetWrapper.set(Type.FLOAT, 0, 0.0f);
						}
						packetWrapper.user().get(EntityTracker.class).setGamemode(gamemode);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.OPEN_SIGN_EDITOR, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					Position position = packetWrapper.read(Type.POSITION);
					packetWrapper.write(Type.INT, position.getX());
					packetWrapper.write(Type.INT, position.getY());
					packetWrapper.write(Type.INT, position.getZ());
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_INFO, new PacketRemapper() {
			@Override
			public void registerMap() {
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
								gameProfile.properties.add(new GameProfileStorage.Property(packetWrapper.read(Type.STRING), packetWrapper.read(Type.STRING), packetWrapper.read(Type.BOOLEAN) ? packetWrapper.read(Type.STRING) : null));
							}
							int gamemode = packetWrapper.read(Type.VAR_INT);
							int ping = packetWrapper.read(Type.VAR_INT);
							gameProfile.ping = ping;
							gameProfile.gamemode = gamemode;
							if (packetWrapper.read(Type.BOOLEAN)) {
								gameProfile.setDisplayName(ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT)));
							}

							PacketWrapper packet = PacketWrapper.create(0x38, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.name);
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10TO1_8.class);
						} else if (action == 1) {
							int gamemode = packetWrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.gamemode == gamemode) continue;

							if (gamemode == 3 || gameProfile.gamemode == 3) {
								EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
								int entityId = tracker.getPlayerEntityId(uuid);
								if (entityId != -1) {
									Item[] equipment;
									if (gamemode == 3) {
										equipment = new Item[5];
										equipment[4] = gameProfile.getSkull();
									} else {
										equipment = tracker.getPlayerEquipment(uuid);
										if (equipment == null) equipment = new Item[5];
									}

									for (short slot = 0; slot < 5; slot++) {
										PacketWrapper equipmentPacket = PacketWrapper.create(0x04, null, packetWrapper.user());
										equipmentPacket.write(Type.INT, entityId);
										equipmentPacket.write(Type.SHORT, slot);
										equipmentPacket.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[slot]);
										PacketUtil.sendPacket(equipmentPacket, Protocol1_7_6_10TO1_8.class);
									}
								}
							}

							gameProfile.gamemode = gamemode;
						} else if (action == 2) {
							int ping = packetWrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) continue;

							gameProfile.ping = ping;

							PacketWrapper packet = PacketWrapper.create(0x38, null, packetWrapper.user());
							packet.write(Type.STRING, gameProfile.name);
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10TO1_8.class);
						} else if (action == 3) {
							String displayName = packetWrapper.read(Type.BOOLEAN) ? ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT)) : null;

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.displayName == null && displayName == null) continue;

							if (gameProfile.displayName == null && displayName != null || gameProfile.displayName != null && displayName == null || !gameProfile.displayName.equals(displayName)) {
								gameProfile.setDisplayName(displayName);
							}
						} else if (action == 4) {
							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.remove(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = PacketWrapper.create(0x38, null, packetWrapper.user());
							//packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.STRING, gameProfile.name);
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10TO1_8.class);
						}
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLAYER_ABILITIES, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(packetWrapper -> {
					byte flags = packetWrapper.get(Type.BYTE, 0);
					float flySpeed = packetWrapper.get(Type.FLOAT, 0);
					float walkSpeed = packetWrapper.get(Type.FLOAT, 1);
					PlayerAbilities abilities = packetWrapper.user().get(PlayerAbilities.class);
					abilities.setInvincible((flags & 8) == 8);
					abilities.setAllowFly((flags & 4) == 4);
					abilities.setFlying((flags & 2) == 2);
					abilities.setCreative((flags & 1) == 1);
					abilities.setFlySpeed(flySpeed);
					abilities.setWalkSpeed(walkSpeed);
					if (abilities.isSprinting() && abilities.isFlying()) {
						packetWrapper.set(Type.FLOAT, 0, abilities.getFlySpeed() * 2.0f);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.PLUGIN_MESSAGE, new PacketRemapper() {
			@Override
			public void registerMap() {
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
							Item item = ItemRewriter.toClient(packetWrapper.read(Type.ITEM));
							packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 1

							item = ItemRewriter.toClient(packetWrapper.read(Type.ITEM));
							packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 3

							boolean has3Items = packetWrapper.passthrough(Type.BOOLEAN);
							if (has3Items) {
								item = ItemRewriter.toClient(packetWrapper.read(Type.ITEM));
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
					packetWrapper.setId(-1);
					ByteBuf newPacketBuf = Unpooled.buffer();
					packetWrapper.writeToBuffer(newPacketBuf);
					PacketWrapper newWrapper = PacketWrapper.create(0x3F, newPacketBuf, packetWrapper.user());
					newWrapper.passthrough(Type.STRING);
					if (newPacketBuf.readableBytes() <= Short.MAX_VALUE) {
						newWrapper.write(Type.SHORT, (short) newPacketBuf.readableBytes());
						newWrapper.send(Protocol1_7_6_10TO1_8.class);
					}
				});
			}
		});

		//Camera
		protocol.registerClientbound(ClientboundPackets1_8.CAMERA, null, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					packetWrapper.cancel();

					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);

					int entityId = packetWrapper.read(Type.VAR_INT);
					int spectating = tracker.getSpectating();

					if (spectating != entityId) {
						tracker.setSpectating(entityId);
					}
				});
			}
		});

		//Title
		protocol.registerClientbound(ClientboundPackets1_8.TITLE, null, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					packetWrapper.cancel();
					TitleRenderProvider titleRenderProvider = Via.getManager().getProviders().get(TitleRenderProvider.class);
					if (titleRenderProvider == null) return;
					int action = packetWrapper.read(Type.VAR_INT);
					UUID uuid = Utils.getUUID(packetWrapper.user());
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
		protocol.registerClientbound(ClientboundPackets1_8.RESOURCE_PACK, ClientboundPackets1_7.PLUGIN_MESSAGE, new PacketRemapper() {
			@Override
			public void registerMap() {
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
				map(Type.STRING, Type.NOTHING); // Hash
			}
		});

		/*  INCOMING  */

		protocol.registerServerbound(ServerboundPackets1_7.CHAT_MESSAGE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(packetWrapper -> {
					String msg = packetWrapper.get(Type.STRING, 0);
					int gamemode = packetWrapper.user().get(EntityTracker.class).getGamemode();
					if (gamemode == 3 && msg.toLowerCase().startsWith("/stp ")) {
						String username = msg.split(" ")[1];
						GameProfileStorage storage = packetWrapper.user().get(GameProfileStorage.class);
						GameProfileStorage.GameProfile profile = storage.get(username, true);
						if (profile != null && profile.uuid != null) {
							packetWrapper.cancel();

							PacketWrapper teleportPacket = PacketWrapper.create(0x18, null, packetWrapper.user());
							teleportPacket.write(Type.UUID, profile.uuid);

							PacketUtil.sendToServer(teleportPacket, Protocol1_7_6_10TO1_8.class, true, true);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.INTERACT_ENTITY, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);
				map(Type.BYTE, Type.VAR_INT);
				handler(packetWrapper -> {
					int mode = packetWrapper.get(Type.VAR_INT, 1);
					if (mode != 0) return;
					int entityId = packetWrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (!(replacement instanceof ArmorStandReplacement)) return;
					ArmorStandReplacement armorStand = (ArmorStandReplacement) replacement;
					AABB boundingBox = armorStand.getBoundingBox();
					PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);
					Vector3d pos = new Vector3d(playerPosition.getPosX(), playerPosition.getPosY() + 1.8, playerPosition.getPosZ());
					double yaw = Math.toRadians(playerPosition.getYaw());
					double pitch = Math.toRadians(playerPosition.getPitch());
					Vector3d dir = new Vector3d(-Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
					Ray3d ray = new Ray3d(pos, dir);
					Vector3d intersection = RayTracing.trace(ray, boundingBox, 5.0);
					if (intersection == null) return;
					intersection.substract(boundingBox.getMin());
					mode = 2;
					packetWrapper.set(Type.VAR_INT, 1, mode);
					packetWrapper.write(Type.FLOAT, (float) intersection.getX());
					packetWrapper.write(Type.FLOAT, (float) intersection.getY());
					packetWrapper.write(Type.FLOAT, (float) intersection.getZ());
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLAYER_MOVEMENT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
					PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);
					playerPosition.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLAYER_POSITION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				map(Type.DOUBLE, Type.NOTHING);
				map(Type.DOUBLE);  //Z
				map(Type.BOOLEAN);  //OnGround
				handler(packetWrapper -> {
					double x = packetWrapper.get(Type.DOUBLE, 0);
					double feetY = packetWrapper.get(Type.DOUBLE, 1);
					double z = packetWrapper.get(Type.DOUBLE, 2);

					PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);

					/*
					Completely useless, this isn't vanilla behaviour so it would trigger anticheats.
					*/
					/*if (playerPosition.isPositionPacketReceived()) {
						playerPosition.setPositionPacketReceived(false);
						feetY -= 0.01;
						packetWrapper.set(Type.DOUBLE, 1, feetY);
					}*/

					playerPosition.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
					playerPosition.setPos(x, feetY, z);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLAYER_ROTATION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
					PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);
					playerPosition.setYaw(packetWrapper.get(Type.FLOAT, 0));
					playerPosition.setPitch(packetWrapper.get(Type.FLOAT, 1));
					playerPosition.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLAYER_POSITION_AND_ROTATION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				map(Type.DOUBLE, Type.NOTHING);
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

					PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);

					/*
					We can't track teleports like this, this packet could've just been a POSLOOK
					sent before client accepted teleport, also there could be multiple teleports
					and the #getReceivedPosY(), would be wrong y-coord
					
					Because of this inaccuracy it could teleport players to a wrong place
					and trigger anticheats
					
					I've tested this change and there is no noticable glitchiness in client view.
					*/
					/*if (playerPosition.isPositionPacketReceived()) {
						playerPosition.setPositionPacketReceived(false);
						feetY = playerPosition.getReceivedPosY();
						packetWrapper.set(Type.DOUBLE, 1, feetY);
					}*/

					playerPosition.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
					playerPosition.setPos(x, feetY, z);
					playerPosition.setYaw(yaw);
					playerPosition.setPitch(pitch);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLAYER_DIGGING, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);  //Status
				handler(packetWrapper -> {
					int x = packetWrapper.read(Type.INT);
					short y = packetWrapper.read(Type.UNSIGNED_BYTE);
					int z = packetWrapper.read(Type.INT);
					packetWrapper.write(Type.POSITION, new Position(x, y, z));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					int x = packetWrapper.read(Type.INT);
					short y = packetWrapper.read(Type.UNSIGNED_BYTE);
					int z = packetWrapper.read(Type.INT);
					packetWrapper.write(Type.POSITION, new Position(x, y, z));

					packetWrapper.passthrough(Type.BYTE);  //Direction
					Item item = packetWrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
					item = ItemRewriter.toServer(item);
					packetWrapper.write(Type.ITEM, item);

					for (int i = 0; i < 3; i++) {
						packetWrapper.passthrough(Type.BYTE);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.ANIMATION, new PacketRemapper() {
			@Override
			public void registerMap() {
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
					PacketUtil.sendPacket(entityAction, Protocol1_7_6_10TO1_8.class, true, true);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.ENTITY_ACTION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);  //Entity Id
				handler(packetWrapper -> packetWrapper.write(Type.VAR_INT, packetWrapper.read(Type.BYTE) - 1));  //Action Id
				map(Type.INT, Type.VAR_INT);  //Action Paramter
				handler(packetWrapper -> {
					int action = packetWrapper.get(Type.VAR_INT, 1);
					if (action == 3 || action == 4) {
						PlayerAbilities abilities = packetWrapper.user().get(PlayerAbilities.class);
						abilities.setSprinting(action == 3);
						PacketWrapper abilitiesPacket = PacketWrapper.create(0x39, null, packetWrapper.user());
						abilitiesPacket.write(Type.BYTE, abilities.getFlags());
						abilitiesPacket.write(Type.FLOAT, abilities.isSprinting() ? abilities.getFlySpeed() * 2.0f : abilities.getFlySpeed());
						abilitiesPacket.write(Type.FLOAT, abilities.getWalkSpeed());
						PacketUtil.sendPacket(abilitiesPacket, Protocol1_7_6_10TO1_8.class);
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.STEER_VEHICLE, new PacketRemapper() {
			@Override
			public void registerMap() {
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
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						if (tracker.getSpectating() != tracker.getPlayerId()) {
							PacketWrapper sneakPacket = PacketWrapper.create(0x0B, null, packetWrapper.user());
							sneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
							sneakPacket.write(Type.VAR_INT, 0);  //Start sneaking
							sneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							PacketWrapper unsneakPacket = PacketWrapper.create(0x0B, null, packetWrapper.user());
							unsneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
							unsneakPacket.write(Type.VAR_INT, 1);  //Stop sneaking
							unsneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10TO1_8.class);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.UPDATE_SIGN, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					int x = packetWrapper.read(Type.INT);
					short y = packetWrapper.read(Type.SHORT);
					int z = packetWrapper.read(Type.INT);
					packetWrapper.write(Type.POSITION, new Position(x, y, z));
					for (int i = 0; i < 4; i++) {
						String line = packetWrapper.read(Type.STRING);
						line = ChatUtil.legacyToJson(line);
						packetWrapper.write(Type.COMPONENT, JsonParser.parseString(line));
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLAYER_ABILITIES, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(packetWrapper -> {
					PlayerAbilities abilities = packetWrapper.user().get(PlayerAbilities.class);
					if (abilities.isAllowFly()) {
						byte flags = packetWrapper.get(Type.BYTE, 0);
						abilities.setFlying((flags & 2) == 2);
					}
					packetWrapper.set(Type.FLOAT, 0, abilities.getFlySpeed());
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.TAB_COMPLETE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				create(Type.OPTIONAL_POSITION, null);
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

							PacketUtil.sendPacket(tabComplete, Protocol1_7_6_10TO1_8.class);
						}
					}
				});
			}
		});

		//Client Settings
		protocol.registerServerbound(ServerboundPackets1_7.CLIENT_SETTINGS, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				map(Type.BYTE, Type.NOTHING);
				handler(packetWrapper -> {
					boolean cape = packetWrapper.read(Type.BOOLEAN);
					packetWrapper.write(Type.UNSIGNED_BYTE, (short) (cape ? 127 : 126));
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.PLUGIN_MESSAGE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.SHORT, Type.NOTHING); // Length
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

							Windows windows = packetWrapper.user().get(Windows.class);
							PacketWrapper updateCost = PacketWrapper.create(0x31, null, packetWrapper.user());
							updateCost.write(Type.UNSIGNED_BYTE, windows.anvilId);
							updateCost.write(Type.SHORT, (short) 0);
							updateCost.write(Type.SHORT, windows.levelCost);

							PacketUtil.sendPacket(updateCost, Protocol1_7_6_10TO1_8.class, true, true);
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
									value = ChatUtil.legacyToJson(value);
									page.setValue(value);
								}
							}
							packetWrapper.write(Type.ITEM, book);
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
