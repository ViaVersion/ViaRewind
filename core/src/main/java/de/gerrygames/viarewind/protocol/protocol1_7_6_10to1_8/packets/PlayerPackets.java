package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.google.common.base.Charsets;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements.ArmorStandReplacement;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider.TitleRenderProvider;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerAbilities;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerPosition;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Windows;
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
import io.netty.buffer.Unpooled;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.CustomByteType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.util.GsonUtil;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ListTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

import java.util.List;
import java.util.UUID;

public class PlayerPackets {

	public static void register(Protocol protocol) {

		/*  OUTGOING  */

		//Join Game
		protocol.registerOutgoing(State.PLAY, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);  //Entiy Id
				map(Type.UNSIGNED_BYTE);  //Gamemode
				map(Type.BYTE);  //Dimension
				map(Type.UNSIGNED_BYTE);  //Difficulty
				map(Type.UNSIGNED_BYTE);  //Max players
				map(Type.STRING);  //Level Type
				handler(wrapper -> wrapper.read(Type.BOOLEAN));  //Reduced Debug Info

				handler(wrapper -> {
					if (!ViaRewind.getConfig().isReplaceAdventureMode()) return;
					if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 2) {
						wrapper.set(Type.UNSIGNED_BYTE, 0, (short) 0);
					}
				});
				handler(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.setGamemode(wrapper.get(Type.UNSIGNED_BYTE, 0));
					tracker.setPlayerId(wrapper.get(Type.INT, 0));
					tracker.getClientEntityTypes().put(tracker.getPlayerId(), Entity1_10Types.EntityType.ENTITY_HUMAN);
					tracker.setDimension(wrapper.get(Type.BYTE, 0));
				});
				handler(wrapper -> {
					ClientWorld world = wrapper.user().get(ClientWorld.class);
					world.setEnvironment(wrapper.get(Type.BYTE, 0));
				});
			}
		});

		//Chat Message
		protocol.registerOutgoing(State.PLAY, 0x02, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.COMPONENT);  //Chat Message

				handler(wrapper -> {
					int position = wrapper.read(Type.BYTE);
					if (position == 2) wrapper.cancel();
				});
			}
		});

		//Spawn Position
		protocol.registerOutgoing(State.PLAY, 0x05, 0x05, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.INT, (int) position.getY());
					wrapper.write(Type.INT, position.getZ());
				});
			}
		});

		//Update Health
		protocol.registerOutgoing(State.PLAY, 0x06, 0x06, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);  //Health
				map(Type.VAR_INT, Type.SHORT);  //Food
				map(Type.FLOAT);  //Food Saturation
			}
		});

		//Respawn
		protocol.registerOutgoing(State.PLAY, 0x07, 0x07, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.STRING);

				handler(wrapper -> {
					if (!ViaRewind.getConfig().isReplaceAdventureMode()) return;
					if (wrapper.get(Type.UNSIGNED_BYTE, 1) == 2) {
						wrapper.set(Type.UNSIGNED_BYTE, 1, (short) 0);
					}
				});
				handler(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.setGamemode(wrapper.get(Type.UNSIGNED_BYTE, 1));
					if (tracker.getDimension() != wrapper.get(Type.INT, 0)) {
						tracker.setDimension(wrapper.get(Type.INT, 0));
						tracker.clearEntities();
						tracker.getClientEntityTypes().put(tracker.getPlayerId(), Entity1_10Types.EntityType.ENTITY_HUMAN);
					}
				});
				handler(wrapper -> {
					ClientWorld world = wrapper.user().get(ClientWorld.class);
					world.setEnvironment(wrapper.get(Type.INT, 0));
				});
			}
		});

		//Player Position And Look
		protocol.registerOutgoing(State.PLAY, 0x08, 0x08, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //x
				map(Type.DOUBLE);  //y
				map(Type.DOUBLE);  //z
				map(Type.FLOAT);  //yaw
				map(Type.FLOAT);  //pitch

				handler(wrapper -> {
					PlayerPosition playerPosition = wrapper.user().get(PlayerPosition.class);
					playerPosition.setPositionPacketReceived(true);

					int flags = wrapper.read(Type.BYTE);
					if ((flags & 0x01) == 0x01) {
						double x = wrapper.get(Type.DOUBLE, 0);
						x += playerPosition.getPosX();
						wrapper.set(Type.DOUBLE, 0, x);
					}
					double y = wrapper.get(Type.DOUBLE, 1);
					if ((flags & 0x02) == 0x02) {
						y += playerPosition.getPosY();
					}
					playerPosition.setReceivedPosY(y);
					y += (double) 1.62F;
					wrapper.set(Type.DOUBLE, 1, y);
					if ((flags & 0x04) == 0x04) {
						double z = wrapper.get(Type.DOUBLE, 2);
						z += playerPosition.getPosZ();
						wrapper.set(Type.DOUBLE, 2, z);
					}
					if ((flags & 0x08) == 0x08) {
						float yaw = wrapper.get(Type.FLOAT, 0);
						yaw += playerPosition.getYaw();
						wrapper.set(Type.FLOAT, 0, yaw);
					}
					if ((flags & 0x10) == 0x10) {
						float pitch = wrapper.get(Type.FLOAT, 1);
						pitch += playerPosition.getPitch();
						wrapper.set(Type.FLOAT, 1, pitch);
					}
				});
				create(wrapper -> {
					PlayerPosition playerPosition = wrapper.user().get(PlayerPosition.class);
					wrapper.write(Type.BOOLEAN, playerPosition.isOnGround());
				});
				handler(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					if (tracker.getSpectating() != tracker.getPlayerId()) {
						wrapper.cancel();
					}
				});
			}
		});

		//Set Experience
		protocol.registerOutgoing(State.PLAY, 0x1F, 0x1F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);  //Experience bar
				map(Type.VAR_INT, Type.SHORT);  //Level
				map(Type.VAR_INT, Type.SHORT);  //Total Experience
			}
		});

		//Change Game State
		protocol.registerOutgoing(State.PLAY, 0x2B, 0x2B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.FLOAT);

				handler(wrapper -> {
					int mode = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (mode != 3) return;
					int gamemode = wrapper.get(Type.FLOAT, 0).intValue();
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					if (gamemode == 3 || tracker.getGamemode() == 3) {
						UUID uuid = wrapper.user().get(ProtocolInfo.class).getUuid();
						Item[] equipment;
						if (gamemode == 3) {
							GameProfileStorage.GameProfile profile = wrapper.user().get(GameProfileStorage.class).get(uuid);
							equipment = new Item[5];
							equipment[4] = profile.getSkull();
						} else {
							equipment = tracker.getPlayerEquipment(uuid);
							if (equipment == null) equipment = new Item[5];
						}

						for (int i = 1; i < 5; i++) {
							PacketWrapper setSlot = new PacketWrapper(0x2F, null, wrapper.user());
							setSlot.write(Type.BYTE, (byte) 0);
							setSlot.write(Type.SHORT, (short) (9 - i));
							setSlot.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[i]);
							PacketUtil.sendPacket(setSlot, Protocol1_7_6_10TO1_8.class);
						}
					}
				});
				handler(wrapper -> {
					int mode = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (mode == 3) {
						int gamemode = wrapper.get(Type.FLOAT, 0).intValue();
						if (gamemode == 2 && ViaRewind.getConfig().isReplaceAdventureMode()) {
							gamemode = 0;
							wrapper.set(Type.FLOAT, 0, 0.0f);
						}
						wrapper.user().get(EntityTracker.class).setGamemode(gamemode);
					}
				});
			}
		});

		//Open Sign Editor
		protocol.registerOutgoing(State.PLAY, 0x36, 0x36, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.INT, (int) position.getY());
					wrapper.write(Type.INT, position.getZ());
				});
			}
		});

		//Player List Item
		protocol.registerOutgoing(State.PLAY, 0x38, 0x38, new PacketRemapper() {
			@Override
			public void registerMap() {
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
								gameProfile.properties.add(new GameProfileStorage.Property(wrapper.read(Type.STRING), wrapper.read(Type.STRING), wrapper.read(Type.BOOLEAN) ? wrapper.read(Type.STRING) : null));
							}
							int gamemode = wrapper.read(Type.VAR_INT);
							int ping = wrapper.read(Type.VAR_INT);
							gameProfile.ping = ping;
							gameProfile.gamemode = gamemode;
							if (wrapper.read(Type.BOOLEAN)) {
								gameProfile.setDisplayName(wrapper.read(Type.STRING));
							}

							PacketWrapper packet = new PacketWrapper(0x38, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.name);
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10TO1_8.class, true, true);
						} else if (action == 1) {
							int gamemode = wrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.gamemode == gamemode) continue;

							if (gamemode == 3 || gameProfile.gamemode == 3) {
								EntityTracker tracker = wrapper.user().get(EntityTracker.class);
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
										PacketWrapper equipmentPacket = new PacketWrapper(0x04, null, wrapper.user());
										equipmentPacket.write(Type.INT, entityId);
										equipmentPacket.write(Type.SHORT, slot);
										equipmentPacket.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[slot]);
										PacketUtil.sendPacket(equipmentPacket, Protocol1_7_6_10TO1_8.class, true, true);
									}
								}
							}

							gameProfile.gamemode = gamemode;
						} else if (action == 2) {
							int ping = wrapper.read(Type.VAR_INT);

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null) continue;

							gameProfile.ping = ping;

							PacketWrapper packet = new PacketWrapper(0x38, null, wrapper.user());
							packet.write(Type.STRING, gameProfile.name);
							packet.write(Type.BOOLEAN, true);
							packet.write(Type.SHORT, (short) ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10TO1_8.class, true, true);
						} else if (action == 3) {
							String displayName = wrapper.read(Type.BOOLEAN) ? wrapper.read(Type.STRING) : null;

							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
							if (gameProfile == null || gameProfile.displayName == null && displayName == null) continue;

							if (gameProfile.displayName == null && displayName != null || gameProfile.displayName != null && displayName == null || !gameProfile.displayName.equals(displayName)) {
								gameProfile.setDisplayName(displayName);
							}
						} else if (action == 4) {
							GameProfileStorage.GameProfile gameProfile = gameProfileStorage.remove(uuid);
							if (gameProfile == null) continue;

							PacketWrapper packet = new PacketWrapper(0x38, null, wrapper.user());
							//packet.write(Type.STRING, gameProfile.getDisplayName());
							packet.write(Type.STRING, gameProfile.name);
							packet.write(Type.BOOLEAN, false);
							packet.write(Type.SHORT, (short) gameProfile.ping);
							PacketUtil.sendPacket(packet, Protocol1_7_6_10TO1_8.class, true, true);
						}
					}
				});
			}
		});

		//Player Abilities
		protocol.registerOutgoing(State.PLAY, 0x39, 0x39, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);

				handler(wrapper -> {
					byte flags = wrapper.get(Type.BYTE, 0);
					float flySpeed = wrapper.get(Type.FLOAT, 0);
					float walkSpeed = wrapper.get(Type.FLOAT, 1);
					PlayerAbilities abilities = wrapper.user().get(PlayerAbilities.class);
					abilities.setInvincible((flags & 8) == 8);
					abilities.setAllowFly((flags & 4) == 4);
					abilities.setFlying((flags & 2) == 2);
					abilities.setCreative((flags & 1) == 1);
					abilities.setFlySpeed(flySpeed);
					abilities.setWalkSpeed(walkSpeed);
					if (abilities.isSprinting() && abilities.isFlying()) {
						wrapper.set(Type.FLOAT, 0, abilities.getFlySpeed() * 2.0f);
					}
				});
			}
		});

		//Custom Payload
		protocol.registerOutgoing(State.PLAY, 0x3F, 0x3F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);

				handler(wrapper -> {
					String channel = wrapper.get(Type.STRING, 0);
					if (channel.equalsIgnoreCase("MC|TrList")) {
						wrapper.passthrough(Type.INT);  //Window Id

						int size;
						if (wrapper.isReadable(Type.BYTE, 0)) {
							size = wrapper.passthrough(Type.BYTE);
						} else {
							size = wrapper.passthrough(Type.UNSIGNED_BYTE);
						}

						for (int i = 0; i < size; i++) {
							Item item;

							//Buy Item 1
							item = wrapper.read(Type.ITEM);
							ItemRewriter.toClient(item);
							wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item);

							//Buy Item 3
							item = wrapper.read(Type.ITEM);
							ItemRewriter.toClient(item);
							wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item);

							//Buy Item 2
							boolean has3Items = wrapper.passthrough(Type.BOOLEAN);
							if (has3Items) {
								item = wrapper.read(Type.ITEM);
								ItemRewriter.toClient(item);
								wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item);
							}

							wrapper.passthrough(Type.BOOLEAN); //Unavailable
							wrapper.read(Type.INT); //Uses
							wrapper.read(Type.INT); //Max Uses
						}
					} else if (channel.equalsIgnoreCase("MC|Brand")) {
						wrapper.write(Type.REMAINING_BYTES, wrapper.read(Type.STRING).getBytes(Charsets.UTF_8));
					}

					wrapper.cancel();
					wrapper.setId(-1);
					ByteBuf newPacketBuf = Unpooled.buffer();
					wrapper.writeToBuffer(newPacketBuf);
					PacketWrapper newWrapper = new PacketWrapper(0x3F, newPacketBuf, wrapper.user());
					newWrapper.passthrough(Type.STRING);
					if (newPacketBuf.readableBytes() <= Short.MAX_VALUE) {
						newWrapper.write(Type.SHORT, (short) newPacketBuf.readableBytes());
						newWrapper.send(Protocol1_7_6_10TO1_8.class, true, true);
					}
				});
			}
		});

		//Camera
		protocol.registerOutgoing(State.PLAY, 0x43, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					wrapper.cancel();

					EntityTracker tracker = wrapper.user().get(EntityTracker.class);

					int entityId = wrapper.read(Type.VAR_INT);
					int spectating = tracker.getSpectating();

					if (spectating != entityId) {
						tracker.setSpectating(entityId);
					}
				});
			}
		});

		//Title
		protocol.registerOutgoing(State.PLAY, 0x45, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					wrapper.cancel();
					TitleRenderProvider titleRenderProvider = Via.getManager().getProviders().get(TitleRenderProvider.class);
					if (titleRenderProvider == null) return;
					int action = wrapper.read(Type.VAR_INT);
					UUID uuid = Utils.getUUID(wrapper.user());
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
		protocol.cancelOutgoing(State.PLAY, 0x47);

		//Resource Pack Send
		protocol.cancelOutgoing(State.PLAY, 0x48);

		/*  INCOMING  */

		//Chat Message
		protocol.registerIncoming(State.PLAY, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);

				handler(wrapper -> {
					String msg = wrapper.get(Type.STRING, 0);
					int gamemode = wrapper.user().get(EntityTracker.class).getGamemode();
					if (gamemode == 3 && msg.toLowerCase().startsWith("/stp ")) {
						String username = msg.split(" ")[1];
						GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
						GameProfileStorage.GameProfile profile = storage.get(username, true);
						if (profile != null && profile.uuid != null) {
							wrapper.cancel();

							PacketWrapper teleportPacket = new PacketWrapper(0x18, null, wrapper.user());
							teleportPacket.write(Type.UUID, profile.uuid);

							PacketUtil.sendToServer(teleportPacket, Protocol1_7_6_10TO1_8.class, true, true);
						}
					}
				});
			}
		});

		//Use Entity
		protocol.registerIncoming(State.PLAY, 0x02, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);
				map(Type.BYTE, Type.VAR_INT);

				handler(wrapper -> {
					int mode = wrapper.get(Type.VAR_INT, 1);
					if (mode != 0) return;
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (!(replacement instanceof ArmorStandReplacement)) return;
					ArmorStandReplacement armorStand = (ArmorStandReplacement) replacement;
					AABB boundingBox = armorStand.getBoundingBox();
					PlayerPosition playerPosition = wrapper.user().get(PlayerPosition.class);
					Vector3d pos = new Vector3d(playerPosition.getPosX(), playerPosition.getPosY() + 1.8, playerPosition.getPosZ());
					double yaw = Math.toRadians(playerPosition.getYaw());
					double pitch = Math.toRadians(playerPosition.getPitch());
					Vector3d dir = new Vector3d(-Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
					Ray3d ray = new Ray3d(pos, dir);
					Vector3d intersection = RayTracing.trace(ray, boundingBox, 5.0);
					if (intersection == null) return;
					intersection.substract(boundingBox.getMin());
					mode = 2;
					wrapper.set(Type.VAR_INT, 1, mode);
					wrapper.write(Type.FLOAT, (float) intersection.getX());
					wrapper.write(Type.FLOAT, (float) intersection.getY());
					wrapper.write(Type.FLOAT, (float) intersection.getZ());
				});
			}
		});

		//Player
		protocol.registerIncoming(State.PLAY, 0x03, 0x03, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BOOLEAN);

				handler(wrapper -> {
					PlayerPosition playerPosition = wrapper.user().get(PlayerPosition.class);
					playerPosition.setOnGround(wrapper.get(Type.BOOLEAN, 0));
				});
			}
		});

		//Player Position
		protocol.registerIncoming(State.PLAY, 0x04, 0x04, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				handler(wrapper -> wrapper.read(Type.DOUBLE));
				map(Type.DOUBLE);  //Z
				map(Type.BOOLEAN);  //OnGround

				handler(wrapper -> {
					double x = wrapper.get(Type.DOUBLE, 0);
					double feetY = wrapper.get(Type.DOUBLE, 1);
					double z = wrapper.get(Type.DOUBLE, 2);

					PlayerPosition playerPosition = wrapper.user().get(PlayerPosition.class);

					if (playerPosition.isPositionPacketReceived()) {
						playerPosition.setPositionPacketReceived(false);
						feetY -= 0.01;
						wrapper.set(Type.DOUBLE, 1, feetY);
					}

					playerPosition.setOnGround(wrapper.get(Type.BOOLEAN, 0));
					playerPosition.setPos(x, feetY, z);
				});
			}
		});

		//Player Look
		protocol.registerIncoming(State.PLAY, 0x05, 0x05, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);

				handler(wrapper -> {
					PlayerPosition playerPosition = wrapper.user().get(PlayerPosition.class);
					playerPosition.setYaw(wrapper.get(Type.FLOAT, 0));
					playerPosition.setPitch(wrapper.get(Type.FLOAT, 1));
					playerPosition.setOnGround(wrapper.get(Type.BOOLEAN, 0));
				});
			}
		});

		//Player Position And Look
		protocol.registerIncoming(State.PLAY, 0x06, 0x06, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				handler(wrapper -> wrapper.read(Type.DOUBLE));
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

					PlayerPosition playerPosition = wrapper.user().get(PlayerPosition.class);

					if (playerPosition.isPositionPacketReceived()) {
						playerPosition.setPositionPacketReceived(false);
						feetY = playerPosition.getReceivedPosY();
						wrapper.set(Type.DOUBLE, 1, feetY);
					}

					playerPosition.setOnGround(wrapper.get(Type.BOOLEAN, 0));
					playerPosition.setPos(x, feetY, z);
					playerPosition.setYaw(yaw);
					playerPosition.setPitch(pitch);
				});
			}
		});

		//Player Digging
		protocol.registerIncoming(State.PLAY, 0x07, 0x07, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);  //Status
				handler(wrapper -> {
					int x = wrapper.read(Type.INT);
					short y = wrapper.read(Type.UNSIGNED_BYTE);
					int z = wrapper.read(Type.INT);
					wrapper.write(Type.POSITION, new Position(x, y, z));
				});
				map(Type.BYTE);  //Face
			}
		});

		//Player Block Placement
		protocol.registerIncoming(State.PLAY, 0x08, 0x08, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					int x = wrapper.read(Type.INT);
					short y = wrapper.read(Type.UNSIGNED_BYTE);
					int z = wrapper.read(Type.INT);
					wrapper.write(Type.POSITION, new Position(x, y, z));

					wrapper.passthrough(Type.BYTE);  //Direction
					Item item = wrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
					ItemRewriter.toServer(item);
					wrapper.write(Type.ITEM, item);

					for (int i = 0; i < 3; i++) {
						wrapper.passthrough(Type.BYTE);
					}
				});
			}
		});

		//Animation
		protocol.registerIncoming(State.PLAY, 0x0A, 0x0A, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					int entityId = wrapper.read(Type.INT);
					int animation = wrapper.read(Type.BYTE);  //Animation
					if (animation == 1) return;
					wrapper.cancel();
					//1.7 vanilla client is not sending this packet with animation != 1
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
					PacketWrapper entityAction = new PacketWrapper(0x0B, null, wrapper.user());
					entityAction.write(Type.VAR_INT, entityId);
					entityAction.write(Type.VAR_INT, animation);
					entityAction.write(Type.VAR_INT, 0);
					PacketUtil.sendPacket(entityAction, Protocol1_7_6_10TO1_8.class, true, true);
				});
			}
		});

		//Entity Action
		protocol.registerIncoming(State.PLAY, 0x0B, 0x0B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);  //Entity Id
				handler(wrapper -> wrapper.write(Type.VAR_INT, wrapper.read(Type.BYTE) - 1));  //Action Id
				map(Type.INT, Type.VAR_INT);  //Action Paramter

				handler(wrapper -> {
					int action = wrapper.get(Type.VAR_INT, 1);
					if (action == 3 || action == 4) {
						PlayerAbilities abilities = wrapper.user().get(PlayerAbilities.class);
						abilities.setSprinting(action == 3);
						PacketWrapper abilitiesPacket = new PacketWrapper(0x39, null, wrapper.user());
						abilitiesPacket.write(Type.BYTE, abilities.getFlags());
						abilitiesPacket.write(Type.FLOAT, abilities.isSprinting() ? abilities.getFlySpeed() * 2.0f : abilities.getFlySpeed());
						abilitiesPacket.write(Type.FLOAT, abilities.getWalkSpeed());
						PacketUtil.sendPacket(abilitiesPacket, Protocol1_7_6_10TO1_8.class);
					}
				});
			}
		});

		//Steer Vehicle
		protocol.registerIncoming(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
			@Override
			public void registerMap() {
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
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						if (tracker.getSpectating() != tracker.getPlayerId()) {
							PacketWrapper sneakPacket = new PacketWrapper(0x0B, null, wrapper.user());
							sneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
							sneakPacket.write(Type.VAR_INT, 0);  //Start sneaking
							sneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							PacketWrapper unsneakPacket = new PacketWrapper(0x0B, null, wrapper.user());
							unsneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
							unsneakPacket.write(Type.VAR_INT, 1);  //Stop sneaking
							unsneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

							PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10TO1_8.class);
							PacketUtil.sendToServer(unsneakPacket, Protocol1_7_6_10TO1_8.class);
						}
					}
				});
			}
		});

		//Update Sign
		protocol.registerIncoming(State.PLAY, 0x12, 0x12, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					int x = wrapper.read(Type.INT);
					short y = wrapper.read(Type.SHORT);
					int z = wrapper.read(Type.INT);
					wrapper.write(Type.POSITION, new Position(x, y, z));

					for (int i = 0; i < 4; i++) {
						String line = wrapper.read(Type.STRING);
						line = ChatUtil.legacyToJson(line);
						wrapper.write(Type.COMPONENT, GsonUtil.getJsonParser().parse(line));
					}
				});
			}
		});

		//Player Abilities
		protocol.registerIncoming(State.PLAY, 0x13, 0x13, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);

				handler(wrapper -> {
					byte flags = wrapper.get(Type.BYTE, 0);
					PlayerAbilities abilities = wrapper.user().get(PlayerAbilities.class);
					abilities.setAllowFly((flags & 4) == 4);
					abilities.setFlying((flags & 2) == 2);
					wrapper.set(Type.FLOAT, 0, abilities.getFlySpeed());
				});
			}
		});

		//Tab-Complete
		protocol.registerIncoming(State.PLAY, 0x14, 0x14, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				create(wrapper -> wrapper.write(Type.OPTIONAL_POSITION, null));

				handler(wrapper -> {
					String msg = wrapper.get(Type.STRING, 0);
					if (msg.toLowerCase().startsWith("/stp ")) {
						wrapper.cancel();
						String[] args = msg.split(" ");
						if (args.length <= 2) {
							String prefix = args.length == 1 ? "" : args[1];
							GameProfileStorage storage = wrapper.user().get(GameProfileStorage.class);
							List<GameProfileStorage.GameProfile> profiles = storage.getAllWithPrefix(prefix, true);

							PacketWrapper tabComplete = new PacketWrapper(0x3A, null, wrapper.user());
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
		protocol.registerIncoming(State.PLAY, 0x15, 0x15, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				handler(wrapper -> {
					wrapper.read(Type.BYTE);

					boolean cape = wrapper.read(Type.BOOLEAN);
					wrapper.write(Type.UNSIGNED_BYTE, (short) (cape ? 127 : 126));
				});
			}
		});

		//Custom Payload
		protocol.registerIncoming(State.PLAY, 0x17, 0x17, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);

				handler(wrapper -> {
					String channel = wrapper.get(Type.STRING, 0);
					int length = wrapper.read(Type.SHORT);

					if (channel.equalsIgnoreCase("MC|ItemName")) {
						CustomByteType customByteType = new CustomByteType(length);
						byte[] data = wrapper.read(customByteType);
						String name = new String(data, Charsets.UTF_8);
						ByteBuf buf = wrapper.user().getChannel().alloc().buffer();
						Type.STRING.write(buf, name);
						data = new byte[buf.readableBytes()];
						buf.readBytes(data);
						buf.release();
						wrapper.write(Type.REMAINING_BYTES, data);

						Windows windows = wrapper.user().get(Windows.class);
						PacketWrapper updateCost = new PacketWrapper(0x31, null, wrapper.user());
						updateCost.write(Type.UNSIGNED_BYTE, windows.anvilId);
						updateCost.write(Type.SHORT, (short) 0);
						updateCost.write(Type.SHORT, windows.levelCost);

						PacketUtil.sendPacket(updateCost, Protocol1_7_6_10TO1_8.class, true, true);
					} else if (channel.equalsIgnoreCase("MC|BEdit") || channel.equalsIgnoreCase("MC|BSign")) {
						Item book = wrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
						CompoundTag tag = book.getTag();
						if (tag != null && tag.contains("pages")) {
							ListTag pages = tag.get("pages");
							for (int i = 0; i < pages.size(); i++) {
								StringTag page = pages.get(i);
								String value = page.getValue();
								value = ChatUtil.legacyToJson(value);
								page.setValue(value);
							}
						}
						wrapper.write(Type.ITEM, book);
					} else if (channel.equalsIgnoreCase("MC|Brand")) {
						wrapper.write(Type.VAR_INT, length);
					}
				});
			}
		});

	}
}
