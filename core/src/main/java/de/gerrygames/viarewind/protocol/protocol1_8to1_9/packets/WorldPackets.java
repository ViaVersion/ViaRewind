package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.chunks.ChunkPacketTransformer;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ReplacementRegistry1_8to1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.Effect;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.SoundRemapper;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.types.Chunk1_8Type;
import de.gerrygames.viarewind.storage.BlockState;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chunks.Chunk1_9to1_8;
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
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int size = packetWrapper.passthrough(Type.VAR_INT);
						for (int i = 0; i < size; i++) {
							packetWrapper.passthrough(Type.UNSIGNED_BYTE);
							packetWrapper.passthrough(Type.UNSIGNED_BYTE);
							int combined = packetWrapper.read(Type.VAR_INT);
							BlockState state = BlockState.rawToState(combined);
							state = ReplacementRegistry1_8to1_9.replace(state);
							packetWrapper.write(Type.VAR_INT, BlockState.stateToRaw(state));
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
						packetWrapper.write(new Chunk1_8Type(world), new Chunk1_9to1_8(chunkX, chunkZ));
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
						ChunkPacketTransformer.transformChunk(packetWrapper);
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
		protocol.registerOutgoing(State.PLAY, 0x46, 0x33, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.STRING);
				map(Type.STRING);
				map(Type.STRING);
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						for (int i = 0; i < 4; i++) {
							String text = packetWrapper.get(Type.STRING, i);
							packetWrapper.set(Type.STRING, i, text);
						}
					}
				});
			}
		});

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
