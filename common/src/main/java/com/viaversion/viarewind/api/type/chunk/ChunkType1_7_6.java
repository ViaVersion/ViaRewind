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

import com.viaversion.viarewind.api.minecraft.ExtendedBlockStorage;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.zip.Deflater;

import static com.viaversion.viaversion.api.minecraft.chunks.ChunkSection.SIZE;
import static com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionLight.LIGHT_LENGTH;

public class ChunkType1_7_6 extends Type<Chunk> {

    public static final ChunkType1_7_6 TYPE = new ChunkType1_7_6();

    public ChunkType1_7_6() {
        super(Chunk.class);
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

        final boolean biomes = chunk.isFullChunk() && chunk.getBiomeData() != null;
        final int totalSize = calculateSize(storageArrays, chunk.getBitmask(), biomes);

        final byte[] output = new byte[totalSize];
        int index = 0;

        for (int i = 0; i < storageArrays.length; i++) {
            if ((chunk.getBitmask() & 1 << i) != 0) {
                final byte[] blockLSBArray = storageArrays[i].getBlockLSBArray();
                System.arraycopy(blockLSBArray, 0, output, index, blockLSBArray.length);
                index += blockLSBArray.length;
            }
        }

        for (int i = 0; i < storageArrays.length; i++) {
            if ((chunk.getBitmask() & 1 << i) != 0) {
                final byte[] blockMetadataArray = storageArrays[i].getBlockMetadataArray().getHandle();
                System.arraycopy(blockMetadataArray, 0, output, index, blockMetadataArray.length);
                index += blockMetadataArray.length;
            }
        }

        for (int i = 0; i < storageArrays.length; i++) {
            if ((chunk.getBitmask() & 1 << i) != 0) {
                final byte[] blockLightArray = storageArrays[i].getBlockLightArray().getHandle();
                System.arraycopy(blockLightArray, 0, output, index, blockLightArray.length);
                index += blockLightArray.length;
            }
        }

        for (int i = 0; i < storageArrays.length; i++) {
            if ((chunk.getBitmask() & 1 << i) != 0 && storageArrays[i].getSkyLightArray() != null) {
                final byte[] skyLightArray = storageArrays[i].getSkyLightArray().getHandle();
                System.arraycopy(skyLightArray, 0, output, index, skyLightArray.length);
                index += skyLightArray.length;
            }
        }

        short additionalBitMask = 0;
        for (int i = 0; i < storageArrays.length; i++) {
            if ((chunk.getBitmask() & 1 << i) != 0 && storageArrays[i].hasBlockMSBArray()) {
                additionalBitMask |= (short) (1 << i);
                final byte[] blockMSBArray = storageArrays[i].getOrCreateBlockMSBArray().getHandle();
                System.arraycopy(blockMSBArray, 0, output, index, blockMSBArray.length);
                index += blockMSBArray.length;
            }
        }

        if (biomes) {
            for (int biome : chunk.getBiomeData()) {
                output[index++] = (byte) biome;
            }
        }

        return new Pair<>(output, additionalBitMask);
    }

    private static int calculateSize(final ExtendedBlockStorage[] storageArrays, final int bitmask, final boolean biomes) {
        int totalSize = 0;
        for (int i = 0; i < storageArrays.length; i++) {
            if ((bitmask & 1 << i) != 0) {
                totalSize += SIZE; // Block lsb array
                totalSize += SIZE / 2; // Block metadata array
                totalSize += LIGHT_LENGTH; // Block light array

                if (storageArrays[i].getSkyLightArray() != null) {
                    totalSize += LIGHT_LENGTH;
                }

                if (storageArrays[i].hasBlockMSBArray()) {
                    totalSize += SIZE / 2; // Block msb array
                }
            }
        }
        if (biomes) {
            totalSize += 256;
        }
        return totalSize;
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

}
