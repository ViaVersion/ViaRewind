package de.gerrygames.viarewind.protocol.protocol1_8to1_9.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.Environment;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk1_8;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.PartialType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.ArrayList;
import java.util.logging.Level;

public class Chunk1_8Type extends PartialType<Chunk, ClientWorld> {
    private static final Type<ChunkSection> CHUNK_SECTION_TYPE = new ChunkSectionType1_8();

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
        int dataLength = Type.VAR_INT.readPrimitive(input);

	    if (bitmask == 0 && groundUp) {
		    // This is a chunks unload packet
		    if (dataLength >= 256) {  //1.8 likes to send biome data in unload packets?!
			    input.readerIndex(input.readerIndex() + 256);
		    }
		    return new Chunk1_8(chunkX, chunkZ);
	    }

        // Data to be read
        ChunkSection[] sections = new ChunkSection[16];
        int[] biomeData = null;

        int startIndex = input.readerIndex();

        // Read blocks
        for (int i = 0; i < 16; i++) {
            if ((bitmask & 1 << i) == 0) continue;
            sections[i] = CHUNK_SECTION_TYPE.read(input);
        }

        // Read block light
        for (int i = 0; i < 16; i++) {
            if ((bitmask & 1 << i) == 0) continue;
            sections[i].readBlockLight(input);
        }

        // Read sky light
        int bytesLeft = dataLength - (input.readerIndex() - startIndex);
        if (bytesLeft >= ChunkSection.LIGHT_LENGTH) {
            for (int i = 0; i < 16; i++) {
                if ((bitmask & 1 << i) == 0) continue;
                sections[i].readSkyLight(input);
                bytesLeft -= ChunkSection.LIGHT_LENGTH;
            }
        }

        // Read biome data
        if (bytesLeft >= 256) {
            biomeData = new int[256];
            for (int i = 0; i < 256; i++) {
                biomeData[i] = input.readByte() & 0xFF;
            }
            bytesLeft -= 256;
        }

        // Check remaining bytes
        if (bytesLeft > 0) {
            Via.getPlatform().getLogger().log(Level.WARNING, bytesLeft + " Bytes left after reading chunks! (" + groundUp + ")");
        }

        // Return chunks
        return new Chunk1_8(chunkX, chunkZ, groundUp, bitmask, sections, biomeData, new ArrayList<>());
    }

    @Override
    public void write(ByteBuf output, ClientWorld world, Chunk chunk) throws Exception {
        ByteBuf buf = output.alloc().buffer();

        for (int i = 0; i < chunk.getSections().length; i++) {
            if ((chunk.getBitmask() & 1 << i) == 0) continue;
            CHUNK_SECTION_TYPE.write(buf, chunk.getSections()[i]);
        }

        for (int i = 0; i < chunk.getSections().length; i++) {
            if ((chunk.getBitmask() & 1 << i) == 0) continue;
            chunk.getSections()[i].writeBlockLight(buf);
        }

        boolean skyLight = world.getEnvironment() == Environment.NORMAL;
        if (skyLight) {
            for (int i = 0; i < chunk.getSections().length; i++) {
                if ((chunk.getBitmask() & 1 << i) == 0) continue;
                chunk.getSections()[i].writeSkyLight(buf);
            }
        }

        if (chunk.isFullChunk() && chunk.isBiomeData()) {
            for (int biome : chunk.getBiomeData()) {
                buf.writeByte((byte) biome);
            }
        }

        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        output.writeBoolean(chunk.isFullChunk());
        output.writeShort(chunk.getBitmask());
        Type.VAR_INT.writePrimitive(output, buf.readableBytes());
        output.writeBytes(buf, buf.readableBytes());
        buf.release();
    }
}
