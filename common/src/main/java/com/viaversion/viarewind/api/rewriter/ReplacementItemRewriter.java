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

package com.viaversion.viarewind.api.rewriter;

import com.viaversion.viarewind.replacement.Replacement;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public class ReplacementItemRewriter {
	private final Int2ObjectMap<Replacement> ITEM_REPLACEMENTS = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<Replacement> BLOCK_REPLACEMENTS = new Int2ObjectOpenHashMap<>();

	public void registerItem(int id, Replacement replacement) {
		registerItem(id, -1, replacement);
	}

	public void registerBlock(int id, Replacement replacement) {
		registerBlock(id, -1, replacement);
	}

	public void registerItemBlock(int id, Replacement replacement) {
		registerItemBlock(id, -1, replacement);
	}

	public void registerItem(int id, int data, Replacement replacement) {
		ITEM_REPLACEMENTS.put(combine(id, data), replacement);
	}

	public void registerBlock(int id, int data, Replacement replacement) {
		BLOCK_REPLACEMENTS.put(combine(id, data), replacement);
	}

	public void registerItemBlock(int id, int data, Replacement replacement) {
		registerItem(id, data, replacement);
		registerBlock(id, data, replacement);
	}

	public Item replace(Item item) {
		Replacement replacement = ITEM_REPLACEMENTS.get(combine(item.identifier(), item.data()));
		if (replacement == null) replacement = ITEM_REPLACEMENTS.get(combine(item.identifier(), -1));
		return replacement == null ? item : replacement.replace(item);
	}

	public Replacement replace(int id, int data) {
		Replacement replacement = BLOCK_REPLACEMENTS.get(combine(id, data));
		if (replacement == null) {
			replacement = BLOCK_REPLACEMENTS.get(combine(id, -1));
		}
		return replacement;
	}

	public static int combine(int id, int data) {
		return (id << 16) | (data & 0xFFFF);
	}
}
