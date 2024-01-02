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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.rewriter;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.api.rewriter.item.Replacement;
import com.viaversion.viarewind.api.rewriter.item.ReplacementItemRewriter;
import com.viaversion.viarewind.utils.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;

import java.util.ArrayList;
import java.util.List;

import static com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter.potionNameFromDamage;

public class ReplacementItemRewriter1_8 extends ReplacementItemRewriter<Protocol1_8To1_9> {
	public final static String VIA_REWIND_TAG_KEY = "ViaRewind1_8to1_9";

	public ReplacementItemRewriter1_8(Protocol1_8To1_9 protocol) {
		super(protocol, ProtocolVersion.v1_9);
	}

	@Override
	public void register() {
		registerItem(198, new Replacement(50, 0, "End Rod"));
		registerItem(434, new Replacement(391, "Beetroot"));
		registerItem(435, new Replacement(361, "Beetroot Seeds"));
		registerItem(436, new Replacement(282, "Beetroot Soup"));
		registerItem(432, new Replacement(322, "Chorus Fruit"));
		registerItem(433, new Replacement(393, "Popped Chorus Fruit"));
		registerItem(437, new Replacement(373, "Dragons Breath"));
		registerItem(443, new Replacement(299, "Elytra"));
		registerItem(426, new Replacement(410, "End Crystal"));
		registerItem(442, new Replacement(425, "Shield"));
		registerItem(439, new Replacement(262, "Spectral Arrow"));
		registerItem(440, new Replacement(262, "Tipped Arrow"));
		registerItem(444, new Replacement(333, "Spruce Boat"));
		registerItem(445, new Replacement(333, "Birch Boat"));
		registerItem(446, new Replacement(333, "Jungle Boat"));
		registerItem(447, new Replacement(333, "Acacia Boat"));
		registerItem(448, new Replacement(333, "Dark Oak Boat"));
		registerItem(204, new Replacement(43, 7, "Purpur Double Slab"));
		registerItem(205, new Replacement(44, 7, "Purpur Slab"));

		registerBlock(209, new Replacement(119));
		registerBlock(198, 0, new Replacement(50, 5));
		registerBlock(198, 1, new Replacement(50, 5));
		registerBlock(198, 2, new Replacement(50, 4));
		registerBlock(198, 3, new Replacement(50, 3));
		registerBlock(198, 4, new Replacement(50, 2));
		registerBlock(198, 5, new Replacement(50, 1));
		registerBlock(204, new Replacement(43, 7));
		registerBlock(205, 0, new Replacement(44, 7));
		registerBlock(205, 8, new Replacement(44, 15));
		registerBlock(207, new Replacement(141));
		registerBlock(137, new Replacement(137, 0));

		registerItemBlock(199, new Replacement(35, 10, "Chorus Plant"));
		registerItemBlock(200, new Replacement(35, 2, "Chorus Flower"));
		registerItemBlock(201, new Replacement(155, 0, "Purpur Block"));
		registerItemBlock(202, new Replacement(155, 2, "Purpur Pillar"));
		registerItemBlock(203, 0, new Replacement(156, 0, "Purpur Stairs"));
		registerItemBlock(203, 1, new Replacement(156, 1, "Purpur Stairs"));
		registerItemBlock(203, 2, new Replacement(156, 2, "Purpur Stairs"));
		registerItemBlock(203, 3, new Replacement(156, 3, "Purpur Stairs"));
		registerItemBlock(203, 4, new Replacement(156, 4, "Purpur Stairs"));
		registerItemBlock(203, 5, new Replacement(156, 5, "Purpur Stairs"));
		registerItemBlock(203, 6, new Replacement(156, 6, "Purpur Stairs"));
		registerItemBlock(203, 7, new Replacement(156, 7, "Purpur Stairs"));
		registerItemBlock(203, 8, new Replacement(156, 8, "Purpur Stairs"));
		registerItemBlock(206, new Replacement(121, 0, "Endstone Bricks"));
		registerItemBlock(207, new Replacement(141, "Beetroot Block"));
		registerItemBlock(208, new Replacement(2, 0, "Grass Path"));
		registerItemBlock(209, new Replacement(90, "End Gateway"));
		registerItemBlock(210, new Replacement(137, 0, "Repeating Command Block"));
		registerItemBlock(211, new Replacement(137, 0, "Chain Command Block"));
		registerItemBlock(212, new Replacement(79, 0, "Frosted Ice"));
		registerItemBlock(214, new Replacement(87, 0, "Nether Wart Block"));
		registerItemBlock(215, new Replacement(112, 0, "Red Nether Brick"));
		registerItemBlock(217, new Replacement(166, 0, "Structure Void"));
		registerItemBlock(255, new Replacement(137, 0, "Structure Block"));
		registerItemBlock(397, 5, new Replacement(397, 0, "Dragon Head"));
	}
	
	@Override
	public Item handleItemToClient(Item item) {
		if (item == null) return null;

		CompoundTag tag = item.tag();
		if (tag == null) item.setTag(tag = new CompoundTag());

		CompoundTag viaVersionTag = new CompoundTag();
		tag.put(VIA_REWIND_TAG_KEY, viaVersionTag);

		viaVersionTag.put("id", new ShortTag((short) item.identifier()));
		viaVersionTag.put("data", new ShortTag(item.data()));

		CompoundTag display = tag.get("display");
		if (display != null && display.contains("Name")) {
			viaVersionTag.put("displayName", new StringTag((String) display.get("Name").getValue()));
		}

		if (display != null && display.contains("Lore")) {
			viaVersionTag.put("lore", new ListTag(((ListTag) display.get("Lore")).getValue()));
		}

		if (tag.contains("ench") || tag.contains("StoredEnchantments")) {
			ListTag enchTag = tag.contains("ench") ? tag.get("ench") : tag.get("StoredEnchantments");
			List<Tag> lore = new ArrayList<>();
			for (Tag ench : new ArrayList<>(enchTag.getValue())) {
				short id = ((NumberTag) ((CompoundTag) ench).get("id")).asShort();
				short lvl = ((NumberTag) ((CompoundTag) ench).get("lvl")).asShort();
				String s;
				if (id == 70) {
					s = "§r§7Mending ";
				} else if (id == 9) {
					s = "§r§7Frost Walker ";
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
					viaVersionTag.put("noDisplay", new ByteTag());
				}
				ListTag loreTag = display.get("Lore");
				if (loreTag == null) display.put("Lore", loreTag = new ListTag(StringTag.class));
				lore.addAll(loreTag.getValue());
				loreTag.setValue(lore);
			}
		}

		if (item.data() != 0 && tag.contains("Unbreakable")) {
			ByteTag unbreakable = tag.get("Unbreakable");
			if (unbreakable.asByte() != 0) {
				viaVersionTag.put("Unbreakable", new ByteTag(unbreakable.asByte()));
				tag.remove("Unbreakable");

				if (display == null) {
					tag.put("display", display = new CompoundTag());
					viaVersionTag.put("noDisplay", new ByteTag());
				}
				ListTag loreTag = display.get("Lore");
				if (loreTag == null) display.put("Lore", loreTag = new ListTag(StringTag.class));
				loreTag.add(new StringTag("§9Unbreakable"));
			}
		}

		if (tag.contains("AttributeModifiers")) {
			viaVersionTag.put("AttributeModifiers", tag.get("AttributeModifiers").clone());
		}

		if (item.identifier() == 383 && item.data() == 0) {
			int data = 0;
			if (tag.contains("EntityTag")) {
				CompoundTag entityTag = tag.get("EntityTag");
				if (entityTag.contains("id")) {
					StringTag id = entityTag.get("id");
					if (ItemRewriter.ENTITY_NAME_TO_ID.containsKey(id.getValue())) {
						data = ItemRewriter.ENTITY_NAME_TO_ID.get(id.getValue());
					} else if (display == null) {
						tag.put("display", display = new CompoundTag());
						viaVersionTag.put("noDisplay", new ByteTag());
						display.put("Name", new StringTag("§rSpawn " + id.getValue()));
					}
				}
			}

			item.setData((short) data);
		}

		replace(item);

		if (item.identifier() == 373 || item.identifier() == 438 || item.identifier() == 441) {
			int data = 0;
			if (tag.contains("Potion")) {
				StringTag potion = tag.get("Potion");
				String potionName = potion.getValue().replace("minecraft:", "");
				if (PotionMappings.POTION_NAME_TO_ID.containsKey(potionName)) {
					data = PotionMappings.POTION_NAME_TO_ID.get(potionName);
				}
				if (item.identifier() == 438) potionName += "_splash";
				else if (item.identifier() == 441) potionName += "_lingering";
				if ((display == null || !display.contains("Name")) && PotionMappings.POTION_NAME_INDEX.containsKey(potionName)) {
					if (display == null) {
						tag.put("display", display = new CompoundTag());
						viaVersionTag.put("noDisplay", new ByteTag());
					}
					display.put("Name", new StringTag(PotionMappings.POTION_NAME_INDEX.get(potionName)));
				}
			}

			if (item.identifier() == 438 || item.identifier() == 441) {
				item.setIdentifier(373);
				data += 8192;
			}

			item.setData((short) data);
		}

		if (tag.contains("AttributeModifiers")) {
			ListTag attributes = tag.get("AttributeModifiers");
			for (int i = 0; i < attributes.size(); i++) {
				CompoundTag attribute = attributes.get(i);
				String name = (String) attribute.get("AttributeName").getValue();
				if (!Protocol1_8To1_9.VALID_ATTRIBUTES.contains(name)) {
					attributes.remove(attribute);
					i--;
				}
			}
		}

		if (viaVersionTag.size() == 2 && (short) viaVersionTag.get("id").getValue() == item.identifier() && (short) viaVersionTag.get("data").getValue() == item.data()) {
			item.tag().remove(VIA_REWIND_TAG_KEY);
			if (item.tag().isEmpty()) item.setTag(null);
		}

		return item;
	}

	@Override
	public Item handleItemToServer(Item item) {
		if (item == null) return null;

		CompoundTag tag = item.tag();

		if (item.identifier() == 383 && item.data() != 0) {
			if (tag == null) item.setTag(tag = new CompoundTag());
			if (!tag.contains("EntityTag") && ItemRewriter.ENTITY_ID_TO_NAME.containsKey((int) item.data())) {
				CompoundTag entityTag = new CompoundTag();
				entityTag.put("id", new StringTag(ItemRewriter.ENTITY_ID_TO_NAME.get((int) item.data())));
				tag.put("EntityTag", entityTag);
			}

			item.setData((short) 0);
		}

		if (item.identifier() == 373 && (tag == null || !tag.contains("Potion"))) {
			if (tag == null) item.setTag(tag = new CompoundTag());

			if (item.data() >= 16384) {
				item.setIdentifier(438);
				item.setData((short) (item.data() - 8192));
			}

			String name = item.data() == 8192 ? "water" : potionNameFromDamage(item.data());
			tag.put("Potion", new StringTag("minecraft:" + name));
			item.setData((short) 0);
		}

		if (tag == null || !item.tag().contains("ViaRewind1_8to1_9")) return item;

		CompoundTag viaVersionTag = tag.remove("ViaRewind1_8to1_9");

		item.setIdentifier((short) viaVersionTag.get("id").getValue());
		item.setData((Short) viaVersionTag.get("data").getValue());

		if (viaVersionTag.contains("noDisplay")) tag.remove("display");

		if (viaVersionTag.contains("Unbreakable")) {
			tag.put("Unbreakable", viaVersionTag.get("Unbreakable").clone());
		}

		if (viaVersionTag.contains("displayName")) {
			CompoundTag display = tag.get("display");
			if (display == null) tag.put("display", display = new CompoundTag());
			StringTag name = display.get("Name");
			if (name == null) display.put("Name", new StringTag((String) viaVersionTag.get("displayName").getValue()));
			else name.setValue((String) viaVersionTag.get("displayName").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag) tag.get("display")).remove("Name");
		}

		if (viaVersionTag.contains("lore")) {
			CompoundTag display = tag.get("display");
			if (display == null) tag.put("display", display = new CompoundTag());
			ListTag lore = display.get("Lore");
			if (lore == null) display.put("Lore", new ListTag((List<Tag>) viaVersionTag.get("lore").getValue()));
			else lore.setValue((List<Tag>) viaVersionTag.get("lore").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag) tag.get("display")).remove("Lore");
		}

		tag.remove("AttributeModifiers");
		if (viaVersionTag.contains("AttributeModifiers")) {
			tag.put("AttributeModifiers", viaVersionTag.get("AttributeModifiers"));
		}

		return item;
	}
}
