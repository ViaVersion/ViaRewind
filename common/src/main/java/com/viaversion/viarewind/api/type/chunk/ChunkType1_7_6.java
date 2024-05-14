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

import com.viaversion.viarewind.api.minecraft.ExtendedBlockStorage;
import com.viaversion.viaversion.api.minecraft.chunks.*;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class ChunkType1_7_6 extends Type<Chunk> {

	public static final ChunkType1_7_6 TYPE = new ChunkType1_7_6();

	public ChunkType1_7_6() {
		super(Chunk.class);
	}

	@Override
	public Chunk read(ByteBuf byteBuf) {
		throw new UnsupportedOperationException(); // Not needed, see https://github.com/ViaVersion/ViaLegacy/blob/main/src/main/java/net/raphimc/vialegacy/protocols/release/protocol1_8to1_7_6_10/types/Chunk1_7_6Type.java
	}

	@Override
	public void write(ByteBuf output, Chunk chunk) {
		Pair<byte[], Short> chunkData;
		try {
			chunkData = serialize(chunk);
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialize chunk", e);
		}
		final byte[] data = chunkData.key();
		final short additionalBitMask = chunkData.value();

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

		output.writeInt(chunk.getX());
		output.writeInt(chunk.getZ());
		output.writeBoolean(chunk.isFullChunk());
		output.writeShort(chunk.getBitmask());
		output.writeShort(additionalBitMask);
		output.writeInt(compressedSize);
		output.writeBytes(compressedData, 0, compressedSize);
	}

	public static Pair<byte[], Short> serialize(final Chunk chunk) throws IOException {
		final ExtendedBlockStorage[] storageArrays = new ExtendedBlockStorage[16];
		for (int i = 0; i < storageArrays.length; i++) {
			final ChunkSection section = chunk.getSections()[i];
			if (section != null) {
				final ExtendedBlockStorage storage = storageArrays[i] = new ExtendedBlockStorage(section.getLight().hasSkyLight());
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						for (int y = 0; y < 16; y++) {
							final int flatBlock = section.palette(PaletteType.BLOCKS).idAt(x, y, z);
							storage.setBlockId(x, y, z, flatBlock >> 4);
							storage.setBlockMetadata(x, y, z, flatBlock & 15);
						}
					}
				}
				storage.getBlockLightArray().setHandle(section.getLight().getBlockLight());
				if (section.getLight().hasSkyLight()) {
					storage.getSkyLightArray().setHandle(section.getLight().getSkyLight());
				}
			}
		}

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		for (int i = 0; i < storageArrays.length; i++) {
			if ((chunk.getBitmask() & 1 << i) != 0) {
				output.write(storageArrays[i].getBlockLSBArray());
			}
		}

		for (int i = 0; i < storageArrays.length; i++) {
			if ((chunk.getBitmask() & 1 << i) != 0) {
				output.write(storageArrays[i].getBlockMetadataArray().getHandle());
			}
		}

		for (int i = 0; i < storageArrays.length; i++) {
			if ((chunk.getBitmask() & 1 << i) != 0) {
				output.write(storageArrays[i].getBlockLightArray().getHandle());
			}
		}

		for (int i = 0; i < storageArrays.length; i++) {
			if ((chunk.getBitmask() & 1 << i) != 0 && storageArrays[i].getSkyLightArray() != null) {
				output.write(storageArrays[i].getSkyLightArray().getHandle());
			}
		}

		short additionalBitMask = 0;
		for (int i = 0; i < storageArrays.length; i++) {
			if ((chunk.getBitmask() & 1 << i) != 0 && storageArrays[i].hasBlockMSBArray()) {
				additionalBitMask |= (short) (1 << i);
				output.write(storageArrays[i].getOrCreateBlockMSBArray().getHandle());
			}
		}

		if (chunk.isFullChunk() && chunk.getBiomeData() != null) {
			for (int biome : chunk.getBiomeData()) {
				output.write(biome);
			}
		}

		return new Pair<>(output.toByteArray(), additionalBitMask);
	}
}
