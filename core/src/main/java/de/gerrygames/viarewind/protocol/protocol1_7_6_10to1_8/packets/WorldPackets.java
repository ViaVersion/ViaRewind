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
import us.myles.ViaVersion.api.remapper.PacketHandler;
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ClientWorld world = packetWrapper.user().get(ClientWorld.class);
						Chunk chunk = packetWrapper.read(new Chunk1_8Type(world));
						packetWrapper.write(new Chunk1_7_10Type(world), chunk);
						for (ChunkSection section : chunk.getSections()){
							if (section == null) continue;
							for (int i = 0; i < section.getPaletteSize(); i++) {
								int block = section.getPaletteEntry(i);
								BlockState state = BlockState.rawToState(block);
								state = ReplacementRegistry1_7_6_10to1_8.replace(state);
								section.setPaletteEntry(i, BlockState.stateToRaw(state));
							}
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						final BlockChangeRecord[] records = packetWrapper.read(Type.BLOCK_CHANGE_RECORD_ARRAY);
						packetWrapper.write(Type.SHORT, (short) records.length);
						packetWrapper.write(Type.INT, records.length * 4);
						for (BlockChangeRecord record : records) {
							short data = (short) (record.getSectionX() << 12 | record.getSectionZ() << 8 | record.getY());
							packetWrapper.write(Type.SHORT, data);
							BlockState state = BlockState.rawToState(record.getBlockId());
							state = ReplacementRegistry1_7_6_10to1_8.replace(state);
							packetWrapper.write(Type.SHORT, (short) BlockState.stateToRaw(state));
						}
					}
				});
			}
		});

		//Block Change
		protocol.registerOutgoing(State.PLAY, 0x23, 0x23, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX());
						packetWrapper.write(Type.UNSIGNED_BYTE, position.getY());
						packetWrapper.write(Type.INT, position.getZ());
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int data = packetWrapper.read(Type.VAR_INT);

						int blockId = data >> 4;
						int meta = data & 0xF;

						BlockState state = ReplacementRegistry1_7_6_10to1_8.replace(new BlockState(blockId, meta));

						blockId = state.getId();
						meta = state.getData();

						packetWrapper.write(Type.VAR_INT, blockId);
						packetWrapper.write(Type.UNSIGNED_BYTE, (short) meta);
					}
				});  //Block Data
			}
		});

		//Block Action
		protocol.registerOutgoing(State.PLAY, 0x24, 0x24, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX());
						packetWrapper.write(Type.SHORT, position.getY());
						packetWrapper.write(Type.INT, position.getZ());
					}
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX());
						packetWrapper.write(Type.INT, (int) position.getY());
						packetWrapper.write(Type.INT, position.getZ());
					}
				});
				map(Type.BYTE);  //Progress
			}
		});

		//Map Chunk Bulk
		protocol.registerOutgoing(State.PLAY, 0x26, 0x26, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ChunkPacketTransformer.transformChunkBulk(packetWrapper);
					}
				});
			}
		});

		//Effect
		protocol.registerOutgoing(State.PLAY, 0x28, 0x28, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX());
						packetWrapper.write(Type.BYTE, (byte) position.getY());
						packetWrapper.write(Type.INT, position.getZ());
					}
				});
				map(Type.INT);
				map(Type.BOOLEAN);
			}
		});

		//Particle
		protocol.registerOutgoing(State.PLAY, 0x2A, 0x2A, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int particleId = packetWrapper.read(Type.INT);
						Particle particle = Particle.find(particleId);
						if (particle == null) particle = Particle.CRIT;
						packetWrapper.write(Type.STRING, particle.name);

						packetWrapper.read(Type.BOOLEAN);
					}
				});
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String name = packetWrapper.get(Type.STRING, 0);
						Particle particle = Particle.find(name);

						if (particle == Particle.ICON_CRACK || particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST) {
							int id = packetWrapper.read(Type.VAR_INT);
							int data = particle == Particle.ICON_CRACK ? packetWrapper.read(Type.VAR_INT) : 0;
							if (id >= 256 && id <= 422 || id >= 2256 && id <= 2267) {  //item
								particle = Particle.ICON_CRACK;
							} else if (id >= 0 && id <= 164 || id >= 170 && id <= 175) {
								if (particle == Particle.ICON_CRACK) particle = Particle.BLOCK_CRACK;
							} else {
								packetWrapper.cancel();
								return;
							}
							name = particle.name + "_" + id + "_" + data;
						}

						packetWrapper.set(Type.STRING, 0, name);
					}
				});
			}
		});

		//Update Sign
		protocol.registerOutgoing(State.PLAY, 0x33, 0x33, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX());
						packetWrapper.write(Type.SHORT, position.getY());
						packetWrapper.write(Type.INT, position.getZ());
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						for (int i = 0; i < 4; i++) {
							String line = packetWrapper.read(Type.STRING);
							line = ChatUtil.jsonToLegacy(line);
							line = ChatUtil.removeUnusedColor(line, '0');
							if (line.length() > 15) {
								line = ChatColor.stripColor(line);
								if (line.length() > 15) line = line.substring(0, 15);
							}
							packetWrapper.write(Type.STRING, line);
						}
					}
				});
			}
		});

		//Map
		protocol.registerOutgoing(State.PLAY, 0x34, 0x34, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.cancel();
						int id = packetWrapper.read(Type.VAR_INT);
						byte scale = packetWrapper.read(Type.BYTE);

						int count = packetWrapper.read(Type.VAR_INT);
						byte[] icons = new byte[count * 4];
						for (int i = 0; i < count; i++) {
							int j = packetWrapper.read(Type.BYTE);
							icons[i * 4] = (byte) (j >> 4 & 0xF);
							icons[i * 4 + 1] = packetWrapper.read(Type.BYTE);
							icons[i * 4 + 2] = packetWrapper.read(Type.BYTE);
							icons[i * 4 + 3] = (byte) (j & 0xF);
						}
						short columns = packetWrapper.read(Type.UNSIGNED_BYTE);
						if (columns > 0) {
							short rows = packetWrapper.read(Type.UNSIGNED_BYTE);
							byte x = packetWrapper.read(Type.BYTE);
							byte z = packetWrapper.read(Type.BYTE);
							byte[] data = packetWrapper.read(Type.BYTE_ARRAY_PRIMITIVE);

							for (int column = 0; column < columns; column++) {
								byte[] columnData = new byte[rows + 3];
								columnData[0] = 0;
								columnData[1] = (byte) (x + column);
								columnData[2] = z;

								for (int i = 0; i < rows; i++) {
									columnData[i + 3] = data[column + i * columns];
								}

								PacketWrapper columnUpdate = new PacketWrapper(0x34, null, packetWrapper.user());
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
							PacketWrapper iconUpdate = new PacketWrapper(0x34, null, packetWrapper.user());
							iconUpdate.write(Type.VAR_INT, id);
							iconUpdate.write(Type.SHORT, (short) iconData.length);
							CustomByteType customByteType = new CustomByteType(iconData.length);
							iconUpdate.write(customByteType, iconData);
							PacketUtil.sendPacket(iconUpdate, Protocol1_7_6_10TO1_8.class, true, true);
						}

						PacketWrapper scaleUpdate = new PacketWrapper(0x34, null, packetWrapper.user());
						scaleUpdate.write(Type.VAR_INT, id);
						scaleUpdate.write(Type.SHORT, (short) 2);
						scaleUpdate.write(new CustomByteType(2), new byte[] {2, scale});
						PacketUtil.sendPacket(scaleUpdate, Protocol1_7_6_10TO1_8.class, true, true);
					}
				});
			}
		});

		//Update Block Entity
		protocol.registerOutgoing(State.PLAY, 0x35, 0x35, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Position position = packetWrapper.read(Type.POSITION);
						packetWrapper.write(Type.INT, position.getX());
						packetWrapper.write(Type.SHORT, position.getY());
						packetWrapper.write(Type.INT, position.getZ());
					}
				});
				map(Type.UNSIGNED_BYTE);  //Action
				map(Type.NBT, Types1_7_6_10.COMPRESSED_NBT);
			}
		});

		//Server Difficulty
		protocol.registerOutgoing(State.PLAY, 0x41, -1, new PacketRemapper() {
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

		//Combat Event
		protocol.registerOutgoing(State.PLAY, 0x42, -1, new PacketRemapper() {
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

		//World Border
		protocol.registerOutgoing(State.PLAY, 0x44, -1, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int action = packetWrapper.read(Type.VAR_INT);
						WorldBorder worldBorder = packetWrapper.user().get(WorldBorder.class);
						if (action == 0) {
							worldBorder.setSize(packetWrapper.read(Type.DOUBLE));
						} else if (action == 1) {
							worldBorder.lerpSize(packetWrapper.read(Type.DOUBLE), packetWrapper.read(Type.DOUBLE), packetWrapper.read(VarLongType.VAR_LONG));
						} else if (action == 2) {
							worldBorder.setCenter(packetWrapper.read(Type.DOUBLE), packetWrapper.read(Type.DOUBLE));
						} else if (action == 3) {
							worldBorder.init(
									packetWrapper.read(Type.DOUBLE), packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(Type.DOUBLE), packetWrapper.read(Type.DOUBLE),
									packetWrapper.read(VarLongType.VAR_LONG),
									packetWrapper.read(Type.VAR_INT),
									packetWrapper.read(Type.VAR_INT), packetWrapper.read(Type.VAR_INT)
							);
						} else if (action == 4) {
							worldBorder.setWarningTime(packetWrapper.read(Type.VAR_INT));
						} else if (action == 5) {
							worldBorder.setWarningBlocks(packetWrapper.read(Type.VAR_INT));
						}

						packetWrapper.cancel();
					}
				});
			}
		});

	}
}
