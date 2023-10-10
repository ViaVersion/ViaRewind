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

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.util.ChatColorUtil;

public class Replacement {
	private final int id;
	private final int data;
	private final String name;

	private String resetName;
	private String bracketName;

	public Replacement(final int id) {
		this(id, -1);
	}

	public Replacement(final int id, final int data) {
		this(id, data, null);
	}

	public Replacement(final int id, final String name) {
		this(id, -1, name);
	}

	public Replacement(final int id, final int data, final String name) {
		this.id = id;
		this.data = data;
		this.name = name;
	}

	public void buildNames(final String protocolVersion) {
		if (this.name != null) {
			this.resetName = ChatColorUtil.translateAlternateColorCodes("&r" + protocolVersion + " " + this.name);
			this.bracketName = ChatColorUtil.translateAlternateColorCodes(" &r&7(" + protocolVersion + " " + this.name + "&r&7)");
		}
	}

	public int getId() {
		return id;
	}

	public int getData() {
		return data;
	}

	public String getName() {
		return name;
	}

	/**
	 * @param item The item to replace
	 * @return The replacement for the item or the item if not found
	 */
	public Item replace(final Item item) {
		item.setIdentifier(id); // Set the new id
		if (data != -1) {
			item.setData((short) data); // Set the new data
		}
		if (name != null) { // Set the new name
			CompoundTag rootTag = item.tag() == null ? new CompoundTag() : item.tag(); // Get root tag or create new one if not exists

			if (!rootTag.contains("display")) rootTag.put("display", new CompoundTag()); // Create display tag if not exists

			final CompoundTag display = rootTag.get("display");
			if (display.contains("Name")) {
				final StringTag name = display.get("Name");
				if (!name.getValue().equals(resetName) && !name.getValue().endsWith(bracketName)) {
					name.setValue(name.getValue() + bracketName); // Append the new name tag
				}
			} else {
				display.put("Name", new StringTag(resetName)); // Set the new name tag
			}
			item.setTag(rootTag);
		}
		return item;
	}

	/**
	 * @param data The data of the item/block
	 * @return The replacement data for the item/block
	 */
	public int replaceData(int data) {
		return this.data == -1 ? data : this.data;
	}
}
