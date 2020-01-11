package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Cooldown;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Levitation;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.PlayerPosition;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.util.RelativeMoveUtil;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.Vector;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.api.type.types.version.Types1_9;
import us.myles.ViaVersion.packets.State;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9.VALID_ATTRIBUTES;

public class EntityPackets {

	public static void register(Protocol protocol) {
		/*  OUTGOING  */

		//Entity Status
		protocol.registerOutgoing(State.PLAY, 0x1B, 0x1A, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						byte status = packetWrapper.read(Type.BYTE);
						if (status > 23) {
							packetWrapper.cancel();
							return;
						}
						packetWrapper.write(Type.BYTE, status);
					}
				});
			}
		});

		//Entity Relative Move
		protocol.registerOutgoing(State.PLAY, 0x25, 0x15, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						int relX = packetWrapper.read(Type.SHORT);
						int relY = packetWrapper.read(Type.SHORT);
						int relZ = packetWrapper.read(Type.SHORT);

						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							replacement.relMove(relX / 4096.0, relY / 4096.0, relZ / 4096.0);
							return;
						}

						Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(packetWrapper.user(), entityId, relX, relY, relZ);

						packetWrapper.write(Type.BYTE, (byte) moves[0].getBlockX());
						packetWrapper.write(Type.BYTE, (byte) moves[0].getBlockY());
						packetWrapper.write(Type.BYTE, (byte) moves[0].getBlockZ());

						boolean onGround = packetWrapper.passthrough(Type.BOOLEAN);

						if (moves.length > 1) {
							PacketWrapper secondPacket = new PacketWrapper(0x15, null, packetWrapper.user());
							secondPacket.write(Type.VAR_INT, packetWrapper.get(Type.VAR_INT, 0));
							secondPacket.write(Type.BYTE, (byte) moves[1].getBlockX());
							secondPacket.write(Type.BYTE, (byte) moves[1].getBlockY());
							secondPacket.write(Type.BYTE, (byte) moves[1].getBlockZ());
							secondPacket.write(Type.BOOLEAN, onGround);

							PacketUtil.sendPacket(secondPacket, Protocol1_8TO1_9.class);
						}
					}
				});
			}
		});

		//Entity Relative Move And Look
		protocol.registerOutgoing(State.PLAY, 0x26, 0x17, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						int relX = packetWrapper.read(Type.SHORT);
						int relY = packetWrapper.read(Type.SHORT);
						int relZ = packetWrapper.read(Type.SHORT);

						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							replacement.relMove(relX / 4096.0, relY / 4096.0, relZ / 4096.0);
							replacement.setYawPitch(packetWrapper.read(Type.BYTE) * 360f / 256, packetWrapper.read(Type.BYTE) * 360f / 256);
							return;
						}

						Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(packetWrapper.user(), entityId, relX, relY, relZ);

						packetWrapper.write(Type.BYTE, (byte) moves[0].getBlockX());
						packetWrapper.write(Type.BYTE, (byte) moves[0].getBlockY());
						packetWrapper.write(Type.BYTE, (byte) moves[0].getBlockZ());

						byte yaw = packetWrapper.passthrough(Type.BYTE);
						byte pitch = packetWrapper.passthrough(Type.BYTE);
						boolean onGround = packetWrapper.passthrough(Type.BOOLEAN);

						Entity1_10Types.EntityType type = packetWrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
						if (type == Entity1_10Types.EntityType.BOAT) {
							yaw -= 64;
							packetWrapper.set(Type.BYTE, 3, yaw);
						}

						if (moves.length > 1) {
							PacketWrapper secondPacket = new PacketWrapper(0x17, null, packetWrapper.user());
							secondPacket.write(Type.VAR_INT, packetWrapper.get(Type.VAR_INT, 0));
							secondPacket.write(Type.BYTE, (byte) moves[1].getBlockX());
							secondPacket.write(Type.BYTE, (byte) moves[1].getBlockY());
							secondPacket.write(Type.BYTE, (byte) moves[1].getBlockZ());
							secondPacket.write(Type.BYTE, yaw);
							secondPacket.write(Type.BYTE, pitch);
							secondPacket.write(Type.BOOLEAN, onGround);

							PacketUtil.sendPacket(secondPacket, Protocol1_8TO1_9.class);
						}
					}
				});
			}
		});

		//Entity Look
		protocol.registerOutgoing(State.PLAY, 0x27, 0x16, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						Entity1_10Types.EntityType type = packetWrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
						if (type == Entity1_10Types.EntityType.BOAT) {
							byte yaw = packetWrapper.get(Type.BYTE, 0);
							yaw -= 64;
							packetWrapper.set(Type.BYTE, 0, yaw);
						}
					}
				});
			}
		});

		//Entity
		protocol.registerOutgoing(State.PLAY, 0x28, 0x14);

		//Vehicle Move -> Entity Teleport
		protocol.registerOutgoing(State.PLAY, 0x29, 0x18, new PacketRemapper() {
			@Override
			public void registerMap() {
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						int vehicle = tracker.getVehicle(tracker.getPlayerId());
						if (vehicle == -1) packetWrapper.cancel();
						packetWrapper.write(Type.VAR_INT, vehicle);
					}
				});
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.FLOAT, Protocol1_8TO1_9.DEGREES_TO_ANGLE);
				map(Type.FLOAT, Protocol1_8TO1_9.DEGREES_TO_ANGLE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						if (packetWrapper.isCancelled()) return;
						PlayerPosition position = packetWrapper.user().get(PlayerPosition.class);
						double x = packetWrapper.get(Type.INT, 0) / 32d;
						double y = packetWrapper.get(Type.INT, 1) / 32d;
						double z = packetWrapper.get(Type.INT, 2) / 32d;
						position.setPos(x, y, z);
					}
				});
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.BOOLEAN, true);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						Entity1_10Types.EntityType type = packetWrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
						if (type == Entity1_10Types.EntityType.BOAT) {
							byte yaw = packetWrapper.get(Type.BYTE, 1);
							yaw -= 64;
							packetWrapper.set(Type.BYTE, 0, yaw);
							int y = packetWrapper.get(Type.INT, 1);
							y += 10;
							packetWrapper.set(Type.INT, 1, y);
						}
					}
				});
			}
		});

		//Use Bed
		protocol.registerOutgoing(State.PLAY, 0x2F, 0x0A);

		//Destroy Entities
		protocol.registerOutgoing(State.PLAY, 0x30, 0x13, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT_ARRAY_PRIMITIVE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						for (int entityId : packetWrapper.get(Type.VAR_INT_ARRAY_PRIMITIVE, 0)) tracker.removeEntity(entityId);
					}
				});
			}
		});

		//Remove Entity Effect
		protocol.registerOutgoing(State.PLAY, 0x31, 0x1E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int id = packetWrapper.get(Type.BYTE, 0);
						if (id > 23) packetWrapper.cancel();
						if (id == 25) {
							if(packetWrapper.get(Type.VAR_INT, 0) != packetWrapper.user().get(EntityTracker.class).getPlayerId()) return;
							Levitation levitation = packetWrapper.user().get(Levitation.class);
							levitation.setActive(false);
						}
					}
				});
			}
		});

		//Entity Head Look
		protocol.registerOutgoing(State.PLAY, 0x34, 0x19, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
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

		//Entity Metadata
		protocol.registerOutgoing(State.PLAY, 0x39, 0x1C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST);
				handler(new PacketHandler() {
					public void handle(PacketWrapper wrapper) throws Exception {
						List<Metadata> metadataList = wrapper.get(Types1_8.METADATA_LIST, 0);
						int entityId = wrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						if (tracker.getClientEntityTypes().containsKey(entityId)) {
							MetadataRewriter.transform(tracker.getClientEntityTypes().get(entityId), metadataList);
							if (metadataList.isEmpty()) wrapper.cancel();
						} else {
							tracker.addMetadataToBuffer(entityId, metadataList);
							wrapper.cancel();
						}
					}
				});
			}
		});

		//Attach Entity
		protocol.registerOutgoing(State.PLAY, 0x3A, 0x1B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.BOOLEAN, true);
					}
				});
			}
		});

		//Entity Velocity
		protocol.registerOutgoing(State.PLAY, 0x3B, 0x12);

		//Entity Equipment
		protocol.registerOutgoing(State.PLAY, 0x3C, 0x04, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int slot = packetWrapper.read(Type.VAR_INT);
						if (slot == 1) {
							packetWrapper.cancel();
						} else if (slot > 1) {
							slot -= 1;
						}
						packetWrapper.write(Type.SHORT, (short) slot);
					}
				});
				map(Type.ITEM);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.set(Type.ITEM, 0, ItemRewriter.toClient(packetWrapper.get(Type.ITEM, 0)));
					}
				});
			}
		});

		//Set Passengers
		protocol.registerOutgoing(State.PLAY, 0x40, 0x1B, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
						EntityTracker entityTracker = packetWrapper.user().get(EntityTracker.class);
						int vehicle = packetWrapper.read(Type.VAR_INT);
						int count = packetWrapper.read(Type.VAR_INT);
						ArrayList<Integer> passengers = new ArrayList<>();
						for (int i = 0; i < count; i++) passengers.add(packetWrapper.read(Type.VAR_INT));
						List<Integer> oldPassengers = entityTracker.getPassengers(vehicle);
						entityTracker.setPassengers(vehicle, passengers);
						if (!oldPassengers.isEmpty()) {
							for (Integer passenger : oldPassengers) {
								PacketWrapper detach = new PacketWrapper(0x1B, null, packetWrapper.user());
								detach.write(Type.INT, passenger);
								detach.write(Type.INT, -1);
								detach.write(Type.BOOLEAN, false);
								PacketUtil.sendPacket(detach, Protocol1_8TO1_9.class);
							}
						}
						for (int i = 0; i < count; i++) {
							int v = i == 0 ? vehicle : passengers.get(i - 1);
							int p = passengers.get(i);
							PacketWrapper attach = new PacketWrapper(0x1B, null, packetWrapper.user());
							attach.write(Type.INT, p);
							attach.write(Type.INT, v);
							attach.write(Type.BOOLEAN, false);
							PacketUtil.sendPacket(attach, Protocol1_8TO1_9.class);
						}
					}
				});
			}
		});

		//Collect Item
		protocol.registerOutgoing(State.PLAY, 0x49, 0x0D);

		//Entity Teleport
		protocol.registerOutgoing(State.PLAY, 0x4A, 0x18, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						Entity1_10Types.EntityType type = packetWrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
						if (type == Entity1_10Types.EntityType.BOAT) {
							byte yaw = packetWrapper.get(Type.BYTE, 1);
							yaw -= 64;
							packetWrapper.set(Type.BYTE, 0, yaw);
							int y = packetWrapper.get(Type.INT, 1);
							y += 10;
							packetWrapper.set(Type.INT, 1, y);
						}
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						packetWrapper.user().get(EntityTracker.class).resetEntityOffset(entityId);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						EntityReplacement replacement = tracker.getEntityReplacement(entityId);
						if (replacement != null) {
							packetWrapper.cancel();
							int x = packetWrapper.get(Type.INT, 0);
							int y = packetWrapper.get(Type.INT, 1);
							int z = packetWrapper.get(Type.INT, 2);
							int yaw = packetWrapper.get(Type.BYTE, 0);
							int pitch = packetWrapper.get(Type.BYTE, 1);
							replacement.setLocation(x / 32.0, y / 32.0, z / 32.0);
							replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
						}
					}
				});
			}
		});

		//Entity Properties
		protocol.registerOutgoing(State.PLAY, 0x4B, 0x20, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						boolean player = packetWrapper.get(Type.VAR_INT, 0) == packetWrapper.user().get(EntityTracker.class).getPlayerId();
						int size = packetWrapper.get(Type.INT, 0);
						int removed = 0;
						for (int i = 0; i < size; i++) {
							String key = packetWrapper.read(Type.STRING);
							boolean skip = !VALID_ATTRIBUTES.contains(key);
							double value = packetWrapper.read(Type.DOUBLE);
							int modifiersize = packetWrapper.read(Type.VAR_INT);
							if (!skip) {
								packetWrapper.write(Type.STRING, key);
								packetWrapper.write(Type.DOUBLE, value);
								packetWrapper.write(Type.VAR_INT, modifiersize);
							} else {
								removed++;
							}
							ArrayList<Pair<Byte, Double>> modifiers = new ArrayList<>();
							for (int j = 0; j < modifiersize; j++) {
								UUID uuid = packetWrapper.read(Type.UUID);
								double amount = packetWrapper.read(Type.DOUBLE);
								byte operation = packetWrapper.read(Type.BYTE);
								modifiers.add(new Pair<>(operation, amount));
								if (skip) continue;
								packetWrapper.write(Type.UUID, uuid);
								packetWrapper.write(Type.DOUBLE, amount);
								packetWrapper.write(Type.BYTE, operation);
							}
							if (player && key.equals("generic.attackSpeed")) {
								packetWrapper.user().get(Cooldown.class).setAttackSpeed(value, modifiers);
							}
						}
						packetWrapper.set(Type.INT, 0, size - removed);
					}
				});
			}
		});

		//Entity Effect
		protocol.registerOutgoing(State.PLAY, 0x4C, 0x1D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.VAR_INT);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int id = packetWrapper.get(Type.BYTE, 0);
						if (id > 23) packetWrapper.cancel();
						if (id == 25) {
							if(packetWrapper.get(Type.VAR_INT, 0) != packetWrapper.user().get(EntityTracker.class).getPlayerId()) return;
							Levitation levitation = packetWrapper.user().get(Levitation.class);
							levitation.setActive(true);
							levitation.setAmplifier(packetWrapper.get(Type.BYTE, 1));
						}
					}
				});
			}
		});
	}
}
