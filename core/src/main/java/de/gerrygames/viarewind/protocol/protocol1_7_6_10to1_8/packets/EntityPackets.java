package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.IntArrayType;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.packets.State;

import java.util.List;
import java.util.UUID;

public class EntityPackets {

	public static void register(Protocol protocol) {

		/*  OUTGOING  */

		//Entity Equipment
		protocol.registerOutgoing(State.PLAY, 0x04, 0x04, new PacketRemapper() {
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
						if (packetWrapper.get(Type.SHORT, 0) > 4) packetWrapper.cancel();
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						if (packetWrapper.isCancelled()) return;
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						UUID uuid = tracker.getPlayerUUID(packetWrapper.get(Type.INT, 0));
						if (uuid == null) return;
						Item[] equipment = tracker.getPlayerEquipment(uuid);
						if (equipment == null) tracker.setPlayerEquipment(uuid, equipment = new Item[5]);
						equipment[packetWrapper.get(Type.SHORT, 0)] = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
						GameProfileStorage storage = packetWrapper.user().get(GameProfileStorage.class);
						GameProfileStorage.GameProfile profile = storage.get(uuid);
						if (profile != null && profile.gamemode == 3) packetWrapper.cancel();
					}
				});
			}
		});

		//Use Bed
		protocol.registerOutgoing(State.PLAY, 0x0A, 0x0A, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX());
						packetWrapper.write(Type.UNSIGNED_BYTE, position.getY());
						packetWrapper.write(Type.INT, position.getZ());
					}
				});
			}
		});

		//Collect Item
		protocol.registerOutgoing(State.PLAY, 0x0D, 0x0D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Collected Entity ID
				map(Type.VAR_INT, Type.INT);  //Collector Entity ID
			}
		});

		//Entity Velocity
		protocol.registerOutgoing(State.PLAY, 0x12, 0x12, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.SHORT);  //velX
				map(Type.SHORT);  //velY
				map(Type.SHORT);  //velZ
			}
		});

		//Destroy Entities
		protocol.registerOutgoing(State.PLAY, 0x13, 0x13, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int[] entityIds = packetWrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);

						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						for (int entityId : entityIds) tracker.removeEntity(entityId);

						while (entityIds.length > 127) {
							int[] entityIds2 = new int[127];
							System.arraycopy(entityIds, 0, entityIds2, 0, 127);
							int[] temp = new int[entityIds.length - 127];
							System.arraycopy(entityIds, 127, temp, 0, temp.length);
							entityIds = temp;

							PacketWrapper destroy = new PacketWrapper(0x13, null, packetWrapper.user());
							destroy.write(Types1_7_6_10.INT_ARRAY, entityIds2);
							PacketUtil.sendPacket(destroy, Protocol1_7_6_10TO1_8.class);
						}

						packetWrapper.write(Types1_7_6_10.INT_ARRAY, entityIds);
					}
				});  //Entity Id Array
			}
		});

		//Entity
		protocol.registerOutgoing(State.PLAY, 0x14, 0x14, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
			}
		});

		//Entity Relative Move
		protocol.registerOutgoing(State.PLAY, 0x15, 0x15, new PacketRemapper() {
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							int x = packetWrapper.get(Type.BYTE, 0);
							int y = packetWrapper.get(Type.BYTE, 1);
							int z = packetWrapper.get(Type.BYTE, 2);
							replacement.relMove(x / 32.0, y / 32.0, z / 32.0);
						}
					}
				});
			}
		});

		//Entity Look
		protocol.registerOutgoing(State.PLAY, 0x16, 0x16, new PacketRemapper() {
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							int yaw = packetWrapper.get(Type.BYTE, 0);
							int pitch = packetWrapper.get(Type.BYTE, 1);
							replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
						}
					}
				});
			}
		});

		//Entity Look and Relative Move
		protocol.registerOutgoing(State.PLAY, 0x17, 0x17, new PacketRemapper() {
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							int x = packetWrapper.get(Type.BYTE, 0);
							int y = packetWrapper.get(Type.BYTE, 1);
							int z = packetWrapper.get(Type.BYTE, 2);
							int yaw = packetWrapper.get(Type.BYTE, 3);
							int pitch = packetWrapper.get(Type.BYTE, 4);
							replacement.relMove(x / 32.0, y / 32.0, z / 32.0);
							replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
						}
					}
				});
			}
		});

		//Entity Teleport
		protocol.registerOutgoing(State.PLAY, 0x18, 0x18, new PacketRemapper() {
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						Entity1_10Types.EntityType type = tracker.getClientEntityTypes().get(entityId);
						if (type == Entity1_10Types.EntityType.MINECART_ABSTRACT) {
							int y = packetWrapper.get(Type.INT, 2);
							y += 12;
							packetWrapper.set(Type.INT, 2, y);
						}
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							int x = packetWrapper.get(Type.INT, 1);
							int y = packetWrapper.get(Type.INT, 2);
							int z = packetWrapper.get(Type.INT, 3);
							int yaw = packetWrapper.get(Type.BYTE, 0);
							int pitch = packetWrapper.get(Type.BYTE, 1);
							replacement.setLocation(x / 32.0, y / 32.0, z / 32.0);
							replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
						}
					}
				});
			}
		});

		//Entity Head Look
		protocol.registerOutgoing(State.PLAY, 0x19, 0x19, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Head yaw
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							int yaw = packetWrapper.get(Type.BYTE, 0);
							replacement.setHeadYaw(yaw * 360f / 256);
						}
					}
				});
			}
		});

		//Attach Entity
		protocol.registerOutgoing(State.PLAY, 0x1B, 0x1B, new PacketRemapper() {
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
		protocol.registerOutgoing(State.PLAY, 0x1C, 0x1C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST);  //Metadata
				handler(new PacketHandler() {
					public void handle(PacketWrapper wrapper) throws Exception {
						List<Metadata> metadataList = wrapper.get(Types1_7_6_10.METADATA_LIST, 0);
						int entityId = wrapper.get(Type.INT, 0);
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						if (tracker.getClientEntityTypes().containsKey(entityId)) {
							EntityReplacement replacement = tracker.getEntityReplacement(entityId);
							if (replacement != null) {
								wrapper.cancel();
								replacement.updateMetadata(metadataList);
							} else {
								MetadataRewriter.transform(tracker.getClientEntityTypes().get(entityId), metadataList);
								if (metadataList.isEmpty()) wrapper.cancel();
							}
						} else {
							tracker.addMetadataToBuffer(entityId, metadataList);
							wrapper.cancel();
						}
					}
				});
			}
		});

		//Entity Effect
		protocol.registerOutgoing(State.PLAY, 0x1D, 0x1D, new PacketRemapper() {
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
		protocol.registerOutgoing(State.PLAY, 0x1E, 0x1E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Effect Id
			}
		});

		//Entity Properties
		protocol.registerOutgoing(State.PLAY, 0x20, 0x20, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						if (tracker.getEntityReplacement(entityId) != null) {
							packetWrapper.cancel();
							return;
						}
						int amount = packetWrapper.passthrough(Type.INT);
						for (int i = 0; i < amount; i++) {
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.DOUBLE);
							int modifierlength = packetWrapper.read(Type.VAR_INT);
							packetWrapper.write(Type.SHORT, (short) modifierlength);
							for (int j = 0; j < modifierlength; j++) {
								packetWrapper.passthrough(Type.UUID);
								packetWrapper.passthrough(Type.DOUBLE);
								packetWrapper.passthrough(Type.BYTE);
							}
						}
					}
				});

			}
		});

		//Update Entity NBT
		protocol.registerOutgoing(State.PLAY, 0x49, -1, new PacketRemapper() {
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


	}
}
