package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ReplacementRegistry1_8to1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.Effect;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.SoundRemapper;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.types.Chunk1_8Type;
import de.gerrygames.viarewind.storage.BlockState;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.Environment;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk1_8;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

public class WorldPackets {

	public static void register(Protocol protocol) {
		/*  OUTGOING  */

		//Block Break Animation
		protocol.registerOutgoing(State.PLAY, 0x08, 0x25);

		//Update Block Entity
		protocol.registerOutgoing(State.PLAY, 0x09, 0x35, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.UNSIGNED_BYTE);
				map(Type.NBT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						CompoundTag tag = packetWrapper.get(Type.NBT, 0);
						if (tag != null && tag.contains("SpawnData")) {
							String entity = (String) ((CompoundTag) tag.get("SpawnData")).get("id").getValue();
							tag.remove("SpawnData");
							tag.put(new StringTag("entityId", entity));
						}
					}
				});
			}
		});

		//Block Action
		protocol.registerOutgoing(State.PLAY, 0x0A, 0x24, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int block = packetWrapper.get(Type.VAR_INT, 0);
						if (block >= 219 && block <= 234) {
							packetWrapper.set(Type.VAR_INT, 0, block = 130);
						}
					}
				});
			}
		});

		//Block Change
		protocol.registerOutgoing(State.PLAY, 0x0B, 0x23, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.VAR_INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int combined = packetWrapper.get(Type.VAR_INT, 0);
						BlockState state = BlockState.rawToState(combined);
						state = ReplacementRegistry1_8to1_9.replace(state);
						packetWrapper.set(Type.VAR_INT, 0, BlockState.stateToRaw(state));
					}
				});
			}
		});

		//Server Difficulty
		protocol.registerOutgoing(State.PLAY, 0x0D, 0x41);

		//Multi Block Change
		protocol.registerOutgoing(State.PLAY, 0x10, 0x22, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);
				map(Type.BLOCK_CHANGE_RECORD_ARRAY);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						for (BlockChangeRecord record : packetWrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
							BlockState state = BlockState.rawToState(record.getBlockId());
							state = ReplacementRegistry1_8to1_9.replace(state);
							record.setBlockId(BlockState.stateToRaw(state));
						}
					}
				});
			}
		});

		//Named Sound Effect
		protocol.registerOutgoing(State.PLAY, 0x19, 0x29, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						String name = packetWrapper.get(Type.STRING, 0);
						name = SoundRemapper.getOldName(name);
						if (name == null) {
							packetWrapper.cancel();
						} else {
							packetWrapper.set(Type.STRING, 0, name);
						}
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.VAR_INT);
					}
				});
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
			}
		});

		//Explosion
		protocol.registerOutgoing(State.PLAY, 0x1C, 0x27, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int count = packetWrapper.read(Type.INT);
						packetWrapper.write(Type.INT, count);
						for (int i = 0; i < count; i++) {
							packetWrapper.passthrough(Type.UNSIGNED_BYTE);
							packetWrapper.passthrough(Type.UNSIGNED_BYTE);
							packetWrapper.passthrough(Type.UNSIGNED_BYTE);
						}
					}
				});
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
			}
		});

		//Unload Chunk
		protocol.registerOutgoing(State.PLAY, 0x1D, 0x21, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int chunkX = packetWrapper.read(Type.INT);
						int chunkZ = packetWrapper.read(Type.INT);
						ClientWorld world = packetWrapper.user().get(ClientWorld.class);
						packetWrapper.write(new Chunk1_8Type(world), new Chunk1_8(chunkX, chunkZ));
					}
				});
			}
		});

		//Chunk Data
		protocol.registerOutgoing(State.PLAY, 0x20, 0x21, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						ClientWorld world = packetWrapper.user().get(ClientWorld.class);

						Chunk chunk = packetWrapper.read(new Chunk1_9_1_2Type(world));

						for (ChunkSection section : chunk.getSections()){
							if (section == null) continue;
							for (int i = 0; i < section.getPaletteSize(); i++) {
								int block = section.getPaletteEntry(i);
								BlockState state = BlockState.rawToState(block);
								state = ReplacementRegistry1_8to1_9.replace(state);
								section.setPaletteEntry(i, BlockState.stateToRaw(state));
							}
						}

						if (chunk.isFullChunk() && chunk.getBitmask() == 0) {  //This would be an unload packet for 1.8 clients. Just set one air section
							boolean skylight = world.getEnvironment() == Environment.NORMAL;
							ChunkSection[] sections = new ChunkSection[16];
							ChunkSection section = new ChunkSection();
							sections[0] = section;
							section.addPaletteEntry(0);
							if (skylight) section.setSkyLight(new byte[2048]);
							chunk = new Chunk1_8(chunk.getX(), chunk.getZ(), true, 1, sections, chunk.getBiomeData(), chunk.getBlockEntities());
						}

						packetWrapper.write(new Chunk1_8Type(world), chunk);

						final UserConnection user = packetWrapper.user();
						chunk.getBlockEntities().forEach(nbt -> {
							if (!nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z") || !nbt.contains("id")) return;
							Position position = new Position((int) nbt.get("x").getValue(), (short) (int) nbt.get("y").getValue(), (int) nbt.get("z").getValue());
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
				});
			}
		});

		//Effect
		protocol.registerOutgoing(State.PLAY, 0x21, 0x28, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.POSITION);
				map(Type.INT);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int id = packetWrapper.get(Type.INT, 0);
						id = Effect.getOldId(id);
						if (id == -1) {
							packetWrapper.cancel();
							return;
						}
						packetWrapper.set(Type.INT, 0, id);
						if (id == 2001) {
							BlockState state = BlockState.rawToState(packetWrapper.get(Type.INT, 1));
							state = ReplacementRegistry1_8to1_9.replace(state);
							packetWrapper.set(Type.INT, 1, BlockState.stateToRaw(state));
						}
					}
				});
			}
		});

		//Particle
		protocol.registerOutgoing(State.PLAY, 0x22, 0x2A, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int type = packetWrapper.get(Type.INT, 0);
						if (type > 41 && !ViaRewind.getConfig().isReplaceParticles()) {
							packetWrapper.cancel();
							return;
						}
						if (type == 42) { // Dragon Breath
							packetWrapper.set(Type.INT, 0, 24); // Portal
						} else if (type == 43) { // End Rod
							packetWrapper.set(Type.INT, 0, 3); // Firework Spark
						} else if (type == 44) { // Damage Indicator
							packetWrapper.set(Type.INT, 0, 34); // Heart
						} else if (type == 45) { // Sweep Attack
							packetWrapper.set(Type.INT, 0, 1); // Large Explosion
						}
					}
				});
			}
		});

		//Map
		protocol.registerOutgoing(State.PLAY, 0x24, 0x34, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.BOOLEAN);
					}
				});
			}
		});

		//Combat Event
		protocol.registerOutgoing(State.PLAY, 0x2C, 0x42);

		//World Border
		protocol.registerOutgoing(State.PLAY, 0x35, 0x44);

		//Update Time
		protocol.registerOutgoing(State.PLAY, 0x44, 0x03);

		//Update Sign
		protocol.registerOutgoing(State.PLAY, 0x46, 0x33);

		//Sound Effects
		protocol.registerOutgoing(State.PLAY, 0x47, 0x29, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int soundId = packetWrapper.read(Type.VAR_INT);
						String sound = SoundRemapper.oldNameFromId(soundId);
						if (sound == null) {
							packetWrapper.cancel();
						} else {
							packetWrapper.write(Type.STRING, sound);
						}
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						packetWrapper.read(Type.VAR_INT);
					}
				});
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
			}
		});
	}
}
