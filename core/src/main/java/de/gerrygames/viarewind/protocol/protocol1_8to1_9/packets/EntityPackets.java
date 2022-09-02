package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.util.Pair;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9.VALID_ATTRIBUTES;

public class EntityPackets {

	public static void register(Protocol<ClientboundPackets1_9, ClientboundPackets1_8,
			ServerboundPackets1_9, ServerboundPackets1_8> protocol) {
		/*  OUTGOING  */

		//Entity Status
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_STATUS, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(packetWrapper -> {
					byte status = packetWrapper.read(Type.BYTE);
					if (status > 23) {
						packetWrapper.cancel();
						return;
					}
					packetWrapper.write(Type.BYTE, status);
				});
			}
		});

		//Entity Relative Move
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_POSITION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(packetWrapper -> {
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
						PacketWrapper secondPacket = PacketWrapper.create(0x15, null, packetWrapper.user());
						secondPacket.write(Type.VAR_INT, packetWrapper.get(Type.VAR_INT, 0));
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
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_POSITION_AND_ROTATION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(packetWrapper -> {
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
						PacketWrapper secondPacket = PacketWrapper.create(0x17, null, packetWrapper.user());
						secondPacket.write(Type.VAR_INT, packetWrapper.get(Type.VAR_INT, 0));
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
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_ROTATION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
					int entityId = packetWrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						packetWrapper.cancel();
						int yaw = packetWrapper.get(Type.BYTE, 0);
						int pitch = packetWrapper.get(Type.BYTE, 1);
						replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
				handler(packetWrapper -> {
					int entityId = packetWrapper.get(Type.VAR_INT, 0);
					Entity1_10Types.EntityType type = packetWrapper.user().get(EntityTracker.class).getClientEntityTypes().get(entityId);
					if (type == Entity1_10Types.EntityType.BOAT) {
						byte yaw = packetWrapper.get(Type.BYTE, 0);
						yaw -= 64;
						packetWrapper.set(Type.BYTE, 0, yaw);
					}
				});
			}
		});

		//Entity

		//Vehicle Move -> Entity Teleport
		protocol.registerClientbound(ClientboundPackets1_9.VEHICLE_MOVE, ClientboundPackets1_8.ENTITY_TELEPORT, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					int vehicle = tracker.getVehicle(tracker.getPlayerId());
					if (vehicle == -1) packetWrapper.cancel();
					packetWrapper.write(Type.VAR_INT, vehicle);
				});
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.FLOAT, Protocol1_8TO1_9.DEGREES_TO_ANGLE);
				map(Type.FLOAT, Protocol1_8TO1_9.DEGREES_TO_ANGLE);
				handler(packetWrapper -> {
					if (packetWrapper.isCancelled()) return;
					PlayerPosition position = packetWrapper.user().get(PlayerPosition.class);
					double x = packetWrapper.get(Type.INT, 0) / 32d;
					double y = packetWrapper.get(Type.INT, 1) / 32d;
					double z = packetWrapper.get(Type.INT, 2) / 32d;
					position.setPos(x, y, z);
				});
				create(Type.BOOLEAN, true);
				handler(packetWrapper -> {
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
				});
			}
		});

		//Use Bed

		//Destroy Entities
		protocol.registerClientbound(ClientboundPackets1_9.DESTROY_ENTITIES, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT_ARRAY_PRIMITIVE);
				handler(packetWrapper -> {
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					for (int entityId : packetWrapper.get(Type.VAR_INT_ARRAY_PRIMITIVE, 0))
						tracker.removeEntity(entityId);
				});
			}
		});

		//Remove Entity Effect
		protocol.registerClientbound(ClientboundPackets1_9.REMOVE_ENTITY_EFFECT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				handler(packetWrapper -> {
					int id = packetWrapper.get(Type.BYTE, 0);
					if (id > 23) packetWrapper.cancel();
					if (id == 25) {
						if (packetWrapper.get(Type.VAR_INT, 0) != packetWrapper.user().get(EntityTracker.class).getPlayerId())
							return;
						Levitation levitation = packetWrapper.user().get(Levitation.class);
						levitation.setActive(false);
					}
				});
			}
		});

		//Entity Head Look
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_HEAD_LOOK, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				handler(packetWrapper -> {
					int entityId = packetWrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						packetWrapper.cancel();
						int yaw = packetWrapper.get(Type.BYTE, 0);
						replacement.setHeadYaw(yaw * 360f / 256);
					}
				});
			}
		});

		//Entity Metadata
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_METADATA, new PacketRemapper() {
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
		protocol.registerClientbound(ClientboundPackets1_9.ATTACH_ENTITY, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);
				create(Type.BOOLEAN, true);
			}
		});

		//Entity Velocity

		//Entity Equipment
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_EQUIPMENT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(packetWrapper -> {
					// todo check if this is correct for the own player
					int slot = packetWrapper.read(Type.VAR_INT);
					if (slot == 1) {
						packetWrapper.cancel();
					} else if (slot > 1) {
						slot -= 1;
					}
					packetWrapper.write(Type.SHORT, (short) slot);
				});
				map(Type.ITEM);
				handler(packetWrapper -> packetWrapper.set(Type.ITEM, 0, ItemRewriter.toClient(packetWrapper.get(Type.ITEM, 0))));
			}
		});

		//Set Passengers
		protocol.registerClientbound(ClientboundPackets1_9.SET_PASSENGERS, null, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
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
							PacketWrapper detach = PacketWrapper.create(0x1B, null, packetWrapper.user());
							detach.write(Type.INT, passenger);
							detach.write(Type.INT, -1);
							detach.write(Type.BOOLEAN, false);
							PacketUtil.sendPacket(detach, Protocol1_8TO1_9.class);
						}
					}
					for (int i = 0; i < count; i++) {
						int v = i == 0 ? vehicle : passengers.get(i - 1);
						int p = passengers.get(i);
						PacketWrapper attach = PacketWrapper.create(0x1B, null, packetWrapper.user());
						attach.write(Type.INT, p);
						attach.write(Type.INT, v);
						attach.write(Type.BOOLEAN, false);
						PacketUtil.sendPacket(attach, Protocol1_8TO1_9.class);
					}
				});
			}
		});

		//Collect Item

		//Entity Teleport
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_TELEPORT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
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
				});
				handler(packetWrapper -> {
					int entityId = packetWrapper.get(Type.VAR_INT, 0);
					packetWrapper.user().get(EntityTracker.class).resetEntityOffset(entityId);
				});
				handler(packetWrapper -> {
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
				});
			}
		});

		//Entity Properties
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_PROPERTIES, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.INT);
				handler(packetWrapper -> {
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
				});
			}
		});

		//Entity Effect
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_EFFECT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.VAR_INT);
				map(Type.BYTE);
				handler(packetWrapper -> {
					int id = packetWrapper.get(Type.BYTE, 0);
					if (id > 23) packetWrapper.cancel();
					if (id == 25) {
						if (packetWrapper.get(Type.VAR_INT, 0) != packetWrapper.user().get(EntityTracker.class).getPlayerId())
							return;
						Levitation levitation = packetWrapper.user().get(Levitation.class);
						levitation.setActive(true);
						levitation.setAmplifier(packetWrapper.get(Type.BYTE, 1));
					}
				});
			}
		});
	}
}
