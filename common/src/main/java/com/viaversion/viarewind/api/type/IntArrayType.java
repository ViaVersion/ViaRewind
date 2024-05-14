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
package com.viaversion.viarewind.api.type;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class IntArrayType extends Type<int[]> {

	public IntArrayType() {
		super(int[].class);
	}

	@Override
	public int[] read(ByteBuf byteBuf) {
		byte size = byteBuf.readByte();
		int[] array = new int[size];
		for (byte i = 0; i < size; i++) {
			array[i] = byteBuf.readInt();
		}
		return array;
	}

	@Override
	public void write(ByteBuf byteBuf, int[] array) {
		byteBuf.writeByte(array.length);
		for (int i : array) byteBuf.writeInt(i);
	}
}
