/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.type.PartialType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class CustomStringType extends PartialType<String[], Integer> {

	public CustomStringType(Integer param) {
		super(param, String[].class);
	}

	@Override
	public String[] read(ByteBuf buffer, Integer size) throws Exception {
		if (buffer.readableBytes() < size / 4) {
			throw new RuntimeException("Readable bytes does not match expected!");
		} else {
			String[] array = new String[size];
			for (int i = 0; i < size; i++) {
				array[i] = Type.STRING.read(buffer);
			}
			return array;
		}
	}

	@Override
	public void write(ByteBuf buffer, Integer size, String[] strings) throws Exception {
		for (String s : strings) Type.STRING.write(buffer, s);
	}
}
