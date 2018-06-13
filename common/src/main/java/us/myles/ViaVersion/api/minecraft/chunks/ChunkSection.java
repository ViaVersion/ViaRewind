package us.myles.ViaVersion.api.minecraft.chunks;

import io.netty.buffer.ByteBuf;

import java.util.List;

public interface ChunkSection {
    int getBlock(int x, int y, int z);

    void setBlock(int x, int y, int z, int type, int data);

    void setFlatBlock(int x, int y, int z, int type);

    int getBlockId(int x, int y, int z);

    void writeBlocks(ByteBuf output) throws Exception;

    void writeBlockLight(ByteBuf output) throws Exception;

    boolean hasSkyLight();

    void writeSkyLight(ByteBuf output) throws Exception;

    List<Integer> getPalette();
}