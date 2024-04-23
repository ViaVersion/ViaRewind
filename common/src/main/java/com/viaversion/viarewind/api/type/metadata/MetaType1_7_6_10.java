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
package com.viaversion.viarewind.api.type.metadata;

import com.viaversion.viarewind.api.type.Types1_7_6_10;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.type.Type;

public enum MetaType1_7_6_10 implements MetaType {
	Byte(0, Type.BYTE),
	Short(1, Type.SHORT),
	Int(2, Type.INT),
	Float(3, Type.FLOAT),
	String(4, Type.STRING),
	Slot(5, Types1_7_6_10.COMPRESSED_NBT_ITEM),
	Position(6, Type.VECTOR);

	private final int typeID;
	private final Type<?> type;

	MetaType1_7_6_10(int typeID, Type<?> type) {
		this.typeID = typeID;
		this.type = type;
	}

	public static MetaType1_7_6_10 byId(int id) {
		return values()[id];
	}

	@Override
	public int typeId() {
		return this.typeID;
	}

	@Override
	public Type<?> type() {
		return this.type;
	}
}
