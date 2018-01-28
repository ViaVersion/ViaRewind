package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemReplacement;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.chunks.BlockStorage;
import us.myles.ViaVersion.api.minecraft.chunks.NibbleArray;

public class Chunk1_7_6_10to1_8 {
	public ExtendedBlockStorage[] storageArrays = new ExtendedBlockStorage[16];
	public byte[] blockBiomeArray = new byte[256];
	private boolean skyLight;
	private int primaryBitMask;
	public boolean groundUp;

	public Chunk1_7_6_10to1_8(byte[] data, int primaryBitMask, boolean skyLight, boolean groundUp) {
		this.primaryBitMask = primaryBitMask;
		this.skyLight = skyLight;
		this.groundUp = groundUp;
		int dataSize = 0;
		for (int i = 0; i < this.storageArrays.length; i++) {
			if ((primaryBitMask & 1 << i) != 0) {
				this.storageArrays[i] = new ExtendedBlockStorage(i << 4, skyLight);
				byte[] blockIds = this.storageArrays[i].getBlockLSBArray();
				NibbleArray nibblearray = this.storageArrays[i].getMetadataArray();
				byte[] handle = nibblearray.getHandle();
				for (int j = 0; j < blockIds.length; j++) {
					short blockData = (short) ((data[(dataSize + 1)] & 0xFF) << 8 | data[dataSize] & 0xFF);
					dataSize += 2;

					BlockStorage.BlockState state = ItemReplacement.replaceBlock(BlockStorage.rawToState(blockData));

					blockIds[j] = (byte) state.getId();

					if (j % 2 == 0) {
						handle[j / 2] = (byte)(handle[j / 2] & 240 | state.getData() & 15);
					} else {
						handle[j / 2] = (byte)(handle[j / 2] & 15 | (state.getData() & 15) << 4);
					}
				}
			}
		}
		for (int i = 0; i < this.storageArrays.length; i++) {
			if ((primaryBitMask & 1 << i) != 0 && this.storageArrays[i] != null) {
				NibbleArray nibblearray = this.storageArrays[i].getBlocklightArray();
				System.arraycopy(data, dataSize, nibblearray.getHandle(), 0, nibblearray.getHandle().length);
				dataSize += nibblearray.getHandle().length;
			}
		}
		if (skyLight) {
			for (int i = 0; i < this.storageArrays.length; i++) {
				if ((primaryBitMask & 1 << i) != 0 && this.storageArrays[i] != null) {
					NibbleArray nibblearray = this.storageArrays[i].getSkylightArray();
					System.arraycopy(data, dataSize, nibblearray.getHandle(), 0, nibblearray.getHandle().length);
					dataSize += nibblearray.getHandle().length;
				}
			}
		}
		if (groundUp && dataSize!=data.length) {
			System.arraycopy(data, dataSize, this.blockBiomeArray, 0, this.blockBiomeArray.length);
		}
	}

	public byte[] get1_7Data() {
		int finalSize = 0;
		int columns = Integer.bitCount(this.primaryBitMask);
		byte[] buffer = new byte[columns * 10240 + (this.skyLight ? columns * 2048 : 0) + 256];

		for (int i = 0; i < storageArrays.length; ++i) {
			if (storageArrays[i] != null && (primaryBitMask & 1 << i) != 0 && (!this.groundUp || storageArrays[i].isEmpty())) {
				byte[] blockIds = storageArrays[i].getBlockLSBArray();
				System.arraycopy(blockIds, 0, buffer, finalSize, blockIds.length);
				finalSize += blockIds.length;
			}
		}

		for (int i = 0; i < storageArrays.length; ++i) {
			if (storageArrays[i] != null && (primaryBitMask & 1 << i) != 0 && (!this.groundUp || storageArrays[i].isEmpty())) {
				NibbleArray nibbleArray = storageArrays[i].getMetadataArray();

				System.arraycopy(nibbleArray.getHandle(), 0, buffer, finalSize, nibbleArray.getHandle().length);
				finalSize += nibbleArray.getHandle().length;
			}
		}

		for (int i = 0; i < storageArrays.length; ++i) {
			if (storageArrays[i] != null && (primaryBitMask & 1 << i) != 0 && (!this.groundUp || storageArrays[i].isEmpty())) {
				NibbleArray nibblearray = storageArrays[i].getBlocklightArray();
				System.arraycopy(nibblearray.getHandle(), 0, buffer, finalSize, nibblearray.getHandle().length);
				finalSize += nibblearray.getHandle().length;
			}
		}

		if (skyLight) {
			for (int i = 0; i < storageArrays.length; ++i) {
				if (storageArrays[i] != null && (primaryBitMask & 1 << i) != 0 && (!this.groundUp || storageArrays[i].isEmpty())) {
					NibbleArray nibblearray = storageArrays[i].getSkylightArray();
					System.arraycopy(nibblearray.getHandle(), 0, buffer, finalSize, nibblearray.getHandle().length);
					finalSize += nibblearray.getHandle().length;
				}
			}
		}

		if (this.groundUp) {
			System.arraycopy(blockBiomeArray, 0, buffer, finalSize, blockBiomeArray.length);
			finalSize += blockBiomeArray.length;
		}

		byte[] finaldata = new byte[finalSize];
		System.arraycopy(buffer, 0, finaldata, 0, finalSize);

		return finaldata;
	}
}