package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8;

import com.google.common.base.Charsets;
import de.gerrygames.viarewind.netty.EmptyChannelHandler;
import de.gerrygames.viarewind.netty.ForwardMessageToByteEncoder;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks.ChunkPacketTransformer;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemReplacement;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider.TitleRenderProvider;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.CompressionSendStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerAbilities;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerPosition;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Scoreboard;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Windows;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.WorldBorder;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.CustomIntType;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Particle;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.chunks.BlockStorage;
import de.gerrygames.viarewind.types.VarLongType;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.CustomByteType;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Protocol1_7_6_10TO1_8 extends Protocol {

	@Override
	protected void registerPackets() {
		//Keep Alive
		this.registerOutgoing(State.PLAY, 0x00, 0x00, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);
			}
		});

		//Join Game
		this.registerOutgoing(State.PLAY, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);  //Entiy Id
				map(Type.UNSIGNED_BYTE);  //Gamemode
				map(Type.BYTE);  //Dimension
				map(Type.UNSIGNED_BYTE);  //Difficulty
				map(Type.UNSIGNED_BYTE);  //Max players
				map(Type.STRING);  //Level Type
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BOOLEAN);  //Reduced Debug Info

						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.setGamemode(packetWrapper.get(Type.UNSIGNED_BYTE, 0));
						tracker.setPlayerId(packetWrapper.get(Type.INT, 0));
						tracker.getClientEntityTypes().put(tracker.getPlayerId(), Entity1_10Types.EntityType.ENTITY_HUMAN);
					}
				});
			}
		});

		//Chat Message
		this.registerOutgoing(State.PLAY, 0x02, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);  //Chat Message
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int position = packetWrapper.read(Type.BYTE);
						if (position==2) packetWrapper.cancel();
					}
				});
			}
		});

		//Entity Equipment
		this.registerOutgoing(State.PLAY, 0x04, 0x04, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.SHORT);  //Slot
				map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM);  //Item
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
						ItemRewriter.toClient(item);
						packetWrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						if (packetWrapper.get(Type.SHORT, 0)>4) packetWrapper.cancel();
					}
				});
			}
		});

		//Spawn Position
		this.registerOutgoing(State.PLAY, 0x05, 0x05,  new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.INT, position.getY().intValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
			}
		});

		//Update Health
		this.registerOutgoing(State.PLAY, 0x06, 0x06, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);  //Health
				map(Type.VAR_INT, Type.SHORT);  //Food
				map(Type.FLOAT);  //Food Saturation
			}
		});

		//Respawn
		this.registerOutgoing(State.PLAY, 0x07, 0x07, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int gamemode = packetWrapper.get(Type.UNSIGNED_BYTE, 1);
						packetWrapper.user().get(EntityTracker.class).setGamemode(gamemode);
					}
				});
			}
		});

		//Player Position And Look
		this.registerOutgoing(State.PLAY, 0x08, 0x08, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //x
				map(Type.DOUBLE);  //y
				map(Type.DOUBLE);  //z
				map(Type.FLOAT);  //yaw
				map(Type.FLOAT);  //pitch
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
						y += 1.621;
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
					}
				});
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);
						packetWrapper.write(Type.BOOLEAN, playerPosition.isOnGround());
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						if (tracker.getSpectating()!=tracker.getPlayerId()) {
							packetWrapper.cancel();
						}
					}
				});
			}
		});

		//Use Bed
		this.registerOutgoing(State.PLAY, 0x0A, 0x0A, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.UNSIGNED_BYTE, position.getY().shortValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
			}
		});

		//Spawn Player
		this.registerOutgoing(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						UUID uuid = packetWrapper.read(Type.UUID);
						packetWrapper.write(Type.STRING, uuid.toString());

						GameProfileStorage gameProfileStorage = packetWrapper.user().get(GameProfileStorage.class);

						GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
						if (gameProfile==null) {
							packetWrapper.write(Type.STRING, "");
							packetWrapper.write(Type.VAR_INT, 0);
						} else {
							packetWrapper.write(Type.STRING, gameProfile.name.length()>16 ? gameProfile.name.substring(0, 16) : gameProfile.name);
							packetWrapper.write(Type.VAR_INT, gameProfile.properties.size());
							for (GameProfileStorage.Property property : gameProfile.properties) {
								packetWrapper.write(Type.STRING, property.name);
								packetWrapper.write(Type.STRING, property.value);
								packetWrapper.write(Type.STRING, property.signature==null ? "" : property.signature);
							}
						}
					}
				});
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						List<Metadata> metadata = packetWrapper.get(Types1_7_6_10.METADATA_LIST, 0);  //Metadata
						MetadataRewriter.transform(Entity1_10Types.EntityType.PLAYER, metadata);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityID = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityID, Entity1_10Types.EntityType.PLAYER);
						tracker.sendMetadataBuffer(entityID);
					}
				});
			}
		});

		//Collect Item
		this.registerOutgoing(State.PLAY, 0x0D, 0x0D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Collected Entity ID
				map(Type.VAR_INT, Type.INT);  //Collector Entity ID
			}
		});

		//Spawn Object
		this.registerOutgoing(State.PLAY, 0x0E, 0x0E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						final int typeID = packetWrapper.get(Type.BYTE, 0);
						if (typeID==71) {
							int data = packetWrapper.get(Type.INT, 3);
							int x = packetWrapper.get(Type.INT, 0);
							int z = packetWrapper.get(Type.INT, 2);
							byte yaw = packetWrapper.get(Type.BYTE, 2);

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

							packetWrapper.set(Type.INT, 3, data);
							packetWrapper.set(Type.INT, 0, x);
							packetWrapper.set(Type.INT, 2, z);
							packetWrapper.set(Type.BYTE, 2, yaw);
						} else if (typeID==78) {
							packetWrapper.cancel();
							PacketWrapper spawnMob = new PacketWrapper(0x0F, null, packetWrapper.user());
							spawnMob.write(Type.VAR_INT, packetWrapper.get(Type.VAR_INT, 0));
							spawnMob.write(Type.UNSIGNED_BYTE, (short)51);
							spawnMob.write(Type.INT, packetWrapper.get(Type.INT, 0));
							spawnMob.write(Type.INT, packetWrapper.get(Type.INT, 1));
							spawnMob.write(Type.INT, packetWrapper.get(Type.INT, 2));
							spawnMob.write(Type.BYTE, packetWrapper.get(Type.BYTE, 2));
							spawnMob.write(Type.BYTE, packetWrapper.get(Type.BYTE, 1));
							spawnMob.write(Type.BYTE, (byte) 0);
							short vX = 0, vY = 0, vZ = 0;
							try {
								vX = packetWrapper.read(Type.SHORT);
								vY = packetWrapper.read(Type.SHORT);
								vZ = packetWrapper.read(Type.SHORT);
							} catch (Exception ignored) {}
							spawnMob.write(Type.SHORT, vX);
							spawnMob.write(Type.SHORT, vY);
							spawnMob.write(Type.SHORT, vZ);
							spawnMob.write(Types1_8.METADATA_LIST, new ArrayList<>());
							spawnMob.send(Protocol1_7_6_10TO1_8.class, false, true);
						}

					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(final PacketWrapper packetWrapper) throws Exception {
						final int entityID = packetWrapper.get(Type.VAR_INT, 0);
						final int typeID = packetWrapper.get(Type.BYTE, 0);
						if (typeID==78) return;
						final EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						final Entity1_10Types.EntityType type = Entity1_10Types.getTypeFromId(typeID, true);
						tracker.getClientEntityTypes().put(entityID, type);
						tracker.sendMetadataBuffer(entityID);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						if (packetWrapper.isCancelled()) return;
						int data = packetWrapper.get(Type.INT, 3);
						short vX = 0, vY = 0, vZ = 0;
						try {
							vX = packetWrapper.read(Type.SHORT);
							vY = packetWrapper.read(Type.SHORT);
							vZ = packetWrapper.read(Type.SHORT);
						} catch (Exception ignored) {}
						if (data!=0) {
							packetWrapper.write(Type.SHORT, vX);
							packetWrapper.write(Type.SHORT, vY);
							packetWrapper.write(Type.SHORT, vZ);
						} else if (vX!=0) {
							PacketWrapper velocity = new PacketWrapper(0x12, null, packetWrapper.user());
							velocity.write(Type.INT, packetWrapper.get(Type.VAR_INT, 0));
							velocity.write(Type.SHORT, vX);
							velocity.write(Type.SHORT, vY);
							velocity.write(Type.SHORT, vZ);
							velocity.send(Protocol1_7_6_10TO1_8.class);
						}
					}
				});
			}
		});

		//Spawn Mob
		this.registerOutgoing(State.PLAY, 0x0F, 0x0F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.UNSIGNED_BYTE);
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.SHORT);
				map(Type.SHORT);
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityID = packetWrapper.get(Type.VAR_INT, 0);
						int typeID = packetWrapper.get(Type.UNSIGNED_BYTE, 0) & 0xff;
						if (typeID==67 || typeID==68 || typeID==101 || typeID==30 || typeID==255) {
							packetWrapper.cancel();
							return;
						}
						if (typeID==78) {
							typeID = 51;
							packetWrapper.set(Type.UNSIGNED_BYTE, 0, (short)51);
						}
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityID, Entity1_10Types.getTypeFromId(typeID, false));
						tracker.sendMetadataBuffer(entityID);
					}
				});
				handler(new PacketHandler() {
					public void handle(PacketWrapper wrapper) throws Exception {
						List<Metadata> metadataList = wrapper.get(Types1_7_6_10.METADATA_LIST, 0);
						int entityID = wrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						if (tracker.getClientEntityTypes().containsKey(entityID)) {
							MetadataRewriter.transform(tracker.getClientEntityTypes().get(entityID), metadataList);
						} else {
							wrapper.cancel();
						}
					}
				});

			}
		});

		//Spawn Painting
		this.registerOutgoing(State.PLAY, 0x10, 0x10, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);  //Entity Id
				map(Type.STRING);  //Title
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.INT, position.getY().intValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
				map(Type.UNSIGNED_BYTE, Type.INT);  //Rotation
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityID = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityID, Entity1_10Types.EntityType.PAINTING);
						tracker.sendMetadataBuffer(entityID);
					}
				});
			}
		});

		//Spawn Experience Orb
		this.registerOutgoing(State.PLAY, 0x11, 0x11, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.SHORT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityID = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityID, Entity1_10Types.EntityType.EXPERIENCE_ORB);
						tracker.sendMetadataBuffer(entityID);
					}
				});
			}
		});

		//Entity Velocity
		this.registerOutgoing(State.PLAY, 0x12, 0x12, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.SHORT);  //velX
				map(Type.SHORT);  //velY
				map(Type.SHORT);  //velZ
			}
		});

		//Destroy Entities
		this.registerOutgoing(State.PLAY, 0x13, 0x13, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Integer[] entityIds = packetWrapper.read(Type.VAR_INT_ARRAY);

						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						for (int entityId : entityIds) tracker.removeEntity(entityId);

						while (entityIds.length > 127) {
							Integer[] entityIds2 = new Integer[127];
							System.arraycopy(entityIds, 0, entityIds2, 0, 127);
							Integer[] temp = new Integer[entityIds.length-127];
							System.arraycopy(entityIds, 127, temp, 0, temp.length);
							entityIds = temp;

							PacketWrapper destroy = new PacketWrapper(0x13, null, packetWrapper.user());
							destroy.write(Type.BYTE, (byte)127);
							CustomIntType customIntType = new CustomIntType(127);
							destroy.write(customIntType, entityIds2);
							destroy.send(Protocol1_7_6_10TO1_8.class);
						}

						packetWrapper.write(Type.BYTE, ((Integer)entityIds.length).byteValue());

						CustomIntType customIntType = new CustomIntType(entityIds.length);
						packetWrapper.write(customIntType, entityIds);
					}
				});  //Entity Id Array
			}
		});

		//Entity
		this.registerOutgoing(State.PLAY, 0x14, 0x14, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
			}
		});

		//Entity Relative Move
		this.registerOutgoing(State.PLAY, 0x15, 0x15, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //x
				map(Type.BYTE);  //y
				map(Type.BYTE);  //z
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BOOLEAN);
					}
				});
			}
		});

		//Entity Look
		this.registerOutgoing(State.PLAY, 0x16, 0x16, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //yaw
				map(Type.BYTE);  //pitch
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BOOLEAN);
					}
				});
			}
		});

		//Entity Look and Relative Move
		this.registerOutgoing(State.PLAY, 0x17, 0x17, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //x
				map(Type.BYTE);  //y
				map(Type.BYTE);  //z
				map(Type.BYTE);  //yaw
				map(Type.BYTE);  //pitch
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BOOLEAN);
					}
				});
			}
		});

		//Entity Teleport
		this.registerOutgoing(State.PLAY, 0x18, 0x18, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.INT);  //x
				map(Type.INT);  //y
				map(Type.INT);  //z
				map(Type.BYTE);  //yaw
				map(Type.BYTE);  //pitch
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BOOLEAN);
					}
				});
			}
		});

		//Entity Head Look
		this.registerOutgoing(State.PLAY, 0x19, 0x19, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Head yaw
			}
		});

		//Attach Entity
		this.registerOutgoing(State.PLAY, 0x1B, 0x1B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						boolean leash = packetWrapper.get(Type.BOOLEAN, 0);
						if (leash) return;
						int passenger = packetWrapper.get(Type.INT, 0);
						int vehicle = packetWrapper.get(Type.INT, 1);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.setPassenger(vehicle, passenger);
					}
				});
			}
		});

		//Entity Metadata
		this.registerOutgoing(State.PLAY, 0x1C, 0x1C, new PacketRemapper() {
			@Override
			public void registerMap() {
				//TODO sneak/unsneak
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST);  //Metadata
				handler(new PacketHandler() {
					public void handle(PacketWrapper wrapper) throws Exception {
						List<Metadata> metadataList = wrapper.get(Types1_7_6_10.METADATA_LIST, 0);
						int entityID = wrapper.get(Type.INT, 0);
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						if (tracker.getClientEntityTypes().containsKey(entityID)) {
							MetadataRewriter.transform(tracker.getClientEntityTypes().get(entityID), metadataList);
							if (metadataList.isEmpty()) wrapper.cancel();
						} else {
							tracker.addMetadataToBuffer(entityID, metadataList);
							wrapper.cancel();
						}
					}
				});
			}
		});

		//Entity Effect
		this.registerOutgoing(State.PLAY, 0x1D, 0x1D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Effect Id
				map(Type.BYTE);  //Amplifier
				map(Type.VAR_INT, Type.SHORT);  //Duration
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BYTE);  //Hide Particles
					}
				});
			}
		});

		//Remove Entity Effect
		this.registerOutgoing(State.PLAY, 0x1E, 0x1E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Effect Id
			}
		});

		//Set Experience
		this.registerOutgoing(State.PLAY, 0x1F, 0x1F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);  //Experience bar
				map(Type.VAR_INT, Type.SHORT);  //Level
				map(Type.VAR_INT, Type.SHORT);  //Total Experience
			}
		});

		//Entity Properties
		this.registerOutgoing(State.PLAY, 0x20, 0x20, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int amount = packetWrapper.passthrough(Type.INT);
						for (int i = 0; i<amount; i++) {
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.DOUBLE);
							int modifierlength = packetWrapper.read(Type.VAR_INT);
							packetWrapper.write(Type.SHORT, (short)modifierlength);
							for (int j = 0; j<modifierlength; j++) {
								packetWrapper.passthrough(Type.UUID);
								packetWrapper.passthrough(Type.DOUBLE);
								packetWrapper.passthrough(Type.BYTE);
							}
						}
					}
				});

			}
		});

		//Chunk Data
		this.registerOutgoing(State.PLAY, 0x21, 0x21, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ChunkPacketTransformer.transformChunk(packetWrapper);
					}
				});
			}
		});

		//Multi Block Change
		this.registerOutgoing(State.PLAY, 0x22, 0x22, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ChunkPacketTransformer.transformMultiBlockChange(packetWrapper);
					}
				});
			}
		});

		//Block Change
		this.registerOutgoing(State.PLAY, 0x23, 0x23, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.UNSIGNED_BYTE, position.getY().shortValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int data = packetWrapper.read(Type.VAR_INT);

						int blockId = data >> 4;
						int meta = data & 0xF;

						BlockStorage.BlockState state = ItemReplacement.replaceBlock(new BlockStorage.BlockState(blockId, meta));

						blockId = state.getId();
						meta = state.getData();

						packetWrapper.write(Type.VAR_INT, blockId);
						packetWrapper.write(Type.UNSIGNED_BYTE, (short)meta);
					}
				});  //Block Data
			}
		});

		//Block Action
		this.registerOutgoing(State.PLAY, 0x24, 0x24, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.SHORT, position.getY().shortValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.VAR_INT);
			}
		});

		//Block Break Animation
		this.registerOutgoing(State.PLAY, 0x25, 0x25, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);  //Entity Id
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.INT, position.getY().intValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
				map(Type.BYTE);  //Progress
			}
		});

		//Map Chunk Bulk
		this.registerOutgoing(State.PLAY, 0x26, 0x26, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ChunkPacketTransformer.transformChunkBulk(packetWrapper);
					}
				});
			}
		});

		//Effect
		this.registerOutgoing(State.PLAY, 0x28, 0x28, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.BYTE, position.getY().byteValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
				map(Type.INT);
				map(Type.BOOLEAN);
			}
		});

		//Particle
		this.registerOutgoing(State.PLAY, 0x2A, 0x2A, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int particleId = packetWrapper.read(Type.INT);
						Particle particle = Particle.values()[particleId];
						if (particle == null) particle = Particle.CRIT;
						packetWrapper.write(Type.STRING, particle.name);

						packetWrapper.read(Type.BOOLEAN);
					}
				});
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String name = packetWrapper.get(Type.STRING, 0);
						Particle particle = Particle.find(name);

						for(int i = 0; i < particle.extra; ++i) {
							int extra = packetWrapper.read(Type.VAR_INT);
							//TODO
						}
					}
				});
			}
		});

		//Change Game State
		this.registerOutgoing(State.PLAY, 0x2B, 0x2B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.FLOAT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int mode = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
						if (mode==3) {
							int gamemode = packetWrapper.get(Type.FLOAT, 0).intValue();
							packetWrapper.user().get(EntityTracker.class).setGamemode(gamemode);
						}
					}
				});
			}
		});

		//Spawn Global Entity
		this.registerOutgoing(State.PLAY, 0x2C, 0x2C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityID = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityID, Entity1_10Types.EntityType.LIGHTNING);
						tracker.sendMetadataBuffer(entityID);
					}
				});
			}
		});

		//Open Window
		this.registerOutgoing(State.PLAY, 0x2D, 0x2D, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowId = packetWrapper.passthrough(Type.UNSIGNED_BYTE);
						String windowType = packetWrapper.read(Type.STRING);
						short windowTypeId = (short)Windows.getInventoryType(windowType);
						packetWrapper.write(Type.UNSIGNED_BYTE, windowTypeId);
						packetWrapper.user().get(Windows.class).types.put(windowId, windowTypeId);
						String title = packetWrapper.read(Type.STRING);  //Title
						boolean useProvidedWindowTitle = title.startsWith("\"");  //Use provided window title
						if (useProvidedWindowTitle) {
							title = title.substring(1, title.length()-1);
						} else {
							title = ChatUtil.jsonToLegacy(title);
						}
						if (title.length()>32) {
							title = title.substring(0, 32);
						}
						packetWrapper.write(Type.STRING, title);  //Window title
						packetWrapper.passthrough(Type.UNSIGNED_BYTE);
						packetWrapper.write(Type.BOOLEAN, useProvidedWindowTitle);
						if (windowTypeId==11) packetWrapper.passthrough(Type.INT);  //Entity Id
					}
				});
			}
		});

		//Set Slot
		this.registerOutgoing(State.PLAY, 0x2F, 0x2F, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowId = packetWrapper.read(Type.BYTE);  //Window Id
						short windowType = packetWrapper.user().get(Windows.class).get(windowId);
						packetWrapper.write(Type.BYTE, (byte)windowId);
						short slot = packetWrapper.read(Type.SHORT);
						if (windowType==4 && slot>=2) slot -= 1;
						packetWrapper.write(Type.SHORT, slot);  //Slot
					}
				});
				map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM);  //Item
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
						ItemRewriter.toClient(item);
						packetWrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
					}
				});
			}
		});

		//Window Items
		this.registerOutgoing(State.PLAY, 0x30, 0x30, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowId = packetWrapper.read(Type.UNSIGNED_BYTE);  //Window Id
						short windowType = packetWrapper.user().get(Windows.class).get(windowId);
						packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
						Item[] items = packetWrapper.read(Type.ITEM_ARRAY);
						if (windowType==4) {
							Item[] old = items;
							items = new Item[old.length - 1];
							items[0] = old[0];
							System.arraycopy(old, 2, items, 1, old.length - 3);
						}
						for (int i = 0; i<items.length; i++) items[i] = ItemRewriter.toClient(items[i]);
						packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, items);  //Items
					}
				});
			}
		});

		this.registerOutgoing(State.PLAY, 0x32, 0x32, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.BOOLEAN);
			}
		});

		//Update Sign
		this.registerOutgoing(State.PLAY, 0x33, 0x33, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.SHORT, position.getY().shortValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						for (int i = 0; i<4; i++) {
							String line = packetWrapper.read(Type.STRING);
							line = ChatUtil.jsonToLegacy(line);
							line = ChatUtil.removeUnusedColor(line);
							if (line.length()>15) {
								line = ChatColor.stripColor(line);
								if (line.length()>15) line = line.substring(0, 15);
							}
							packetWrapper.write(Type.STRING, line);
						}
					}
				});
			}
		});

		//Map
		this.registerOutgoing(State.PLAY, 0x34, 0x34, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
						int id = packetWrapper.read(Type.VAR_INT);
						byte scale = packetWrapper.read(Type.BYTE);

						int count = packetWrapper.read(Type.VAR_INT);
						byte[] icons = new byte[count*4];
						for (int i = 0; i < count; i++) {
							int j = packetWrapper.read(Type.BYTE);
							icons[i*4] = (byte)(j >> 4 & 0xF);
							icons[i*4+1] = packetWrapper.read(Type.BYTE);
							icons[i*4+2] = packetWrapper.read(Type.BYTE);
							icons[i*4+3] = (byte)(j & 0xF);
						}
						short columns = packetWrapper.read(Type.UNSIGNED_BYTE);
						if (columns > 0) {
							short rows = packetWrapper.read(Type.UNSIGNED_BYTE);
							byte x = packetWrapper.read(Type.BYTE);
							byte z = packetWrapper.read(Type.BYTE);
							Byte[] data = packetWrapper.read(Type.BYTE_ARRAY);

							for (int column = 0; column<columns; column++) {
								byte[] columnData = new byte[rows + 3];
								columnData[0] = 0;
								columnData[1] = (byte) (x + column);
								columnData[2] = z;

								for (int i = 0; i<rows; i++) {
									columnData[i+3] = data[column + i * columns];
								}

								PacketWrapper columnUpdate = new PacketWrapper(0x34, null, packetWrapper.user());
								columnUpdate.write(Type.VAR_INT, id);
								columnUpdate.write(Type.SHORT, (short)columnData.length);
								columnUpdate.write(new CustomByteType(columnData.length), columnData);

								//packetWrapper.user().get(MapPacketCache.class).add(columnUpdate);
								columnUpdate.send(Protocol1_7_6_10TO1_8.class, true, true);
							}
						}

						if (count>0) {
							byte[] iconData = new byte[count*3+1];
							iconData[0] = 1;
							for (int i = 0; i<count; i++) {
								iconData[i*3+1] = (byte)(icons[i*4] << 4 | icons[i*4+3] & 0xF);
								iconData[i*3+2] = icons[i*4+1];
								iconData[i*3+3] = icons[i*4+2];
							}
							PacketWrapper iconUpdate = new PacketWrapper(0x34, null, packetWrapper.user());
							iconUpdate.write(Type.VAR_INT, id);
							iconUpdate.write(Type.SHORT, (short)iconData.length);
							CustomByteType customByteType = new CustomByteType(iconData.length);
							iconUpdate.write(customByteType, iconData);
							iconUpdate.send(Protocol1_7_6_10TO1_8.class, true, true);
						}

						PacketWrapper scaleUpdate = new PacketWrapper(0x34, null, packetWrapper.user());
						scaleUpdate.write(Type.VAR_INT, id);
						scaleUpdate.write(Type.SHORT, (short)2);
						scaleUpdate.write(new CustomByteType(2), new byte[] {2, scale});
						scaleUpdate.send(Protocol1_7_6_10TO1_8.class, true, true);
					}
				});
			}
		});

		//Update Block Entity
		this.registerOutgoing(State.PLAY, 0x35, 0x35, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.SHORT, position.getY().shortValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
				map(Type.UNSIGNED_BYTE);  //Action
				map(Type.NBT, Types1_7_6_10.COMPRESSED_NBT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						if (packetWrapper.get(Type.UNSIGNED_BYTE, 0)==2) return;
						CompoundTag nbt = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT, 0);
						Utils.iterateCompountTagRecursive(nbt, tag -> {
							if (tag instanceof StringTag) {
								String value = (String) tag.getValue();
								value = ChatUtil.jsonToLegacy(value);
								value = ChatUtil.removeUnusedColor(value);
								((StringTag) tag).setValue(value);
							}
						});
					}
				});
			}
		});

		//Open Sign Editor
		this.registerOutgoing(State.PLAY, 0x36, 0x36, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX().intValue());
						packetWrapper.write(Type.INT, position.getY().intValue());
						packetWrapper.write(Type.INT, position.getZ().intValue());
					}
				});
			}
		});

		//Player List Item
		this.registerOutgoing(State.PLAY, 0x38, 0x38, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
						int action = packetWrapper.read(Type.VAR_INT);
						if (action==1) return;
						int count = packetWrapper.read(Type.VAR_INT);
						GameProfileStorage gameProfileStorage = packetWrapper.user().get(GameProfileStorage.class);
						for (int i = 0; i<count; i++) {
							UUID uuid = packetWrapper.read(Type.UUID);
							if (action==0) {
								String name = packetWrapper.read(Type.STRING);

								GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
								if (gameProfile==null) gameProfile = gameProfileStorage.put(uuid, name);

								int propertyCount = packetWrapper.read(Type.VAR_INT);
								while (propertyCount-->0) {
									gameProfile.properties.add(new GameProfileStorage.Property(packetWrapper.read(Type.STRING), packetWrapper.read(Type.STRING),
											packetWrapper.read(Type.BOOLEAN) ? packetWrapper.read(Type.STRING) : null));
								}
								packetWrapper.read(Type.VAR_INT);  //Gamemode
								int ping = packetWrapper.read(Type.VAR_INT);
								gameProfile.ping = ping;
								if (packetWrapper.read(Type.BOOLEAN)) {
									gameProfile.setDisplayName(packetWrapper.read(Type.STRING));
								}

								PacketWrapper packet = new PacketWrapper(0x38, null, packetWrapper.user());
								//packet.write(Type.STRING, gameProfile.getDisplayName());
								packet.write(Type.STRING, gameProfile.name);
								packet.write(Type.BOOLEAN, true);
								packet.write(Type.SHORT, (short) ping);
								packet.send(Protocol1_7_6_10TO1_8.class);
							} else if (action==2) {
								int ping = packetWrapper.read(Type.VAR_INT);

								GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
								if (gameProfile==null) continue;

								gameProfile.ping = ping;

								PacketWrapper packet = new PacketWrapper(0x38, null, packetWrapper.user());
								//packet.write(Type.STRING, gameProfile.getDisplayName());
								packet.write(Type.STRING, gameProfile.name);
								packet.write(Type.BOOLEAN, true);
								packet.write(Type.SHORT, (short) ping);
								packet.send(Protocol1_7_6_10TO1_8.class);
							} else if (action==3) {
								String displayName = packetWrapper.read(Type.BOOLEAN) ? packetWrapper.read(Type.STRING) : null;
								if (displayName!=null) displayName = GameProfileStorage.GameProfile.fixDisplayName(displayName);

								GameProfileStorage.GameProfile gameProfile = gameProfileStorage.get(uuid);
								if (gameProfile==null || gameProfile.displayName==null && displayName==null) continue;

								if (gameProfile.displayName==null && displayName!=null || gameProfile.displayName!=null && displayName==null || !gameProfile.displayName.equals(displayName)) {
									gameProfile.setDisplayName(displayName);
								}
							} else if (action==4) {
								GameProfileStorage.GameProfile gameProfile = gameProfileStorage.remove(uuid);
								if (gameProfile==null) continue;

								PacketWrapper packet = new PacketWrapper(0x38, null, packetWrapper.user());
								//packet.write(Type.STRING, gameProfile.getDisplayName());
								packet.write(Type.STRING, gameProfile.name);
								packet.write(Type.BOOLEAN, false);
								packet.write(Type.SHORT, (short) gameProfile.ping);
								packet.send(Protocol1_7_6_10TO1_8.class);
							}
						}
					}
				});
			}
		});

		//Player Abilities
		this.registerOutgoing(State.PLAY, 0x39, 0x39, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
					}
				});
			}
		});

		//Scoreboard Objective
		this.registerOutgoing(State.PLAY, 0x3B, 0x3B, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String name = packetWrapper.passthrough(Type.STRING);
						if (name.length()>16) {
							packetWrapper.set(Type.STRING, 0, name.substring(0, 16));
						}
						byte mode = packetWrapper.read(Type.BYTE);

						if (mode==0 || mode==2) {
							String displayName = packetWrapper.passthrough(Type.STRING);
							if (displayName.length()>32) {
								packetWrapper.set(Type.STRING, 1, displayName.substring(0, 32));
							}
							packetWrapper.read(Type.STRING);
						} else {
							packetWrapper.write(Type.STRING, "");
						}
						packetWrapper.write(Type.BYTE, mode);
					}
				});
			}
		});

		//Update Score
		this.registerOutgoing(State.PLAY, 0x3C, 0x3C, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String name = packetWrapper.passthrough(Type.STRING);
						if (name.length()>16) {
							name = ChatColor.stripColor(name);
							if (name.length()>16) {
								name = name.substring(0, 16);
							}
							packetWrapper.set(Type.STRING, 0, name);
						}
						byte mode = packetWrapper.passthrough(Type.BYTE);
						String objective = packetWrapper.read(Type.STRING);
						if (objective.length()>16) {
							objective = objective.substring(0, 16);
						}

						if (mode!=1) {
							int score = packetWrapper.read(Type.VAR_INT);
							packetWrapper.write(Type.STRING, objective);
							packetWrapper.write(Type.INT, score);
						}
					}
				});
			}
		});

		//Scoreboard Teams
		this.registerOutgoing(State.PLAY, 0x3E, 0x3E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String team = packetWrapper.get(Type.STRING, 0);
						if (team==null) {
							packetWrapper.cancel();
							return;
						}
						byte mode = packetWrapper.passthrough(Type.BYTE);

						Scoreboard scoreboard = packetWrapper.user().get(Scoreboard.class);

						if (mode!=0 && !scoreboard.teamExists(team)) {
							packetWrapper.cancel();
							return;
						} else if (mode==0 && scoreboard.teamExists(team)) {
							scoreboard.removeTeam(team);

							PacketWrapper remove = new PacketWrapper(0x3E, null, packetWrapper.user());
							remove.write(Type.STRING, team);
							remove.write(Type.BYTE, (byte)1);
							remove.send(Protocol1_7_6_10TO1_8.class, true, true);
						}

						if (mode==0) scoreboard.addTeam(team);
						else if (mode==1) scoreboard.removeTeam(team);

						if (mode==0 || mode==2) {
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.BYTE);
							packetWrapper.read(Type.STRING);
							packetWrapper.read(Type.BYTE);
						}
						if (mode==0 || mode==3 || mode==4 || mode==5) {
							int size = packetWrapper.read(Type.VAR_INT);
							List<String> entryList = new ArrayList<>();

							for (int i = 0; i<size; i++) {
								String entry = packetWrapper.read(Type.STRING);
								if (entry==null) continue;
								if (mode == 4) {
									if (!scoreboard.isPlayerInTeam(entry, team)) continue;
									scoreboard.removePlayerFromTeam(entry, team);
								} else {
									scoreboard.addPlayerToTeam(entry, team);
								}
								entryList.add(entry);
							}

							packetWrapper.write(Type.SHORT, (short)entryList.size());
							for (String entry : entryList) {
								packetWrapper.write(Type.STRING, entry);
							}
						}
					}
				});
			}
		});

		//Custom Payload
		this.registerOutgoing(State.PLAY, 0x3F, 0x3F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String channel = packetWrapper.get(Type.STRING, 0);
						if (channel.equalsIgnoreCase("MC|TrList")) {
							packetWrapper.write(Type.SHORT, (short)0);  //Size Placeholder

							ByteBuf buf = Unpooled.buffer();

							Type.INT.write(buf, packetWrapper.passthrough(Type.INT));  //Window Id

							int size = packetWrapper.passthrough(Type.BYTE);  //Size
							Type.BYTE.write(buf, (byte)size);

							for (int i = 0; i < size; i++) {
								Item item = ItemRewriter.toClient(packetWrapper.read(Type.ITEM));
								packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 1
								Types1_7_6_10.COMPRESSED_NBT_ITEM.write(buf, item);

								item = ItemRewriter.toClient(packetWrapper.read(Type.ITEM));
								packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 3
								Types1_7_6_10.COMPRESSED_NBT_ITEM.write(buf, item);

								boolean has3Items = packetWrapper.passthrough(Type.BOOLEAN);
								Type.BOOLEAN.write(buf, has3Items);
								if (has3Items) {
									item = ItemRewriter.toClient(packetWrapper.read(Type.ITEM));
									packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item); //Buy Item 2
									Types1_7_6_10.COMPRESSED_NBT_ITEM.write(buf, item);
								}

								Type.BOOLEAN.write(buf, packetWrapper.passthrough(Type.BOOLEAN)); //Unavailable
								packetWrapper.read(Type.INT); //Uses
								packetWrapper.read(Type.INT); //Max Uses
							}

							packetWrapper.set(Type.SHORT, 0, (short)buf.readableBytes());
							buf.release();
						} else {
							byte[] data = packetWrapper.read(Type.REMAINING_BYTES);
							packetWrapper.write(Type.SHORT, (short)data.length);
							packetWrapper.write(Type.REMAINING_BYTES, data);
						}
					}
				});
			}
		});

		//Server Difficulty
		this.registerOutgoing(State.PLAY, 0x41, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
					}
				});
			}
		});

		//Combat Event
		this.registerOutgoing(State.PLAY, 0x42, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
					}
				});
			}
		});

		//Camera
		this.registerOutgoing(State.PLAY, 0x43, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();

						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);

						int entityId = packetWrapper.read(Type.VAR_INT);
						int spectating = tracker.getSpectating();

						if (spectating!=entityId) {
							tracker.setSpectating(entityId);
						}
					}
				});
			}
		});

		//World Border
		this.registerOutgoing(State.PLAY, 0x44, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int action = packetWrapper.read(Type.VAR_INT);
						WorldBorder worldBorder = packetWrapper.user().get(WorldBorder.class);
						if (action==0) {
							worldBorder.setSize(packetWrapper.read(Type.DOUBLE));
						} else if (action==1) {
							worldBorder.lerpSize(
									packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(VarLongType.VAR_LONG)
							);
						} else if (action==2) {
							worldBorder.setCenter(
									packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(Type.DOUBLE)
							);
						} else if (action==3) {
							worldBorder.init(
									packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(VarLongType.VAR_LONG),
									packetWrapper.read(Type.VAR_INT),
									packetWrapper.read(Type.VAR_INT),
									packetWrapper.read(Type.VAR_INT)
							);
						} else if (action==4) {
							worldBorder.setWarningTime(packetWrapper.read(Type.VAR_INT));
						} else if (action==5) {
							worldBorder.setWarningBlocks(packetWrapper.read(Type.VAR_INT));
						}

						packetWrapper.cancel();
					}
				});
			}
		});

		//Title
		this.registerOutgoing(State.PLAY, 0x45, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
						TitleRenderProvider titleRenderProvider = Via.getManager().getProviders().get(TitleRenderProvider.class);
						if (titleRenderProvider==null) return;
						int action = packetWrapper.read(Type.VAR_INT);
						switch (action) {
							case 0:
								titleRenderProvider.setTitle(packetWrapper.user(), packetWrapper.read(Type.STRING));
								break;
							case 1:
								titleRenderProvider.setSubTitle(packetWrapper.user(), packetWrapper.read(Type.STRING));
								break;
							case 2:
								titleRenderProvider.setTimings(packetWrapper.user(), packetWrapper.read(Type.INT), packetWrapper.read(Type.INT), packetWrapper.read(Type.INT));
								break;
							case 3:
								titleRenderProvider.hide(packetWrapper.user());
								break;
							case 4:
								titleRenderProvider.reset(packetWrapper.user());
								break;
						}
					}
				});
			}
		});

		//Set Compression
		this.registerOutgoing(State.PLAY, 0x46, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
					}
				});
			}
		});

		//Player List Header And Footer
		this.registerOutgoing(State.PLAY, 0x47, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
					}
				});
			}
		});

		//Resource Pack Send
		this.registerOutgoing(State.PLAY, 0x48, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
					}
				});
			}
		});

		//Update Entity NBT
		this.registerOutgoing(State.PLAY, 0x49, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
					}
				});
			}
		});

		//Keep Alive
		this.registerIncoming(State.PLAY, 0x00, 0x00, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);
			}
		});

		//Chat Message
		this.registerIncoming(State.PLAY, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String msg = packetWrapper.get(Type.STRING, 0);
						int gamemode = packetWrapper.user().get(EntityTracker.class).getGamemode();
						if (gamemode==3 && msg.toLowerCase().startsWith("/stp ")) {
							String username = msg.split(" ")[1];
							GameProfileStorage storage = packetWrapper.user().get(GameProfileStorage.class);
							GameProfileStorage.GameProfile profile = storage.get(username, true);
							if (profile!=null && profile.uuid!=null) {
								packetWrapper.cancel();

								PacketWrapper teleportPacket = new PacketWrapper(0x18, null, packetWrapper.user());
								teleportPacket.write(Type.UUID, profile.uuid);

								PacketUtil.sendToServer(teleportPacket, Protocol1_7_6_10TO1_8.class, true, true);
							}
						}
					}
				});
			}
		});

		//Use Entity
		this.registerIncoming(State.PLAY, 0x02, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);
				map(Type.BYTE, Type.VAR_INT);
			}
		});

		//Player
		this.registerIncoming(State.PLAY, 0x03, 0x03, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);
						playerPosition.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
					}
				});
			}
		});

		//Player Position
		this.registerIncoming(State.PLAY, 0x04, 0x04, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.DOUBLE);
					}
				});
				map(Type.DOUBLE);  //Z
				map(Type.BOOLEAN);  //OnGround
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						double x = packetWrapper.get(Type.DOUBLE, 0);
						double feetY = packetWrapper.get(Type.DOUBLE, 1);
						double z = packetWrapper.get(Type.DOUBLE, 2);

						PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);

						if (playerPosition.isPositionPacketReceived()) {
							playerPosition.setPositionPacketReceived(false);
							feetY -= 0.01;
							packetWrapper.set(Type.DOUBLE, 1, feetY);
						}

						playerPosition.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
						playerPosition.setPos(x, feetY, z);
					}
				});
			}
		});

		//Player Position And Look
		this.registerIncoming(State.PLAY, 0x06, 0x06, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);  //X
				map(Type.DOUBLE);  //Y
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.DOUBLE);
					}
				});
				map(Type.DOUBLE);  //Z
				map(Type.FLOAT);  //Yaw
				map(Type.FLOAT);  //Pitch
				map(Type.BOOLEAN);  //OnGround
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						double x = packetWrapper.get(Type.DOUBLE, 0);
						double feetY = packetWrapper.get(Type.DOUBLE, 1);
						double z = packetWrapper.get(Type.DOUBLE, 2);

						float yaw = packetWrapper.get(Type.FLOAT, 0);
						float pitch = packetWrapper.get(Type.FLOAT, 1);

						PlayerPosition playerPosition = packetWrapper.user().get(PlayerPosition.class);

						if (playerPosition.isPositionPacketReceived()) {
							playerPosition.setPositionPacketReceived(false);
							feetY = playerPosition.getReceivedPosY();
							packetWrapper.set(Type.DOUBLE, 1, feetY);
						}

						playerPosition.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
						playerPosition.setPos(x, feetY, z);
						playerPosition.setYaw(yaw);
						playerPosition.setPitch(pitch);
					}
				});
			}
		});

		//Player Digging
		this.registerIncoming(State.PLAY, 0x07, 0x07, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);  //Status
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						long x = packetWrapper.read(Type.INT);
						long y = packetWrapper.read(Type.UNSIGNED_BYTE);
						long z = packetWrapper.read(Type.INT);
						packetWrapper.write(Type.POSITION, new Position(x, y, z));
					}
				});
				map(Type.BYTE);  //Face
			}
		});

		//Player Block Placement
		this.registerIncoming(State.PLAY, 0x08, 0x08, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						long x = packetWrapper.read(Type.INT);
						long y = packetWrapper.read(Type.UNSIGNED_BYTE);
						long z = packetWrapper.read(Type.INT);
						packetWrapper.write(Type.POSITION, new Position(x, y, z));

						packetWrapper.passthrough(Type.BYTE);  //Direction
						Item item = packetWrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
						item = ItemRewriter.toServer(item);
						packetWrapper.write(Type.ITEM, item);

						for (int i = 0; i<3; i++) {
							packetWrapper.passthrough(Type.BYTE);
						}
					}
				});
			}
		});

		//Animation
		this.registerIncoming(State.PLAY, 0x0A, 0x0A, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.read(Type.INT);
						int animation = packetWrapper.read(Type.BYTE);  //Animation
						if (animation==1) return;
						packetWrapper.cancel();
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
						PacketWrapper entityAction = new PacketWrapper(0x0B, null, packetWrapper.user());
						entityAction.write(Type.VAR_INT, entityId);
						entityAction.write(Type.VAR_INT, animation);
						entityAction.write(Type.VAR_INT, 0);
					}
				});
			}
		});

		//Entity Action
		this.registerIncoming(State.PLAY, 0x0B, 0x0B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);  //Entity Id
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.VAR_INT, packetWrapper.read(Type.BYTE)-1);
					}
				});  //Action Id
				map(Type.INT, Type.VAR_INT);  //Action Paramter
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int action = packetWrapper.get(Type.VAR_INT, 1);
						if (action==3 || action==4) {
							PlayerAbilities abilities = packetWrapper.user().get(PlayerAbilities.class);
							abilities.setSprinting(action==3);
							PacketWrapper abilitiesPacket = new PacketWrapper(0x39, null, packetWrapper.user());
							abilitiesPacket.write(Type.BYTE, abilities.getFlags());
							abilitiesPacket.write(Type.FLOAT, abilities.isSprinting() ? abilities.getFlySpeed() * 2.0f : abilities.getFlySpeed());
							abilitiesPacket.write(Type.FLOAT, abilities.getWalkSpeed());
							abilitiesPacket.send(Protocol1_7_6_10TO1_8.class, true, false);
						}
					}
				});
			}
		});

		//Steer Vehicle
		this.registerIncoming(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);  //Sideways
				map(Type.FLOAT);  //Forwards
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						boolean jump = packetWrapper.read(Type.BOOLEAN);
						boolean unmount = packetWrapper.read(Type.BOOLEAN);
						short flags = 0;
						if (jump) flags += 0x01;
						if (unmount) flags += 0x02;
						packetWrapper.write(Type.UNSIGNED_BYTE, flags);

						if (unmount) {
							EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
							if (tracker.getSpectating()!=tracker.getPlayerId()) {
								PacketWrapper sneakPacket = new PacketWrapper(0x0B, null, packetWrapper.user());
								sneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
								sneakPacket.write(Type.VAR_INT, 0);  //Start sneaking
								sneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

								PacketWrapper unsneakPacket = new PacketWrapper(0x0B, null, packetWrapper.user());
								unsneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
								unsneakPacket.write(Type.VAR_INT, 1);  //Stop sneaking
								unsneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

								PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10TO1_8.class, true, false);
								//PacketUtil.sendToServer(unsneakPacket, Protocol1_7_6_10TO1_8.class, true, false);
							}
						}
					}
				});
			}
		});

		//Click Window
		this.registerIncoming(State.PLAY, 0x0E, 0x0E, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowId = packetWrapper.read(Type.BYTE);  //Window Id
						packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
						short windowType = packetWrapper.user().get(Windows.class).get(windowId);
						short slot = packetWrapper.read(Type.SHORT);
						if (windowType==4) {
							if (slot>2) {
								slot -= 1;
							}
						}
						packetWrapper.write(Type.SHORT, slot);
					}
				});
				map(Type.BYTE);  //Button
				map(Type.SHORT);  //Action Number
				map(Type.BYTE);  //Mode
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Item item = packetWrapper.get(Type.ITEM, 0);
						ItemRewriter.toServer(item);
						packetWrapper.set(Type.ITEM, 0, item);
					}
				});
			}
		});

		//Confirm Transaction
		this.registerIncoming(State.PLAY, 0x0F, 0x0F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int action = packetWrapper.get(Type.SHORT, 0);
						if (action==-89) packetWrapper.cancel();
					}
				});
			}
		});

		//Creative Inventory Action
		this.registerIncoming(State.PLAY, 0x10, 0x10, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.SHORT);  //Slot
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);  //Item
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Item item = packetWrapper.get(Type.ITEM, 0);
						ItemRewriter.toServer(item);
						packetWrapper.set(Type.ITEM, 0, item);
					}
				});
			}
		});

		//Update Sign
		this.registerIncoming(State.PLAY, 0x12, 0x12, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						long x = packetWrapper.read(Type.INT);
						long y = packetWrapper.read(Type.SHORT);
						long z = packetWrapper.read(Type.INT);
						packetWrapper.write(Type.POSITION, new Position(x, y, z));

						packetWrapper.write(Type.STRING, "{\"text\": \"" + packetWrapper.read(Type.STRING) + "\"}");  //Line 1
						packetWrapper.write(Type.STRING, "{\"text\": \"" + packetWrapper.read(Type.STRING) + "\"}");  //Line 2
						packetWrapper.write(Type.STRING, "{\"text\": \"" + packetWrapper.read(Type.STRING) + "\"}");  //Line 3
						packetWrapper.write(Type.STRING, "{\"text\": \"" + packetWrapper.read(Type.STRING) + "\"}");  //Line 4
					}
				});
			}
		});

		//Player Abilities
		this.registerIncoming(State.PLAY, 0x13, 0x13, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						byte flags = packetWrapper.get(Type.BYTE, 0);
						PlayerAbilities abilities = packetWrapper.user().get(PlayerAbilities.class);
						abilities.setAllowFly((flags & 4) == 4);
						abilities.setFlying((flags & 2) == 2);
						packetWrapper.set(Type.FLOAT, 0, abilities.getFlySpeed());
					}
				});
			}
		});

		//Tab-Complete
		this.registerIncoming(State.PLAY, 0x14, 0x14, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.BOOLEAN, false);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String msg = packetWrapper.get(Type.STRING, 0);
						if (msg.toLowerCase().startsWith("/stp ")) {
							packetWrapper.cancel();
							String[] args = msg.split(" ");
							if (args.length<=2) {
								String prefix = args.length==1 ? "" : args[1];
								GameProfileStorage storage = packetWrapper.user().get(GameProfileStorage.class);
								List<GameProfileStorage.GameProfile> profiles = storage.getAllWithPrefix(prefix, true);

								PacketWrapper tabComplete = new PacketWrapper(0x3A, null, packetWrapper.user());
								tabComplete.write(Type.VAR_INT, profiles.size());
								for (GameProfileStorage.GameProfile profile : profiles) {
									tabComplete.write(Type.STRING, profile.name);
								}

								tabComplete.send(Protocol1_7_6_10TO1_8.class, true, false);
							}
						}
					}
				});
			}
		});

		//Client Settings
		this.registerIncoming(State.PLAY, 0x15, 0x15, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BYTE);

						boolean cape = packetWrapper.read(Type.BOOLEAN);
						packetWrapper.write(Type.UNSIGNED_BYTE, (short)(cape ? 127 : 126));
					}
				});
			}
		});

		//Custom Payload
		this.registerIncoming(State.PLAY, 0x17, 0x17, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String channel = packetWrapper.get(Type.STRING, 0);
						if (channel.equalsIgnoreCase("MC|ItemName")) {
							int length = packetWrapper.read(Type.SHORT);
							CustomByteType customByteType = new CustomByteType(length);
							byte[] data = packetWrapper.read(customByteType);
							String name = new String(data, Charsets.UTF_8);
							ByteBuf buf = Unpooled.buffer();
							Type.STRING.write(buf, name);
							data = new byte[buf.readableBytes()];
							buf.readBytes(data);
							buf.release();
							packetWrapper.write(Type.REMAINING_BYTES, data);
						} else {
							int length = packetWrapper.read(Type.SHORT);
							CustomByteType customByteType = new CustomByteType(length);
							byte[] data = packetWrapper.read(customByteType);
							packetWrapper.write(Type.REMAINING_BYTES, data);
						}
					}
				});
			}
		});

		//Disconnect
		this.registerOutgoing(State.LOGIN, 0x00, 0x00, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
			}
		});

		//Encryption Request
		this.registerOutgoing(State.LOGIN, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);  //Server ID
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int publicKeyLength = packetWrapper.read(Type.VAR_INT);
						packetWrapper.write(Type.SHORT, (short)publicKeyLength);
						for (int i = 0; i<publicKeyLength; i++) {
							packetWrapper.passthrough(Type.BYTE);
						}
						int verifyTokenLength = packetWrapper.read(Type.VAR_INT);
						packetWrapper.write(Type.SHORT, (short)verifyTokenLength);
						for (int i = 0; i<verifyTokenLength; i++) {
							packetWrapper.passthrough(Type.BYTE);
						}
					}
				});
			}
		});

		//Set Compression
		this.registerOutgoing(State.LOGIN, 0x03, 0x03, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
						packetWrapper.user().get(CompressionSendStorage.class).setCompressionSend(true);
					}
				});
			}
		});

		//Encryption Response
		this.registerIncoming(State.LOGIN, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int sharedSecretLength = packetWrapper.read(Type.SHORT);
						packetWrapper.write(Type.VAR_INT, sharedSecretLength);
						for (int i = 0; i<sharedSecretLength; i++) {
							packetWrapper.passthrough(Type.BYTE);
						}
						int verifyTokenLength = packetWrapper.read(Type.SHORT);
						packetWrapper.write(Type.VAR_INT, verifyTokenLength);
						for (int i = 0; i<verifyTokenLength; i++) {
							packetWrapper.passthrough(Type.BYTE);
						}
					}
				});
			}
		});
	}

	@Override
	public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
		CompressionSendStorage compressionSendStorage = packetWrapper.user().get(CompressionSendStorage.class);
		if (compressionSendStorage.isCompressionSend()) {
			Channel channel = packetWrapper.user().getChannel();
			channel.pipeline().replace("decompress", "decompress", new EmptyChannelHandler());
			channel.pipeline().replace("compress", "compress", new ForwardMessageToByteEncoder());

			compressionSendStorage.setCompressionSend(false);
		}

		super.transform(direction, state, packetWrapper);
	}

	@Override
	public void init(UserConnection userConnection) {
		userConnection.put(new Windows(userConnection));
		userConnection.put(new EntityTracker(userConnection));
		userConnection.put(new PlayerPosition(userConnection));
		userConnection.put(new GameProfileStorage(userConnection));
		userConnection.put(new ClientChunks(userConnection));
		userConnection.put(new Scoreboard(userConnection));
		userConnection.put(new CompressionSendStorage(userConnection));
		userConnection.put(new WorldBorder(userConnection));
		userConnection.put(new PlayerAbilities(userConnection));
	}
}
