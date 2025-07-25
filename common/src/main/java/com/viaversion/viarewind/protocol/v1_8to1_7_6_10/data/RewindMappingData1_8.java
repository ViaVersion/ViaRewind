/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2025 ViaVersion and contributors
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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data;

import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;

public final class RewindMappingData1_8 extends com.viaversion.viarewind.api.data.RewindMappingData {

	private final Object2IntMap<String> identifiers1_8 = new Object2IntOpenHashMap<>(336);

	public RewindMappingData1_8() {
		super("1.8", "1.7.10");

        this.loadIdentifiers();
	}
    
    public void loadIdentifiers() {
        identifiers1_8.put("stone", 1);
        identifiers1_8.put("grass", 2);
        identifiers1_8.put("dirt", 3);
        identifiers1_8.put("cobblestone", 4);
        identifiers1_8.put("planks", 5);
        identifiers1_8.put("sapling", 6);
        identifiers1_8.put("bedrock", 7);
        identifiers1_8.put("sand", 12);
        identifiers1_8.put("gravel", 13);
        identifiers1_8.put("gold_ore", 14);
        identifiers1_8.put("iron_ore", 15);
        identifiers1_8.put("coal_ore", 16);
        identifiers1_8.put("log", 17);
        identifiers1_8.put("leaves", 18);
        identifiers1_8.put("sponge", 19);
        identifiers1_8.put("glass", 20);
        identifiers1_8.put("lapis_ore", 21);
        identifiers1_8.put("lapis_block", 22);
        identifiers1_8.put("dispenser", 23);
        identifiers1_8.put("sandstone", 24);
        identifiers1_8.put("noteblock", 25);
        identifiers1_8.put("golden_rail", 27);
        identifiers1_8.put("detector_rail", 28);
        identifiers1_8.put("sticky_piston", 29);
        identifiers1_8.put("web", 30);
        identifiers1_8.put("tallgrass", 31);
        identifiers1_8.put("deadbush", 32);
        identifiers1_8.put("piston", 33);
        identifiers1_8.put("wool", 35);
        identifiers1_8.put("yellow_flower", 37);
        identifiers1_8.put("red_flower", 38);
        identifiers1_8.put("brown_mushroom", 39);
        identifiers1_8.put("red_mushroom", 40);
        identifiers1_8.put("gold_block", 41);
        identifiers1_8.put("iron_block", 42);
        identifiers1_8.put("stone_slab", 44);
        identifiers1_8.put("brick_block", 45);
        identifiers1_8.put("tnt", 46);
        identifiers1_8.put("bookshelf", 47);
        identifiers1_8.put("mossy_cobblestone", 48);
        identifiers1_8.put("obsidian", 49);
        identifiers1_8.put("torch", 50);
        identifiers1_8.put("mob_spawner", 52);
        identifiers1_8.put("oak_stairs", 53);
        identifiers1_8.put("chest", 54);
        identifiers1_8.put("diamond_ore", 56);
        identifiers1_8.put("diamond_block", 57);
        identifiers1_8.put("crafting_table", 58);
        identifiers1_8.put("farmland", 60);
        identifiers1_8.put("furnace", 61);
        identifiers1_8.put("ladder", 65);
        identifiers1_8.put("rail", 66);
        identifiers1_8.put("stone_stairs", 67);
        identifiers1_8.put("lever", 69);
        identifiers1_8.put("stone_pressure_plate", 70);
        identifiers1_8.put("wooden_pressure_plate", 72);
        identifiers1_8.put("redstone_ore", 73);
        identifiers1_8.put("redstone_torch", 76);
        identifiers1_8.put("stone_button", 77);
        identifiers1_8.put("snow_layer", 78);
        identifiers1_8.put("ice", 79);
        identifiers1_8.put("snow", 80);
        identifiers1_8.put("cactus", 81);
        identifiers1_8.put("clay", 82);
        identifiers1_8.put("jukebox", 84);
        identifiers1_8.put("fence", 85);
        identifiers1_8.put("pumpkin", 86);
        identifiers1_8.put("netherrack", 87);
        identifiers1_8.put("soul_sand", 88);
        identifiers1_8.put("glowstone", 89);
        identifiers1_8.put("lit_pumpkin", 91);
        identifiers1_8.put("stained_glass", 95);
        identifiers1_8.put("trapdoor", 96);
        identifiers1_8.put("monster_egg", 97);
        identifiers1_8.put("stonebrick", 98);
        identifiers1_8.put("brown_mushroom_block", 99);
        identifiers1_8.put("red_mushroom_block", 100);
        identifiers1_8.put("iron_bars", 101);
        identifiers1_8.put("glass_pane", 102);
        identifiers1_8.put("melon_block", 103);
        identifiers1_8.put("vine", 106);
        identifiers1_8.put("fence_gate", 107);
        identifiers1_8.put("brick_stairs", 108);
        identifiers1_8.put("stone_brick_stairs", 109);
        identifiers1_8.put("mycelium", 110);
        identifiers1_8.put("waterlily", 111);
        identifiers1_8.put("nether_brick", 112);
        identifiers1_8.put("nether_brick_fence", 113);
        identifiers1_8.put("nether_brick_stairs", 114);
        identifiers1_8.put("enchanting_table", 116);
        identifiers1_8.put("end_portal_frame", 120);
        identifiers1_8.put("end_stone", 121);
        identifiers1_8.put("dragon_egg", 122);
        identifiers1_8.put("redstone_lamp", 123);
        identifiers1_8.put("wooden_slab", 126);
        identifiers1_8.put("sandstone_stairs", 128);
        identifiers1_8.put("emerald_ore", 129);
        identifiers1_8.put("ender_chest", 130);
        identifiers1_8.put("tripwire_hook", 131);
        identifiers1_8.put("emerald_block", 133);
        identifiers1_8.put("spruce_stairs", 134);
        identifiers1_8.put("birch_stairs", 135);
        identifiers1_8.put("jungle_stairs", 136);
        identifiers1_8.put("command_block", 137);
        identifiers1_8.put("beacon", 138);
        identifiers1_8.put("cobblestone_wall", 139);
        identifiers1_8.put("wooden_button", 143);
        identifiers1_8.put("anvil", 145);
        identifiers1_8.put("trapped_chest", 146);
        identifiers1_8.put("light_weighted_pressure_plate", 147);
        identifiers1_8.put("heavy_weighted_pressure_plate", 148);
        identifiers1_8.put("daylight_detector", 151);
        identifiers1_8.put("redstone_block", 152);
        identifiers1_8.put("quartz_ore", 153);
        identifiers1_8.put("hopper", 154);
        identifiers1_8.put("quartz_block", 155);
        identifiers1_8.put("quartz_stairs", 156);
        identifiers1_8.put("activator_rail", 157);
        identifiers1_8.put("dropper", 158);
        identifiers1_8.put("stained_hardened_clay", 159);
        identifiers1_8.put("stained_glass_pane", 160);
        identifiers1_8.put("leaves2", 161);
        identifiers1_8.put("log2", 162);
        identifiers1_8.put("acacia_stairs", 163);
        identifiers1_8.put("dark_oak_stairs", 164);
        identifiers1_8.put("slime", 165);
        identifiers1_8.put("barrier", 166);
        identifiers1_8.put("iron_trapdoor", 167);
        identifiers1_8.put("prismarine", 168);
        identifiers1_8.put("sea_lantern", 169);
        identifiers1_8.put("hay_block", 170);
        identifiers1_8.put("carpet", 171);
        identifiers1_8.put("hardened_clay", 172);
        identifiers1_8.put("coal_block", 173);
        identifiers1_8.put("packed_ice", 174);
        identifiers1_8.put("double_plant", 175);
        identifiers1_8.put("red_sandstone", 179);
        identifiers1_8.put("red_sandstone_stairs", 180);
        identifiers1_8.put("stone_slab2", 182);
        identifiers1_8.put("spruce_fence_gate", 183);
        identifiers1_8.put("birch_fence_gate", 184);
        identifiers1_8.put("jungle_fence_gate", 185);
        identifiers1_8.put("dark_oak_fence_gate", 186);
        identifiers1_8.put("acacia_fence_gate", 187);
        identifiers1_8.put("spruce_fence", 188);
        identifiers1_8.put("birch_fence", 189);
        identifiers1_8.put("jungle_fence", 190);
        identifiers1_8.put("dark_oak_fence", 191);
        identifiers1_8.put("acacia_fence", 192);
        identifiers1_8.put("iron_shovel", 256);
        identifiers1_8.put("iron_pickaxe", 257);
        identifiers1_8.put("iron_axe", 258);
        identifiers1_8.put("flint_and_steel", 259);
        identifiers1_8.put("apple", 260);
        identifiers1_8.put("bow", 261);
        identifiers1_8.put("arrow", 262);
        identifiers1_8.put("coal", 263);
        identifiers1_8.put("diamond", 264);
        identifiers1_8.put("iron_ingot", 265);
        identifiers1_8.put("gold_ingot", 266);
        identifiers1_8.put("iron_sword", 267);
        identifiers1_8.put("wooden_sword", 268);
        identifiers1_8.put("wooden_shovel", 269);
        identifiers1_8.put("wooden_pickaxe", 270);
        identifiers1_8.put("wooden_axe", 271);
        identifiers1_8.put("stone_sword", 272);
        identifiers1_8.put("stone_shovel", 273);
        identifiers1_8.put("stone_pickaxe", 274);
        identifiers1_8.put("stone_axe", 275);
        identifiers1_8.put("diamond_sword", 276);
        identifiers1_8.put("diamond_shovel", 277);
        identifiers1_8.put("diamond_pickaxe", 278);
        identifiers1_8.put("diamond_axe", 279);
        identifiers1_8.put("stick", 280);
        identifiers1_8.put("bowl", 281);
        identifiers1_8.put("mushroom_stew", 282);
        identifiers1_8.put("golden_sword", 283);
        identifiers1_8.put("golden_shovel", 284);
        identifiers1_8.put("golden_pickaxe", 285);
        identifiers1_8.put("golden_axe", 286);
        identifiers1_8.put("string", 287);
        identifiers1_8.put("feather", 288);
        identifiers1_8.put("gunpowder", 289);
        identifiers1_8.put("wooden_hoe", 290);
        identifiers1_8.put("stone_hoe", 291);
        identifiers1_8.put("iron_hoe", 292);
        identifiers1_8.put("diamond_hoe", 293);
        identifiers1_8.put("golden_hoe", 294);
        identifiers1_8.put("wheat_seeds", 295);
        identifiers1_8.put("wheat", 296);
        identifiers1_8.put("bread", 297);
        identifiers1_8.put("leather_helmet", 298);
        identifiers1_8.put("leather_chestplate", 299);
        identifiers1_8.put("leather_leggings", 300);
        identifiers1_8.put("leather_boots", 301);
        identifiers1_8.put("chainmail_helmet", 302);
        identifiers1_8.put("chainmail_chestplate", 303);
        identifiers1_8.put("chainmail_leggings", 304);
        identifiers1_8.put("chainmail_boots", 305);
        identifiers1_8.put("iron_helmet", 306);
        identifiers1_8.put("iron_chestplate", 307);
        identifiers1_8.put("iron_leggings", 308);
        identifiers1_8.put("iron_boots", 309);
        identifiers1_8.put("diamond_helmet", 310);
        identifiers1_8.put("diamond_chestplate", 311);
        identifiers1_8.put("diamond_leggings", 312);
        identifiers1_8.put("diamond_boots", 313);
        identifiers1_8.put("golden_helmet", 314);
        identifiers1_8.put("golden_chestplate", 315);
        identifiers1_8.put("golden_leggings", 316);
        identifiers1_8.put("golden_boots", 317);
        identifiers1_8.put("flint", 318);
        identifiers1_8.put("porkchop", 319);
        identifiers1_8.put("cooked_porkchop", 320);
        identifiers1_8.put("painting", 321);
        identifiers1_8.put("golden_apple", 322);
        identifiers1_8.put("sign", 323);
        identifiers1_8.put("wooden_door", 324);
        identifiers1_8.put("bucket", 325);
        identifiers1_8.put("water_bucket", 326);
        identifiers1_8.put("lava_bucket", 327);
        identifiers1_8.put("minecart", 328);
        identifiers1_8.put("saddle", 329);
        identifiers1_8.put("iron_door", 330);
        identifiers1_8.put("redstone", 331);
        identifiers1_8.put("snowball", 332);
        identifiers1_8.put("boat", 333);
        identifiers1_8.put("leather", 334);
        identifiers1_8.put("milk_bucket", 335);
        identifiers1_8.put("brick", 336);
        identifiers1_8.put("clay_ball", 337);
        identifiers1_8.put("reeds", 338);
        identifiers1_8.put("paper", 339);
        identifiers1_8.put("book", 340);
        identifiers1_8.put("slime_ball", 341);
        identifiers1_8.put("chest_minecart", 342);
        identifiers1_8.put("furnace_minecart", 343);
        identifiers1_8.put("egg", 344);
        identifiers1_8.put("compass", 345);
        identifiers1_8.put("fishing_rod", 346);
        identifiers1_8.put("clock", 347);
        identifiers1_8.put("glowstone_dust", 348);
        identifiers1_8.put("fish", 349);
        identifiers1_8.put("cooked_fish", 350);
        identifiers1_8.put("dye", 351);
        identifiers1_8.put("bone", 352);
        identifiers1_8.put("sugar", 353);
        identifiers1_8.put("cake", 354);
        identifiers1_8.put("bed", 355);
        identifiers1_8.put("repeater", 356);
        identifiers1_8.put("cookie", 357);
        identifiers1_8.put("filled_map", 358);
        identifiers1_8.put("shears", 359);
        identifiers1_8.put("melon", 360);
        identifiers1_8.put("pumpkin_seeds", 361);
        identifiers1_8.put("melon_seeds", 362);
        identifiers1_8.put("beef", 363);
        identifiers1_8.put("cooked_beef", 364);
        identifiers1_8.put("chicken", 365);
        identifiers1_8.put("cooked_chicken", 366);
        identifiers1_8.put("rotten_flesh", 367);
        identifiers1_8.put("ender_pearl", 368);
        identifiers1_8.put("blaze_rod", 369);
        identifiers1_8.put("ghast_tear", 370);
        identifiers1_8.put("gold_nugget", 371);
        identifiers1_8.put("nether_wart", 372);
        identifiers1_8.put("potion", 373);
        identifiers1_8.put("glass_bottle", 374);
        identifiers1_8.put("spider_eye", 375);
        identifiers1_8.put("fermented_spider_eye", 376);
        identifiers1_8.put("blaze_powder", 377);
        identifiers1_8.put("magma_cream", 378);
        identifiers1_8.put("brewing_stand", 379);
        identifiers1_8.put("cauldron", 380);
        identifiers1_8.put("ender_eye", 381);
        identifiers1_8.put("speckled_melon", 382);
        identifiers1_8.put("spawn_egg", 383);
        identifiers1_8.put("experience_bottle", 384);
        identifiers1_8.put("fire_charge", 385);
        identifiers1_8.put("writable_book", 386);
        identifiers1_8.put("written_book", 387);
        identifiers1_8.put("emerald", 388);
        identifiers1_8.put("item_frame", 389);
        identifiers1_8.put("flower_pot", 390);
        identifiers1_8.put("carrot", 391);
        identifiers1_8.put("potato", 392);
        identifiers1_8.put("baked_potato", 393);
        identifiers1_8.put("poisonous_potato", 394);
        identifiers1_8.put("map", 395);
        identifiers1_8.put("golden_carrot", 396);
        identifiers1_8.put("skull", 397);
        identifiers1_8.put("carrot_on_a_stick", 398);
        identifiers1_8.put("nether_star", 399);
        identifiers1_8.put("pumpkin_pie", 400);
        identifiers1_8.put("fireworks", 401);
        identifiers1_8.put("firework_charge", 402);
        identifiers1_8.put("enchanted_book", 403);
        identifiers1_8.put("comparator", 404);
        identifiers1_8.put("netherbrick", 405);
        identifiers1_8.put("quartz", 406);
        identifiers1_8.put("tnt_minecart", 407);
        identifiers1_8.put("hopper_minecart", 408);
        identifiers1_8.put("prismarine_shard", 409);
        identifiers1_8.put("prismarine_crystals", 410);
        identifiers1_8.put("rabbit", 411);
        identifiers1_8.put("cooked_rabbit", 412);
        identifiers1_8.put("rabbit_stew", 413);
        identifiers1_8.put("rabbit_foot", 414);
        identifiers1_8.put("rabbit_hide", 415);
        identifiers1_8.put("armor_stand", 416);
        identifiers1_8.put("iron_horse_armor", 417);
        identifiers1_8.put("golden_horse_armor", 418);
        identifiers1_8.put("diamond_horse_armor", 419);
        identifiers1_8.put("lead", 420);
        identifiers1_8.put("name_tag", 421);
        identifiers1_8.put("command_block_minecart", 422);
        identifiers1_8.put("mutton", 423);
        identifiers1_8.put("cooked_mutton", 424);
        identifiers1_8.put("banner", 425);
        identifiers1_8.put("spruce_door", 427);
        identifiers1_8.put("birch_door", 428);
        identifiers1_8.put("jungle_door", 429);
        identifiers1_8.put("acacia_door", 430);
        identifiers1_8.put("dark_oak_door", 431);
        identifiers1_8.put("record_13", 2256);
        identifiers1_8.put("record_cat", 2257);
        identifiers1_8.put("record_blocks", 2258);
        identifiers1_8.put("record_chirp", 2259);
        identifiers1_8.put("record_far", 2260);
        identifiers1_8.put("record_mall", 2261);
        identifiers1_8.put("record_mellohi", 2262);
        identifiers1_8.put("record_stal", 2263);
        identifiers1_8.put("record_strad", 2264);
        identifiers1_8.put("record_ward", 2265);
        identifiers1_8.put("record_11", 2266);
        identifiers1_8.put("record_wait", 2267);
    }

    public int getByNameOrId(final String identifier) {
        int id = identifiers1_8.getOrDefault(identifier.replace("minecraft:", ""), -1);

        if (id == -1) {
            try {
                return Integer.parseInt(identifier);
            } catch (NumberFormatException var3) {
                return -1;
            }
        }

        return id;
    }
}
