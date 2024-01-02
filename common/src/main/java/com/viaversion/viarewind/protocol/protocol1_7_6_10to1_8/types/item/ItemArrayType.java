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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.item;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class ItemArrayType extends Type<Item[]> {

	public ItemArrayType() {
		super(Item[].class);
	}

	@Override
	public Item[] read(ByteBuf buffer) throws Exception {
		int amount = Type.SHORT.readPrimitive(buffer);
		Item[] items = new Item[amount];

		for (int i = 0; i < amount; ++i) {
			items[i] = Types1_7_6_10.COMPRESSED_NBT_ITEM.read(buffer);
		}
		return items;
	}

	@Override
	public void write(ByteBuf buffer, Item[] items) throws Exception {
		Type.SHORT.writePrimitive(buffer, (short) items.length);
		for (Item item : items) {
			Types1_7_6_10.COMPRESSED_NBT_ITEM.write(buffer, item);
		}
	}
}
