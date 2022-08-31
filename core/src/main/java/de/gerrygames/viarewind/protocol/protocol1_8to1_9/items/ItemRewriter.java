package de.gerrygames.viarewind.protocol.protocol1_8to1_9.items;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.Enchantments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter.potionNameFromDamage;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
public class ItemRewriter {
	private static Map<String, Integer> ENTTIY_NAME_TO_ID;
	private static Map<Integer, String> ENTTIY_ID_TO_NAME;
	private static Map<String, Integer> POTION_NAME_TO_ID;
	private static Map<Integer, String> POTION_ID_TO_NAME;
	private static final Map<String, String> POTION_NAME_INDEX = new HashMap<>();

	static {
		for (Field field : de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter.class.getDeclaredFields()) {
			try {
				Field other = com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter.class.getDeclaredField(field.getName());
				other.setAccessible(true);
				field.setAccessible(true);
				field.set(null, other.get(null));
			} catch (Exception ignored) {}
		}

		POTION_NAME_TO_ID.put("luck", 8203);

		POTION_NAME_INDEX.put("water", "§rWater Bottle");
		POTION_NAME_INDEX.put("mundane", "§rMundane Potion");
		POTION_NAME_INDEX.put("thick", "§rThick Potion");
		POTION_NAME_INDEX.put("awkward", "§rAwkward Potion");
		POTION_NAME_INDEX.put("water_splash", "§rSplash Water Bottle");
		POTION_NAME_INDEX.put("mundane_splash", "§rMundane Splash Potion");
		POTION_NAME_INDEX.put("thick_splash", "§rThick Splash Potion");
		POTION_NAME_INDEX.put("awkward_splash", "§rAwkward Splash Potion");
		POTION_NAME_INDEX.put("water_lingering", "§rLingering Water Bottle");
		POTION_NAME_INDEX.put("mundane_lingering", "§rMundane Lingering Potion");
		POTION_NAME_INDEX.put("thick_lingering", "§rThick Lingering Potion");
		POTION_NAME_INDEX.put("awkward_lingering", "§rAwkward Lingering Potion");
		POTION_NAME_INDEX.put("night_vision_lingering", "§rLingering Potion of Night Vision");
		POTION_NAME_INDEX.put("long_night_vision_lingering", "§rLingering Potion of Night Vision");
		POTION_NAME_INDEX.put("invisibility_lingering", "§rLingering Potion of Invisibility");
		POTION_NAME_INDEX.put("long_invisibility_lingering", "§rLingering Potion of Invisibility");
		POTION_NAME_INDEX.put("leaping_lingering", "§rLingering Potion of Leaping");
		POTION_NAME_INDEX.put("long_leaping_lingering", "§rLingering Potion of Leaping");
		POTION_NAME_INDEX.put("strong_leaping_lingering", "§rLingering Potion of Leaping");
		POTION_NAME_INDEX.put("fire_resistance_lingering", "§rLingering Potion of Fire Resistance");
		POTION_NAME_INDEX.put("long_fire_resistance_lingering", "§rLingering Potion of Fire Resistance");
		POTION_NAME_INDEX.put("swiftness_lingering", "§rLingering Potion of Swiftness");
		POTION_NAME_INDEX.put("long_swiftness_lingering", "§rLingering Potion of Swiftness");
		POTION_NAME_INDEX.put("strong_swiftness_lingering", "§rLingering Potion of Swiftness");
		POTION_NAME_INDEX.put("slowness_lingering", "§rLingering Potion of Slowness");
		POTION_NAME_INDEX.put("long_slowness_lingering", "§rLingering Potion of Slowness");
		POTION_NAME_INDEX.put("water_breathing_lingering", "§rLingering Potion of Water Breathing");
		POTION_NAME_INDEX.put("long_water_breathing_lingering", "§rLingering Potion of Water Breathing");
		POTION_NAME_INDEX.put("healing_lingering", "§rLingering Potion of Healing");
		POTION_NAME_INDEX.put("strong_healing_lingering", "§rLingering Potion of Healing");
		POTION_NAME_INDEX.put("harming_lingering", "§rLingering Potion of Harming");
		POTION_NAME_INDEX.put("strong_harming_lingering", "§rLingering Potion of Harming");
		POTION_NAME_INDEX.put("poison_lingering", "§rLingering Potion of Poisen");
		POTION_NAME_INDEX.put("long_poison_lingering", "§rLingering Potion of Poisen");
		POTION_NAME_INDEX.put("strong_poison_lingering", "§rLingering Potion of Poisen");
		POTION_NAME_INDEX.put("regeneration_lingering", "§rLingering Potion of Regeneration");
		POTION_NAME_INDEX.put("long_regeneration_lingering", "§rLingering Potion of Regeneration");
		POTION_NAME_INDEX.put("strong_regeneration_lingering", "§rLingering Potion of Regeneration");
		POTION_NAME_INDEX.put("strength_lingering", "§rLingering Potion of Strength");
		POTION_NAME_INDEX.put("long_strength_lingering", "§rLingering Potion of Strength");
		POTION_NAME_INDEX.put("strong_strength_lingering", "§rLingering Potion of Strength");
		POTION_NAME_INDEX.put("weakness_lingering", "§rLingering Potion of Weakness");
		POTION_NAME_INDEX.put("long_weakness_lingering", "§rLingering Potion of Weakness");
		POTION_NAME_INDEX.put("luck_lingering", "§rLingering Potion of Luck");
		POTION_NAME_INDEX.put("luck", "§rPotion of Luck");
		POTION_NAME_INDEX.put("luck_splash", "§rSplash Potion of Luck");
	}

	public static Item toClient(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.tag();
		if (tag==null) item.setTag(tag = new CompoundTag());

		CompoundTag viaVersionTag = new CompoundTag();
		tag.put("ViaRewind1_8to1_9", viaVersionTag);

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
				if (id==70) {
					s  = "§r§7Mending ";
				} else if (id==9) {
					s  = "§r§7Frost Walker ";
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

		if (item.data()!=0 && tag.contains("Unbreakable")) {
			ByteTag unbreakable = tag.get("Unbreakable");
			if (unbreakable.asByte()!=0) {
				viaVersionTag.put("Unbreakable", new ByteTag(unbreakable.asByte()));
				tag.remove("Unbreakable");

				if (display==null) {
					tag.put("display", display = new CompoundTag());
					viaVersionTag.put("noDisplay", new ByteTag());
				}
				ListTag loreTag = display.get("Lore");
				if (loreTag==null) display.put("Lore", loreTag = new ListTag(StringTag.class));
				loreTag.add(new StringTag("§9Unbreakable"));
			}
		}

		if (tag.contains("AttributeModifiers")) {
			viaVersionTag.put("AttributeModifiers", tag.get("AttributeModifiers").clone());
		}

		if (item.identifier()==383 && item.data()==0) {
			int data = 0;
			if (tag.contains("EntityTag")) {
				CompoundTag entityTag = tag.get("EntityTag");
				if (entityTag.contains("id")) {
					StringTag id = entityTag.get("id");
					if (ENTTIY_NAME_TO_ID.containsKey(id.getValue())) {
						data = ENTTIY_NAME_TO_ID.get(id.getValue());
					} else if (display==null) {
						tag.put("display", display = new CompoundTag());
						viaVersionTag.put("noDisplay", new ByteTag());
						display.put("Name", new StringTag("§rSpawn " + id.getValue()));
					}
				}
			}

			item.setData((short)data);
		}

		ReplacementRegistry1_8to1_9.replace(item);

		if (item.identifier()==373 || item.identifier()==438 || item.identifier()==441) {
			int data = 0;
			if (tag.contains("Potion")) {
				StringTag potion = tag.get("Potion");
				String potionName = potion.getValue().replace("minecraft:", "");
				if (POTION_NAME_TO_ID.containsKey(potionName)) {
					data = POTION_NAME_TO_ID.get(potionName);
				}
				if (item.identifier()==438) potionName += "_splash";
				else if (item.identifier()==441) potionName += "_lingering";
				if ((display==null || !display.contains("Name")) && POTION_NAME_INDEX.containsKey(potionName)) {
					if (display==null) {
						tag.put("display", display = new CompoundTag());
						viaVersionTag.put("noDisplay", new ByteTag());
					}
					display.put("Name", new StringTag(POTION_NAME_INDEX.get(potionName)));
				}
			}

			if (item.identifier()==438 || item.identifier()==441) {
				item.setIdentifier(373);
				data += 8192;
			}

			item.setData((short)data);
		}

		if (tag.contains("AttributeModifiers")) {
			ListTag attributes = tag.get("AttributeModifiers");
			for (int i = 0; i<attributes.size(); i++) {
				CompoundTag attribute = attributes.get(i);
				String name = (String) attribute.get("AttributeName").getValue();
				if (!Protocol1_8TO1_9.VALID_ATTRIBUTES.contains(attribute)) {
					attributes.remove(attribute);
					i--;
				}
			}
		}

		if (viaVersionTag.size()==2 && (short)viaVersionTag.get("id").getValue()==item.identifier() && (short)viaVersionTag.get("data").getValue()==item.data()) {
			item.tag().remove("ViaRewind1_8to1_9");
			if (item.tag().isEmpty()) item.setTag(null);
		}

		return item;
	}

	public static Item toServer(Item item) {
		if (item==null) return null;

		CompoundTag tag = item.tag();

		if (item.identifier() == 383 && item.data() != 0) {
			if (tag == null) item.setTag(tag = new CompoundTag());
			if (!tag.contains("EntityTag") && ENTTIY_ID_TO_NAME.containsKey((int) item.data())) {
				CompoundTag entityTag = new CompoundTag();
				entityTag.put("id", new StringTag(ENTTIY_ID_TO_NAME.get((int) item.data())));
				tag.put("EntityTag", entityTag);
			}

			item.setData((short) 0);
		}

		if (item.identifier() == 373 && (tag==null || !tag.contains("Potion"))) {
			if (tag == null) item.setTag(tag = new CompoundTag());

			if (item.data() >= 16384) {
				item.setIdentifier(438);
				item.setData((short) (item.data() - 8192));
			}

			String name = item.data() == 8192 ? "water" : potionNameFromDamage(item.data());
			tag.put("Potion", new StringTag("minecraft:" + name));
			item.setData((short) 0);
		}
		
		 if (tag==null || !item.tag().contains("ViaRewind1_8to1_9")) return item;

		CompoundTag viaVersionTag = tag.remove("ViaRewind1_8to1_9");

		item.setIdentifier((short) viaVersionTag.get("id").getValue());
		item.setData((Short) viaVersionTag.get("data").getValue());

		if (viaVersionTag.contains("noDisplay")) tag.remove("display");

		if (viaVersionTag.contains("Unbreakable")) {
			tag.put("Unbreakable", viaVersionTag.get("Unbreakable").clone());
		}

		if (viaVersionTag.contains("displayName")) {
			CompoundTag display = tag.get("display");
			if (display==null) tag.put("display", display = new CompoundTag());
			StringTag name = display.get("Name");
			if (name==null) display.put("Name", new StringTag((String) viaVersionTag.get("displayName").getValue()));
			else name.setValue((String) viaVersionTag.get("displayName").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag)tag.get("display")).remove("Name");
		}

		if (viaVersionTag.contains("lore")) {
			CompoundTag display = tag.get("display");
			if (display==null) tag.put("display", display = new CompoundTag());
			ListTag lore = display.get("Lore");
			if (lore==null) display.put("Lore", new ListTag((List<Tag>) viaVersionTag.get("lore").getValue()));
			else lore.setValue((List<Tag>) viaVersionTag.get("lore").getValue());
		} else if (tag.contains("display")) {
			((CompoundTag)tag.get("display")).remove("Lore");
		}

		tag.remove("AttributeModifiers");
		if (viaVersionTag.contains("AttributeModifiers")) {
			tag.put("AttributeModifiers", viaVersionTag.get("AttributeModifiers"));
		}

		return item;
	}
}
