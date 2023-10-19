package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.BaseChunkBulkType;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

public class ChunkBulk1_7_6_10Type extends PartialType<Chunk[], ClientWorld> {

	public ChunkBulk1_7_6_10Type(final ClientWorld clientWorld) {
		super(clientWorld, Chunk[].class);
	}

	@Override
	public Class<? extends Type> getBaseClass() {
		return BaseChunkBulkType.class;
	}

	@Override
	public Chunk[] read(ByteBuf byteBuf, ClientWorld clientWorld) throws Exception {
		throw new UnsupportedOperationException(); // Not needed, see https://github.com/ViaVersion/ViaLegacy/blob/main/src/main/java/net/raphimc/vialegacy/protocols/release/protocol1_8to1_7_6_10/types/ChunkBulk1_7_6Type.java
	}

	@Override
	public void write(ByteBuf byteBuf, ClientWorld clientWorld, Chunk[] chunks) throws Exception {
		final int chunkCount = chunks.length;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final int[] chunkX = new int[chunkCount];
		final int[] chunkZ = new int[chunkCount];
		final short[] primaryBitMask = new short[chunkCount];
		final short[] additionalBitMask = new short[chunkCount];

		for (int i = 0; i < chunkCount; i++) {
			final Chunk chunk = chunks[i];
			final Pair<byte[], Short> chunkData = Chunk1_7_6_10Type.serialize(chunk);
			output.write(chunkData.key());
			chunkX[i] = chunk.getX();
			chunkZ[i] = chunk.getZ();
			primaryBitMask[i] = (short) chunk.getBitmask();
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
		byteBuf.writeBoolean(true); // hasSkyLight
		byteBuf.writeBytes(compressedData, 0, compressedSize);

		for (int i = 0; i < chunkCount; i++) {
			byteBuf.writeInt(chunkX[i]);
			byteBuf.writeInt(chunkZ[i]);
			byteBuf.writeShort(primaryBitMask[i]);
			byteBuf.writeShort(additionalBitMask[i]);
		}
	}
}
