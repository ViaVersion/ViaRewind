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

package com.viaversion.viarewind.api.rewriter;

import com.viaversion.viarewind.api.minecraft.IdDataCombine;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class ReplacementItemRewriter<T extends AbstractProtocol<?, ?, ?, ?>> implements ItemRewriter<T> {
	private final Int2ObjectMap<Replacement> ITEM_REPLACEMENTS = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<Replacement> BLOCK_REPLACEMENTS = new Int2ObjectOpenHashMap<>();

	private final T protocol;
	private final ProtocolVersion protocolVersion;

	public ReplacementItemRewriter(final T protocol, final ProtocolVersion protocolVersion) {
		this.protocol = protocol;
		this.protocolVersion = protocolVersion;
	}

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
		ITEM_REPLACEMENTS.put(generateTrackingId(id, data), replacement);
		replacement.buildNames(protocolVersion.getName());
	}

	public void registerBlock(int id, int data, Replacement replacement) {
		BLOCK_REPLACEMENTS.put(generateTrackingId(id, data), replacement);
		replacement.buildNames(protocolVersion.getName());
	}

	public void registerItemBlock(int id, int data, Replacement replacement) {
		registerItem(id, data, replacement);
		registerBlock(id, data, replacement);
	}

	/**
	 * @param item The item to replace
	 * @return The replacement for the item or the item if not found
	 */
	public Item replace(Item item) {
		Replacement replacement = ITEM_REPLACEMENTS.get(generateTrackingId(item.identifier(), item.data()));
		if (replacement == null) replacement = ITEM_REPLACEMENTS.get(generateTrackingId(item.identifier(), -1));

		return replacement == null ? item : replacement.replace(item);
	}

	/**
	 * @param id   The id of the item/block
	 * @param data The data of the item/block
	 * @return The replacement for the item/block or null if not found
	 */
	public Replacement replace(int id, int data) {
		Replacement replacement = BLOCK_REPLACEMENTS.get(generateTrackingId(id, data));
		if (replacement == null) replacement = BLOCK_REPLACEMENTS.get(generateTrackingId(id, -1));

		return replacement;
	}

	/**
	 * @param combined The combined id and data
	 * @return The generated tracking id for the item/block
	 */
	public int replace(int combined) {
		final int data = IdDataCombine.dataFromCombined(combined);
		final Replacement replace = replace(IdDataCombine.idFromCombined(combined), data);

		return replace != null ? IdDataCombine.toCombined(replace.getId(), replace.replaceData(data)) : combined;
	}

	/**
	 * @param id   The id of the item/block
	 * @param data The data of the item/block
	 * @return The generated tracking id for the item/block
	 */
	private int generateTrackingId(int id, int data) {
		return (id << 16) | (data & 0xFFFF);
	}

	@Override
	public T protocol() {
		return this.protocol;
	}

	/*
	ViaRewind protocols don't need this
	 */

	@Override
	public Type<Item> itemType() {
		return null;
	}

	@Override
	public Type<Item[]> itemArrayType() {
		return null;
	}

	@Override
	public Type<Item> mappedItemType() {
		return itemType();
	}

	@Override
	public Type<Item[]> mappedItemArrayType() {
		return itemArrayType();
	}
}
