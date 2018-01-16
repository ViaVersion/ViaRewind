package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemReplacement;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.LoadedChunks;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.chunks.BlockStorage;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.CustomByteType;

import java.util.zip.Deflater;

public class ChunkPacketTransformer {
	public static void transformChunk(PacketWrapper packetWrapper) throws Exception {
		int chunkX = packetWrapper.read(Type.INT);
		int chunkZ = packetWrapper.read(Type.INT);
		boolean groundUp = packetWrapper.read(Type.BOOLEAN);
		int primaryBitMask = packetWrapper.read(Type.UNSIGNED_SHORT);
		int size = packetWrapper.read(Type.VAR_INT);
		CustomByteType customByteType = new CustomByteType(size);
		byte[] data = packetWrapper.read(customByteType);

		boolean isUnloadPacket = groundUp && primaryBitMask == 0;

		LoadedChunks loadedChunks = packetWrapper.user().get(LoadedChunks.class);
		if (loadedChunks.isLoaded(chunkX, chunkZ) && groundUp) {
			try {
				getUnloadPacket(chunkX, chunkZ, packetWrapper.user())
						.send(Protocol1_7_6_10TO1_8.class, true, true);
			} catch (Exception ex) {ex.printStackTrace();}
		}

		if (isUnloadPacket && loadedChunks.isInViewDistance(chunkX, chunkZ)) {
			loadedChunks.load(chunkX, chunkZ);

			packetWrapper.write(Type.INT, chunkX);
			packetWrapper.write(Type.INT, chunkZ);

			writeEmptyData(packetWrapper);

			return;
		}

		Chunk1_7_6_10to1_8 chunk = new Chunk1_7_6_10to1_8(data, primaryBitMask, true, groundUp);

		packetWrapper.write(Type.INT, chunkX);
		packetWrapper.write(Type.INT, chunkZ);
		packetWrapper.write(Type.BOOLEAN, chunk.groundUp);
		packetWrapper.write(Type.SHORT, (short) primaryBitMask);
		packetWrapper.write(Type.SHORT, (short) 0);

		data = chunk.get1_7Data();

		packetWrapper.write(Type.INT, data.length);

		Deflater deflater = new Deflater(4);

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

		customByteType = new CustomByteType(compressedSize);
		packetWrapper.write(customByteType, compressedData);

		if (isUnloadPacket) {
			loadedChunks.unload(chunkX, chunkZ);
		} else {
			loadedChunks.load(chunkX, chunkZ);
		}
	}

	public static PacketWrapper getUnloadPacket(int chunkX, int chunkZ, UserConnection user) {
		PacketWrapper unloadChunk = new PacketWrapper(0x21, null, user);
		unloadChunk.write(Type.INT, chunkX);
		unloadChunk.write(Type.INT, chunkZ);
		unloadChunk.write(Type.BOOLEAN, true);
		unloadChunk.write(Type.UNSIGNED_SHORT, 0);
		unloadChunk.write(Type.UNSIGNED_SHORT, 0);

		byte[] data = new byte[256];

		unloadChunk.write(Type.INT, data.length);

		Deflater deflater = new Deflater(4);

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

		unloadChunk.write(new CustomByteType(compressedSize), compressedData);

		return unloadChunk;
	}

	private static void writeEmptyData(PacketWrapper packetWrapper) {
		packetWrapper.write(Type.BOOLEAN, true);
		packetWrapper.write(Type.UNSIGNED_SHORT, 65535);
		packetWrapper.write(Type.UNSIGNED_SHORT, 0);

		byte[] data = new byte[calcSize(16, true, true)];

		packetWrapper.write(Type.INT, data.length);

		Deflater deflater = new Deflater(4);

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

		packetWrapper.write(new CustomByteType(compressedSize), compressedData);
	}

	public static PacketWrapper getEmptyChunkPacket(int chunkX, int chunkZ, UserConnection user) {
		PacketWrapper emptyChunk = new PacketWrapper(0x21, null, user);
		emptyChunk.write(Type.INT, chunkX);
		emptyChunk.write(Type.INT, chunkZ);

		writeEmptyData(emptyChunk);

		return emptyChunk;
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
		int[] size = new int[columnCount];
		byte[][] data = new byte[columnCount][];
		Chunk1_7_6_10to1_8[] chunks = new Chunk1_7_6_10to1_8[columnCount];

		for (int i = 0; i < columnCount; i++) {
			chunkX[i] = packetWrapper.read(Type.INT);
			chunkZ[i] = packetWrapper.read(Type.INT);
			primaryBitMask[i] = packetWrapper.read(Type.SHORT);
			size[i] = calcSize(Integer.bitCount(primaryBitMask[i]), skyLightSent, true);
		}

		int totalSize = 0;
		for (int i = 0; i < columnCount; i++) {
			CustomByteType customByteType = new CustomByteType(size[i]);
			chunks[i] = new Chunk1_7_6_10to1_8(packetWrapper.read(customByteType), primaryBitMask[i], skyLightSent, true);
			data[i] = chunks[i].get1_7Data();
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

		CustomByteType customByteType = new CustomByteType(compressedSize);
		packetWrapper.write(customByteType, finalBuffer);

		LoadedChunks loadedChunks = packetWrapper.user().get(LoadedChunks.class);

		for (int i = 0; i < columnCount; i++) {
			packetWrapper.write(Type.INT, chunkX[i]);
			packetWrapper.write(Type.INT, chunkZ[i]);
			packetWrapper.write(Type.SHORT, (short) primaryBitMask[i]);
			packetWrapper.write(Type.SHORT, (short) 0);

			loadedChunks.load(chunkX[i], chunkZ[i]);
		}
	}

	public static void transformMultiBlockChange(PacketWrapper packetWrapper) throws Exception {
		packetWrapper.passthrough(Type.INT);
		packetWrapper.passthrough(Type.INT);
		int count = packetWrapper.read(Type.VAR_INT);
		packetWrapper.write(Type.SHORT, (short) count);
		packetWrapper.write(Type.INT, count * 4);
		for (int i = 0; i < count; i++) {
			packetWrapper.passthrough(Type.UNSIGNED_BYTE);
			packetWrapper.passthrough(Type.UNSIGNED_BYTE);
			int blockData = packetWrapper.read(Type.VAR_INT);

			BlockStorage.BlockState state = ItemReplacement.replaceBlock(BlockStorage.rawToState(blockData));

			blockData = BlockStorage.stateToRaw(state);

			packetWrapper.write(Type.SHORT, (short) blockData);
		}
	}
}