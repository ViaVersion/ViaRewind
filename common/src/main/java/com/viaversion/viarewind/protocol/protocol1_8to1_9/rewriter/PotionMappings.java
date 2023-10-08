package com.viaversion.viarewind.protocol.protocol1_8to1_9.rewriter;

import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;

import java.util.HashMap;
import java.util.Map;

public class PotionMappings {
	public final static Map<String, String> POTION_NAME_INDEX = new HashMap<>();
	public final static Map<String, Integer> POTION_NAME_TO_ID = new HashMap<>();

	static {
		POTION_NAME_TO_ID.putAll(ItemRewriter.POTION_NAME_TO_ID);
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
}
