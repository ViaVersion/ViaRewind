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

package com.viaversion.viarewind.replacement;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;

public class Replacement {
	private final int id;
	private final int data;
	private final String name;
	private String resetName;
	private String bracketName;

	public Replacement(int id) {
		this(id, -1);
	}

	public Replacement(int id, int data) {
		this(id, data, null);
	}

	public Replacement(int id, String name) {
		this(id, -1, name);
	}

	public Replacement(int id, int data, String name) {
		this.id = id;
		this.data = data;
		this.name = name;
		if (name!=null) {
			this.resetName = "§r" + name;
			this.bracketName = " §r§7(" + name + "§r§7)";
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

	public Item replace(Item item) {
		item.setIdentifier(id);
		if (data!=-1) item.setData((short)data);
		if (name!=null) {
			CompoundTag compoundTag = item.tag()==null ? new CompoundTag() : item.tag();
			if (!compoundTag.contains("display")) compoundTag.put("display", new CompoundTag());
			CompoundTag display = compoundTag.get("display");
			if (display.contains("Name")) {
				StringTag name = display.get("Name");
				if (!name.getValue().equals(resetName) && !name.getValue().endsWith(bracketName))
					name.setValue(name.getValue() + bracketName);
			} else {
				display.put("Name", new StringTag(resetName));
			}
			item.setTag(compoundTag);
		}
		return item;
	}

	public int replaceData(int data) {
		return this.data == -1 ? data : this.data;
	}
}
