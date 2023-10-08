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

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;

import java.util.List;

public class Types1_7_6_10 {

	public final static Type<int[]> INT_ARRAY = new IntArrayType();

	public final static Type<CompoundTag> NBT = new NBTType();
	public final static Type<Item> ITEM = new ItemType(false);
	
	public final static Type<CompoundTag> COMPRESSED_NBT = new CompressedNBTType();
	public final static Type<Item> COMPRESSED_NBT_ITEM = new ItemType(true);
	public final static Type<Item[]> COMPRESSED_NBT_ITEM_ARRAY = new ItemArrayType(true);

	public final static Type<Metadata> METADATA = new MetadataType();
	public final static Type<List<Metadata>> METADATA_LIST = new MetadataListType();
}
