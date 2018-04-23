package de.gerrygames.viarewind.protocol.protocol1_8to1_9.chunks;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ReplacementRegistry1_8to1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.types.Chunk1_8Type;
import de.gerrygames.viarewind.storage.BlockState;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;

public class ChunkPacketTransformer {
	public static void transformChunk(PacketWrapper packetWrapper) throws Exception {
		ClientWorld world = packetWrapper.user().get(ClientWorld.class);

		Chunk chunk = packetWrapper.read(new Chunk1_9_1_2Type(world));
		packetWrapper.write(new Chunk1_8Type(world), chunk);

		for (int i = 0; i < chunk.getSections().length; i++) {
			if ((chunk.getBitmask() & 1 << i) == 0) continue;
			ChunkSection section = chunk.getSections()[i];
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						int raw = section.getBlock(x, y, z);
						BlockState state = BlockState.rawToState(raw);
						state = ReplacementRegistry1_8to1_9.replace(state);
						section.setBlock(x, y, z, state.getId(), state.getData());
					}
				}
			}
		}

		final UserConnection user = packetWrapper.user();
		chunk.getBlockEntities().forEach(nbt -> {
			if (!nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z") || !nbt.contains("id")) return;
			Position position = new Position((long) (int) nbt.get("x").getValue(), (long) (int) nbt.get("y").getValue(), (long) (int) nbt.get("z").getValue());
			String id = (String) nbt.get("id").getValue();

			short action;
			switch (id) {
				case "minecraft:mob_spawner":
					action = 1;
					break;
				case "minecraft:command_block":
					action = 2;
					break;
				case "minecraft:beacon":
					action = 3;
					break;
				case "minecraft:skull":
					action = 4;
					break;
				case "minecraft:flower_pot":
					action = 5;
					break;
				case "minecraft:banner":
					action = 6;
					break;
				default:
					return;
			}

			PacketWrapper updateTileEntity = new PacketWrapper(0x09, null, user);
			updateTileEntity.write(Type.POSITION, position);
			updateTileEntity.write(Type.UNSIGNED_BYTE, action);
			updateTileEntity.write(Type.NBT, nbt);

			PacketUtil.sendPacket(updateTileEntity, Protocol1_8TO1_9.class, false, false);
		});
	}
}
