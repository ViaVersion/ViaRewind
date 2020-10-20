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
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ListTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

import java.util.ArrayList;
import java.util.List;
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
				handler(wrapper -> {
					wrapper.cancel();

					UUID uuid = wrapper.read(Type.UUID);
					int action = wrapper.read(Type.VAR_INT);
					BossBarStorage bossBarStorage = wrapper.user().get(BossBarStorage.class);
					if (action == 0) {
						bossBarStorage.add(uuid, ChatUtil.jsonToLegacy(wrapper.read(Type.COMPONENT)), wrapper.read(Type.FLOAT));
						wrapper.read(Type.VAR_INT);
						wrapper.read(Type.VAR_INT);
						wrapper.read(Type.UNSIGNED_BYTE);
					} else if (action == 1) {
						bossBarStorage.remove(uuid);
					} else if (action == 2) {
						bossBarStorage.updateHealth(uuid, wrapper.read(Type.FLOAT));
					} else if (action == 3) {
						String title = ChatUtil.jsonToLegacy(wrapper.read(Type.COMPONENT));
						bossBarStorage.updateTitle(uuid, title);
					}
				});
			}
		});

		//Tab-Complete
		protocol.registerOutgoing(State.PLAY, 0x0E, 0x3A);

		//Chat Message
		protocol.registerOutgoing(State.PLAY, 0x0F, 0x02);

		//Set Cooldown
		protocol.cancelOutgoing(State.PLAY, 0x17);

		//Custom Payload
		protocol.registerOutgoing(State.PLAY, 0x18, 0x3F, new PacketRemapper() {
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
							ItemRewriter.toClient(wrapper.passthrough(Type.ITEM)); //Buy Item 1
							ItemRewriter.toClient(wrapper.passthrough(Type.ITEM)); //Buy Item 3

							boolean has3Items = wrapper.passthrough(Type.BOOLEAN);
							if (has3Items) {
								ItemRewriter.toClient(wrapper.passthrough(Type.ITEM)); //Buy Item 2
							}

							wrapper.passthrough(Type.BOOLEAN); //Unavailable
							wrapper.passthrough(Type.INT); //Uses
							wrapper.passthrough(Type.INT); //Max Uses
						}
					} else if (channel.equalsIgnoreCase("MC|BOpen")) {
						wrapper.read(Type.VAR_INT);
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

				handler(wrapper -> {
					int reason = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (reason == 3) wrapper.user().get(EntityTracker.class).setPlayerGamemode(wrapper.get(Type.FLOAT, 0).intValue());
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

				handler(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.setPlayerId(wrapper.get(Type.INT, 0));
					tracker.setPlayerGamemode(wrapper.get(Type.UNSIGNED_BYTE, 0));
					tracker.getClientEntityTypes().put(tracker.getPlayerId(), Entity1_10Types.EntityType.ENTITY_HUMAN);
				});
				handler(wrapper -> {
					ClientWorld world = wrapper.user().get(ClientWorld.class);
					world.setEnvironment(wrapper.get(Type.BYTE, 0));
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

				handler(wrapper -> {
					PlayerPosition pos = wrapper.user().get(PlayerPosition.class);

					int teleportId = wrapper.read(Type.VAR_INT);
					pos.setConfirmId(teleportId);

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

				handler(wrapper -> wrapper.user().get(EntityTracker.class).setPlayerGamemode(wrapper.get(Type.UNSIGNED_BYTE, 1)));
				handler(wrapper -> {
					wrapper.user().get(BossBarStorage.class).updateLocation();
					wrapper.user().get(BossBarStorage.class).changeWorld();
				});
				handler(wrapper -> {
					ClientWorld world = wrapper.user().get(ClientWorld.class);
					world.setEnvironment(wrapper.get(Type.INT, 0));
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

				handler(wrapper -> {
					String msg = wrapper.get(Type.STRING, 0);
					if (msg.toLowerCase().startsWith("/offhand")) {
						wrapper.cancel();
						PacketWrapper swapItems = new PacketWrapper(0x13, null, wrapper.user());
						swapItems.write(Type.VAR_INT, 6);
						swapItems.write(Type.POSITION, new Position(0, (short) 0, 0));
						swapItems.write(Type.BYTE, (byte) 255);

						PacketUtil.sendToServer(swapItems, Protocol1_8TO1_9.class, true, true);
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

				handler(wrapper -> {
					int type = wrapper.get(Type.VAR_INT, 1);
					if (type == 2) {
						wrapper.passthrough(Type.FLOAT);
						wrapper.passthrough(Type.FLOAT);
						wrapper.passthrough(Type.FLOAT);
					}
					if (type == 2 || type == 0) {
						wrapper.write(Type.VAR_INT, 0);
					}
				});
			}
		});

		//Player
		protocol.registerIncoming(State.PLAY, 0x0F, 0x03, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BOOLEAN);

				handler(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					int playerId = tracker.getPlayerId();
					if (tracker.isInsideVehicle(playerId)) wrapper.cancel();
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

				handler(wrapper -> {
					PlayerPosition pos = wrapper.user().get(PlayerPosition.class);
					if (pos.getConfirmId() != -1) return;
					pos.setPos(wrapper.get(Type.DOUBLE, 0), wrapper.get(Type.DOUBLE, 1), wrapper.get(Type.DOUBLE, 2));
					pos.setOnGround(wrapper.get(Type.BOOLEAN, 0));
				});
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).updateLocation());
			}
		});

		//Player Look
		protocol.registerIncoming(State.PLAY, 0x0E, 0x05, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.BOOLEAN);

				handler(wrapper -> {
					PlayerPosition pos = wrapper.user().get(PlayerPosition.class);
					if (pos.getConfirmId() != -1) return;
					pos.setYaw(wrapper.get(Type.FLOAT, 0));
					pos.setPitch(wrapper.get(Type.FLOAT, 1));
					pos.setOnGround(wrapper.get(Type.BOOLEAN, 0));
				});
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).updateLocation());
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

				handler(wrapper -> {
					double x = wrapper.get(Type.DOUBLE, 0);
					double y = wrapper.get(Type.DOUBLE, 1);
					double z = wrapper.get(Type.DOUBLE, 2);
					float yaw = wrapper.get(Type.FLOAT, 0);
					float pitch = wrapper.get(Type.FLOAT, 1);
					boolean onGround = wrapper.get(Type.BOOLEAN, 0);

					PlayerPosition pos = wrapper.user().get(PlayerPosition.class);
					if (pos.getConfirmId() != -1) {
						if (pos.getPosX() == x && pos.getPosY() == y && pos.getPosZ() == z && pos.getYaw() == yaw && pos.getPitch() == pitch) {
							PacketWrapper confirmTeleport = wrapper.create(0x00);
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
				});
				handler(wrapper -> wrapper.user().get(BossBarStorage.class).updateLocation());
			}
		});

		//Player Digging
		protocol.registerIncoming(State.PLAY, 0x13, 0x07, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE, Type.VAR_INT);
				map(Type.POSITION);
				map(Type.BYTE);

				handler(wrapper -> {
					int state = wrapper.get(Type.VAR_INT, 0);
					if (state == 0) {
						wrapper.user().get(BlockPlaceDestroyTracker.class).setMining(true);
					} else if (state == 2) {
						BlockPlaceDestroyTracker tracker = wrapper.user().get(BlockPlaceDestroyTracker.class);
						tracker.setMining(false);
						tracker.setLastMining(System.currentTimeMillis() + 100);
						wrapper.user().get(Cooldown.class).setLastHit(0);
					} else if (state == 1) {
						BlockPlaceDestroyTracker tracker = wrapper.user().get(BlockPlaceDestroyTracker.class);
						tracker.setMining(false);
						tracker.setLastMining(0);
						wrapper.user().get(Cooldown.class).hit();
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
				handler(wrapper -> wrapper.read(Type.ITEM));
				create(wrapper -> wrapper.write(Type.VAR_INT, 0)); //Main Hand
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				map(Type.BYTE, Type.UNSIGNED_BYTE);

				handler(wrapper -> {
					if (wrapper.get(Type.VAR_INT, 0) == -1) {
						wrapper.clearPacket();
						wrapper.setId(0x1D);
						wrapper.write(Type.VAR_INT, 0);
					}
				});
				handler(wrapper -> {
					if (wrapper.get(Type.VAR_INT, 0) != -1) {
						wrapper.user().get(BlockPlaceDestroyTracker.class).place();
					}
				});
			}
		});

		//Held Item Change
		protocol.registerIncoming(State.PLAY, 0x17, 0x09, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> wrapper.user().get(Cooldown.class).hit());
			}
		});

		//Animation
		protocol.registerIncoming(State.PLAY, 0x1A, 0x0A, new PacketRemapper() {
			@Override
			public void registerMap() {
				create(wrapper -> {
					wrapper.cancel();
					final PacketWrapper delayedPacket = new PacketWrapper(0x1A, null, wrapper.user());
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
				});
				handler(wrapper -> {
					wrapper.user().get(BlockPlaceDestroyTracker.class).updateMining();
					wrapper.user().get(Cooldown.class).hit();
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

				handler(wrapper -> {
					int action = wrapper.get(Type.VAR_INT, 1);
					if (action == 6) {
						wrapper.set(Type.VAR_INT, 1, 7);
					} else if (action == 0) {
						PlayerPosition pos = wrapper.user().get(PlayerPosition.class);
						if (!pos.isOnGround()) {
							PacketWrapper elytra = new PacketWrapper(0x14, null, wrapper.user());
							elytra.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0));
							elytra.write(Type.VAR_INT, 8);
							elytra.write(Type.VAR_INT, 0);
							PacketUtil.sendToServer(elytra, Protocol1_8TO1_9.class, true, false);
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

				handler(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					int playerId = tracker.getPlayerId();
					int vehicle = tracker.getVehicle(playerId);
					if (vehicle != -1 && tracker.getClientEntityTypes().get(vehicle) == Entity1_10Types.EntityType.BOAT) {
						PacketWrapper steerBoat = new PacketWrapper(0x11, null, wrapper.user());
						float left = wrapper.get(Type.FLOAT, 0);
						float forward = wrapper.get(Type.FLOAT, 1);
						steerBoat.write(Type.BOOLEAN, forward != 0.0f || left < 0.0f);
						steerBoat.write(Type.BOOLEAN, forward != 0.0f || left > 0.0f);
						PacketUtil.sendToServer(steerBoat, Protocol1_8TO1_9.class, true, true);
					}
				});
			}
		});

		//Update Sign
		protocol.registerIncoming(State.PLAY, 0x19, 0x12, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				handler(wrapper -> {
					for (int i = 0; i < 4; i++) {
						wrapper.write(Type.STRING, ChatUtil.jsonToLegacy(wrapper.read(Type.COMPONENT)));
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
				create(wrapper -> wrapper.write(Type.BOOLEAN, false));
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
				create(wrapper -> wrapper.write(Type.VAR_INT, 1));

				handler(wrapper -> {
					short flags = wrapper.get(Type.UNSIGNED_BYTE, 0);

					PacketWrapper updateSkin = new PacketWrapper(0x1C, null, wrapper.user());
					updateSkin.write(Type.VAR_INT, wrapper.user().get(EntityTracker.class).getPlayerId());

					List<Metadata> metadata = new ArrayList<>(1);
					metadata.add(new Metadata(10, MetaType1_8.Byte, (byte) flags));

					updateSkin.write(Types1_8.METADATA_LIST, metadata);

					PacketUtil.sendPacket(updateSkin, Protocol1_8TO1_9.class);
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

				handler(wrapper -> {
					String channel = wrapper.get(Type.STRING, 0);
					if (channel.equalsIgnoreCase("MC|BEdit") || channel.equalsIgnoreCase("MC|BSign")) {
						Item book = wrapper.passthrough(Type.ITEM);
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
						wrapper.set(Type.STRING, 0, channel = "MC|AdvCmd");
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
