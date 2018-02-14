package de.gerrygames.viarewind.protocol.protocol1_8to1_9.items;

import de.gerrygames.viarewind.api.Enchantments;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.viaversion.libs.opennbt.tag.builtin.ByteTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ListTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ShortTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.Tag;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter.potionNameFromDamage;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
public class ItemRewriter {
	private static Map<String, Integer> ENTTIY_NAME_TO_ID;
	private static Map<Integer, String> ENTTIY_ID_TO_NAME;
	private static Map<String, Integer> POTION_NAME_TO_ID;
	private static Map<Integer, String> POTION_ID_TO_NAME;
	private static Map<Integer, Integer> POTION_INDEX;
	private static Map<String, String> POTION_NAME_INDEX = new HashMap<>();

	static {
		for (Field field : de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter.class.getDeclaredFields()) {
			try {
				Field other = us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter.class.getDeclaredField(field.getName());
				other.setAccessible(true);
				field.setAccessible(true);
				field.set(null, other.get(null));
			} catch (Exception ignored) {}
		}

		POTION_NAME_INDEX.put("water", "§rSplash Water Bottle");
		POTION_NAME_INDEX.put("mundane", "§rMundane Splash Potion");
		POTION_NAME_INDEX.put("thick", "§rThick Splash Potion");
		POTION_NAME_INDEX.put("awkward", "§rAwkward Splash Potion");
	}

	public static Item toClient(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.getTag();
		if (tag==null) item.setTag(tag = new CompoundTag(""));

		CompoundTag viaVersionTag = new CompoundTag("ViaRewind1_8to1_9");
		tag.put(viaVersionTag);

		viaVersionTag.put(new ShortTag("id", item.getId()));
		viaVersionTag.put(new ShortTag("data", item.getData()));

		CompoundTag display = tag.get("display");
		if (display!=null && display.contains("Name")) {
			viaVersionTag.put(new StringTag("displayName", (String) display.get("Name").getValue()));
		}

		if (display!=null && display.contains("Lore")) {
			viaVersionTag.put(new ListTag("lore", ((ListTag)display.get("Lore")).getValue()));
		}

		if (tag.contains("ench")) {
			List<Tag> lore = new ArrayList<>();
			List<CompoundTag> enchants = (List<CompoundTag>) tag.get("ench").getValue();
			for (CompoundTag ench : enchants) {
				short id = (short) ench.get("id").getValue();
				short lvl = (short) ench.get("lvl").getValue();
				String s;
				if (id==70) {
					s  = "§r§7Mending ";
				} else if (id==9) {
					s  = "§r§7Frost Walker ";
				} else {
					continue;
				}
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

		if (tag.contains("Unbreakable")) {
			ByteTag unbreakable = tag.get("Unbreakable");
			if (unbreakable.getValue()!=0) {
				viaVersionTag.put(new ByteTag("Unbreakable", unbreakable.getValue()));
				tag.remove("Unbreakable");

				if (display==null) {
					tag.put(display = new CompoundTag("display"));
					viaVersionTag.put(new ByteTag("noDisplay"));
				}
				ListTag loreTag = display.get("Lore");
				if (loreTag==null) display.put(loreTag = new ListTag("Lore", StringTag.class));
				loreTag.add(new StringTag("", "§9Unbreakable"));
			}
		}

		if (tag.contains("AttributeModifiers")) {
			viaVersionTag.put(tag.get("AttributeModifiers").clone());
		}

		if (item.getId()==383 && item.getData()==0) {
			int data = 0;
			if (tag.contains("EntityTag")) {
				CompoundTag entityTag = tag.remove("EntityTag");
				if (entityTag.contains("id")) {
					StringTag id = entityTag.get("id");
					if (ENTTIY_NAME_TO_ID.containsKey(id.getValue())) {
						data = ENTTIY_NAME_TO_ID.get(id.getValue());
					} else if (display==null) {
						tag.put(display = new CompoundTag("display"));
						viaVersionTag.put(new ByteTag("noDisplay"));
						display.put(new StringTag("Name", "§rSpawn " + id.getValue()));
					}
				}
			}

			item.setData((short)data);
		}

		ReplacementRegistry1_8to1_9.replace(item);

		if (item.getId()==373 || item.getId()==438) {
			int data = 0;
			if (tag.contains("Potion")) {
				StringTag potion = tag.remove("Potion");
				String potionName = potion.getValue().replace("minecraft:", "");
				if (POTION_NAME_TO_ID.containsKey(potionName)) {
					data = POTION_NAME_TO_ID.get(potionName);
				}
				if (display==null && POTION_NAME_INDEX.containsKey(potionName)) {
					tag.put(display = new CompoundTag("display"));
					viaVersionTag.put(new ByteTag("noDisplay"));
					display.put(new StringTag("Name", POTION_NAME_INDEX.get(potionName)));
				}
			}

			if (item.getId()==438) {
				item.setId((short) 373);
				data += 8192;
			}

			item.setData((short)data);
		}

		if (tag.contains("AttributeModifiers")) {
			ListTag attributes = tag.get("AttributeModifiers");
			for (int i = 0; i<attributes.size(); i++) {
				CompoundTag attribute = attributes.get(i);
				String name = (String) attribute.get("AttributeName").getValue();
				if (name.equals("generic.armor") || name.equals("generic.armorToughness") || name.equals("generic.attackSpeed") || name.equals("generic.luck")) {
					attributes.remove(attribute);
					i--;
				}
			}
		}

		if (viaVersionTag.size()==2 && (short)viaVersionTag.get("id").getValue()==item.getId() && (short)viaVersionTag.get("data").getValue()==item.getData()) {
			item.getTag().remove("ViaRewind1_8to1_9");
			if (item.getTag().isEmpty()) item.setTag(null);
		}

		return item;
	}

	public static Item toServer(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.getTag();

		if (item.getId()==383 && item.getData()!=0) {
			if (tag==null) item.setTag(tag = new CompoundTag(""));

			if (ENTTIY_ID_TO_NAME.containsKey((int) item.getData())) {
				CompoundTag entityTag = new CompoundTag("EntityTag");
				entityTag.put(new StringTag("id", ENTTIY_ID_TO_NAME.get((int) item.getData())));
				tag.put(entityTag);
			}

			item.setData((short)0);
		}

		if (item.getId() == 373) {
			if (tag==null) item.setTag(tag = new CompoundTag(""));

			if (item.getData() >= 16384) {
				item.setId((short)438);
				item.setData((short)(item.getData() - 8192));
			}

			String name = item.getData()==8192 ? "water" : potionNameFromDamage(item.getData());
			tag.put(new StringTag("Potion", "minecraft:" + name));
			item.setData((short)0);
		}

		 if (tag==null || !item.getTag().contains("ViaRewind1_8to1_9")) return item;


		CompoundTag viaVersionTag = tag.remove("ViaRewind1_8to1_9");

		item.setId((Short) viaVersionTag.get("id").getValue());
		item.setData((Short) viaVersionTag.get("data").getValue());

		if (viaVersionTag.contains("noDisplay")) tag.remove("display");

		if (viaVersionTag.contains("Unbreakable")) {
			tag.put(viaVersionTag.get("Unbreakable").clone());
		}

		if (viaVersionTag.contains("displayName")) {
			CompoundTag display = tag.get("display");
			if (display==null) tag.put(display = new CompoundTag("display"));
			StringTag name = display.get("Name");
			if (name==null) display.put(new StringTag("Name", (String) viaVersionTag.get("displayName").getValue()));
			else name.setValue((String) viaVersionTag.get("displayName").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag)tag.get("display")).remove("Name");
		}

		if (viaVersionTag.contains("lore")) {
			CompoundTag display = tag.get("display");
			if (display==null) tag.put(display = new CompoundTag("display"));
			ListTag lore = display.get("Lore");
			if (lore==null) display.put(new ListTag("Lore", (List<Tag>) viaVersionTag.get("lore").getValue()));
			else lore.setValue((List<Tag>) viaVersionTag.get("lore").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag)tag.get("display")).remove("Lore");
		}

		tag.remove("AttributeModifiers");
		if (viaVersionTag.contains("AttributeModifiers")) {
			tag.put(viaVersionTag.get("AttributeModifiers"));
		}

		return item;
	}
}
