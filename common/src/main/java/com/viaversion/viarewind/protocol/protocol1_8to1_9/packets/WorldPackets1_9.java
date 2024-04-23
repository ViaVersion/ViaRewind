/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viaversion.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.sound.Effect;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.sound.SoundRemapper;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_8;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_1;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;

import java.util.ArrayList;

public class WorldPackets1_9 {

	public static void register(Protocol1_8To1_9 protocol) {
		protocol.registerClientbound(ClientboundPackets1_9.BLOCK_ENTITY_DATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8);
				map(Type.UNSIGNED_BYTE);
				map(Type.NAMED_COMPOUND_TAG);
				handler(wrapper -> {
					CompoundTag tag = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);
					if (tag != null && tag.contains("SpawnData")) {
						CompoundTag spawnData = tag.get("SpawnData");
						if (spawnData.contains("id")) {
							String entity = (String) spawnData.get("id").getValue();
							tag.remove("SpawnData");
							tag.put("entityId", new StringTag(entity));
						}
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.BLOCK_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8);
				map(Type.UNSIGNED_BYTE);
				map(Type.UNSIGNED_BYTE);
				map(Type.VAR_INT);
				handler(wrapper -> {
					int block = wrapper.get(Type.VAR_INT, 0);
					if (block >= 219 && block <= 234) {
						wrapper.set(Type.VAR_INT, 0, 130);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.NAMED_SOUND, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				handler(wrapper -> {
					String name = wrapper.get(Type.STRING, 0);
					name = SoundRemapper.getOldName(name);
					if (name == null) {
						wrapper.cancel();
					} else {
						wrapper.set(Type.STRING, 0, name);
					}
				});
				read(Type.VAR_INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.UNLOAD_CHUNK, ClientboundPackets1_8.CHUNK_DATA, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final Environment environment = wrapper.user().get(ClientWorld.class).getEnvironment();

					final int chunkX = wrapper.read(Type.INT);
					final int chunkZ = wrapper.read(Type.INT);

					wrapper.write(ChunkType1_8.forEnvironment(environment), new BaseChunk(chunkX, chunkZ, true, false, 0, new ChunkSection[16], null, new ArrayList<>()));
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.CHUNK_DATA, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final Environment environment = wrapper.user().get(ClientWorld.class).getEnvironment();

					Chunk chunk = wrapper.read(ChunkType1_9_1.forEnvironment(environment));

					for (ChunkSection section : chunk.getSections()) {
						if (section == null) continue;
						DataPalette palette = section.palette(PaletteType.BLOCKS);
						for (int i = 0; i < palette.size(); i++) {
							int block = palette.idByIndex(i);
							int replacedBlock = protocol.getItemRewriter().handleBlockId(block);
							palette.setIdByIndex(i, replacedBlock);
						}
					}

					if (chunk.isFullChunk() && chunk.getBitmask() == 0) {  //This would be an unload packet for 1.8 clients. Just set one air section
						boolean skylight = environment == Environment.NORMAL;
						ChunkSection[] sections = new ChunkSection[16];
						ChunkSection section = new ChunkSectionImpl(true);
						sections[0] = section;
						section.palette(PaletteType.BLOCKS).addId(0);
						if (skylight) section.getLight().setSkyLight(new byte[2048]);
						chunk = new BaseChunk(chunk.getX(), chunk.getZ(), true, false, 1, sections, chunk.getBiomeData(), chunk.getBlockEntities());
					}

					wrapper.write(ChunkType1_8.forEnvironment(environment), chunk);

					final UserConnection user = wrapper.user();
					chunk.getBlockEntities().forEach(nbt -> {
						if (!nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z") || !nbt.contains("id"))
							return;
						Position position = new Position((int) nbt.get("x").getValue(), (int) nbt.get("y").getValue(), (int) nbt.get("z").getValue());
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
						updateTileEntity.write(Type.POSITION1_8, position);
						updateTileEntity.write(Type.UNSIGNED_BYTE, action);
						updateTileEntity.write(Type.NBT, nbt);

						try {
							updateTileEntity.scheduleSend(Protocol1_8To1_9.class, false);
						} catch (Exception e) {
							ViaRewind.getPlatform().getLogger().warning("Error sending tile entity update packet: " + e.getMessage());
						}
					});
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.EFFECT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT);
				map(Type.POSITION1_8);
				map(Type.INT);
				map(Type.BOOLEAN);
				handler(wrapper -> {
					int id = wrapper.get(Type.INT, 0);
					id = Effect.getOldId(id);
					if (id == -1) {
						wrapper.cancel();
						return;
					}
					wrapper.set(Type.INT, 0, id);
					if (id == 2001) {
						int replacedBlock = protocol.getItemRewriter().handleBlockId(wrapper.get(Type.INT, 1));
						wrapper.set(Type.INT, 1, replacedBlock);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SPAWN_PARTICLE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT);
				handler(wrapper -> {
					int type = wrapper.get(Type.INT, 0);
					if (type > 41 && !ViaRewind.getConfig().isReplaceParticles()) {
						wrapper.cancel();
						return;
					}
					if (type == 42) { // Dragon Breath
						wrapper.set(Type.INT, 0, 24); // Portal
					} else if (type == 43) { // End Rod
						wrapper.set(Type.INT, 0, 3); // Firework Spark
					} else if (type == 44) { // Damage Indicator
						wrapper.set(Type.INT, 0, 34); // Heart
					} else if (type == 45) { // Sweep Attack
						wrapper.set(Type.INT, 0, 1); // Large Explosion
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.MAP_DATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT);
				map(Type.BYTE);
				read(Type.BOOLEAN);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SOUND, ClientboundPackets1_8.NAMED_SOUND, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					int soundId = wrapper.read(Type.VAR_INT);
					String sound = SoundRemapper.oldNameFromId(soundId);
					if (sound == null) {
						wrapper.cancel();
					} else {
						wrapper.write(Type.STRING, sound);
					}
				});
				handler(wrapper -> wrapper.read(Type.VAR_INT));
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.FLOAT);
				map(Type.UNSIGNED_BYTE);
			}
		});
	}
}
