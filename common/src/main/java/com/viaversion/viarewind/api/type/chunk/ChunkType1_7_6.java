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

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import java.util.zip.Deflater;

import static com.viaversion.viaversion.api.minecraft.chunks.ChunkSection.SIZE;
import static com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionLight.LIGHT_LENGTH;

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
    public void write(ByteBuf buffer, Chunk chunk) {
        final int bitmask = chunk.getBitmask();
        final int addBitmask = getAddBitMask(chunk);
        final boolean hasSkyLight = hasSkyLight(chunk);
        final boolean biomes = chunk.isFullChunk() && chunk.getBiomeData() != null;

        final int size = calcSize(bitmask, addBitmask, hasSkyLight, biomes);
        final byte[] data = new byte[size];

        serialize(chunk, data, 0, addBitmask, hasSkyLight, biomes);

        buffer.writeInt(chunk.getX());
        buffer.writeInt(chunk.getZ());
        buffer.writeBoolean(chunk.isFullChunk());
        buffer.writeShort(bitmask);
        buffer.writeShort(addBitmask);

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

        buffer.writeInt(compressedSize);
        buffer.writeBytes(compressedData, 0, compressedSize);
    }

    public static int serialize(Chunk chunk, byte[] output, int offset, int addBitmask, boolean writeSkyLight, boolean biomes) {
        final ChunkSection[] sections = chunk.getSections();
        final int bitmask = chunk.getBitmask();

        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                final ChunkSection section = sections[i];
                final DataPalette palette = section.palette(PaletteType.BLOCKS);
                for (int j = 0; j < SIZE; j++) {
                    final int block = palette.idAt(j);
                    output[offset++] = (byte) ((block >> 4) & 0xFF);
                }
            }
        }

        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                final ChunkSection section = sections[i];
                final DataPalette palette = section.palette(PaletteType.BLOCKS);
                for (int j = 0; j < ChunkSection.SIZE; j += 2) {
                    final int meta1 = palette.idAt(j) & 0xF;
                    final int meta2 = palette.idAt(j + 1) & 0xF;
                    output[offset++] = (byte) (meta1 | (meta2 << 4));
                }
            }
        }

        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                final byte[] blockLight = sections[i].getLight().getBlockLight();
                System.arraycopy(blockLight, 0, output, offset, LIGHT_LENGTH);
                offset += LIGHT_LENGTH;
            }
        }

        if (writeSkyLight) {
            for (int i = 0; i < 16; i++) {
                if ((bitmask & (1 << i)) != 0) {
                    if (sections[i].getLight().hasSkyLight()) {
                        final byte[] skyLight = sections[i].getLight().getSkyLight();
                        System.arraycopy(skyLight, 0, output, offset, LIGHT_LENGTH);
                    }
                    offset += LIGHT_LENGTH;
                }
            }
        }

        if (addBitmask != 0) {
            for (int i = 0; i < 16; i++) {
                if ((bitmask & (1 << i)) != 0 && (addBitmask & (1 << i)) != 0) {
                    final ChunkSection section = sections[i];
                    final DataPalette palette = section.palette(PaletteType.BLOCKS);
                    for (int j = 0; j < SIZE; j += 2) {
                        final int add1 = (palette.idAt(j) >> 12) & 0xF;
                        final int add2 = (palette.idAt(j + 1) >> 12) & 0xF;
                        output[offset++] = (byte) (add1 | (add2 << 4));
                    }
                }
            }
        }

        if (biomes && chunk.getBiomeData() != null) {
            final int[] biomeData = chunk.getBiomeData();
            for (int biome : biomeData) {
                output[offset++] = (byte) biome;
            }
        }

        return offset;
    }

    public static int calcSize(int bitmask, int addBitmask, boolean hasSkyLight, boolean biomes) {
        int size = 0;
        int sections = Integer.bitCount(bitmask);

        size += sections * SIZE;
        size += sections * (SIZE / 2);
        size += sections * LIGHT_LENGTH;

        if (hasSkyLight) {
            size += sections * LIGHT_LENGTH;
        }

        if (addBitmask != 0) {
            size += Integer.bitCount(addBitmask) * (SIZE / 2);
        }

        if (biomes) {
            size += 256;
        }

        return size;
    }

    public static int getAddBitMask(Chunk chunk) {
        int addBitMask = 0;
        for (int i = 0; i < 16; i++) {
            if ((chunk.getBitmask() & (1 << i)) != 0) {
                final ChunkSection section = chunk.getSections()[i];
                final DataPalette palette = section.palette(PaletteType.BLOCKS);
                for (int j = 0; j < SIZE; j++) {
                    final int id = palette.idAt(j);
                    if ((id >> 12) != 0) {
                        addBitMask |= (1 << i);
                        break;
                    }
                }
            }
        }
        return addBitMask;
    }

    public static boolean hasSkyLight(Chunk chunk) {
        for (ChunkSection section : chunk.getSections()) {
            if (section != null && section.getLight().hasSkyLight()) {
                return true;
            }
        }
        return false;
    }
}
