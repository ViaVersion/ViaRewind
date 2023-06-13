package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks;

import com.viaversion.viaversion.api.minecraft.chunks.NibbleArray;

public class ExtendedBlockStorage {
	private final int yBase;
	private byte[] blockLSBArray;
	private NibbleArray blockMSBArray;
	private NibbleArray blockMetadataArray;
	private NibbleArray blocklightArray;
	private NibbleArray skylightArray;

	public ExtendedBlockStorage(int paramInt, boolean paramBoolean) {
		this.yBase = paramInt;
		this.blockLSBArray = new byte[4096];
		this.blockMetadataArray = new NibbleArray(this.blockLSBArray.length);
		this.blocklightArray = new NibbleArray(this.blockLSBArray.length);
		if (paramBoolean) {
			this.skylightArray = new NibbleArray(this.blockLSBArray.length);
		}
	}

	public int getExtBlockMetadata(int paramInt1, int paramInt2, int paramInt3) {
		return this.blockMetadataArray.get(paramInt1, paramInt2, paramInt3);
	}

	public void setExtBlockMetadata(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		this.blockMetadataArray.set(paramInt1, paramInt2, paramInt3, paramInt4);
	}

	public int getYLocation() {
		return this.yBase;
	}

	public void setExtSkylightValue(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		this.skylightArray.set(paramInt1, paramInt2, paramInt3, paramInt4);
	}

	public int getExtSkylightValue(int paramInt1, int paramInt2, int paramInt3) {
		return this.skylightArray.get(paramInt1, paramInt2, paramInt3);
	}

	public void setExtBlocklightValue(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		this.blocklightArray.set(paramInt1, paramInt2, paramInt3, paramInt4);
	}

	public int getExtBlocklightValue(int paramInt1, int paramInt2, int paramInt3) {
		return this.blocklightArray.get(paramInt1, paramInt2, paramInt3);
	}

	public byte[] getBlockLSBArray() {
		return this.blockLSBArray;
	}

	public boolean isEmpty() {
		return this.blockMSBArray==null;
	}

	public void clearMSBArray() {
		this.blockMSBArray = null;
	}

	public NibbleArray getBlockMSBArray() {
		return this.blockMSBArray;
	}

	public NibbleArray getMetadataArray() {
		return this.blockMetadataArray;
	}

	public NibbleArray getBlocklightArray() {
		return this.blocklightArray;
	}

	public NibbleArray getSkylightArray() {
		return this.skylightArray;
	}

	public void setBlockLSBArray(byte[] paramArrayOfByte) {
		this.blockLSBArray = paramArrayOfByte;
	}

	public void setBlockMSBArray(NibbleArray paramNibbleArray) {
		this.blockMSBArray = paramNibbleArray;
	}

	public void setBlockMetadataArray(NibbleArray paramNibbleArray) {
		this.blockMetadataArray = paramNibbleArray;
	}

	public void setBlocklightArray(NibbleArray paramNibbleArray) {
		this.blocklightArray = paramNibbleArray;
	}

	public void setSkylightArray(NibbleArray paramNibbleArray) {
		this.skylightArray = paramNibbleArray;
	}

	public NibbleArray createBlockMSBArray() {
		this.blockMSBArray = new NibbleArray(this.blockLSBArray.length);
		return this.blockMSBArray;
	}
}
