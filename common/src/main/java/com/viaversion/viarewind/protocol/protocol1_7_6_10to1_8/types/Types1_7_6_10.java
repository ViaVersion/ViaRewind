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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.item.NBTType;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.item.ItemArrayType;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.item.ItemType;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata.MetadataListType;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata.MetadataType;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.primitive.ByteIntArrayType;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.primitive.PositionUYType;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;

import java.util.List;

public class Types1_7_6_10 {

	// Primitives
	public static final Type<int[]> BYTE_INT_ARRAY = new ByteIntArrayType(); // Integer array with byte as length indicator

	// Positions /shrug
	public static final Type<Position> SHORT_POSITION = new PositionUYType<>(Type.SHORT, value -> (short) value);
	public static final Type<Position> INT_POSITION = new PositionUYType<>(Type.INT, value -> value);
	public static final Type<Position> BYTE_POSITION = new PositionUYType<>(Type.BYTE, value -> (byte) value);
	public static final Type<Position> U_BYTE_POSITION = new PositionUYType<>(Type.UNSIGNED_BYTE, value -> (short) value);

	// Items
	public static final Type<CompoundTag> COMPRESSED_NBT = new NBTType();
	public static final Type<Item> COMPRESSED_NBT_ITEM = new ItemType();
	public static final Type<Item[]> COMPRESSED_NBT_ITEM_ARRAY = new ItemArrayType();

	// Metadata
	public static final Type<Metadata> METADATA = new MetadataType();
	public static final Type<List<Metadata>> METADATA_LIST = new MetadataListType();

}
