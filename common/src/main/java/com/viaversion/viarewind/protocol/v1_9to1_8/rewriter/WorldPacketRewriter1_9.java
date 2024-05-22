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
package com.viaversion.viarewind.protocol.v1_9to1_8.rewriter;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.data.EffectIdMappings1_8;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_8;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_1;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.ClientWorld;

import java.util.ArrayList;

public class WorldPacketRewriter1_9 {

	public static void register(Protocol1_9To1_8 protocol) {
		protocol.registerClientbound(ClientboundPackets1_9.BLOCK_ENTITY_DATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8); // Position
				map(Types.UNSIGNED_BYTE); // Action
				map(Types.NAMED_COMPOUND_TAG); // Tag
				handler(wrapper -> {
					final CompoundTag tag = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);

					if (tag.remove("SpawnData") instanceof CompoundTag spawnData) {
						final Tag id = spawnData.remove("id");
						if (id instanceof StringTag) {
							tag.put("EntityId", id);
						}
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.BLOCK_EVENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8); // Position
				map(Types.UNSIGNED_BYTE); // Byte 1 (depending on block type)
				map(Types.UNSIGNED_BYTE); // Byte 2 (depending on block type)
				map(Types.VAR_INT); // Block
				handler(wrapper -> {
					final int block = wrapper.get(Types.VAR_INT, 0);
					if (block >= 219 && block <= 234) {
						wrapper.set(Types.VAR_INT, 0, 130);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.CUSTOM_SOUND, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING); // Sound name
				handler(wrapper -> {
					String name = wrapper.get(Types.STRING, 0);
					name = protocol.getMappingData().getMappedNamedSound(name);
					if (name == null) {
						wrapper.cancel();
					} else {
						wrapper.set(Types.STRING, 0, name);
					}
				});
				read(Types.VAR_INT); // Sound category
				map(Types.INT); // Effect position x
				map(Types.INT); // Effect position y
				map(Types.INT); // Effect position z
				map(Types.FLOAT); // Volume
				map(Types.UNSIGNED_BYTE); // Pitch
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.FORGET_LEVEL_CHUNK, ClientboundPackets1_8.LEVEL_CHUNK, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final Environment environment = wrapper.user().get(ClientWorld.class).getEnvironment();

					final int chunkX = wrapper.read(Types.INT);
					final int chunkZ = wrapper.read(Types.INT);

					wrapper.write(ChunkType1_8.forEnvironment(environment), new BaseChunk(chunkX, chunkZ, true, false, 0, new ChunkSection[16], null, new ArrayList<>()));
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.LEVEL_CHUNK, new PacketHandlers() {
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

					if (chunk.isFullChunk() && chunk.getBitmask() == 0) { // This would be an unload packet for 1.8 clients. Just set one air section
						boolean skylight = environment == Environment.NORMAL;
						ChunkSection[] sections = new ChunkSection[16];
						ChunkSection section = new ChunkSectionImpl(true);
						sections[0] = section;
						section.palette(PaletteType.BLOCKS).addId(0);
						if (skylight) section.getLight().setSkyLight(new byte[2048]);
						chunk = new BaseChunk(chunk.getX(), chunk.getZ(), true, false, 1, sections, chunk.getBiomeData(), chunk.getBlockEntities());
					}

					wrapper.write(ChunkType1_8.forEnvironment(environment), chunk);

					chunk.getBlockEntities().forEach(nbt -> {
						if (!nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z") || !nbt.contains("id")) {
							return;
						}
						BlockPosition position = new BlockPosition((int) nbt.get("x").getValue(), (int) nbt.get("y").getValue(), (int) nbt.get("z").getValue());
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

						final PacketWrapper blockEntityData = PacketWrapper.create(ClientboundPackets1_9.BLOCK_ENTITY_DATA, wrapper.user());
						blockEntityData.write(Types.BLOCK_POSITION1_8, position);
						blockEntityData.write(Types.UNSIGNED_BYTE, action);
						blockEntityData.write(Types.NAMED_COMPOUND_TAG, nbt);
						blockEntityData.scheduleSend(Protocol1_9To1_8.class, false);
					});
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.LEVEL_EVENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Effect id
				map(Types.BLOCK_POSITION1_8); // Position
				map(Types.INT); // data
				map(Types.BOOLEAN); // disable relative volume
				handler(wrapper -> {
					int id = wrapper.get(Types.INT, 0);
					id = EffectIdMappings1_8.getOldId(id);
					if (id == -1) {
						wrapper.cancel();
						return;
					}
					wrapper.set(Types.INT, 0, id);
					if (id == 2001) {
						int replacedBlock = protocol.getItemRewriter().handleBlockId(wrapper.get(Types.INT, 1));
						wrapper.set(Types.INT, 1, replacedBlock);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.LEVEL_PARTICLES, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Particle id
				handler(wrapper -> {
					int id = wrapper.get(Types.INT, 0);
					if (id > 41 && !ViaRewind.getConfig().isReplaceParticles()) {
						wrapper.cancel();
						return;
					}
					if (id == 42) { // Dragon Breath
						wrapper.set(Types.INT, 0, 24); // Portal
					} else if (id == 43) { // End Rod
						wrapper.set(Types.INT, 0, 3); // Firework Spark
					} else if (id == 44) { // Damage Indicator
						wrapper.set(Types.INT, 0, 34); // Heart
					} else if (id == 45) { // Sweep Attack
						wrapper.set(Types.INT, 0, 1); // Large Explosion
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.MAP_ITEM_DATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Item damage
				map(Types.BYTE); // Scale
				read(Types.BOOLEAN); // Tracking position
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SOUND, ClientboundPackets1_8.CUSTOM_SOUND, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final int soundId = wrapper.read(Types.VAR_INT);
					final String soundName = protocol.getMappingData().soundName(soundId);
					if (soundName == null) {
						wrapper.cancel();
					} else {
						wrapper.write(Types.STRING, protocol.getMappingData().getMappedNamedSound(soundName));
					}
				});
				read(Types.VAR_INT); // Sound category
				map(Types.INT); // Effect position x
				map(Types.INT); // Effect position y
				map(Types.INT); // Effect position z
				map(Types.FLOAT); // Volume
				map(Types.UNSIGNED_BYTE); // Pitch
			}
		});
	}
}
