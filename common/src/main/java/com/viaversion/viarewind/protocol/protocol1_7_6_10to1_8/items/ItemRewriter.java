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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.items;

import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viarewind.utils.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;

import java.util.ArrayList;
import java.util.List;

public class ItemRewriter {

	public static Item toClient(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.tag();
		if (tag==null) item.setTag(tag = new CompoundTag());

		CompoundTag viaVersionTag = new CompoundTag();
		tag.put("ViaRewind1_7_6_10to1_8", viaVersionTag);

		viaVersionTag.put("id", new ShortTag((short) item.identifier()));
		viaVersionTag.put("data", new ShortTag(item.data()));

		CompoundTag display = tag.get("display");
		if (display!=null && display.contains("Name")) {
			viaVersionTag.put("displayName", new StringTag((String) display.get("Name").getValue()));
		}

		if (display!=null && display.contains("Lore")) {
			viaVersionTag.put("lore", new ListTag(((ListTag)display.get("Lore")).getValue()));
		}

		if (tag.contains("ench") || tag.contains("StoredEnchantments")) {
			ListTag enchTag = tag.contains("ench") ? tag.get("ench") : tag.get("StoredEnchantments");
			List<Tag> lore = new ArrayList<>();
			for (Tag ench : new ArrayList<>(enchTag.getValue())) {
				short id = ((NumberTag) ((CompoundTag)ench).get("id")).asShort();
				short lvl = ((NumberTag) ((CompoundTag)ench).get("lvl")).asShort();
				String s;
				if (id==8) {
					s  = "§r§7Depth Strider ";
				} else {
					continue;
				}
				enchTag.remove(ench);
				s += Enchantments.ENCHANTMENTS.getOrDefault(lvl, "enchantment.level." + lvl);
				lore.add(new StringTag(s));
			}
			if (!lore.isEmpty()) {
				if (display==null) {
					tag.put("display", display = new CompoundTag());
					viaVersionTag.put("noDisplay", new ByteTag());
				}
				ListTag loreTag = display.get("Lore");
				if (loreTag==null) display.put("Lore", loreTag = new ListTag(StringTag.class));
				lore.addAll(loreTag.getValue());
				loreTag.setValue(lore);
			}
		}

		if (item.identifier()==387 && tag.contains("pages")) {
			ListTag pages = tag.get("pages");
			ListTag oldPages = new ListTag(StringTag.class);
			viaVersionTag.put("pages", oldPages);

			for (int i = 0; i<pages.size(); i++) {
				StringTag page = pages.get(i);
				String value = page.getValue();
				oldPages.add(new StringTag(value));
				value = ChatUtil.jsonToLegacy(value);
				page.setValue(value);
			}
		}

		ReplacementRegistry1_7_6_10to1_8.replace(item);

		if (viaVersionTag.size()==2 && (short)viaVersionTag.get("id").getValue()==item.identifier() && (short)viaVersionTag.get("data").getValue()==item.data()) {
			item.tag().remove("ViaRewind1_7_6_10to1_8");
			if (item.tag().isEmpty()) item.setTag(null);
		}

		return item;
	}

	public static Item toServer(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.tag();

		if (tag==null || !item.tag().contains("ViaRewind1_7_6_10to1_8")) return item;

		CompoundTag viaVersionTag = tag.remove("ViaRewind1_7_6_10to1_8");

		item.setIdentifier((short) viaVersionTag.get("id").getValue());
		item.setData((Short) viaVersionTag.get("data").getValue());

		if (viaVersionTag.contains("noDisplay")) tag.remove("display");

		if (viaVersionTag.contains("displayName")) {
			CompoundTag display = tag.get("display");
			if (display==null) tag.put("display", display = new CompoundTag());
			StringTag name = display.get("Name");
			if (name==null) display.put("Name", new StringTag((String) viaVersionTag.get("displayName").getValue()));
			else name.setValue((String) viaVersionTag.get("displayName").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag)tag.get("display")).remove("Name");
		}

		if (item.identifier()==387) {
			ListTag oldPages = viaVersionTag.get("pages");
			tag.remove("pages");
			tag.put("pages", oldPages);
		}

		return item;
	}
}
