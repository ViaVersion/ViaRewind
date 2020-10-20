package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks.ChunkPacketTransformer;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ReplacementRegistry1_7_6_10to1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.WorldBorder;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Chunk1_7_10Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Particle;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.types.Chunk1_8Type;
import de.gerrygames.viarewind.storage.BlockState;
import de.gerrygames.viarewind.types.VarLongType;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.PacketUtil;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.CustomByteType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class WorldPackets {

	public static void register(Protocol protocol) {

		/*  OUTGOING  */

		//Chunk Data
		protocol.registerOutgoing(State.PLAY, 0x21, 0x21, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					ClientWorld world = wrapper.user().get(ClientWorld.class);
					Chunk chunk = wrapper.read(new Chunk1_8Type(world));
					wrapper.write(new Chunk1_7_10Type(world), chunk);
					for (ChunkSection section : chunk.getSections()){
						if (section == null) continue;
						for (int i = 0; i < section.getPaletteSize(); i++) {
							int block = section.getPaletteEntry(i);
							BlockState state = BlockState.rawToState(block);
							state = ReplacementRegistry1_7_6_10to1_8.replace(state);
							section.setPaletteEntry(i, BlockState.stateToRaw(state));
						}
					}
				});
			}
		});

		//Multi Block Change
		protocol.registerOutgoing(State.PLAY, 0x22, 0x22, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);

				handler(wrapper -> {
					BlockChangeRecord[] records = wrapper.read(Type.BLOCK_CHANGE_RECORD_ARRAY);
					wrapper.write(Type.SHORT, (short) records.length);
					wrapper.write(Type.INT, records.length * 4);
					for (BlockChangeRecord record : records) {
						short data = (short) (record.getSectionX() << 12 | record.getSectionZ() << 8 | record.getY());
						wrapper.write(Type.SHORT, data);
						BlockState state = BlockState.rawToState(record.getBlockId());
						state = ReplacementRegistry1_7_6_10to1_8.replace(state);
						wrapper.write(Type.SHORT, (short) BlockState.stateToRaw(state));
					}
				});
			}
		});

		//Block Change
		protocol.registerOutgoing(State.PLAY, 0x23, 0x23, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.UNSIGNED_BYTE, position.getY());
					wrapper.write(Type.INT, position.getZ());
				});
				handler(wrapper -> {
					int data = wrapper.read(Type.VAR_INT);

					int blockId = data >> 4;
					int meta = data & 0xF;

					BlockState state = ReplacementRegistry1_7_6_10to1_8.replace(new BlockState(blockId, meta));

					blockId = state.getId();
					meta = state.getData();

					wrapper.write(Type.VAR_INT, blockId);
					wrapper.write(Type.UNSIGNED_BYTE, (short) meta);
				});  //Block Data
			}
		});

		//Block Action
		protocol.registerOutgoing(State.PLAY, 0x24, 0x24, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.SHORT, position.getY());
					wrapper.write(Type.INT, position.getZ());
				});
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.VAR_INT);
			}
		});

		//Block Break Animation
		protocol.registerOutgoing(State.PLAY, 0x25, 0x25, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);  //Entity Id
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.INT, (int) position.getY());
					wrapper.write(Type.INT, position.getZ());
				});
				map(Type.BYTE);  //Progress
			}
		});

		//Map Chunk Bulk
		protocol.registerOutgoing(State.PLAY, 0x26, 0x26, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(ChunkPacketTransformer::transformChunkBulk);
			}
		});

		//Effect
		protocol.registerOutgoing(State.PLAY, 0x28, 0x28, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.BYTE, (byte) position.getY());
					wrapper.write(Type.INT, position.getZ());
				});
				map(Type.INT);
				map(Type.BOOLEAN);
			}
		});

		//Particle
		protocol.registerOutgoing(State.PLAY, 0x2A, 0x2A, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					int particleId = wrapper.read(Type.INT);
					Particle particle = Particle.find(particleId);
					if (particle == null) particle = Particle.CRIT;
					wrapper.write(Type.STRING, particle.name);

					wrapper.read(Type.BOOLEAN);
				});
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.INT);
				handler(wrapper -> {
					String name = wrapper.get(Type.STRING, 0);
					Particle particle = Particle.find(name);

					if (particle == Particle.ICON_CRACK || particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST) {
						int id = wrapper.read(Type.VAR_INT);
						int data = particle == Particle.ICON_CRACK ? wrapper.read(Type.VAR_INT) : 0;
						if (id >= 256 && id <= 422 || id >= 2256 && id <= 2267) {  //item
							particle = Particle.ICON_CRACK;
						} else if (id >= 0 && id <= 164 || id >= 170 && id <= 175) {
							if (particle == Particle.ICON_CRACK) particle = Particle.BLOCK_CRACK;
						} else {
							wrapper.cancel();
							return;
						}
						name = particle.name + "_" + id + "_" + data;
					}

					wrapper.set(Type.STRING, 0, name);
				});
			}
		});

		//Update Sign
		protocol.registerOutgoing(State.PLAY, 0x33, 0x33, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.SHORT, position.getY());
					wrapper.write(Type.INT, position.getZ());
				});

				handler(wrapper -> {
					for (int i = 0; i < 4; i++) {
						String line = wrapper.read(Type.STRING);
						line = ChatUtil.jsonToLegacy(line);
						line = ChatUtil.removeUnusedColor(line, '0');
						if (line.length() > 15) {
							line = ChatColor.stripColor(line);
							if (line.length() > 15) line = line.substring(0, 15);
						}
						wrapper.write(Type.STRING, line);
					}
				});
			}
		});

		//Map
		protocol.registerOutgoing(State.PLAY, 0x34, 0x34, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					wrapper.cancel();
					int id = wrapper.read(Type.VAR_INT);
					byte scale = wrapper.read(Type.BYTE);

					int count = wrapper.read(Type.VAR_INT);
					byte[] icons = new byte[count * 4];
					for (int i = 0; i < count; i++) {
						int j = wrapper.read(Type.BYTE);
						icons[i * 4] = (byte) (j >> 4 & 0xF);
						icons[i * 4 + 1] = wrapper.read(Type.BYTE);
						icons[i * 4 + 2] = wrapper.read(Type.BYTE);
						icons[i * 4 + 3] = (byte) (j & 0xF);
					}
					short columns = wrapper.read(Type.UNSIGNED_BYTE);
					if (columns > 0) {
						short rows = wrapper.read(Type.UNSIGNED_BYTE);
						byte x = wrapper.read(Type.BYTE);
						byte z = wrapper.read(Type.BYTE);
						byte[] data = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);

						for (int column = 0; column < columns; column++) {
							byte[] columnData = new byte[rows + 3];
							columnData[0] = 0;
							columnData[1] = (byte) (x + column);
							columnData[2] = z;

							for (int i = 0; i < rows; i++) {
								columnData[i + 3] = data[column + i * columns];
							}

							PacketWrapper columnUpdate = new PacketWrapper(0x34, null, wrapper.user());
							columnUpdate.write(Type.VAR_INT, id);
							columnUpdate.write(Type.SHORT, (short) columnData.length);
							columnUpdate.write(new CustomByteType(columnData.length), columnData);

							PacketUtil.sendPacket(columnUpdate, Protocol1_7_6_10TO1_8.class, true, true);
						}
					}

					if (count > 0) {
						byte[] iconData = new byte[count * 3 + 1];
						iconData[0] = 1;
						for (int i = 0; i < count; i++) {
							iconData[i * 3 + 1] = (byte) (icons[i * 4] << 4 | icons[i * 4 + 3] & 0xF);
							iconData[i * 3 + 2] = icons[i * 4 + 1];
							iconData[i * 3 + 3] = icons[i * 4 + 2];
						}
						PacketWrapper iconUpdate = new PacketWrapper(0x34, null, wrapper.user());
						iconUpdate.write(Type.VAR_INT, id);
						iconUpdate.write(Type.SHORT, (short) iconData.length);
						CustomByteType customByteType = new CustomByteType(iconData.length);
						iconUpdate.write(customByteType, iconData);
						PacketUtil.sendPacket(iconUpdate, Protocol1_7_6_10TO1_8.class, true, true);
					}

					PacketWrapper scaleUpdate = new PacketWrapper(0x34, null, wrapper.user());
					scaleUpdate.write(Type.VAR_INT, id);
					scaleUpdate.write(Type.SHORT, (short) 2);
					scaleUpdate.write(new CustomByteType(2), new byte[] {2, scale});
					PacketUtil.sendPacket(scaleUpdate, Protocol1_7_6_10TO1_8.class, true, true);
				});
			}
		});

		//Update Block Entity
		protocol.registerOutgoing(State.PLAY, 0x35, 0x35, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					Position position = wrapper.read(Type.POSITION);
					wrapper.write(Type.INT, position.getX());
					wrapper.write(Type.SHORT, position.getY());
					wrapper.write(Type.INT, position.getZ());
				});
				map(Type.UNSIGNED_BYTE);  //Action
				map(Type.NBT, Types1_7_6_10.COMPRESSED_NBT);
			}
		});

		//Server Difficulty
		protocol.cancelOutgoing(State.PLAY, 0x41);

		//Combat Event
		protocol.cancelOutgoing(State.PLAY, 0x42);

		//World Border
		protocol.registerOutgoing(State.PLAY, 0x44, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					int action = wrapper.read(Type.VAR_INT);
					WorldBorder worldBorder = wrapper.user().get(WorldBorder.class);
					if (action == 0) {
						worldBorder.setSize(wrapper.read(Type.DOUBLE));
					} else if (action == 1) {
						worldBorder.lerpSize(wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(VarLongType.VAR_LONG));
					} else if (action == 2) {
						worldBorder.setCenter(wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE));
					} else if (action == 3) {
						worldBorder.init(
								wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE),
								wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE),
								wrapper.read(VarLongType.VAR_LONG),
								wrapper.read(Type.VAR_INT),
								wrapper.read(Type.VAR_INT), wrapper.read(Type.VAR_INT)
						);
					} else if (action == 4) {
						worldBorder.setWarningTime(wrapper.read(Type.VAR_INT));
					} else if (action == 5) {
						worldBorder.setWarningBlocks(wrapper.read(Type.VAR_INT));
					}

					wrapper.cancel();
				});
			}
		});

	}
}
