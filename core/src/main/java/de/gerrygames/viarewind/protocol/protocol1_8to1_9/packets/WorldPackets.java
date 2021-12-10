package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.types.Chunk1_8Type;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ReplacementRegistry1_8to1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.Effect;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound.SoundRemapper;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.ArrayList;

public class WorldPackets {

	public static void register(Protocol<ClientboundPackets1_9, ClientboundPackets1_8,
			ServerboundPackets1_9, ServerboundPackets1_8> protocol) {
		/*  OUTGOING  */

		//Block Break Animation

		//Update Block Entity
		protocol.registerClientbound(ClientboundPackets1_9.BLOCK_ENTITY_DATA, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.UNSIGNED_BYTE);
				map(Type.NBT);
				handler(packetWrapper -> {
					CompoundTag tag = packetWrapper.get(Type.NBT, 0);
					if (tag != null && tag.contains("SpawnData")) {
						String entity = (String) ((CompoundTag) tag.get("SpawnData")).get("id").getValue();
						tag.remove("SpawnData");
						tag.put("entityId", new StringTag(entity));
					}
				});
			}
		});

		//Block Action
		protocol.registerClientbound(ClientboundPackets1_9.BLOCK_ACTION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.VAR_INT);
				handler(packetWrapper -> {
					int block = packetWrapper.get(Type.VAR_INT, 0);
					if (block >= 219 && block <= 234) {
						packetWrapper.set(Type.VAR_INT, 0, block = 130);
					}
				});
			}
		});

		//Block Change
		protocol.registerClientbound(ClientboundPackets1_9.BLOCK_CHANGE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.POSITION);
				map(Type.VAR_INT);
				handler(packetWrapper -> {
					int combined = packetWrapper.get(Type.VAR_INT, 0);
					int replacedCombined = ReplacementRegistry1_8to1_9.replace(combined);
					packetWrapper.set(Type.VAR_INT, 0, replacedCombined);
				});
			}
		});

		//Server Difficulty

		//Multi Block Change
		protocol.registerClientbound(ClientboundPackets1_9.MULTI_BLOCK_CHANGE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.INT);
				map(Type.BLOCK_CHANGE_RECORD_ARRAY);
				handler(packetWrapper -> {
					for (BlockChangeRecord record : packetWrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
						int replacedCombined = ReplacementRegistry1_8to1_9.replace(record.getBlockId());
						record.setBlockId(replacedCombined);
					}
				});
			}
		});

		//Named Sound Effect
		protocol.registerClientbound(ClientboundPackets1_9.NAMED_SOUND, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				handler(packetWrapper -> {
					String name = packetWrapper.get(Type.STRING, 0);
					name = SoundRemapper.getOldName(name);
					if (name == null) {
						packetWrapper.cancel();
					} else {
						packetWrapper.set(Type.STRING, 0, name);
					}
				});
				map(Type.VAR_INT, Type.NOTHING);
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
			}
		});

		//Explosion
		protocol.registerClientbound(ClientboundPackets1_9.EXPLOSION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
				handler(packetWrapper -> {
					int count = packetWrapper.read(Type.INT);
					packetWrapper.write(Type.INT, count);
					for (int i = 0; i < count; i++) {
						packetWrapper.passthrough(Type.UNSIGNED_BYTE);
						packetWrapper.passthrough(Type.UNSIGNED_BYTE);
						packetWrapper.passthrough(Type.UNSIGNED_BYTE);
					}
				});
				map(Type.FLOAT);
				map(Type.FLOAT);
				map(Type.FLOAT);
			}
		});

		//Unload Chunk
		protocol.registerClientbound(ClientboundPackets1_9.UNLOAD_CHUNK, ClientboundPackets1_8.CHUNK_DATA, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					int chunkX = packetWrapper.read(Type.INT);
					int chunkZ = packetWrapper.read(Type.INT);
					ClientWorld world = packetWrapper.user().get(ClientWorld.class);
					packetWrapper.write(new Chunk1_8Type(world), new BaseChunk(chunkX, chunkZ, true, false, 0, new ChunkSection[16], null, new ArrayList<>()));
				});
			}
		});

		//Chunk Data
		protocol.registerClientbound(ClientboundPackets1_9.CHUNK_DATA, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					ClientWorld world = packetWrapper.user().get(ClientWorld.class);

					Chunk chunk = packetWrapper.read(new Chunk1_9_1_2Type(world));

					for (ChunkSection section : chunk.getSections()) {
						if (section == null) continue;
						for (int i = 0; i < section.getPaletteSize(); i++) {
							int block = section.getPaletteEntry(i);
							int replacedBlock = ReplacementRegistry1_8to1_9.replace(block);
							section.setPaletteEntry(i, replacedBlock);
						}
					}

					if (chunk.isFullChunk() && chunk.getBitmask() == 0) {  //This would be an unload packet for 1.8 clients. Just set one air section
						boolean skylight = world.getEnvironment() == Environment.NORMAL;
						ChunkSection[] sections = new ChunkSection[16];
						ChunkSection section = new ChunkSectionImpl(true);
						sections[0] = section;
						section.addPaletteEntry(0);
						if (skylight) section.getLight().setSkyLight(new byte[2048]);
						chunk = new BaseChunk(chunk.getX(), chunk.getZ(), true, false, 1, sections, chunk.getBiomeData(), chunk.getBlockEntities());
					}

					packetWrapper.write(new Chunk1_8Type(world), chunk);

					final UserConnection user = packetWrapper.user();
					chunk.getBlockEntities().forEach(nbt -> {
						if (!nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z") || !nbt.contains("id"))
							return;
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

						PacketWrapper updateTileEntity = PacketWrapper.create(0x09, null, user);
						updateTileEntity.write(Type.POSITION, position);
						updateTileEntity.write(Type.UNSIGNED_BYTE, action);
						updateTileEntity.write(Type.NBT, nbt);

						PacketUtil.sendPacket(updateTileEntity, Protocol1_8TO1_9.class, false, false);
					});
				});
			}
		});

		//Effect
		protocol.registerClientbound(ClientboundPackets1_9.EFFECT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				map(Type.POSITION);
				map(Type.INT);
				map(Type.BOOLEAN);
				handler(packetWrapper -> {
					int id = packetWrapper.get(Type.INT, 0);
					id = Effect.getOldId(id);
					if (id == -1) {
						packetWrapper.cancel();
						return;
					}
					packetWrapper.set(Type.INT, 0, id);
					if (id == 2001) {
						int replacedBlock = ReplacementRegistry1_8to1_9.replace(packetWrapper.get(Type.INT, 1));
						packetWrapper.set(Type.INT, 1, replacedBlock);
					}
				});
			}
		});

		//Particle
		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_PARTICLE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(packetWrapper -> {
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
				});
			}
		});

		//Map
		protocol.registerClientbound(ClientboundPackets1_9.MAP_DATA, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				map(Type.BOOLEAN, Type.NOTHING);
			}
		});

		//Combat Event
		//World Border
		//Update Time
		//Update Sign

		//Sound Effects
		protocol.registerClientbound(ClientboundPackets1_9.SOUND, ClientboundPackets1_8.NAMED_SOUND, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					int soundId = packetWrapper.read(Type.VAR_INT);
					String sound = SoundRemapper.oldNameFromId(soundId);
					if (sound == null) {
						packetWrapper.cancel();
					} else {
						packetWrapper.write(Type.STRING, sound);
					}
				});
				handler(packetWrapper -> packetWrapper.read(Type.VAR_INT));
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
			}
		});
	}
}
