package de.gerrygames.viarewind.protocol.protocol1_8to1_9.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.Environment;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.PartialType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chunks.Chunk1_9to1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chunks.ChunkSection1_9to1_8;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;

public class Chunk1_8Type extends PartialType<Chunk, ClientWorld> {


    public Chunk1_8Type(ClientWorld param) {
        super(param, Chunk.class);
    }

    @Override
    public Chunk read(ByteBuf input, ClientWorld world) throws Exception {
        // Copied from ViaVersion, removed some things
        int chunkX = input.readInt();
        int chunkZ = input.readInt();
        boolean groundUp = input.readByte() != 0;
        int bitmask = input.readUnsignedShort();
        int dataLength = Type.VAR_INT.read(input);

        // Data to be read
        BitSet usedSections = new BitSet(16);
        ChunkSection1_9to1_8[] sections = new ChunkSection1_9to1_8[16];
        byte[] biomeData = null;

        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((bitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }
        int sectionCount = usedSections.cardinality(); // the amount of sections set

        if (sectionCount == 0 && groundUp) {
            // This is a chunks unload packet
            if (input.readableBytes() >= 256) {  //1.8 likes to send biome data in unload packets?!
                input.readerIndex(input.readerIndex() + 256);
            }
            return new Chunk1_9to1_8(chunkX, chunkZ);
        }

        int startIndex = input.readerIndex();

        // Read blocks
        for (int i = 0; i < 16; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            ChunkSection1_9to1_8 section = new ChunkSection1_9to1_8();
            sections[i] = section;

            // Read block data and convert to short buffer
            byte[] blockData = new byte[ChunkSection1_9to1_8.SIZE * 2];
            input.readBytes(blockData);
            ShortBuffer blockBuf = ByteBuffer.wrap(blockData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

            for (int j = 0; j < ChunkSection1_9to1_8.SIZE; j++) {
                int mask = blockBuf.get();
                int type = mask >> 4;
                int data = mask & 0xF;
                section.setBlock(j, type, data);
            }
        }

        // Read block light
        for (int i = 0; i < 16; i++) {
            if (!usedSections.get(i)) continue; // Section not set, has no light
            byte[] blockLightArray = new byte[ChunkSection1_9to1_8.LIGHT_LENGTH];
            input.readBytes(blockLightArray);
            sections[i].setBlockLight(blockLightArray);
        }

        // Read sky light
        int bytesLeft = dataLength - (input.readerIndex() - startIndex);
        if (bytesLeft >= ChunkSection1_9to1_8.LIGHT_LENGTH) {
            for (int i = 0; i < 16; i++) {
                if (!usedSections.get(i)) continue; // Section not set, has no light
                byte[] skyLightArray = new byte[ChunkSection1_9to1_8.LIGHT_LENGTH];
                input.readBytes(skyLightArray);
                sections[i].setSkyLight(skyLightArray);
                bytesLeft -= ChunkSection1_9to1_8.LIGHT_LENGTH;
            }
        }

        // Read biome data
        if (bytesLeft >= 256) {
            biomeData = new byte[256];
            input.readBytes(biomeData);
            bytesLeft -= 256;
        }

        // Check remaining bytes
        if (bytesLeft > 0) {
            Via.getPlatform().getLogger().log(Level.WARNING, bytesLeft + " Bytes left after reading chunks! (" + groundUp + ")");
        }

        // Return chunks
        return new Chunk1_9to1_8(chunkX, chunkZ, groundUp, bitmask, sections, biomeData, new ArrayList<>());
    }

    @Override
    public void write(ByteBuf output, ClientWorld world, Chunk chunk) throws Exception {
        ByteBuf buf = Unpooled.buffer();

        for (int i = 0; i < chunk.getSections().length; i++) {
            if ((chunk.getBitmask() & 1 << i) == 0) continue;
            ChunkSection section = chunk.getSections()[i];
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int block = section.getBlock(x, y, z);
                        buf.writeByte(block/* & 0xFF*/);
                        buf.writeByte(block >> 8);
                    }
                }
            }
        }

        for (int i = 0; i < chunk.getSections().length; i++) {
            if ((chunk.getBitmask() & 1 << i) == 0) continue;
            ChunkSection section = chunk.getSections()[i];
            section.writeBlockLight(buf);
        }

        boolean skyLight = world.getEnvironment() == Environment.NORMAL;
        if (skyLight) {
            for (int i = 0; i < chunk.getSections().length; i++) {
                if ((chunk.getBitmask() & 1 << i) == 0) continue;
                ChunkSection section = chunk.getSections()[i];
                section.writeSkyLight(buf);
            }
        }

        int bitmask = chunk.getBitmask();
        if (chunk.isGroundUp() && bitmask == 0) {
            bitmask = 65535;
            buf.writeBytes(new byte[2 * 16 * 4096 + 16 * 4096 / 2 + (skyLight ? 16 * 4096 / 2 : 0)]);
        }

        if (chunk.isGroundUp()) {
            buf.writeBytes(chunk.getBiomeData());
        }

        byte[] finalData = new byte[buf.readableBytes()];
        buf.readBytes(finalData);
        buf.release();

        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        output.writeBoolean(chunk.isGroundUp());
        output.writeShort(bitmask);
        Type.VAR_INT.write(output, finalData.length);
        output.writeBytes(finalData);
    }
}
