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

import com.viaversion.viarewind.api.type.item.NBTType;
import com.viaversion.viarewind.api.type.item.ItemArrayType;
import com.viaversion.viarewind.api.type.item.ItemType;
import com.viaversion.viarewind.api.type.entitydata.EntityDataListType;
import com.viaversion.viarewind.api.type.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.nbt.tag.CompoundTag;

import java.util.List;

public class RewindTypes {

	public static final Type<int[]> INT_ARRAY = new IntArrayType(); // Integer array with byte as length indicator

	public static final Type<Position> SHORT_POSITION = new PositionVarYType<>(Types.SHORT, value -> (short) value);
	public static final Type<Position> INT_POSITION = new PositionVarYType<>(Types.INT, value -> value);
	public static final Type<Position> BYTE_POSITION = new PositionVarYType<>(Types.BYTE, value -> (byte) value);
	public static final Type<Position> U_BYTE_POSITION = new PositionVarYType<>(Types.UNSIGNED_BYTE, value -> (short) value);

	public static final Type<CompoundTag> COMPRESSED_NBT = new NBTType();
	public static final Type<Item> COMPRESSED_NBT_ITEM = new ItemType();
	public static final Type<Item[]> COMPRESSED_NBT_ITEM_ARRAY = new ItemArrayType();

}
