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
package com.viaversion.viarewind.api.type.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

public class BulkChunkType1_7_6 extends Type<Chunk[]> {

	public static final BulkChunkType1_7_6 TYPE = new BulkChunkType1_7_6();

	public BulkChunkType1_7_6() {
		super(Chunk[].class);
	}

	@Override
	public Chunk[] read(ByteBuf byteBuf) {
		throw new UnsupportedOperationException(); // Not needed, see https://github.com/ViaVersion/ViaLegacy/blob/main/src/main/java/net/raphimc/vialegacy/protocols/release/protocol1_8to1_7_6_10/types/ChunkBulk1_7_6Type.java
	}

	@Override
	public void write(ByteBuf byteBuf, Chunk[] chunks) {
		final int chunkCount = chunks.length;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final int[] chunkX = new int[chunkCount];
		final int[] chunkZ = new int[chunkCount];
		final short[] primaryBitMask = new short[chunkCount];
		final short[] additionalBitMask = new short[chunkCount];

		for (int i = 0; i < chunkCount; i++) {
			final Chunk chunk = chunks[i];
			Pair<byte[], Short> chunkData;
			try {
				chunkData = ChunkType1_7_6.serialize(chunk);
				output.write(chunkData.key());
			} catch (Exception e) {
				throw new RuntimeException("Unable to serialize chunk", e);
			}
			chunkX[i] = chunk.getX();
			chunkZ[i] = chunk.getZ();
			primaryBitMask[i] = (short) chunk.getBitmask();
			additionalBitMask[i] = chunkData.value();
		}
		final byte[] data = output.toByteArray();

		final Deflater deflater = new Deflater();
		byte[] compressedData;
		int compressedSize;
		try {
			deflater.setInput(data, 0, data.length);
			deflater.finish();
			compressedData = new byte[data.length];
			compressedSize = deflater.deflate(compressedData);
		} finally {
			deflater.end();
		}

		byteBuf.writeShort(chunkCount);
		byteBuf.writeInt(compressedSize);

		boolean skyLight = false;
		for (Chunk chunk : chunks) {
			for (ChunkSection section : chunk.getSections()) {
				if (section != null && section.getLight().hasSkyLight()) {
					skyLight = true;
					break;
				}
			}
		}

		byteBuf.writeBoolean(skyLight); // hasSkyLight
		byteBuf.writeBytes(compressedData, 0, compressedSize);

		for (int i = 0; i < chunkCount; i++) {
			byteBuf.writeInt(chunkX[i]);
			byteBuf.writeInt(chunkZ[i]);
			byteBuf.writeShort(primaryBitMask[i]);
			byteBuf.writeShort(additionalBitMask[i]);
		}
	}
}
