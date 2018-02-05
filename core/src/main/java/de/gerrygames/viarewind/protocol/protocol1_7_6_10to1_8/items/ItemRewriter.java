package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items;

import de.gerrygames.viarewind.utils.ChatUtil;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.viaversion.libs.opennbt.tag.builtin.ByteTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ListTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ShortTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

import java.util.HashMap;

public class ItemRewriter {
	private static final HashMap<Short, String> ENTITY_SPAWN_EGG_NAMES = new HashMap<>();
	static {
		ENTITY_SPAWN_EGG_NAMES.put((short) 30, "ArmorStand");
		ENTITY_SPAWN_EGG_NAMES.put((short) 67, "Endermite");
		ENTITY_SPAWN_EGG_NAMES.put((short) 68, "Guardian");
		ENTITY_SPAWN_EGG_NAMES.put((short) 101, "Rabbit");
	}

	public static Item toClient(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.getTag();
		if (tag==null) item.setTag(tag = new CompoundTag(""));

		CompoundTag viaVersionTag = new CompoundTag("ViaRewind1_7_6_10to1_8");
		tag.put(viaVersionTag);

		viaVersionTag.put(new ShortTag("id", item.getId()));
		viaVersionTag.put(new ShortTag("data", item.getData()));

		CompoundTag display = tag.get("display");
		if (display!=null && display.contains("Name")) {
			viaVersionTag.put(new StringTag("displayName", (String) display.get("Name").getValue()));
		}

		if (item.getId()==387 && tag.contains("pages")) {
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

		if (item.getId()==383 && ENTITY_SPAWN_EGG_NAMES.containsKey(item.getData())) {
			if (display==null) {
				tag.put(display = new CompoundTag("display"));
				viaVersionTag.put(new ByteTag("noDisplay"));
			}
			display.put(new StringTag("Name", "§rSpawn " + ENTITY_SPAWN_EGG_NAMES.get(item.getData())));
		}

		ItemReplacement.toClient(item);

		if (viaVersionTag.size()==2 && (short)viaVersionTag.get("id").getValue()==item.getId() && (short)viaVersionTag.get("data").getValue()==item.getData()) {
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

		item.setId((Short) viaVersionTag.get("id").getValue());
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

		if (item.getId()==387) {
			ListTag oldPages = viaVersionTag.get("pages");
			tag.remove("pages");
			tag.put(oldPages);
		}

		return item;
	}
}
