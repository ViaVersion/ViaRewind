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
package com.viaversion.viarewind.api.minecraft.entitydata;

import com.viaversion.viarewind.api.type.RewindTypes;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;

public enum EntityDataTypes1_7_6_10 implements EntityDataType {
	BYTE(0, Types.BYTE),
	SHORT(1, Types.SHORT),
	INT(2, Types.INT),
	FLOAT(3, Types.FLOAT),
	STRING(4, Types.STRING),
	ITEM(5, RewindTypes.COMPRESSED_NBT_ITEM),
	POSITION(6, Types.VECTOR);

	private final int typeID;
	private final Type<?> type;

	EntityDataTypes1_7_6_10(int typeID, Type<?> type) {
		this.typeID = typeID;
		this.type = type;
	}

	public static EntityDataTypes1_7_6_10 byId(int id) {
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
