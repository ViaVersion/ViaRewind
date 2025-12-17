/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2025 ViaVersion and contributors
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

import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
    public void write(ByteBuf byteBuf, Chunk[] chunks) {
        final int chunkCount = chunks.length;
        final int[] chunkX = new int[chunkCount];
        final int[] chunkZ = new int[chunkCount];
        final short[] primaryBitMask = new short[chunkCount];
        final short[] additionalBitMask = new short[chunkCount];

        final byte[][] dataArrays = new byte[chunkCount][];
        int dataSize = 0;

        for (int i = 0; i < chunkCount; i++) {
            final Chunk chunk = chunks[i];
            Pair<byte[], Short> chunkData;
            try {
                chunkData = ChunkType1_7_6.serialize(chunk);
                final byte[] data = chunkData.key();
                dataArrays[i] = data;
                dataSize += data.length;
            } catch (Exception e) {
                throw new RuntimeException("Unable to serialize chunk", e);
            }
            chunkX[i] = chunk.getX();
            chunkZ[i] = chunk.getZ();
            primaryBitMask[i] = (short) chunk.getBitmask();
            additionalBitMask[i] = chunkData.value();
        }

        final byte[] data = new byte[dataSize];
        int destPos = 0;
        for (final byte[] array : dataArrays) {
            System.arraycopy(array, 0, data, destPos, array.length);
            destPos += array.length;
        }

        byteBuf.writeShort(chunkCount);
        final int sizeIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(sizeIndex + 4);

        boolean skyLight = false;
        for (Chunk chunk : chunks) {
            for (ChunkSection section : chunk.getSections()) {
                if (section != null && section.getLight().hasSkyLight()) {
                    skyLight = true;
                    break;
                }
            }
        }
        byteBuf.writeBoolean(skyLight);

        final int startCompressIndex = byteBuf.writerIndex();
        try {
            Protocol1_8To1_7_6_10.COMPRESSOR_THREAD_LOCAL.get().deflate(Unpooled.wrappedBuffer(data), byteBuf);
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }

        final int endCompressIndex = byteBuf.writerIndex();
        final int compressedSize = endCompressIndex - startCompressIndex;
        byteBuf.setInt(sizeIndex, compressedSize);

        for (int i = 0; i < chunkCount; i++) {
            byteBuf.writeInt(chunkX[i]);
            byteBuf.writeInt(chunkZ[i]);
            byteBuf.writeShort(primaryBitMask[i]);
            byteBuf.writeShort(additionalBitMask[i]);
        }
    }
}
