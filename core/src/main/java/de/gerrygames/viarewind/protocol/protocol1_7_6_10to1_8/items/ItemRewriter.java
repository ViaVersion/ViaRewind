package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items;

import de.gerrygames.viarewind.utils.Enchantments;
import de.gerrygames.viarewind.utils.ChatUtil;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.viaversion.libs.opennbt.tag.builtin.ByteTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ListTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ShortTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.Tag;

import java.util.ArrayList;
import java.util.List;

public class ItemRewriter {

	public static Item toClient(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.getTag();
		if (tag==null) item.setTag(tag = new CompoundTag(""));

		CompoundTag viaVersionTag = new CompoundTag("ViaRewind1_7_6_10to1_8");
		tag.put(viaVersionTag);

		viaVersionTag.put(new ShortTag("id", (short) item.getIdentifier()));
		viaVersionTag.put(new ShortTag("data", item.getData()));

		CompoundTag display = tag.get("display");
		if (display!=null && display.contains("Name")) {
			viaVersionTag.put(new StringTag("displayName", (String) display.get("Name").getValue()));
		}

		if (display!=null && display.contains("Lore")) {
			viaVersionTag.put(new ListTag("lore", ((ListTag)display.get("Lore")).getValue()));
		}

		if (tag.contains("ench") || tag.contains("StoredEnchantments")) {
			ListTag enchTag = tag.contains("ench") ? tag.get("ench") : tag.get("StoredEnchantments");
			List<Tag> enchants = enchTag.getValue();
			List<Tag> lore = new ArrayList<>();
			for (Tag ench : enchants) {
				short id = (short) ((CompoundTag)ench).get("id").getValue();
				short lvl = (short) ((CompoundTag)ench).get("lvl").getValue();
				String s;
				if (id==8) {
					s  = "§r§7Depth Strider ";
				} else {
					continue;
				}
				enchTag.remove(ench);
				s += Enchantments.ENCHANTMENTS.getOrDefault(lvl, "enchantment.level." + lvl);
				lore.add(new StringTag("", s));
			}
			if (!lore.isEmpty()) {
				if (display==null) {
					tag.put(display = new CompoundTag("display"));
					viaVersionTag.put(new ByteTag("noDisplay"));
				}
				ListTag loreTag = display.get("Lore");
				if (loreTag==null) display.put(loreTag = new ListTag("Lore", StringTag.class));
				lore.addAll(loreTag.getValue());
				loreTag.setValue(lore);
			}
		}

		if (item.getIdentifier()==387 && tag.contains("pages")) {
			ListTag pages = tag.get("pages");
			ListTag oldPages = new ListTag("pages", StringTag.class);
			viaVersionTag.put(oldPages);

			for (int i = 0; i<pages.size(); i++) {
				StringTag page = pages.get(i);
				String value = page.getValue();
				oldPages.add(new StringTag(page.getName(), value));
				value = ChatUtil.jsonToLegacy(value);
				page.setValue(value);
			}
		}

		ReplacementRegistry1_7_6_10to1_8.replace(item);

		if (viaVersionTag.size()==2 && (short)viaVersionTag.get("id").getValue()==item.getIdentifier() && (short)viaVersionTag.get("data").getValue()==item.getData()) {
			item.getTag().remove("ViaRewind1_7_6_10to1_8");
			if (item.getTag().isEmpty()) item.setTag(null);
		}

		return item;
	}

	public static Item toServer(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.getTag();

		if (tag==null || !item.getTag().contains("ViaRewind1_7_6_10to1_8")) return item;

		CompoundTag viaVersionTag = tag.remove("ViaRewind1_7_6_10to1_8");

		item.setIdentifier((short) viaVersionTag.get("id").getValue());
		item.setData((Short) viaVersionTag.get("data").getValue());

		if (viaVersionTag.contains("noDisplay")) tag.remove("display");

		if (viaVersionTag.contains("displayName")) {
			CompoundTag display = tag.get("display");
			if (display==null) tag.put(display = new CompoundTag("display"));
			StringTag name = display.get("Name");
			if (name==null) display.put(new StringTag("Name", (String) viaVersionTag.get("displayName").getValue()));
			else name.setValue((String) viaVersionTag.get("displayName").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag)tag.get("display")).remove("Name");
		}

		if (item.getIdentifier()==387) {
			ListTag oldPages = viaVersionTag.get("pages");
			tag.remove("pages");
			tag.put(oldPages);
		}

		return item;
	}
}
