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
import us.myles.ViaVersion.api.remapper.PacketRemapper;
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
				handler(wrapper -> wrapper.read(Type.UUID));
				map(Type.BYTE);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.DOUBLE, Protocol1_8TO1_9.TO_OLD_INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.INT);

				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					final int typeId = wrapper.get(Type.BYTE, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					final Entity1_10Types.EntityType type = Entity1_10Types.getTypeFromId(typeId, true);

					//cancel AREA_EFFECT_CLOUD = 3, SPECTRAL_ARROW = 91, DRAGON_FIREBALL = 93
					if (typeId == 3 || typeId == 91 || typeId == 92 || typeId == 93) {
						wrapper.cancel();
						return;
					}

					if (type == null) {
						ViaRewind.getPlatform().getLogger().warning("[ViaRewind] Unhandled Spawn Object Type: " + typeId);
						wrapper.cancel();
						return;
					}

					int x = wrapper.get(Type.INT, 0);
					int y = wrapper.get(Type.INT, 1);
					int z = wrapper.get(Type.INT, 2);

					if (type.is(Entity1_10Types.EntityType.BOAT)) {
						byte yaw = wrapper.get(Type.BYTE, 1);
						yaw -= 64;
						wrapper.set(Type.BYTE, 1, yaw);
						y += 10;
						wrapper.set(Type.INT, 1, y);
					} else if (type.is(Entity1_10Types.EntityType.SHULKER_BULLET)) {
						wrapper.cancel();
						ShulkerBulletReplacement shulkerBulletReplacement = new ShulkerBulletReplacement(entityId, wrapper.user());
						shulkerBulletReplacement.setLocation(x / 32.0, y / 32.0, z / 32.0);
						tracker.addEntityReplacement(shulkerBulletReplacement);
						return;
					}

					int data = wrapper.get(Type.INT, 3);

					//Rewrite Object Data
					if (type.isOrHasParent(Entity1_10Types.EntityType.ARROW) && data != 0) {
						wrapper.set(Type.INT, 3, --data);
					}
					if (type.is(Entity1_10Types.EntityType.FALLING_BLOCK)) {
						BlockState state = new BlockState(data & 0xFFF, data >> 12 & 0xF);
						state = ReplacementRegistry1_8to1_9.replace(state);
						wrapper.set(Type.INT, 3, state.getId() | state.getData() << 12);
					}

					if (data > 0) {
						wrapper.passthrough(Type.SHORT);
						wrapper.passthrough(Type.SHORT);
						wrapper.passthrough(Type.SHORT);
					} else {
						short vX = wrapper.read(Type.SHORT);
						short vY = wrapper.read(Type.SHORT);
						short vZ = wrapper.read(Type.SHORT);
						PacketWrapper velocityPacket = new PacketWrapper(0x12, null, wrapper.user());
						velocityPacket.write(Type.VAR_INT, entityId);
						velocityPacket.write(Type.SHORT, vX);
						velocityPacket.write(Type.SHORT, vY);
						velocityPacket.write(Type.SHORT, vZ);
						PacketUtil.sendPacket(velocityPacket, Protocol1_8TO1_9.class);
					}

					tracker.getClientEntityTypes().put(entityId, type);
					tracker.sendMetadataBuffer(entityId);
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

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.EXPERIENCE_ORB);
					tracker.sendMetadataBuffer(entityId);
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

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.LIGHTNING);
					tracker.sendMetadataBuffer(entityId);
				});
			}
		});

		//Spawn Mob
		protocol.registerOutgoing(State.PLAY, 0x03, 0x0F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(wrapper -> wrapper.read(Type.UUID));
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

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					int typeId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					int x = wrapper.get(Type.INT, 0);
					int y = wrapper.get(Type.INT, 1);
					int z = wrapper.get(Type.INT, 2);
					byte pitch = wrapper.get(Type.BYTE, 1);
					byte yaw = wrapper.get(Type.BYTE, 0);
					byte headYaw = wrapper.get(Type.BYTE, 2);

					if (typeId == 69) {
						wrapper.cancel();
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						ShulkerReplacement shulkerReplacement = new ShulkerReplacement(entityId, wrapper.user());
						shulkerReplacement.setLocation(x / 32.0, y / 32.0, z / 32.0);
						shulkerReplacement.setYawPitch(yaw * 360f / 256, pitch * 360f / 256);
						shulkerReplacement.setHeadYaw(headYaw * 360f / 256);
						tracker.addEntityReplacement(shulkerReplacement);
					} else if (typeId == -1 || typeId == 255) {
						wrapper.cancel();
					}
				});
				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					int typeId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.getClientEntityTypes().put(entityId, Entity1_10Types.getTypeFromId(typeId, false));
					tracker.sendMetadataBuffer(entityId);
				});
				handler(wrapper -> {
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
				});
			}
		});

		//Spawn Painting
		protocol.registerOutgoing(State.PLAY, 0x04, 0x10, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				handler(wrapper -> wrapper.read(Type.UUID));
				map(Type.STRING);
				map(Type.POSITION);
				map(Type.BYTE, Type.UNSIGNED_BYTE);

				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.PAINTING);
					tracker.sendMetadataBuffer(entityId);
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
				create(wrapper -> wrapper.write(Type.SHORT, (short) 0));
				map(Types1_9.METADATA_LIST, Types1_8.METADATA_LIST);

				handler(wrapper -> {
					List<Metadata> metadataList = wrapper.get(Types1_8.METADATA_LIST, 0);
					MetadataRewriter.transform(Entity1_10Types.EntityType.PLAYER, metadataList);
				});
				handler(wrapper -> {
					int entityId = wrapper.get(Type.VAR_INT, 0);
					EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					tracker.getClientEntityTypes().put(entityId, Entity1_10Types.EntityType.PLAYER);
					tracker.sendMetadataBuffer(entityId);
				});
			}
		});
	}
}
