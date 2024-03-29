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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viabackwards.utils.Block;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.WorldBorderEmulator;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.chunk.ChunkType1_7_6;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.chunk.BulkChunkType1_7_6;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.data.ParticleIndex1_7_6_10;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.FixedByteArrayType;
import com.viaversion.viaversion.api.type.types.chunk.BulkChunkType1_8;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.util.ChatColorUtil;

public class WorldPackets1_8 {

	public static void register(Protocol1_7_6_10To1_8 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.CHUNK_DATA, wrapper -> {
			final ClientWorld world = wrapper.user().get(ClientWorld.class);
			final Chunk chunk = wrapper.read(ChunkType1_8.forEnvironment(world.getEnvironment()));
			protocol.getItemRewriter().handleChunk(chunk);

			wrapper.write(ChunkType1_7_6.TYPE, chunk);
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_CHANGE, new PacketHandlers() {
			@Override
			protected void register() {
				map(Type.POSITION1_8, Types1_7_6_10.U_BYTE_POSITION); // position
				handler(wrapper -> {
					int data = wrapper.read(Type.VAR_INT); // block data
					data = protocol.getItemRewriter().handleBlockId(data);

					wrapper.write(Type.VAR_INT, Block.getId(data)); // block id
					wrapper.write(Type.UNSIGNED_BYTE, (short) Block.getData(data)); // block data
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MULTI_BLOCK_CHANGE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // chunk x
				map(Type.INT); // chunk z

				handler(wrapper -> {
					final BlockChangeRecord[] records = wrapper.read(Type.BLOCK_CHANGE_RECORD_ARRAY);

					wrapper.write(Type.SHORT, (short) records.length); // record count
					wrapper.write(Type.INT, records.length * 4); // data array length (position + block id)

					for (BlockChangeRecord record : records) {
						wrapper.write(Type.SHORT, (short) (record.getSectionX() << 12 | record.getSectionZ() << 8 | record.getY())); // position
						wrapper.write(Type.SHORT, (short) protocol.getItemRewriter().handleBlockId(record.getBlockId())); // block id
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8, Types1_7_6_10.SHORT_POSITION); // position
				map(Type.UNSIGNED_BYTE); // type
				map(Type.UNSIGNED_BYTE); // data
				map(Type.VAR_INT); // block id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_BREAK_ANIMATION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				map(Type.POSITION1_8, Types1_7_6_10.INT_POSITION); // position
				map(Type.BYTE); // progress
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MAP_BULK_CHUNK, wrapper -> {
			final Chunk[] chunks = wrapper.read(BulkChunkType1_8.TYPE);
			for (Chunk chunk : chunks) {
				protocol.getItemRewriter().handleChunk(chunk);
			}

			wrapper.write(BulkChunkType1_7_6.TYPE, chunks);
		});

		protocol.registerClientbound(ClientboundPackets1_8.EFFECT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // effect id
				map(Type.POSITION1_8, Types1_7_6_10.BYTE_POSITION); // position
				map(Type.INT); // data
				map(Type.BOOLEAN); // disable relative volume
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SPAWN_PARTICLE, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final int particleId = wrapper.read(Type.INT); // particle id
					ParticleIndex1_7_6_10 particle = ParticleIndex1_7_6_10.find(particleId);
					if (particle == null) particle = ParticleIndex1_7_6_10.CRIT;
					wrapper.write(Type.STRING, particle.name); // particle name

				});
				read(Type.BOOLEAN); // long distance
				map(Type.FLOAT); // x
				map(Type.FLOAT); // y
				map(Type.FLOAT); // z
				map(Type.FLOAT); // offset x
				map(Type.FLOAT); // offset y
				map(Type.FLOAT); // offset z
				map(Type.FLOAT); // particle data
				map(Type.INT); // particle count
				handler(wrapper -> {
					String name = wrapper.get(Type.STRING, 0);
					ParticleIndex1_7_6_10 particle = ParticleIndex1_7_6_10.find(name);

					if (particle == ParticleIndex1_7_6_10.ICON_CRACK || particle == ParticleIndex1_7_6_10.BLOCK_CRACK || particle == ParticleIndex1_7_6_10.BLOCK_DUST) {
						int id = wrapper.read(Type.VAR_INT);
						int data = particle == ParticleIndex1_7_6_10.ICON_CRACK ? wrapper.read(Type.VAR_INT) : id / 4096;
						id %= 4096;
						if (id >= 256 && id <= 422 || id >= 2256 && id <= 2267) {  //item
							particle = ParticleIndex1_7_6_10.ICON_CRACK;
						} else if (id >= 0 && id <= 164 || id >= 170 && id <= 175) {
							if (particle == ParticleIndex1_7_6_10.ICON_CRACK)
								particle = ParticleIndex1_7_6_10.BLOCK_CRACK;
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

		protocol.registerClientbound(ClientboundPackets1_8.UPDATE_SIGN, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8, Types1_7_6_10.SHORT_POSITION); // position
				handler(wrapper -> {
					for (int i = 0; i < 4; i++) {
						String line = wrapper.read(Type.STRING);
						line = ChatUtil.jsonToLegacy(line);
						line = ChatUtil.removeUnusedColor(line, '0');

						if (line.length() > 15) {
							line = ChatColorUtil.stripColor(line);
							if (line.length() > 15) line = line.substring(0, 15);
						}
						wrapper.write(Type.STRING, line);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MAP_DATA, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					wrapper.cancel();
					final int id = wrapper.read(Type.VAR_INT);
					final byte scale = wrapper.read(Type.BYTE);

					final int iconCount = wrapper.read(Type.VAR_INT);
					byte[] icons = new byte[iconCount * 4];
					for (int i = 0; i < iconCount; i++) {
						final int directionAndType = wrapper.read(Type.BYTE);

						icons[i * 4] = (byte) (directionAndType >> 4 & 0xF);
						icons[i * 4 + 1] = wrapper.read(Type.BYTE); // x
						icons[i * 4 + 2] = wrapper.read(Type.BYTE); // z
						icons[i * 4 + 3] = (byte) (directionAndType & 0xF);
					}
					final short columns = wrapper.read(Type.UNSIGNED_BYTE);
					if (columns > 0) {
						final short rows = wrapper.read(Type.UNSIGNED_BYTE);
						final short x = wrapper.read(Type.UNSIGNED_BYTE);
						final short z = wrapper.read(Type.UNSIGNED_BYTE);

						final byte[] data = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
						for (int column = 0; column < columns; column++) {
							byte[] columnData = new byte[rows + 3];
							columnData[0] = 0;
							columnData[1] = (byte) (x + column);
							columnData[2] = (byte) z;

							for (int i = 0; i < rows; i++) {
								columnData[i + 3] = data[column + i * columns];
							}

							final PacketWrapper mapData = PacketWrapper.create(ClientboundPackets1_8.MAP_DATA, wrapper.user());
							mapData.write(Type.VAR_INT, id); // map id
							mapData.write(Type.SHORT, (short) columnData.length); // data length
							mapData.write(new FixedByteArrayType(columnData.length), columnData); // data

							mapData.send(Protocol1_7_6_10To1_8.class, true);
						}
					}

					if (iconCount > 0) {
						final byte[] iconData = new byte[iconCount * 3 + 1];
						iconData[0] = 1;
						for (int i = 0; i < iconCount; i++) {
							iconData[i * 3 + 1] = (byte) (icons[i * 4] << 4 | icons[i * 4 + 3] & 0xF);
							iconData[i * 3 + 2] = icons[i * 4 + 1];
							iconData[i * 3 + 3] = icons[i * 4 + 2];
						}

						final PacketWrapper mapData = PacketWrapper.create(ClientboundPackets1_8.MAP_DATA, wrapper.user());
						mapData.write(Type.VAR_INT, id); // map id
						mapData.write(Type.SHORT, (short) iconData.length); // data length
						mapData.write(new FixedByteArrayType(iconData.length), iconData); // data

						mapData.send(Protocol1_7_6_10To1_8.class, true);
					}

					// Update scale
					final PacketWrapper mapData = PacketWrapper.create(ClientboundPackets1_8.MAP_DATA, wrapper.user());
					mapData.write(Type.VAR_INT, id); // map id
					mapData.write(Type.SHORT, (short) 2); // data length
					mapData.write(new FixedByteArrayType(2), new byte[]{2, scale}); // data

					mapData.send(Protocol1_7_6_10To1_8.class, true);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_ENTITY_DATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.POSITION1_8, Types1_7_6_10.SHORT_POSITION); // position
				map(Type.UNSIGNED_BYTE); // action
				map(Type.NAMED_COMPOUND_TAG, Types1_7_6_10.COMPRESSED_NBT); // nbt
			}
		});

		protocol.cancelClientbound(ClientboundPackets1_8.SERVER_DIFFICULTY);
		protocol.cancelClientbound(ClientboundPackets1_8.COMBAT_EVENT);

		protocol.registerClientbound(ClientboundPackets1_8.WORLD_BORDER, null, wrapper -> {
			final WorldBorderEmulator emulator = wrapper.user().get(WorldBorderEmulator.class);
			wrapper.cancel();

			final int action = wrapper.read(Type.VAR_INT);
			if (action == 0) { // set size
				emulator.setSize(wrapper.read(Type.DOUBLE)); // radius
			} else if (action == 1) { // lerp size
				emulator.lerpSize(wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), wrapper.read(Type.VAR_LONG)); // old radius, new radius, speed
			} else if (action == 2) { // set center
				emulator.setCenter(wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE)); // x, z
			} else if (action == 3) { // initialize
				emulator.init(
					wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), // x, z
					wrapper.read(Type.DOUBLE), wrapper.read(Type.DOUBLE), // old radius, new radius
					wrapper.read(Type.VAR_LONG) // speed
				);
			}
		});
	}
}
