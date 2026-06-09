/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
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

import com.viaversion.viarewind.api.compression.ThreadLocalCompressionProvider;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

import java.util.zip.DataFormatException;


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
    public void write(ByteBuf buffer, Chunk[] chunks) {
        final int chunkCount = chunks.length;
        final int[] addBitMasks = new int[chunkCount];
        int totalSize = 0;
        boolean anySkyLight = false;

        for (Chunk chunk : chunks) {
            if (ChunkType1_7_6.hasSkyLight(chunk)) {
                anySkyLight = true;
                break;
            }
        }

        for (int i = 0; i < chunkCount; i++) {
            Chunk chunk = chunks[i];
            addBitMasks[i] = ChunkType1_7_6.getAddBitMask(chunk);
            boolean biomes = chunk.isFullChunk() && chunk.getBiomeData() != null;

            totalSize += ChunkType1_7_6.calcSize(
                chunk.getBitmask(),
                addBitMasks[i],
                anySkyLight,
                biomes
            );
        }

        final ByteBuf uncompressed = buffer.alloc().buffer(totalSize);

        try {
            for (int i = 0; i < chunkCount; i++) {
                Chunk chunk = chunks[i];
                boolean biomes = chunk.isFullChunk() && chunk.getBiomeData() != null;

                ChunkType1_7_6.serialize(
                    chunk,
                    uncompressed,
                    addBitMasks[i],
                    anySkyLight,
                    biomes
                );
            }

            buffer.writeShort(chunkCount);

            // Reserve 4 bytes for the compressed size
            final int sizeIndex = buffer.writerIndex();
            buffer.writeInt(0); // Placeholder for compressed size

            buffer.writeBoolean(anySkyLight);

            // Write compressed data directly to output buffer
            final int compressedStart = buffer.writerIndex();
            try {
                ThreadLocalCompressionProvider.deflate(uncompressed, buffer);
            } catch (DataFormatException e) {
                throw new RuntimeException("Failed to compress bulk chunk data", e);
            }
            final int compressedSize = buffer.writerIndex() - compressedStart;

            // Go back and write the compressed size
            final int endIndex = buffer.writerIndex();
            buffer.writerIndex(sizeIndex);
            buffer.writeInt(compressedSize);
            buffer.writerIndex(endIndex);
        } finally {
            uncompressed.release();
        }

        for (int i = 0; i < chunkCount; i++) {
            Chunk chunk = chunks[i];
            buffer.writeInt(chunk.getX());
            buffer.writeInt(chunk.getZ());
            buffer.writeShort(chunk.getBitmask());
            buffer.writeShort(addBitMasks[i]);
        }
    }
}
