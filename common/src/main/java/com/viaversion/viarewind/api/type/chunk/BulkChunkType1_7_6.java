/* BulkChunkType1_7_6.java - PATCHED */
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
        throw new UnsupportedOperationException();
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
            primaryBitMask[i] = (short) (chunk.getBitmask() & 0xFFFF); // Clamp to 16 sections
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

        byteBuf.writeBoolean(true); // Force sky light to true for 1.8 compatibility
        byteBuf.writeBytes(compressedData, 0, compressedSize);

        for (int i = 0; i < chunkCount; i++) {
            byteBuf.writeInt(chunkX[i]);
            byteBuf.writeInt(chunkZ[i]);
            byteBuf.writeShort(primaryBitMask[i]);
            byteBuf.writeShort(additionalBitMask[i]);
        }
    }
}
