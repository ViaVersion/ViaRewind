package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks;

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.CustomByteType;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ReplacementRegistry1_7_6_10to1_8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;

public class ChunkPacketTransformer {

    private static byte[] transformChunkData(byte[] data, int primaryBitMask, boolean skyLight, boolean groundUp) throws Exception {
        ByteBuf inputData = Unpooled.wrappedBuffer(data);

        ByteBuf finalBuf = ByteBufAllocator.DEFAULT.buffer();
        try {
            ChunkSection[] sections = new ChunkSection[16];
            for (int i = 0; i < 16; i++) {
                if ((primaryBitMask & 1 << i) == 0) continue;
                sections[i] = Types1_8.CHUNK_SECTION.read(inputData);
            }

            for (int i = 0; i < 16; i++) {
                if ((primaryBitMask & 1 << i) == 0) continue;
                ChunkSection section = sections[i];

                for (int k = 0; k < section.getPaletteSize(); k++) {
                    int blockData = section.getPaletteEntry(k);

                    blockData = ReplacementRegistry1_7_6_10to1_8.replace(blockData);

                    section.setPaletteEntry(k, blockData);
                }
            }

            for (int i = 0; i < 16; i++) {
                if ((primaryBitMask & 1 << i) == 0) continue;
                ChunkSection section = sections[i];
                for (int j = 0; j < 4096; j++) {
                    int raw = section.getFlatBlock(j);
                    finalBuf.writeByte(raw >> 4);
                }
            }

            for (int i = 0; i < 16; i++) {
                if ((primaryBitMask & 1 << i) == 0) continue;
                ChunkSection section = sections[i];
                for (int j = 0; j < 4096; j += 2) {
                    int meta0 = section.getFlatBlock(j) & 0xF;
                    int meta1 = section.getFlatBlock(j + 1) & 0xF;

                    finalBuf.writeByte(meta1 << 4 | meta0);
                }
            }

            int columnCount = Integer.bitCount(primaryBitMask);

            // Block light
            finalBuf.writeBytes(inputData, 2048 * columnCount);

            // Skylight
            if (skyLight) {
                finalBuf.writeBytes(inputData, 2048 * columnCount);
            }

            if (groundUp && inputData.isReadable(256)) {
                finalBuf.writeBytes(inputData, 256);
            }

            return Type.REMAINING_BYTES.read(finalBuf);
        } finally {
            finalBuf.release();
        }
    }

    private static int calcSize(int i, boolean hasSkyLight, boolean hasBiome) {
        int blocks = i * 2 * 16 * 16 * 16;
        int blockLight = i * 16 * 16 * 16 / 2;
        int skyLight = hasSkyLight ? i * 16 * 16 * 16 / 2 : 0;
        int biome = hasBiome ? 256 : 0;

        return blocks + blockLight + skyLight + biome;
    }

    public static void transformChunkBulk(PacketWrapper packetWrapper) throws Exception {
        boolean skyLightSent = packetWrapper.read(Type.BOOLEAN);
        int columnCount = packetWrapper.read(Type.VAR_INT);
        int[] chunkX = new int[columnCount];
        int[] chunkZ = new int[columnCount];
        int[] primaryBitMask = new int[columnCount];
        byte[][] data = new byte[columnCount][];

        for (int i = 0; i < columnCount; i++) {
            chunkX[i] = packetWrapper.read(Type.INT);
            chunkZ[i] = packetWrapper.read(Type.INT);
            primaryBitMask[i] = packetWrapper.read(Type.UNSIGNED_SHORT);
        }

        for (int i = 0; i < columnCount; i++) {
            int size = calcSize(Integer.bitCount(primaryBitMask[i]), skyLightSent, true);
            CustomByteType customByteType = new CustomByteType(size);
            data[i] = transformChunkData(packetWrapper.read(customByteType), primaryBitMask[i], skyLightSent, true);
        }

        ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
        // todo compression level config
        try (DeflaterOutputStream deflaterStream = new DeflaterOutputStream(compressedData)) {
            for (int i = 0; i < columnCount; ++i) {
                deflaterStream.write(data[i]);
            }
        }

        packetWrapper.write(Type.SHORT, (short) columnCount);
        packetWrapper.write(Type.INT, compressedData.size());
        packetWrapper.write(Type.BOOLEAN, skyLightSent);

        CustomByteType customByteType = new CustomByteType(compressedData.size());
        packetWrapper.write(customByteType, compressedData.toByteArray());

        for (int i = 0; i < columnCount; i++) {
            packetWrapper.write(Type.INT, chunkX[i]);
            packetWrapper.write(Type.INT, chunkZ[i]);
            packetWrapper.write(Type.SHORT, (short) primaryBitMask[i]);
            packetWrapper.write(Type.SHORT, (short) 0);
        }
    }
}