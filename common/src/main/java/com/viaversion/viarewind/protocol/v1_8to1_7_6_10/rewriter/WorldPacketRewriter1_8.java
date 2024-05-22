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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.rewriter;

import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.WorldBorderEmulator;
import com.viaversion.viarewind.api.type.chunk.ChunkType1_7_6;
import com.viaversion.viarewind.api.type.chunk.BulkChunkType1_7_6;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data.Particles1_8;
import com.viaversion.viarewind.api.type.RewindTypes;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.FixedByteArrayType;
import com.viaversion.viaversion.api.type.types.chunk.BulkChunkType1_8;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_8;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.IdAndData;

public class WorldPacketRewriter1_8 {

	public static void register(Protocol1_8To1_7_6_10 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.LEVEL_CHUNK, wrapper -> {
			final ClientWorld world = wrapper.user().get(ClientWorld.class);
			final Chunk chunk = wrapper.read(ChunkType1_8.forEnvironment(world.getEnvironment()));
			protocol.getItemRewriter().handleChunk(chunk);

			wrapper.write(ChunkType1_7_6.TYPE, chunk);
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_UPDATE, new PacketHandlers() {
			@Override
			protected void register() {
				map(Types.BLOCK_POSITION1_8, RewindTypes.U_BYTE_POSITION); // Position
				handler(wrapper -> {
					int data = wrapper.read(Types.VAR_INT); // Block data
					data = protocol.getItemRewriter().handleBlockId(data);

					wrapper.write(Types.VAR_INT, IdAndData.getId(data)); // Block id
					wrapper.write(Types.UNSIGNED_BYTE, (short) IdAndData.getData(data)); // Block data
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.CHUNK_BLOCKS_UPDATE, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Chunk x
				map(Types.INT); // Chunk z

				handler(wrapper -> {
					final BlockChangeRecord[] records = wrapper.read(Types.BLOCK_CHANGE_ARRAY);

					wrapper.write(Types.SHORT, (short) records.length); // Record count
					wrapper.write(Types.INT, records.length * 4); // Data array length (position + block id)

					for (BlockChangeRecord record : records) {
						wrapper.write(Types.SHORT, (short) (record.getSectionX() << 12 | record.getSectionZ() << 8 | record.getY())); // Position
						wrapper.write(Types.SHORT, (short) protocol.getItemRewriter().handleBlockId(record.getBlockId())); // Block id
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_EVENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8, RewindTypes.SHORT_POSITION); // Position
				map(Types.UNSIGNED_BYTE); // Type
				map(Types.UNSIGNED_BYTE); // Data
				map(Types.VAR_INT); // Block id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_DESTRUCTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.BLOCK_POSITION1_8, RewindTypes.INT_POSITION); // Position
				map(Types.BYTE); // Progress
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MAP_BULK_CHUNK, wrapper -> {
			final Chunk[] chunks = wrapper.read(BulkChunkType1_8.TYPE);
			for (Chunk chunk : chunks) {
				protocol.getItemRewriter().handleChunk(chunk);
			}

			wrapper.write(BulkChunkType1_7_6.TYPE, chunks);
		});

		protocol.registerClientbound(ClientboundPackets1_8.LEVEL_EVENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT); // Effect id
				map(Types.BLOCK_POSITION1_8, RewindTypes.BYTE_POSITION); // Position
				map(Types.INT); // Data
				map(Types.BOOLEAN); // Disable relative volume
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.LEVEL_PARTICLES, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final int particleId = wrapper.read(Types.INT); // Particle id
					Particles1_8 particle = Particles1_8.find(particleId);
					if (particle == null) {
						particle = Particles1_8.CRIT;
					}
					wrapper.write(Types.STRING, particle.name); // Particle name

				});
				read(Types.BOOLEAN); // Long distance
				map(Types.FLOAT); // X
				map(Types.FLOAT); // Y
				map(Types.FLOAT); // Z
				map(Types.FLOAT); // Offset x
				map(Types.FLOAT); // Offset y
				map(Types.FLOAT); // Offset z
				map(Types.FLOAT); // Particle data
				map(Types.INT); // Particle count
				handler(wrapper -> {
					String name = wrapper.get(Types.STRING, 0);
					Particles1_8 particle = Particles1_8.find(name);

					if (particle == Particles1_8.ICON_CRACK || particle == Particles1_8.BLOCK_CRACK || particle == Particles1_8.BLOCK_DUST) {
						int id = wrapper.read(Types.VAR_INT);
						int data = particle == Particles1_8.ICON_CRACK ? wrapper.read(Types.VAR_INT) : id / 4096;
						id %= 4096;
						if (id >= 256 && id <= 422 || id >= 2256 && id <= 2267) {  //item
							particle = Particles1_8.ICON_CRACK;
						} else if (id >= 0 && id <= 164 || id >= 170 && id <= 175) {
							if (particle == Particles1_8.ICON_CRACK)
								particle = Particles1_8.BLOCK_CRACK;
						} else {
							wrapper.cancel();
							return;
						}
						name = particle.name + "_" + id + "_" + data;
					}

					wrapper.set(Types.STRING, 0, name);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.UPDATE_SIGN, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8, RewindTypes.SHORT_POSITION); // position
				handler(wrapper -> {
					for (int i = 0; i < 4; i++) {
						String line = wrapper.read(Types.STRING);
						line = ChatUtil.jsonToLegacy(wrapper.user(), line);
						line = ChatUtil.removeUnusedColor(line, '0');

						if (line.length() > 15) {
							line = ChatColorUtil.stripColor(line);
							if (line.length() > 15) line = line.substring(0, 15);
						}
						wrapper.write(Types.STRING, line);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.MAP_ITEM_DATA, wrapper -> {
			wrapper.cancel();
			final int id = wrapper.read(Types.VAR_INT);
			final byte scale = wrapper.read(Types.BYTE);

			final int iconCount = wrapper.read(Types.VAR_INT);
			byte[] icons = new byte[iconCount * 4];
			for (int i = 0; i < iconCount; i++) {
				final int directionAndType = wrapper.read(Types.BYTE);

				icons[i * 4] = (byte) (directionAndType >> 4 & 0xF);
				icons[i * 4 + 1] = wrapper.read(Types.BYTE); // x
				icons[i * 4 + 2] = wrapper.read(Types.BYTE); // z
				icons[i * 4 + 3] = (byte) (directionAndType & 0xF);
			}
			final short columns = wrapper.read(Types.UNSIGNED_BYTE);
			if (columns > 0) {
				final short rows = wrapper.read(Types.UNSIGNED_BYTE);
				final short x = wrapper.read(Types.UNSIGNED_BYTE);
				final short z = wrapper.read(Types.UNSIGNED_BYTE);

				final byte[] data = wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
				for (int column = 0; column < columns; column++) {
					byte[] columnData = new byte[rows + 3];
					columnData[0] = 0;
					columnData[1] = (byte) (x + column);
					columnData[2] = (byte) z;

					for (int i = 0; i < rows; i++) {
						columnData[i + 3] = data[column + i * columns];
					}

					final PacketWrapper mapData = PacketWrapper.create(ClientboundPackets1_8.MAP_ITEM_DATA, wrapper.user());
					mapData.write(Types.VAR_INT, id); // map id
					mapData.write(Types.SHORT, (short) columnData.length); // data length
					mapData.write(new FixedByteArrayType(columnData.length), columnData); // data

					mapData.send(Protocol1_8To1_7_6_10.class);
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

				final PacketWrapper mapData = PacketWrapper.create(ClientboundPackets1_8.MAP_ITEM_DATA, wrapper.user());
				mapData.write(Types.VAR_INT, id); // map id
				mapData.write(Types.SHORT, (short) iconData.length); // data length
				mapData.write(new FixedByteArrayType(iconData.length), iconData); // data

				mapData.send(Protocol1_8To1_7_6_10.class);
			}

			// Update scale
			final PacketWrapper mapData = PacketWrapper.create(ClientboundPackets1_8.MAP_ITEM_DATA, wrapper.user());
			mapData.write(Types.VAR_INT, id); // map id
			mapData.write(Types.SHORT, (short) 2); // data length
			mapData.write(new FixedByteArrayType(2), new byte[]{2, scale}); // data

			mapData.send(Protocol1_8To1_7_6_10.class);
		});

		protocol.registerClientbound(ClientboundPackets1_8.BLOCK_ENTITY_DATA, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BLOCK_POSITION1_8, RewindTypes.SHORT_POSITION); // position
				map(Types.UNSIGNED_BYTE); // action
				map(Types.NAMED_COMPOUND_TAG, RewindTypes.COMPRESSED_NBT); // nbt
			}
		});

		protocol.cancelClientbound(ClientboundPackets1_8.CHANGE_DIFFICULTY);
		protocol.cancelClientbound(ClientboundPackets1_8.PLAYER_COMBAT);

		protocol.registerClientbound(ClientboundPackets1_8.SET_BORDER, null, wrapper -> {
			final WorldBorderEmulator emulator = wrapper.user().get(WorldBorderEmulator.class);
			wrapper.cancel();

			final int action = wrapper.read(Types.VAR_INT);
			if (action == 0) { // set size
				emulator.setSize(wrapper.read(Types.DOUBLE)); // radius
			} else if (action == 1) { // lerp size
				emulator.lerpSize(wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), wrapper.read(Types.VAR_LONG)); // old radius, new radius, speed
			} else if (action == 2) { // set center
				emulator.setCenter(wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE)); // x, z
			} else if (action == 3) { // initialize
				emulator.init(
					wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), // x, z
					wrapper.read(Types.DOUBLE), wrapper.read(Types.DOUBLE), // old radius, new radius
					wrapper.read(Types.VAR_LONG) // speed
				);
			}
		});
	}
}
