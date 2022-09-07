package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.List;
import java.util.UUID;

public class EntityPackets {

	public static void register(Protocol1_7_6_10TO1_8 protocol) {

		/*  OUTGOING  */

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EQUIPMENT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.SHORT);  //Slot
				map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM);  //Item
				handler(packetWrapper -> {
					Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					ItemRewriter.toClient(item);
					packetWrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
				});
				handler(packetWrapper -> {
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					int id = packetWrapper.get(Type.INT, 0);
					int limit = tracker.getPlayerId() == id ? 3 : 4;
					if (packetWrapper.get(Type.SHORT, 0) > limit) packetWrapper.cancel();
				});
				handler(packetWrapper -> {
					short slot = packetWrapper.get(Type.SHORT, 0);
					if (packetWrapper.isCancelled()) return;
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					UUID uuid = tracker.getPlayerUUID(packetWrapper.get(Type.INT, 0));
					if (uuid == null) return;

					Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					tracker.setPlayerEquipment(uuid, item, slot);

					GameProfileStorage storage = packetWrapper.user().get(GameProfileStorage.class);
					GameProfileStorage.GameProfile profile = storage.get(uuid);
					if (profile != null && profile.gamemode == 3) packetWrapper.cancel();
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.USE_BED, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				handler(packetWrapper -> {
					Position position = packetWrapper.read(Type.POSITION);
					packetWrapper.write(Type.INT, position.getX());
					packetWrapper.write(Type.UNSIGNED_BYTE, (short) position.getY());
					packetWrapper.write(Type.INT, position.getZ());
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.COLLECT_ITEM, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Collected Entity ID
				map(Type.VAR_INT, Type.INT);  //Collector Entity ID
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_VELOCITY, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.SHORT);  //velX
				map(Type.SHORT);  //velY
				map(Type.SHORT);  //velZ
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.DESTROY_ENTITIES, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					int[] entityIds = packetWrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);

					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					for (int entityId : entityIds) tracker.removeEntity(entityId);

					List<List<Integer>> parts = Lists.partition(Ints.asList(entityIds), Byte.MAX_VALUE);

					for (int i = 0; i < parts.size() - 1; i++) {
						PacketWrapper destroy = PacketWrapper.create(ClientboundPackets1_7.DESTROY_ENTITIES,
								packetWrapper.user());
						destroy.write(Types1_7_6_10.INT_ARRAY, parts.get(i).stream()
								.mapToInt(Integer::intValue).toArray());
						PacketUtil.sendPacket(destroy, Protocol1_7_6_10TO1_8.class);
					}

					packetWrapper.write(Types1_7_6_10.INT_ARRAY, parts.get(parts.size() - 1).stream()
							.mapToInt(Integer::intValue).toArray());
				});  //Entity Id Array
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_MOVEMENT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //x
				map(Type.BYTE);  //y
				map(Type.BYTE);  //z
				map(Type.BOOLEAN, Type.NOTHING);
				handler(packetWrapper -> {
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
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_ROTATION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //yaw
				map(Type.BYTE);  //pitch
				map(Type.BOOLEAN, Type.NOTHING);
				handler(packetWrapper -> {
					int entityId = packetWrapper.get(Type.INT, 0);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					EntityReplacement replacement = tracker.getEntityReplacement(entityId);
					if (replacement != null) {
						packetWrapper.cancel();
						int yaw = packetWrapper.get(Type.BYTE, 0);
						int pitch = packetWrapper.get(Type.BYTE, 1);
						replacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION_AND_ROTATION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //x
				map(Type.BYTE);  //y
				map(Type.BYTE);  //z
				map(Type.BYTE);  //yaw
				map(Type.BYTE);  //pitch
				map(Type.BOOLEAN, Type.NOTHING);
				handler(packetWrapper -> {
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
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_TELEPORT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.INT);  //x
				map(Type.INT);  //y
				map(Type.INT);  //z
				map(Type.BYTE);  //yaw
				map(Type.BYTE);  //pitch
				map(Type.BOOLEAN, Type.NOTHING);
				handler(packetWrapper -> {
					int entityId = packetWrapper.get(Type.INT, 0);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					Entity1_10Types.EntityType type = tracker.getClientEntityTypes().get(entityId);
					if (type == Entity1_10Types.EntityType.MINECART_ABSTRACT) {
						int y = packetWrapper.get(Type.INT, 2);
						y += 12;
						packetWrapper.set(Type.INT, 2, y);
					}
				});
				handler(packetWrapper -> {
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
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_HEAD_LOOK, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Head yaw
				handler(packetWrapper -> {
					int entityId = packetWrapper.get(Type.INT, 0);
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

		protocol.registerClientbound(ClientboundPackets1_8.ATTACH_ENTITY, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
					boolean leash = packetWrapper.get(Type.BOOLEAN, 0);
					if (leash) return;
					int passenger = packetWrapper.get(Type.INT, 0);
					int vehicle = packetWrapper.get(Type.INT, 1);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					tracker.setPassenger(vehicle, passenger);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_METADATA, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Types1_8.METADATA_LIST, Types1_7_6_10.METADATA_LIST);  //Metadata
				handler(wrapper -> {
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
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EFFECT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Effect Id
				map(Type.BYTE);  //Amplifier
				map(Type.VAR_INT, Type.SHORT);  //Duration
				map(Type.BYTE, Type.NOTHING); //Hide Particles
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.REMOVE_ENTITY_EFFECT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				map(Type.BYTE);  //Effect Id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.ENTITY_PROPERTIES, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);  //Entity Id
				handler(packetWrapper -> {
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
				});

			}
		});

		protocol.cancelClientbound(ClientboundPackets1_8.UPDATE_ENTITY_NBT);
	}
}
