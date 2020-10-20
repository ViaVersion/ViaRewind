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
import us.myles.ViaVersion.api.remapper.PacketRemapper;
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
				map(Type.BYTE);

				handler(wrapper -> {
					byte status = wrapper.get(Type.BYTE, 0);
					if (status > 23) wrapper.cancel();
				});
			}
		});

		//Entity Relative Move
		protocol.registerOutgoing(State.PLAY, 0x25, 0x15, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					int relX = wrapper.read(Type.SHORT);
					int relY = wrapper.read(Type.SHORT);
					int relZ = wrapper.read(Type.SHORT);

					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						wrapper.cancel();
						replacement.relMove(relX / 4096.0, relY / 4096.0, relZ / 4096.0);
						return;
					}

					Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(wrapper.user(), entityId, relX, relY, relZ);

					wrapper.write(Type.BYTE, (byte) moves[0].getBlockX());
					wrapper.write(Type.BYTE, (byte) moves[0].getBlockY());
					wrapper.write(Type.BYTE, (byte) moves[0].getBlockZ());

					boolean onGround = wrapper.passthrough(Type.BOOLEAN);

					if (moves.length > 1) {
						PacketWrapper secondPacket = new PacketWrapper(0x15, null, wrapper.user());
						secondPacket.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0));
						secondPacket.write(Type.BYTE, (byte) moves[1].getBlockX());
						secondPacket.write(Type.BYTE, (byte) moves[1].getBlockY());
						secondPacket.write(Type.BYTE, (byte) moves[1].getBlockZ());
						secondPacket.write(Type.BOOLEAN, onGround);

						PacketUtil.sendPacket(secondPacket, Protocol1_8TO1_9.class);
					}
				});
			}
		});

		//Entity Relative Move And Look
		protocol.registerOutgoing(State.PLAY, 0x26, 0x17, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					int relX = wrapper.read(Type.SHORT);
					int relY = wrapper.read(Type.SHORT);
					int relZ = wrapper.read(Type.SHORT);

					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						wrapper.cancel();
						replacement.relMove(relX / 4096.0, relY / 4096.0, relZ / 4096.0);
						replacement.setYawPitch(wrapper.read(Type.BYTE) * 360f / 256, wrapper.read(Type.BYTE) * 360f / 256);
						return;
					}

					Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(wrapper.user(), entityId, relX, relY, relZ);

					wrapper.write(Type.BYTE, (byte) moves[0].getBlockX());
					wrapper.write(Type.BYTE, (byte) moves[0].getBlockY());
					wrapper.write(Type.BYTE, (byte) moves[0].getBlockZ());

					byte yaw = wrapper.passthrough(Type.BYTE);
					byte pitch = wrapper.passthrough(Type.BYTE);
					boolean onGround = wrapper.passthrough(Type.BOOLEAN);

					Entity1_10Types.EntityType type = wrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
					if (type == Entity1_10Types.EntityType.BOAT) {
						yaw -= 64;
						wrapper.set(Type.BYTE, 3, yaw);
					}

					if (moves.length > 1) {
						PacketWrapper secondPacket = new PacketWrapper(0x17, null, wrapper.user());
						secondPacket.write(Type.VAR_INT, wrapper.get(Type.VAR_INT, 0));
						secondPacket.write(Type.BYTE, (byte) moves[1].getBlockX());
						secondPacket.write(Type.BYTE, (byte) moves[1].getBlockY());
						secondPacket.write(Type.BYTE, (byte) moves[1].getBlockZ());
						secondPacket.write(Type.BYTE, yaw);
						secondPacket.write(Type.BYTE, pitch);
						secondPacket.write(Type.BOOLEAN, onGround);

						PacketUtil.sendPacket(secondPacket, Protocol1_8TO1_9.class);
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

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						wrapper.cancel();
						int yaw = wrapper.get(Type.BYTE, 0);
						int pitch = wrapper.get(Type.BYTE, 1);
						replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					Entity1_10Types.EntityType type = wrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
					if (type == Entity1_10Types.EntityType.BOAT) {
						byte yaw = wrapper.get(Type.BYTE, 0);
						yaw -= 64;
						wrapper.set(Type.BYTE, 0, yaw);
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
				create(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					int vehicle = tracker.getVehicle(tracker.getPlayerId());
					if (vehicle == -1) wrapper.cancel();
					wrapper.write(Type.VAR_INT, vehicle);
				});
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.FLOAT, Protocol1_8TO1_9.DEGREES_TO_ANGLE);
				map(Type.FLOAT, Protocol1_8TO1_9.DEGREES_TO_ANGLE);
				create(wrapper -> wrapper.write(Type.BOOLEAN, true));

				handler(wrapper -> {
					if (wrapper.isCancelled()) return;
					PlayerPosition position = wrapper.user().get(PlayerPosition.class);
					double x = wrapper.get(Type.INT, 0) / 32d;
					double y = wrapper.get(Type.INT, 1) / 32d;
					double z = wrapper.get(Type.INT, 2) / 32d;
					position.setPos(x, y, z);
				});
				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					Entity1_10Types.EntityType type = wrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
					if (type == Entity1_10Types.EntityType.BOAT) {
						byte yaw = wrapper.get(Type.BYTE, 1);
						yaw -= 64;
						wrapper.set(Type.BYTE, 0, yaw);
						int y = wrapper.get(Type.INT, 1);
						y += 10;
						wrapper.set(Type.INT, 1, y);
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
				handler(wrapper -> {
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					for (int entityId : wrapper.get(Type.VAR_INT_ARRAY_PRIMITIVE, 0)) tracker.removeEntity(entityId);
				});
			}
		});

		//Remove Entity Effect
		protocol.registerOutgoing(State.PLAY, 0x31, 0x1E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);

				handler(wrapper -> {
					int id = wrapper.get(Type.BYTE, 0);
					if (id > 23) wrapper.cancel();
					if (id == 25) {
						if(wrapper.get(Type.VAR_INT, 0) != wrapper.user().get(EntityTracker.class).getPlayerId()) return;
						Levitation levitation = wrapper.user().get(Levitation.class);
						levitation.setActive(false);
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

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						wrapper.cancel();
						int yaw = wrapper.get(Type.BYTE, 0);
						replacement.setHeadYaw(yaw * 360f / 256);
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

				handler(wrapper -> {
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
				});
			}
		});

		//Attach Entity
		protocol.registerOutgoing(State.PLAY, 0x3A, 0x1B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);
				create(wrapper -> wrapper.write(Type.BOOLEAN, true));
			}
		});

		//Entity Velocity
		protocol.registerOutgoing(State.PLAY, 0x3B, 0x12);

		//Entity Equipment
		protocol.registerOutgoing(State.PLAY, 0x3C, 0x04, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(wrapper -> {
					int slot = wrapper.read(Type.VAR_INT);
					if (slot == 1) {
						wrapper.cancel();
					} else if (slot > 1) {
						slot -= 1;
					}
					wrapper.write(Type.SHORT, (short) slot);
				});
				map(Type.ITEM);
				handler(wrapper -> ItemRewriter.toClient(wrapper.get(Type.ITEM, 0)));
			}
		});

		//Set Passengers
		protocol.registerOutgoing(State.PLAY, 0x40, 0x1B, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					wrapper.cancel();
					EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
					int vehicle = wrapper.read(Type.VAR_INT);
					int count = wrapper.read(Type.VAR_INT);
					List<Integer> passengers = new ArrayList<>(count);
					for (int i = 0; i < count; i++) passengers.add(wrapper.read(Type.VAR_INT));
					List<Integer> oldPassengers = entityTracker.getPassengers(vehicle);
					entityTracker.setPassengers(vehicle, passengers);
					if (!oldPassengers.isEmpty()) {
						for (Integer passenger : oldPassengers) {
							PacketWrapper detach = new PacketWrapper(0x1B, null, wrapper.user());
							detach.write(Type.INT, passenger);
							detach.write(Type.INT, -1);
							detach.write(Type.BOOLEAN, false);
							PacketUtil.sendPacket(detach, Protocol1_8TO1_9.class);
						}
					}
					for (int i = 0; i < count; i++) {
						int v = i == 0 ? vehicle : passengers.get(i - 1);
						int p = passengers.get(i);
						PacketWrapper attach = new PacketWrapper(0x1B, null, wrapper.user());
						attach.write(Type.INT, p);
						attach.write(Type.INT, v);
						attach.write(Type.BOOLEAN, false);
						PacketUtil.sendPacket(attach, Protocol1_8TO1_9.class);
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

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					Entity1_10Types.EntityType type = wrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
					if (type == Entity1_10Types.EntityType.BOAT) {
						byte yaw = wrapper.get(Type.BYTE, 1);
						yaw -= 64;
						wrapper.set(Type.BYTE, 0, yaw);
						int y = wrapper.get(Type.INT, 1);
						y += 10;
						wrapper.set(Type.INT, 1, y);
					}
				});
				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					wrapper.user().get(EntityTracker.class).resetEntityOffset(entityId);
				});
				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						wrapper.cancel();
						int x = wrapper.get(Type.INT, 0);
						int y = wrapper.get(Type.INT, 1);
						int z = wrapper.get(Type.INT, 2);
						int yaw = wrapper.get(Type.BYTE, 0);
						int pitch = wrapper.get(Type.BYTE, 1);
						replacement.setLocation(x / 32.0, y / 32.0, z / 32.0);
						replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
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

				handler(wrapper -> {
					boolean player = wrapper.get(Type.VAR_INT, 0) == wrapper.user().get(EntityTracker.class).getPlayerId();
					int size = wrapper.get(Type.INT, 0);
					int removed = 0;
					for (int i = 0; i < size; i++) {
						String key = wrapper.read(Type.STRING);
						boolean skip = !VALID_ATTRIBUTES.contains(key);
						double value = wrapper.read(Type.DOUBLE);
						int modifiersize = wrapper.read(Type.VAR_INT);
						if (!skip) {
							wrapper.write(Type.STRING, key);
							wrapper.write(Type.DOUBLE, value);
							wrapper.write(Type.VAR_INT, modifiersize);
						} else {
							removed++;
						}
						ArrayList<Pair<Byte, Double>> modifiers = new ArrayList<>();
						for (int j = 0; j < modifiersize; j++) {
							UUID uuid = wrapper.read(Type.UUID);
							double amount = wrapper.read(Type.DOUBLE);
							byte operation = wrapper.read(Type.BYTE);
							modifiers.add(new Pair<>(operation, amount));
							if (skip) continue;
							wrapper.write(Type.UUID, uuid);
							wrapper.write(Type.DOUBLE, amount);
							wrapper.write(Type.BYTE, operation);
						}
						if (player && key.equals("generic.attackSpeed")) {
							wrapper.user().get(Cooldown.class).setAttackSpeed(value, modifiers);
						}
					}
					wrapper.set(Type.INT, 0, size - removed);
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

				handler(wrapper -> {
					int id = wrapper.get(Type.BYTE, 0);
					if (id > 23) wrapper.cancel();
					if (id == 25) {
						if(wrapper.get(Type.VAR_INT, 0) != wrapper.user().get(EntityTracker.class).getPlayerId()) return;
						Levitation levitation = wrapper.user().get(Levitation.class);
						levitation.setActive(true);
						levitation.setAmplifier(wrapper.get(Type.BYTE, 1));
					}
				});
			}
		});
	}
}
