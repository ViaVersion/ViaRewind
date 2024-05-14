/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viarewind.api.minecraft;

import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.NibbleArray;

public class ExtendedBlockStorage {
	private final byte[] blockLSBArray = new byte[4096];

	private final NibbleArray blockMetadataArray = new NibbleArray(this.blockLSBArray.length);
	private final NibbleArray blockLightArray = new NibbleArray(this.blockLSBArray.length);

	private NibbleArray blockMSBArray;
	private NibbleArray skyLightArray;

	public ExtendedBlockStorage(final boolean skylight) {
		if (skylight) {
			this.skyLightArray = new NibbleArray(this.blockLSBArray.length);
		}
	}

	public void setBlockId(final int x, final int y, final int z, final int value) {
		this.blockLSBArray[ChunkSection.index(x, y, z)] = (byte) (value & 255);
		if (value > 255) {
			this.getOrCreateBlockMSBArray().set(x, y, z, (value & 0xF00) >> 8);
		} else if (this.blockMSBArray != null) {
			this.blockMSBArray.set(x, y, z, 0);
		}
	}

	public void setBlockMetadata(final int x, final int y, final int z, final int value) {
		this.blockMetadataArray.set(x, y, z, value);
	}

	public boolean hasBlockMSBArray() {
		return this.blockMSBArray != null;
	}

	public byte[] getBlockLSBArray() {
		return this.blockLSBArray;
	}

	public NibbleArray getOrCreateBlockMSBArray() {
		if (this.blockMSBArray == null) {
			return this.blockMSBArray = new NibbleArray(this.blockLSBArray.length);
		}
		return this.blockMSBArray;
	}

	public NibbleArray getBlockMetadataArray() {
		return this.blockMetadataArray;
	}

	public NibbleArray getBlockLightArray() {
		return this.blockLightArray;
	}

	public NibbleArray getSkyLightArray() {
		return this.skyLightArray;
	}
}
