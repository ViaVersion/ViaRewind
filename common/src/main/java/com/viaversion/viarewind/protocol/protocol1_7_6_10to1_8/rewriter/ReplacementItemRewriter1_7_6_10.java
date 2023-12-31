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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.rewriter;

import com.viaversion.viarewind.api.rewriter.item.ReplacementItemRewriter;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.api.rewriter.item.Replacement;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viarewind.utils.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;

import java.util.ArrayList;
import java.util.List;

public class ReplacementItemRewriter1_7_6_10 extends ReplacementItemRewriter<Protocol1_7_6_10To1_8> {
	private final static String VIA_REWIND_TAG_KEY = "ViaRewind1_7_6_10to1_8";

	public ReplacementItemRewriter1_7_6_10(Protocol1_7_6_10To1_8 protocol) {
		super(protocol, ProtocolVersion.v1_8);
	}

	@Override
	public void register() {
		registerBlock(176, new Replacement(63));
		registerBlock(177, new Replacement(68));
		registerBlock(193, new Replacement(64));
		registerBlock(194, new Replacement(64));
		registerBlock(195, new Replacement(64));
		registerBlock(196, new Replacement(64));
		registerBlock(197, new Replacement(64));
		registerBlock(77, 5, new Replacement(69, 6));
		registerBlock(77, 13, new Replacement(69, 14));
		registerBlock(77, 0, new Replacement(69, 0));
		registerBlock(77, 8, new Replacement(69, 8));
		registerBlock(143, 5, new Replacement(69, 6));
		registerBlock(143, 13, new Replacement(69, 14));
		registerBlock(143, 0, new Replacement(69, 0));
		registerBlock(143, 8, new Replacement(69, 8));
		registerBlock(178, new Replacement(151));
		registerBlock(182, 0, new Replacement(44, 1));
		registerBlock(182, 8, new Replacement(44, 9));

		registerItem(425, new Replacement(323, "Banner"));
		registerItem(409, new Replacement(406, "Prismarine Shard"));
		registerItem(410, new Replacement(406, "Prismarine Crystal"));
		registerItem(416, new Replacement(280, "Armor Stand"));
		registerItem(423, new Replacement(363, "Raw Mutton"));
		registerItem(424, new Replacement(364, "Cooked Mutton"));
		registerItem(411, new Replacement(365, "Raw Rabbit"));
		registerItem(412, new Replacement(366, "Cooked Rabbit"));
		registerItem(413, new Replacement(282, "Rabbit Stew"));
		registerItem(414, new Replacement(375, "Rabbit's Foot"));
		registerItem(415, new Replacement(334, "Rabbit Hide"));
		registerItem(373, 8203, new Replacement(373, 0, "Potion of Leaping"));
		registerItem(373, 8235, new Replacement(373, 0, "Potion of Leaping"));
		registerItem(373, 8267, new Replacement(373, 0, "Potion of Leaping"));
		registerItem(373, 16395, new Replacement(373, 0, "Splash Potion of Leaping"));
		registerItem(373, 16427, new Replacement(373, 0, "Splash Potion of Leaping"));
		registerItem(373, 16459, new Replacement(373, 0, "Splash Potion of Leaping"));
		registerItem(383, 30, new Replacement(383, "Spawn ArmorStand"));
		registerItem(383, 67, new Replacement(383, "Spawn Endermite"));
		registerItem(383, 68, new Replacement(383, "Spawn Guardian"));
		registerItem(383, 101, new Replacement(383, "Spawn Rabbit"));
		registerItem(19, 1, new Replacement(19, 0, "Wet Sponge"));
		registerItem(182, new Replacement(44, 1, "Red Sandstone Slab"));

		registerItemBlock(166, new Replacement(20, "Barrier"));
		registerItemBlock(167, new Replacement(96, "Iron Trapdoor"));
		registerItemBlock(1, 1, new Replacement(1, 0, "Granite"));
		registerItemBlock(1, 2, new Replacement(1, 0, "Polished Granite"));
		registerItemBlock(1, 3, new Replacement(1, 0, "Diorite"));
		registerItemBlock(1, 4, new Replacement(1, 0, "Polished Diorite"));
		registerItemBlock(1, 5, new Replacement(1, 0, "Andesite"));
		registerItemBlock(1, 6, new Replacement(1, 0, "Polished Andesite"));
		registerItemBlock(168, 0, new Replacement(1, 0, "Prismarine"));
		registerItemBlock(168, 1, new Replacement(98, 0, "Prismarine Bricks"));
		registerItemBlock(168, 2, new Replacement(98, 1, "Dark Prismarine"));
		registerItemBlock(169, new Replacement(89, "Sea Lantern"));
		registerItemBlock(165, new Replacement(95, 5, "Slime Block"));
		registerItemBlock(179, 0, new Replacement(24, "Red Sandstone"));
		registerItemBlock(179, 1, new Replacement(24, "Chiseled Red Sandstone"));
		registerItemBlock(179, 2, new Replacement(24, "Smooth Sandstone"));
		registerItemBlock(181, new Replacement(43, 1, "Double Red Sandstone Slab"));
		registerItemBlock(180, new Replacement(128, "Red Sandstone Stairs"));
		registerItemBlock(188, new Replacement(85, "Spruce Fence"));
		registerItemBlock(189, new Replacement(85, "Birch Fence"));
		registerItemBlock(190, new Replacement(85, "Jungle Fence"));
		registerItemBlock(191, new Replacement(85, "Dark Oak Fence"));
		registerItemBlock(192, new Replacement(85, "Acacia Fence"));
		registerItemBlock(183, new Replacement(107, "Spruce Fence Gate"));
		registerItemBlock(184, new Replacement(107, "Birch Fence Gate"));
		registerItemBlock(185, new Replacement(107, "Jungle Fence Gate"));
		registerItemBlock(186, new Replacement(107, "Dark Oak Fence Gate"));
		registerItemBlock(187, new Replacement(107, "Acacia Fence Gate"));
		registerItemBlock(427, new Replacement(324, "Spruce Door"));
		registerItemBlock(428, new Replacement(324, "Birch Door"));
		registerItemBlock(429, new Replacement(324, "Jungle Door"));
		registerItemBlock(430, new Replacement(324, "Dark Oak Door"));
		registerItemBlock(431, new Replacement(324, "Acacia Door"));
		registerItemBlock(157, new Replacement(28, "Activator Rail"));
	}

	@Override
	public Item handleItemToClient(Item item) {
		if (item == null) return null;

		CompoundTag tag = item.tag();
		if (tag == null) {
			item.setTag(tag = new CompoundTag());
		}

		CompoundTag viaRewindTag = new CompoundTag();
		tag.put(VIA_REWIND_TAG_KEY, viaRewindTag);

		viaRewindTag.put("id", new ShortTag((short) item.identifier()));
		viaRewindTag.put("data", new ShortTag(item.data()));

		CompoundTag display = tag.get("display");
		if (display != null && display.contains("Name")) {
			viaRewindTag.put("displayName", new StringTag((String) display.get("Name").getValue()));
		}

		if (display != null && display.contains("Lore")) {
			viaRewindTag.put("lore", new ListTag(((ListTag) display.get("Lore")).getValue()));
		}

		if (tag.contains("ench") || tag.contains("StoredEnchantments")) {
			ListTag enchTag = tag.contains("ench") ? tag.get("ench") : tag.get("StoredEnchantments");
			List<Tag> lore = new ArrayList<>();
			for (Tag ench : new ArrayList<>(enchTag.getValue())) {
				short id = ((NumberTag) ((CompoundTag) ench).get("id")).asShort();
				short lvl = ((NumberTag) ((CompoundTag) ench).get("lvl")).asShort();
				String s;
				if (id == 8) {
					s = "§r§7Depth Strider ";
				} else {
					continue;
				}
				enchTag.remove(ench);
				s += Enchantments.ENCHANTMENTS.getOrDefault(lvl, "enchantment.level." + lvl);
				lore.add(new StringTag(s));
			}
			if (!lore.isEmpty()) {
				if (display == null) {
					tag.put("display", display = new CompoundTag());
					viaRewindTag.put("noDisplay", new ByteTag());
				}
				ListTag loreTag = display.get("Lore");
				if (loreTag == null) display.put("Lore", loreTag = new ListTag(StringTag.class));
				lore.addAll(loreTag.getValue());
				loreTag.setValue(lore);
			}
		}

		if (item.identifier() == 387 && tag.contains("pages")) {
			ListTag pages = tag.get("pages");
			ListTag oldPages = new ListTag(StringTag.class);
			viaRewindTag.put("pages", oldPages);

			for (int i = 0; i < pages.size(); i++) {
				StringTag page = pages.get(i);
				String value = page.getValue();
				oldPages.add(new StringTag(value));
				value = ChatUtil.jsonToLegacy(value);
				page.setValue(value);
			}
		}

		replace(item);

		if (viaRewindTag.size() == 2 && (short) viaRewindTag.get("id").getValue() == item.identifier() && (short) viaRewindTag.get("data").getValue() == item.data()) {
			item.tag().remove(VIA_REWIND_TAG_KEY);
			if (item.tag().isEmpty()) item.setTag(null);
		}

		return item;
	}

	@Override
	public Item handleItemToServer(Item item) {
		if (item == null) return null;

		CompoundTag tag = item.tag();

		if (tag == null || !item.tag().contains(VIA_REWIND_TAG_KEY)) return item;

		CompoundTag viaVersionTag = tag.remove(VIA_REWIND_TAG_KEY);

		item.setIdentifier((short) viaVersionTag.get("id").getValue());
		item.setData((Short) viaVersionTag.get("data").getValue());

		if (viaVersionTag.contains("noDisplay")) tag.remove("display");

		if (viaVersionTag.contains("displayName")) {
			CompoundTag display = tag.get("display");
			if (display == null) tag.put("display", display = new CompoundTag());
			StringTag name = display.get("Name");
			if (name == null) display.put("Name", new StringTag((String) viaVersionTag.get("displayName").getValue()));
			else name.setValue((String) viaVersionTag.get("displayName").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag) tag.get("display")).remove("Name");
		}

		if (item.identifier() == 387) {
			ListTag oldPages = viaVersionTag.get("pages");
			tag.remove("pages");
			tag.put("pages", oldPages);
		}

		return item;
	}
}
