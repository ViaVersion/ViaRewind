package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.BlockPlaceDestroyTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.BossBarStorage;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Cooldown;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.PlayerPosition;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ListTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.UUID;

public class PlayerPackets {

	public static void register(Protocol protocol) {
		/*  OUTGOING  */

		//Animation
		protocol.registerOutgoing(State.PLAY, 0x06, 0x0B);

		//Statistics
		protocol.registerOutgoing(State.PLAY, 0x07, 0x37);

		//Boss Bar
		protocol.registerOutgoing(State.PLAY, 0x0C, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();

						UUID uuid = packetWrapper.read(Type.UUID);
						int action = packetWrapper.read(Type.VAR_INT);
						BossBarStorage bossBarStorage = packetWrapper.user().get(BossBarStorage.class);
						if (action == 0) {
							bossBarStorage.add(uuid, ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT)), packetWrapper.read(Type.FLOAT));
							packetWrapper.read(Type.VAR_INT);
							packetWrapper.read(Type.VAR_INT);
							packetWrapper.read(Type.UNSIGNED_BYTE);
						} else if (action == 1) {
							bossBarStorage.remove(uuid);
						} else if (action == 2) {
							bossBarStorage.updateHealth(uuid, packetWrapper.read(Type.FLOAT));
						} else if (action == 3) {
							String title = ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT));
							bossBarStorage.updateTitle(uuid, title);
						}
					}
				});
			}
		});

		//Tab-Complete
		protocol.registerOutgoing(State.PLAY, 0x0E, 0x3A);

		//Chat Message
		protocol.registerOutgoing(State.PLAY, 0x0F, 0x02);

		//Set Cooldown
		protocol.registerOutgoing(State.PLAY, 0x17, -1, new PacketRemapper() {
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

		//Custom Payload
		protocol.registerOutgoing(State.PLAY, 0x18, 0x3F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
								packetWrapper.write(Type.ITEM, ItemRewriter.toClient(packetWrapper.read(Type.ITEM))); //Buy Item 1
								packetWrapper.write(Type.ITEM, ItemRewriter.toClient(packetWrapper.read(Type.ITEM))); //Buy Item 3

								boolean has3Items = packetWrapper.passthrough(Type.BOOLEAN);
								if (has3Items) {
									packetWrapper.write(Type.ITEM, ItemRewriter.toClient(packetWrapper.read(Type.ITEM))); //Buy Item 2
								}

								packetWrapper.passthrough(Type.BOOLEAN); //Unavailable
								packetWrapper.passthrough(Type.INT); //Uses
								packetWrapper.passthrough(Type.INT); //Max Uses
							}
						} else if (channel.equalsIgnoreCase("MC|BOpen")) {
							packetWrapper.read(Type.VAR_INT);
						}
					}
				});
			}
		});

		//Disconnect
		protocol.registerOutgoing(State.PLAY, 0x1A, 0x40);

		//Change Game State
		protocol.registerOutgoing(State.PLAY, 0x1E, 0x2B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.FLOAT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int reason = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
						if (reason == 3) packetWrapper.user().get(EntityTracker.class).setPlayerGamemode(packetWrapper.get(Type.FLOAT, 0).intValue());
					}
				});
			}
		});

		//Join Game
		protocol.registerOutgoing(State.PLAY, 0x23, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.UNSIGNED_BYTE);
				map(Type.BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.STRING);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.setPlayerId(packetWrapper.get(Type.INT, 0));
						tracker.setPlayerGamemode(packetWrapper.get(Type.UNSIGNED_BYTE, 0));
						tracker.getClientEntityTypes().put(tracker.getPlayerId(), Entity1_10Types.EntityType.ENTITY_HUMAN);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ClientWorld world = packetWrapper.user().get(ClientWorld.class);
						world.setEnvironment(packetWrapper.get(Type.BYTE, 0));
					}
				});
			}
		});

		//Open Sign Editor
		protocol.registerOutgoing(State.PLAY, 0x2A, 0x36);

		//Player Abilities
		protocol.registerOutgoing(State.PLAY, 0x2B, 0x39);

		//Player List Item
		protocol.registerOutgoing(State.PLAY, 0x2D, 0x38);

		//Player Position And Look
		protocol.registerOutgoing(State.PLAY, 0x2E, 0x08, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);

						int teleportId = packetWrapper.read(Type.VAR_INT);
						pos.setConfirmId(teleportId);

						byte flags = packetWrapper.get(Type.BYTE, 0);
						double x = packetWrapper.get(Type.DOUBLE, 0);
						double y = packetWrapper.get(Type.DOUBLE, 1);
						double z = packetWrapper.get(Type.DOUBLE, 2);
						float yaw = packetWrapper.get(Type.FLOAT, 0);
						float pitch = packetWrapper.get(Type.FLOAT, 1);

						packetWrapper.set(Type.BYTE, 0, (byte) 0);

						if (flags != 0) {
							if ((flags & 0x01) != 0) {
								x += pos.getPosX();
								packetWrapper.set(Type.DOUBLE, 0, x);
							}
							if ((flags & 0x02) != 0) {
								y += pos.getPosY();
								packetWrapper.set(Type.DOUBLE, 1, y);
							}
							if ((flags & 0x04) != 0) {
								z += pos.getPosZ();
								packetWrapper.set(Type.DOUBLE, 2, z);
							}
							if ((flags & 0x08) != 0) {
								yaw += pos.getYaw();
								packetWrapper.set(Type.FLOAT, 0, yaw);
							}
							if ((flags & 0x10) != 0) {
								pitch += pos.getPitch();
								packetWrapper.set(Type.FLOAT, 1, pitch);
							}
						}

						pos.setPos(x, y, z);
						pos.setYaw(yaw);
						pos.setPitch(pitch);
					}
				});
			}
		});

		//Resource Pack Send
		protocol.registerOutgoing(State.PLAY, 0x32, 0x48);

		//Respawn
		protocol.registerOutgoing(State.PLAY, 0x33, 0x07, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.user().get(EntityTracker.class).setPlayerGamemode(packetWrapper.get(Type.UNSIGNED_BYTE, 1));
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.user().get(BossBarStorage.class).updateLocation();
						packetWrapper.user().get(BossBarStorage.class).changeWorld();
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ClientWorld world = packetWrapper.user().get(ClientWorld.class);
						world.setEnvironment(packetWrapper.get(Type.INT, 0));
					}
				});
			}
		});

		//Camera
		protocol.registerOutgoing(State.PLAY, 0x36, 0x43);

		//Held Item Change
		protocol.registerOutgoing(State.PLAY, 0x37, 0x09);

		//Set Experience
		protocol.registerOutgoing(State.PLAY, 0x3D, 0x1F);

		//Update Health
		protocol.registerOutgoing(State.PLAY, 0x3E, 0x06);

		//Spawn Position
		protocol.registerOutgoing(State.PLAY, 0x43, 0x05);

		//Title
		protocol.registerOutgoing(State.PLAY, 0x45, 0x45);

		//Player List Header And Footer
		protocol.registerOutgoing(State.PLAY, 0x48, 0x47);

		/*  INCMOING  */

		//Chat Message
		protocol.registerIncoming(State.PLAY, 0x02, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String msg = packetWrapper.get(Type.STRING, 0);
						if (msg.toLowerCase().startsWith("/offhand")) {
							packetWrapper.cancel();
							PacketWrapper swapItems = new PacketWrapper(0x13, null, packetWrapper.user());
							swapItems.write(Type.VAR_INT, 6);
							swapItems.write(Type.POSITION, new Position(0, (short) 0, 0));
							swapItems.write(Type.BYTE, (byte) 255);

							PacketUtil.sendToServer(swapItems, Protocol1_8TO1_9.class, true, true);
						}
					}
				});
			}
		});

		//Confirm Transaction
		protocol.registerIncoming(State.PLAY, 0x05, 0x0F);

		//Use Entity
		protocol.registerIncoming(State.PLAY, 0x0A, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int type = packetWrapper.get(Type.VAR_INT, 1);
						if (type == 2) {
							packetWrapper.passthrough(Type.FLOAT);
							packetWrapper.passthrough(Type.FLOAT);
							packetWrapper.passthrough(Type.FLOAT);
						}
						if (type == 2 || type == 0) {
							packetWrapper.write(Type.VAR_INT, 0);
						}
					}
				});
			}
		});

		//Player
		protocol.registerIncoming(State.PLAY, 0x0F, 0x03, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						int playerId = tracker.getPlayerId();
						if (tracker.isInsideVehicle(playerId)) packetWrapper.cancel();
					}
				});
			}
		});

		//Player Position
		protocol.registerIncoming(State.PLAY, 0x0C, 0x04, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
						if (pos.getConfirmId() != -1) return;
						pos.setPos(packetWrapper.get(Type.DOUBLE, 0), packetWrapper.get(Type.DOUBLE, 1), packetWrapper.get(Type.DOUBLE, 2));
						pos.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.user().get(BossBarStorage.class).updateLocation();
					}
				});
			}
		});

		//Player Look
		protocol.registerIncoming(State.PLAY, 0x0E, 0x05, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
						if (pos.getConfirmId() != -1) return;
						pos.setYaw(packetWrapper.get(Type.FLOAT, 0));
						pos.setPitch(packetWrapper.get(Type.FLOAT, 1));
						pos.setOnGround(packetWrapper.get(Type.BOOLEAN, 0));
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.user().get(BossBarStorage.class).updateLocation();
					}
				});
			}
		});

		//Player Position And Look
		protocol.registerIncoming(State.PLAY, 0x0D, 0x06, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.DOUBLE);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						double x = packetWrapper.get(Type.DOUBLE, 0);
						double y = packetWrapper.get(Type.DOUBLE, 1);
						double z = packetWrapper.get(Type.DOUBLE, 2);
						float yaw = packetWrapper.get(Type.FLOAT, 0);
						float pitch = packetWrapper.get(Type.FLOAT, 1);
						boolean onGround = packetWrapper.get(Type.BOOLEAN, 0);

						PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
						if (pos.getConfirmId() != -1) {
							if (pos.getPosX() == x && pos.getPosY() == y && pos.getPosZ() == z && pos.getYaw() == yaw && pos.getPitch() == pitch) {
								PacketWrapper confirmTeleport = packetWrapper.create(0x00);
								confirmTeleport.write(Type.VAR_INT, pos.getConfirmId());
								PacketUtil.sendToServer(confirmTeleport, Protocol1_8TO1_9.class, true, true);

								pos.setConfirmId(-1);
							}
						} else {
							pos.setPos(x, y, z);
							pos.setYaw(yaw);
							pos.setPitch(pitch);
							pos.setOnGround(onGround);
						}
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.user().get(BossBarStorage.class).updateLocation();
					}
				});
			}
		});

		//Player Digging
		protocol.registerIncoming(State.PLAY, 0x13, 0x07, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE, Type.VAR_INT);
				map(Type.POSITION);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int state = packetWrapper.get(Type.VAR_INT, 0);
						if (state == 0) {
							packetWrapper.user().get(BlockPlaceDestroyTracker.class).setMining(true);
						} else if (state == 2) {
							BlockPlaceDestroyTracker tracker = packetWrapper.user().get(BlockPlaceDestroyTracker.class);
							tracker.setMining(false);
							tracker.setLastMining(System.currentTimeMillis() + 100);
							packetWrapper.user().get(Cooldown.class).setLastHit(0);
						} else if (state == 1) {
							BlockPlaceDestroyTracker tracker = packetWrapper.user().get(BlockPlaceDestroyTracker.class);
							tracker.setMining(false);
							tracker.setLastMining(0);
							packetWrapper.user().get(Cooldown.class).hit();
						}
					}
				});
			}
		});

		//Player Block Placement
		protocol.registerIncoming(State.PLAY, 0x1C, 0x08, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.BYTE, Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.ITEM);
					}
				});
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.VAR_INT, 0);  //Main Hand
					}
				});
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						if (packetWrapper.get(Type.VAR_INT, 0) == -1) {
							packetWrapper.cancel();
							PacketWrapper useItem = new PacketWrapper(0x1D, null, packetWrapper.user());
							useItem.write(Type.VAR_INT, 0);

							PacketUtil.sendToServer(useItem, Protocol1_8TO1_9.class, true, true);
						}
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						if (packetWrapper.get(Type.VAR_INT, 0) != -1) {
							packetWrapper.user().get(BlockPlaceDestroyTracker.class).place();
						}
					}
				});
			}
		});

		//Held Item Change
		protocol.registerIncoming(State.PLAY, 0x17, 0x09, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.user().get(Cooldown.class).hit();
					}
				});
			}
		});

		//Animation
		protocol.registerIncoming(State.PLAY, 0x1A, 0x0A, new PacketRemapper() {
			@Override
			public void registerMap() {
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
						final PacketWrapper delayedPacket = new PacketWrapper(0x1A, null, packetWrapper.user());
						delayedPacket.write(Type.VAR_INT, 0);  //Main Hand
						//delay packet in order to deal damage to entities
						//the cooldown value gets reset by this packet
						//1.8 sends it before the use entity packet
						//1.9 afterwards
						Protocol1_8TO1_9.TIMER.schedule(new TimerTask() {
							@Override
							public void run() {
								PacketUtil.sendToServer(delayedPacket, Protocol1_8TO1_9.class);
							}
						}, 5);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.user().get(BlockPlaceDestroyTracker.class).updateMining();
						packetWrapper.user().get(Cooldown.class).hit();
					}
				});
			}
		});

		//Entity Action
		protocol.registerIncoming(State.PLAY, 0x14, 0x0B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.VAR_INT);
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int action = packetWrapper.get(Type.VAR_INT, 1);
						if (action == 6) {
							packetWrapper.set(Type.VAR_INT, 1, 7);
						} else if (action == 0) {
							PlayerPosition pos = packetWrapper.user().get(PlayerPosition.class);
							if (!pos.isOnGround()) {
								PacketWrapper elytra = new PacketWrapper(0x14, null, packetWrapper.user());
								elytra.write(Type.VAR_INT, packetWrapper.get(Type.VAR_INT, 0));
								elytra.write(Type.VAR_INT, 8);
								elytra.write(Type.VAR_INT, 0);
								PacketUtil.sendToServer(elytra, Protocol1_8TO1_9.class, true, false);
							}
						}
					}
				});
			}
		});

		//Steer Vehicle
		protocol.registerIncoming(State.PLAY, 0x15, 0x0C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						int playerId = tracker.getPlayerId();
						int vehicle = tracker.getVehicle(playerId);
						if (vehicle != -1 && tracker.getClientEntityTypes().get(vehicle) == Entity1_10Types.EntityType.BOAT) {
							PacketWrapper steerBoat = new PacketWrapper(0x11, null, packetWrapper.user());
							float left = packetWrapper.get(Type.FLOAT, 0);
							float forward = packetWrapper.get(Type.FLOAT, 1);
							steerBoat.write(Type.BOOLEAN, forward != 0.0f || left < 0.0f);
							steerBoat.write(Type.BOOLEAN, forward != 0.0f || left > 0.0f);
							PacketUtil.sendToServer(steerBoat, Protocol1_8TO1_9.class);
						}
					}
				});
			}
		});

		//Update Sign
		protocol.registerIncoming(State.PLAY, 0x19, 0x12, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						for (int i = 0; i < 4; i++) {
							packetWrapper.write(Type.STRING, ChatUtil.jsonToLegacy(packetWrapper.read(Type.COMPONENT)));
						}
					}
				});
			}
		});

		//Player Abilities
		protocol.registerIncoming(State.PLAY, 0x12, 0x13);

		//Tab Complete
		protocol.registerIncoming(State.PLAY, 0x01, 0x14, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.BOOLEAN, false);
					}
				});
				map(Type.OPTIONAL_POSITION);
			}
		});

		//Client Settings
		protocol.registerIncoming(State.PLAY, 0x04, 0x15, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.BYTE);
				map(Type.BYTE, Type.VAR_INT);
				map(Type.BOOLEAN);
				map(Type.UNSIGNED_BYTE);
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.VAR_INT, 1);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short flags = packetWrapper.get(Type.UNSIGNED_BYTE, 0);

						PacketWrapper updateSkin = new PacketWrapper(0x1C, null, packetWrapper.user());
						updateSkin.write(Type.VAR_INT, packetWrapper.user().get(EntityTracker.class).getPlayerId());

						ArrayList<Metadata> metadata = new ArrayList<>();
						metadata.add(new Metadata(10, MetaType1_8.Byte, (byte) flags));

						updateSkin.write(Types1_8.METADATA_LIST, metadata);

						PacketUtil.sendPacket(updateSkin, Protocol1_8TO1_9.class);
					}
				});
			}
		});

		//Client Status
		protocol.registerIncoming(State.PLAY, 0x03, 0x16);

		//Custom Payload
		protocol.registerIncoming(State.PLAY, 0x09, 0x17, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String channel = packetWrapper.get(Type.STRING, 0);
						if (channel.equalsIgnoreCase("MC|BEdit") || channel.equalsIgnoreCase("MC|BSign")) {
							Item book = packetWrapper.passthrough(Type.ITEM);
							book.setIdentifier(386);
							CompoundTag tag = book.getTag();
							if (tag.contains("pages")) {
								ListTag pages = tag.get("pages");
								for (int i = 0; i < pages.size(); i++) {
									StringTag page = pages.get(i);
									String value = page.getValue();
									value = ChatUtil.jsonToLegacy(value);
									page.setValue(value);
								}
							}
						} else if (channel.equalsIgnoreCase("MC|AdvCdm")) {
							packetWrapper.set(Type.STRING, 0, channel = "MC|AdvCmd");
						}
					}
				});
			}
		});

		//Spectate
		protocol.registerIncoming(State.PLAY, 0x1B, 0x18);

		//Resource Pack Status
		protocol.registerIncoming(State.PLAY, 0x16, 0x19);
	}
}
