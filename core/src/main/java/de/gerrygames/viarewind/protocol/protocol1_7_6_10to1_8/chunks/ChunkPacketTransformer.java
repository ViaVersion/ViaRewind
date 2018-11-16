package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ReplacementRegistry1_7_6_10to1_8;
import de.gerrygames.viarewind.storage.BlockState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.CustomByteType;

import java.util.zip.Deflater;

public class ChunkPacketTransformer {

	private static byte[] transformChunkData(byte[] data, int primaryBitMask, boolean skyLight, boolean groundUp) {
		int dataSize = 0;

		ByteBuf buf = Unpooled.buffer();
		ByteBuf blockDataBuf = Unpooled.buffer();

		for (int i = 0; i < 16; i++) {
			if ((primaryBitMask & 1 << i) == 0) continue;
			byte tmp = 0;
			for (int j = 0; j < 4096; j++) {
				short blockData = (short) ((data[(dataSize + 1)] & 0xFF) << 8 | data[dataSize] & 0xFF);
				dataSize += 2;

				BlockState state = BlockState.rawToState(blockData);
				state = ReplacementRegistry1_7_6_10to1_8.replace(state);

				buf.writeByte(state.getId());

				if (j % 2 == 0) {
					tmp = (byte) (state.getData() & 0xF);
				} else {
					blockDataBuf.writeByte(tmp | (state.getData() & 15) << 4);
				}
			}
		}

		buf.writeBytes(blockDataBuf);
		blockDataBuf.release();

		int columnCount = Integer.bitCount(primaryBitMask);

		//Block light
		buf.writeBytes(data, dataSize, 2048 * columnCount);
		dataSize += 2048 * columnCount;

		//Sky light
		if (skyLight) {
			buf.writeBytes(data, dataSize, 2048 * columnCount);
			dataSize += 2048 * columnCount;
		}

		if (groundUp && dataSize + 256 <= data.length) {
			buf.writeBytes(data, dataSize, 256);
			dataSize += 256;
		}

		data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		buf.release();

		return data;
	}

	private static int calcSize(int i, boolean flag, boolean flag1) {
		int j = i * 2 * 16 * 16 * 16;
		int k = i * 16 * 16 * 16 / 2;
		int l = flag ? i * 16 * 16 * 16 / 2 : 0;
		int i1 = flag1 ? 256 : 0;

		return j + k + l + i1;
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

		int totalSize = 0;
		for (int i = 0; i < columnCount; i++) {
			int size = calcSize(Integer.bitCount(primaryBitMask[i]), skyLightSent, true);
			CustomByteType customByteType = new CustomByteType(size);
			data[i] = transformChunkData(packetWrapper.read(customByteType), primaryBitMask[i], skyLightSent, true);
			totalSize += data[i].length;
		}

		packetWrapper.write(Type.SHORT, (short) columnCount);

		byte[] buildBuffer = new byte[totalSize];

		int bufferLocation = 0;

		for (int i = 0; i < columnCount; ++i) {
			System.arraycopy(data[i], 0, buildBuffer, bufferLocation, data[i].length);
			bufferLocation += data[i].length;
		}

		Deflater deflater = new Deflater(4);
		deflater.reset();
		deflater.setInput(buildBuffer);
		deflater.finish();
		byte[] buffer = new byte[buildBuffer.length + 100];
		int compressedSize = deflater.deflate(buffer);
		byte[] finalBuffer = new byte[compressedSize];
		System.arraycopy(buffer, 0, finalBuffer, 0, compressedSize);

		packetWrapper.write(Type.INT, compressedSize);
		packetWrapper.write(Type.BOOLEAN, skyLightSent);

		CustomByteType customByteType = new CustomByteType(compressedSize);
		packetWrapper.write(customByteType, finalBuffer);

		for (int i = 0; i < columnCount; i++) {
			packetWrapper.write(Type.INT, chunkX[i]);
			packetWrapper.write(Type.INT, chunkZ[i]);
			packetWrapper.write(Type.SHORT, (short) primaryBitMask[i]);
			packetWrapper.write(Type.SHORT, (short) 0);
		}
	}
}