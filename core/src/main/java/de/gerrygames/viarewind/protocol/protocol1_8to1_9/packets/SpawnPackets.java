package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement.ShulkerBulletReplacement;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.entityreplacement.ShulkerReplacement;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ReplacementRegistry1_8to1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import de.gerrygames.viarewind.storage.BlockState;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;
import us.myles.ViaVersion.api.type.types.version.Types1_9;
import us.myles.ViaVersion.packets.State;

import java.util.List;

public class SpawnPackets {

	public static void register(Protocol protocol) {
		/*  OUTGOING  */

		//Spawn Object
		protocol.registerOutgoing(State.PLAY, 0x00, 0x0E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.UUID);
					}
				});
				map(Type.BYTE);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.INT);

				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						final int entityId = packetWrapper.get(Type.VAR_INT, 0);
						final int typeId = packetWrapper.get(Type.BYTE, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						final Entity1_10Types.EntityType type = Entity1_10Types.getTypeFromId(typeId, true);

						//cancel AREA_EFFECT_CLOUD = 3, SPECTRAL_ARROW = 91, DRAGON_FIREBALL = 93
						if (typeId == 3 || typeId == 91 || typeId == 92 || typeId == 93) {
							packetWrapper.cancel();
							return;
						}

						if (type == null) {
							ViaRewind.getPlatform().getLogger().warning("[ViaRewind] Unhandled Spawn Object Type: " + typeId);
							packetWrapper.cancel();
							return;
						}

						int x = packetWrapper.get(Type.INT, 0);
						int y = packetWrapper.get(Type.INT, 1);
						int z = packetWrapper.get(Type.INT, 2);

						if (type.is(Entity1_10Types.EntityType.BOAT)) {
							byte yaw = packetWrapper.get(Type.BYTE, 1);
							yaw -= 64;
							packetWrapper.set(Type.BYTE, 1, yaw);
							y += 10;
							packetWrapper.set(Type.INT, 1, y);
						} else if (type.is(Entity1_10Types.EntityType.SHULKER_BULLET)) {
							packetWrapper.cancel();
							ShulkerBulletReplacement shulkerBulletReplacement = new ShulkerBulletReplacement(entityId, packetWrapper.user());
							shulkerBulletReplacement.setLocation(x / 32.0, y / 32.0, z / 32.0);
							tracker.addEntityReplacement(shulkerBulletReplacement);
							return;
						}

						int data = packetWrapper.get(Type.INT, 3);

						//Rewrite Object Data
						if (type.isOrHasParent(Entity1_10Types.EntityType.ARROW) && data != 0) {
							packetWrapper.set(Type.INT, 3, --data);
						}
						if (type.is(Entity1_10Types.EntityType.FALLING_BLOCK)) {
							BlockState state = new BlockState(data & 0xFFF, data >> 12 & 0xF);
							state = ReplacementRegistry1_8to1_9.replace(state);
							packetWrapper.set(Type.INT, 3, state.getId() | state.getData() << 12);
						}

						if (data > 0) {
							packetWrapper.passthrough(Type.SHORT);
							packetWrapper.passthrough(Type.SHORT);
							packetWrapper.passthrough(Type.SHORT);
						} else {
							short vX = packetWrapper.read(Type.SHORT);
							short vY = packetWrapper.read(Type.SHORT);
							short vZ = packetWrapper.read(Type.SHORT);
							PacketWrapper velocityPacket = new PacketWrapper(0x12, null, packetWrapper.user());
							velocityPacket.write(Type.VAR_INT, entityId);
							velocityPacket.write(Type.SHORT, vX);
							velocityPacket.write(Type.SHORT, vY);
							velocityPacket.write(Type.SHORT, vZ);
							PacketUtil.sendPacket(velocityPacket, Protocol1_8TO1_9.class);
						}

						tracker.getClientEntityTypes().put(entityId, type);
						tracker.sendMetadataBuffer(entityId);
					}
				});
			}
		});

		//Spawn Experience Orb
		protocol.registerOutgoing(State.PLAY, 0x01, 0x11, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.SHORT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.EXPERIENCE_ORB);
						tracker.sendMetadataBuffer(entityId);
					}
				});
			}
		});

		//Spawn Global Entity
		protocol.registerOutgoing(State.PLAY, 0x02, 0x2C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.LIGHTNING);
						tracker.sendMetadataBuffer(entityId);
					}
				});
			}
		});

		//Spawn Mob
		protocol.registerOutgoing(State.PLAY, 0x03, 0x0F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.UUID);
					}
				});
				map(Type.UNSIGNED_BYTE);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.SHORT);
				map(Type.SHORT);
				map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						int typeId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
						int x = packetWrapper.get(Type.INT, 0);
						int y = packetWrapper.get(Type.INT, 1);
						int z = packetWrapper.get(Type.INT, 2);
						byte pitch = packetWrapper.get(Type.BYTE, 1);
						byte yaw = packetWrapper.get(Type.BYTE, 0);
						byte headYaw = packetWrapper.get(Type.BYTE, 2);

						if (typeId == 69) {
							packetWrapper.cancel();
							EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
							ShulkerReplacement shulkerReplacement = new ShulkerReplacement(entityId, packetWrapper.user());
							shulkerReplacement.setLocation(x / 32.0, y / 32.0, z / 32.0);
							shulkerReplacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
							shulkerReplacement.setHeadYaw(headYaw * 360f / 256);
							tracker.addEntityReplacement(shulkerReplacement);
						} else if (typeId == -1 || typeId == 255) {
							packetWrapper.cancel();
						}
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						int typeId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityId, Entity1_10Types.getTypeFromId(typeId, false));
						tracker.sendMetadataBuffer(entityId);
					}
				});
				handler(new PacketHandler() {
					public void handle(PacketWrapper wrapper) throws Exception {
						List<Metadata> metadataList = wrapper.get(Types1_8.METADATA_LIST, 0);
						int entityId = wrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						if (tracker.getEntityReplacement(entityId) != null) {
							tracker.getEntityReplacement(entityId).updateMetadata(metadataList);
						} else if (tracker.getClientEntityTypes().containsKey(entityId)) {
							MetadataRewriter.transform(tracker.getClientEntityTypes().get(entityId), metadataList);
						} else {
							wrapper.cancel();
						}
					}
				});
			}
		});

		//Spawn Painting
		protocol.registerOutgoing(State.PLAY, 0x04, 0x10, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.UUID);
					}
				});
				map(Type.STRING);
				map(Type.POSITION);
				map(Type.BYTE, Type.UNSIGNED_BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.PAINTING);
						tracker.sendMetadataBuffer(entityId);
					}
				});
			}
		});

		//Spawn Player
		protocol.registerOutgoing(State.PLAY, 0x05, 0x0C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.UUID);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.write(Type.SHORT, (short) 0);
					}
				});
				map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST);
				this.handler(new PacketHandler() {
					public void handle(PacketWrapper wrapper) throws Exception {
						List<Metadata> metadataList = wrapper.get(Types1_8.METADATA_LIST, 0);
						MetadataRewriter.transform(Entity1_10Types.EntityType.PLAYER, metadataList);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int entityId = packetWrapper.get(Type.VAR_INT, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.PLAYER);
						tracker.sendMetadataBuffer(entityId);
					}
				});
			}
		});
	}
}
