/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;

// TODO move this into a ComponentRewriter
@Deprecated
public final class ChatItemRewriter {

    private static final Object2IntMap<String> NAME_TO_ID = new Object2IntOpenHashMap<>(336);

    static {
        NAME_TO_ID.put("stone", 1);
        NAME_TO_ID.put("grass", 2);
        NAME_TO_ID.put("dirt", 3);
        NAME_TO_ID.put("cobblestone", 4);
        NAME_TO_ID.put("planks", 5);
        NAME_TO_ID.put("sapling", 6);
        NAME_TO_ID.put("bedrock", 7);
        NAME_TO_ID.put("sand", 12);
        NAME_TO_ID.put("gravel", 13);
        NAME_TO_ID.put("gold_ore", 14);
        NAME_TO_ID.put("iron_ore", 15);
        NAME_TO_ID.put("coal_ore", 16);
        NAME_TO_ID.put("log", 17);
        NAME_TO_ID.put("leaves", 18);
        NAME_TO_ID.put("sponge", 19);
        NAME_TO_ID.put("glass", 20);
        NAME_TO_ID.put("lapis_ore", 21);
        NAME_TO_ID.put("lapis_block", 22);
        NAME_TO_ID.put("dispenser", 23);
        NAME_TO_ID.put("sandstone", 24);
        NAME_TO_ID.put("noteblock", 25);
        NAME_TO_ID.put("golden_rail", 27);
        NAME_TO_ID.put("detector_rail", 28);
        NAME_TO_ID.put("sticky_piston", 29);
        NAME_TO_ID.put("web", 30);
        NAME_TO_ID.put("tallgrass", 31);
        NAME_TO_ID.put("deadbush", 32);
        NAME_TO_ID.put("piston", 33);
        NAME_TO_ID.put("wool", 35);
        NAME_TO_ID.put("yellow_flower", 37);
        NAME_TO_ID.put("red_flower", 38);
        NAME_TO_ID.put("brown_mushroom", 39);
        NAME_TO_ID.put("red_mushroom", 40);
        NAME_TO_ID.put("gold_block", 41);
        NAME_TO_ID.put("iron_block", 42);
        NAME_TO_ID.put("stone_slab", 44);
        NAME_TO_ID.put("brick_block", 45);
        NAME_TO_ID.put("tnt", 46);
        NAME_TO_ID.put("bookshelf", 47);
        NAME_TO_ID.put("mossy_cobblestone", 48);
        NAME_TO_ID.put("obsidian", 49);
        NAME_TO_ID.put("torch", 50);
        NAME_TO_ID.put("mob_spawner", 52);
        NAME_TO_ID.put("oak_stairs", 53);
        NAME_TO_ID.put("chest", 54);
        NAME_TO_ID.put("diamond_ore", 56);
        NAME_TO_ID.put("diamond_block", 57);
        NAME_TO_ID.put("crafting_table", 58);
        NAME_TO_ID.put("farmland", 60);
        NAME_TO_ID.put("furnace", 61);
        NAME_TO_ID.put("ladder", 65);
        NAME_TO_ID.put("rail", 66);
        NAME_TO_ID.put("stone_stairs", 67);
        NAME_TO_ID.put("lever", 69);
        NAME_TO_ID.put("stone_pressure_plate", 70);
        NAME_TO_ID.put("wooden_pressure_plate", 72);
        NAME_TO_ID.put("redstone_ore", 73);
        NAME_TO_ID.put("redstone_torch", 76);
        NAME_TO_ID.put("stone_button", 77);
        NAME_TO_ID.put("snow_layer", 78);
        NAME_TO_ID.put("ice", 79);
        NAME_TO_ID.put("snow", 80);
        NAME_TO_ID.put("cactus", 81);
        NAME_TO_ID.put("clay", 82);
        NAME_TO_ID.put("jukebox", 84);
        NAME_TO_ID.put("fence", 85);
        NAME_TO_ID.put("pumpkin", 86);
        NAME_TO_ID.put("netherrack", 87);
        NAME_TO_ID.put("soul_sand", 88);
        NAME_TO_ID.put("glowstone", 89);
        NAME_TO_ID.put("lit_pumpkin", 91);
        NAME_TO_ID.put("stained_glass", 95);
        NAME_TO_ID.put("trapdoor", 96);
        NAME_TO_ID.put("monster_egg", 97);
        NAME_TO_ID.put("stonebrick", 98);
        NAME_TO_ID.put("brown_mushroom_block", 99);
        NAME_TO_ID.put("red_mushroom_block", 100);
        NAME_TO_ID.put("iron_bars", 101);
        NAME_TO_ID.put("glass_pane", 102);
        NAME_TO_ID.put("melon_block", 103);
        NAME_TO_ID.put("vine", 106);
        NAME_TO_ID.put("fence_gate", 107);
        NAME_TO_ID.put("brick_stairs", 108);
        NAME_TO_ID.put("stone_brick_stairs", 109);
        NAME_TO_ID.put("mycelium", 110);
        NAME_TO_ID.put("waterlily", 111);
        NAME_TO_ID.put("nether_brick", 112);
        NAME_TO_ID.put("nether_brick_fence", 113);
        NAME_TO_ID.put("nether_brick_stairs", 114);
        NAME_TO_ID.put("enchanting_table", 116);
        NAME_TO_ID.put("end_portal_frame", 120);
        NAME_TO_ID.put("end_stone", 121);
        NAME_TO_ID.put("dragon_egg", 122);
        NAME_TO_ID.put("redstone_lamp", 123);
        NAME_TO_ID.put("wooden_slab", 126);
        NAME_TO_ID.put("sandstone_stairs", 128);
        NAME_TO_ID.put("emerald_ore", 129);
        NAME_TO_ID.put("ender_chest", 130);
        NAME_TO_ID.put("tripwire_hook", 131);
        NAME_TO_ID.put("emerald_block", 133);
        NAME_TO_ID.put("spruce_stairs", 134);
        NAME_TO_ID.put("birch_stairs", 135);
        NAME_TO_ID.put("jungle_stairs", 136);
        NAME_TO_ID.put("command_block", 137);
        NAME_TO_ID.put("beacon", 138);
        NAME_TO_ID.put("cobblestone_wall", 139);
        NAME_TO_ID.put("wooden_button", 143);
        NAME_TO_ID.put("anvil", 145);
        NAME_TO_ID.put("trapped_chest", 146);
        NAME_TO_ID.put("light_weighted_pressure_plate", 147);
        NAME_TO_ID.put("heavy_weighted_pressure_plate", 148);
        NAME_TO_ID.put("daylight_detector", 151);
        NAME_TO_ID.put("redstone_block", 152);
        NAME_TO_ID.put("quartz_ore", 153);
        NAME_TO_ID.put("hopper", 154);
        NAME_TO_ID.put("quartz_block", 155);
        NAME_TO_ID.put("quartz_stairs", 156);
        NAME_TO_ID.put("activator_rail", 157);
        NAME_TO_ID.put("dropper", 158);
        NAME_TO_ID.put("stained_hardened_clay", 159);
        NAME_TO_ID.put("stained_glass_pane", 160);
        NAME_TO_ID.put("leaves2", 161);
        NAME_TO_ID.put("log2", 162);
        NAME_TO_ID.put("acacia_stairs", 163);
        NAME_TO_ID.put("dark_oak_stairs", 164);
        NAME_TO_ID.put("slime", 165);
        NAME_TO_ID.put("barrier", 166);
        NAME_TO_ID.put("iron_trapdoor", 167);
        NAME_TO_ID.put("prismarine", 168);
        NAME_TO_ID.put("sea_lantern", 169);
        NAME_TO_ID.put("hay_block", 170);
        NAME_TO_ID.put("carpet", 171);
        NAME_TO_ID.put("hardened_clay", 172);
        NAME_TO_ID.put("coal_block", 173);
        NAME_TO_ID.put("packed_ice", 174);
        NAME_TO_ID.put("double_plant", 175);
        NAME_TO_ID.put("red_sandstone", 179);
        NAME_TO_ID.put("red_sandstone_stairs", 180);
        NAME_TO_ID.put("stone_slab2", 182);
        NAME_TO_ID.put("spruce_fence_gate", 183);
        NAME_TO_ID.put("birch_fence_gate", 184);
        NAME_TO_ID.put("jungle_fence_gate", 185);
        NAME_TO_ID.put("dark_oak_fence_gate", 186);
        NAME_TO_ID.put("acacia_fence_gate", 187);
        NAME_TO_ID.put("spruce_fence", 188);
        NAME_TO_ID.put("birch_fence", 189);
        NAME_TO_ID.put("jungle_fence", 190);
        NAME_TO_ID.put("dark_oak_fence", 191);
        NAME_TO_ID.put("acacia_fence", 192);
        NAME_TO_ID.put("iron_shovel", 256);
        NAME_TO_ID.put("iron_pickaxe", 257);
        NAME_TO_ID.put("iron_axe", 258);
        NAME_TO_ID.put("flint_and_steel", 259);
        NAME_TO_ID.put("apple", 260);
        NAME_TO_ID.put("bow", 261);
        NAME_TO_ID.put("arrow", 262);
        NAME_TO_ID.put("coal", 263);
        NAME_TO_ID.put("diamond", 264);
        NAME_TO_ID.put("iron_ingot", 265);
        NAME_TO_ID.put("gold_ingot", 266);
        NAME_TO_ID.put("iron_sword", 267);
        NAME_TO_ID.put("wooden_sword", 268);
        NAME_TO_ID.put("wooden_shovel", 269);
        NAME_TO_ID.put("wooden_pickaxe", 270);
        NAME_TO_ID.put("wooden_axe", 271);
        NAME_TO_ID.put("stone_sword", 272);
        NAME_TO_ID.put("stone_shovel", 273);
        NAME_TO_ID.put("stone_pickaxe", 274);
        NAME_TO_ID.put("stone_axe", 275);
        NAME_TO_ID.put("diamond_sword", 276);
        NAME_TO_ID.put("diamond_shovel", 277);
        NAME_TO_ID.put("diamond_pickaxe", 278);
        NAME_TO_ID.put("diamond_axe", 279);
        NAME_TO_ID.put("stick", 280);
        NAME_TO_ID.put("bowl", 281);
        NAME_TO_ID.put("mushroom_stew", 282);
        NAME_TO_ID.put("golden_sword", 283);
        NAME_TO_ID.put("golden_shovel", 284);
        NAME_TO_ID.put("golden_pickaxe", 285);
        NAME_TO_ID.put("golden_axe", 286);
        NAME_TO_ID.put("string", 287);
        NAME_TO_ID.put("feather", 288);
        NAME_TO_ID.put("gunpowder", 289);
        NAME_TO_ID.put("wooden_hoe", 290);
        NAME_TO_ID.put("stone_hoe", 291);
        NAME_TO_ID.put("iron_hoe", 292);
        NAME_TO_ID.put("diamond_hoe", 293);
        NAME_TO_ID.put("golden_hoe", 294);
        NAME_TO_ID.put("wheat_seeds", 295);
        NAME_TO_ID.put("wheat", 296);
        NAME_TO_ID.put("bread", 297);
        NAME_TO_ID.put("leather_helmet", 298);
        NAME_TO_ID.put("leather_chestplate", 299);
        NAME_TO_ID.put("leather_leggings", 300);
        NAME_TO_ID.put("leather_boots", 301);
        NAME_TO_ID.put("chainmail_helmet", 302);
        NAME_TO_ID.put("chainmail_chestplate", 303);
        NAME_TO_ID.put("chainmail_leggings", 304);
        NAME_TO_ID.put("chainmail_boots", 305);
        NAME_TO_ID.put("iron_helmet", 306);
        NAME_TO_ID.put("iron_chestplate", 307);
        NAME_TO_ID.put("iron_leggings", 308);
        NAME_TO_ID.put("iron_boots", 309);
        NAME_TO_ID.put("diamond_helmet", 310);
        NAME_TO_ID.put("diamond_chestplate", 311);
        NAME_TO_ID.put("diamond_leggings", 312);
        NAME_TO_ID.put("diamond_boots", 313);
        NAME_TO_ID.put("golden_helmet", 314);
        NAME_TO_ID.put("golden_chestplate", 315);
        NAME_TO_ID.put("golden_leggings", 316);
        NAME_TO_ID.put("golden_boots", 317);
        NAME_TO_ID.put("flint", 318);
        NAME_TO_ID.put("porkchop", 319);
        NAME_TO_ID.put("cooked_porkchop", 320);
        NAME_TO_ID.put("painting", 321);
        NAME_TO_ID.put("golden_apple", 322);
        NAME_TO_ID.put("sign", 323);
        NAME_TO_ID.put("wooden_door", 324);
        NAME_TO_ID.put("bucket", 325);
        NAME_TO_ID.put("water_bucket", 326);
        NAME_TO_ID.put("lava_bucket", 327);
        NAME_TO_ID.put("minecart", 328);
        NAME_TO_ID.put("saddle", 329);
        NAME_TO_ID.put("iron_door", 330);
        NAME_TO_ID.put("redstone", 331);
        NAME_TO_ID.put("snowball", 332);
        NAME_TO_ID.put("boat", 333);
        NAME_TO_ID.put("leather", 334);
        NAME_TO_ID.put("milk_bucket", 335);
        NAME_TO_ID.put("brick", 336);
        NAME_TO_ID.put("clay_ball", 337);
        NAME_TO_ID.put("reeds", 338);
        NAME_TO_ID.put("paper", 339);
        NAME_TO_ID.put("book", 340);
        NAME_TO_ID.put("slime_ball", 341);
        NAME_TO_ID.put("chest_minecart", 342);
        NAME_TO_ID.put("furnace_minecart", 343);
        NAME_TO_ID.put("egg", 344);
        NAME_TO_ID.put("compass", 345);
        NAME_TO_ID.put("fishing_rod", 346);
        NAME_TO_ID.put("clock", 347);
        NAME_TO_ID.put("glowstone_dust", 348);
        NAME_TO_ID.put("fish", 349);
        NAME_TO_ID.put("cooked_fish", 350);
        NAME_TO_ID.put("dye", 351);
        NAME_TO_ID.put("bone", 352);
        NAME_TO_ID.put("sugar", 353);
        NAME_TO_ID.put("cake", 354);
        NAME_TO_ID.put("bed", 355);
        NAME_TO_ID.put("repeater", 356);
        NAME_TO_ID.put("cookie", 357);
        NAME_TO_ID.put("filled_map", 358);
        NAME_TO_ID.put("shears", 359);
        NAME_TO_ID.put("melon", 360);
        NAME_TO_ID.put("pumpkin_seeds", 361);
        NAME_TO_ID.put("melon_seeds", 362);
        NAME_TO_ID.put("beef", 363);
        NAME_TO_ID.put("cooked_beef", 364);
        NAME_TO_ID.put("chicken", 365);
        NAME_TO_ID.put("cooked_chicken", 366);
        NAME_TO_ID.put("rotten_flesh", 367);
        NAME_TO_ID.put("ender_pearl", 368);
        NAME_TO_ID.put("blaze_rod", 369);
        NAME_TO_ID.put("ghast_tear", 370);
        NAME_TO_ID.put("gold_nugget", 371);
        NAME_TO_ID.put("nether_wart", 372);
        NAME_TO_ID.put("potion", 373);
        NAME_TO_ID.put("glass_bottle", 374);
        NAME_TO_ID.put("spider_eye", 375);
        NAME_TO_ID.put("fermented_spider_eye", 376);
        NAME_TO_ID.put("blaze_powder", 377);
        NAME_TO_ID.put("magma_cream", 378);
        NAME_TO_ID.put("brewing_stand", 379);
        NAME_TO_ID.put("cauldron", 380);
        NAME_TO_ID.put("ender_eye", 381);
        NAME_TO_ID.put("speckled_melon", 382);
        NAME_TO_ID.put("spawn_egg", 383);
        NAME_TO_ID.put("experience_bottle", 384);
        NAME_TO_ID.put("fire_charge", 385);
        NAME_TO_ID.put("writable_book", 386);
        NAME_TO_ID.put("written_book", 387);
        NAME_TO_ID.put("emerald", 388);
        NAME_TO_ID.put("item_frame", 389);
        NAME_TO_ID.put("flower_pot", 390);
        NAME_TO_ID.put("carrot", 391);
        NAME_TO_ID.put("potato", 392);
        NAME_TO_ID.put("baked_potato", 393);
        NAME_TO_ID.put("poisonous_potato", 394);
        NAME_TO_ID.put("map", 395);
        NAME_TO_ID.put("golden_carrot", 396);
        NAME_TO_ID.put("skull", 397);
        NAME_TO_ID.put("carrot_on_a_stick", 398);
        NAME_TO_ID.put("nether_star", 399);
        NAME_TO_ID.put("pumpkin_pie", 400);
        NAME_TO_ID.put("fireworks", 401);
        NAME_TO_ID.put("firework_charge", 402);
        NAME_TO_ID.put("enchanted_book", 403);
        NAME_TO_ID.put("comparator", 404);
        NAME_TO_ID.put("netherbrick", 405);
        NAME_TO_ID.put("quartz", 406);
        NAME_TO_ID.put("tnt_minecart", 407);
        NAME_TO_ID.put("hopper_minecart", 408);
        NAME_TO_ID.put("prismarine_shard", 409);
        NAME_TO_ID.put("prismarine_crystals", 410);
        NAME_TO_ID.put("rabbit", 411);
        NAME_TO_ID.put("cooked_rabbit", 412);
        NAME_TO_ID.put("rabbit_stew", 413);
        NAME_TO_ID.put("rabbit_foot", 414);
        NAME_TO_ID.put("rabbit_hide", 415);
        NAME_TO_ID.put("armor_stand", 416);
        NAME_TO_ID.put("iron_horse_armor", 417);
        NAME_TO_ID.put("golden_horse_armor", 418);
        NAME_TO_ID.put("diamond_horse_armor", 419);
        NAME_TO_ID.put("lead", 420);
        NAME_TO_ID.put("name_tag", 421);
        NAME_TO_ID.put("command_block_minecart", 422);
        NAME_TO_ID.put("mutton", 423);
        NAME_TO_ID.put("cooked_mutton", 424);
        NAME_TO_ID.put("banner", 425);
        NAME_TO_ID.put("spruce_door", 427);
        NAME_TO_ID.put("birch_door", 428);
        NAME_TO_ID.put("jungle_door", 429);
        NAME_TO_ID.put("acacia_door", 430);
        NAME_TO_ID.put("dark_oak_door", 431);
        NAME_TO_ID.put("record_13", 2256);
        NAME_TO_ID.put("record_cat", 2257);
        NAME_TO_ID.put("record_blocks", 2258);
        NAME_TO_ID.put("record_chirp", 2259);
        NAME_TO_ID.put("record_far", 2260);
        NAME_TO_ID.put("record_mall", 2261);
        NAME_TO_ID.put("record_mellohi", 2262);
        NAME_TO_ID.put("record_stal", 2263);
        NAME_TO_ID.put("record_strad", 2264);
        NAME_TO_ID.put("record_ward", 2265);
        NAME_TO_ID.put("record_11", 2266);
        NAME_TO_ID.put("record_wait", 2267);
    }

    public static void toClient(final Protocol1_8To1_7_6_10 protocol, final UserConnection connection, JsonElement element) {
        if (element instanceof final JsonObject obj) {
            if (obj.has("hoverEvent")) {
                if (!(obj.get("hoverEvent") instanceof final JsonObject hoverEvent)) {
                    return;
                }

                if (!hoverEvent.has("action") || !hoverEvent.has("value")) {
                    return;
                }

                final String type = hoverEvent.get("action").getAsString();
                if (!type.equals("show_item")) {
                    return;
                }

                final CompoundTag compound = ComponentUtil.deserializeLegacyShowItem(hoverEvent.get("value"), SerializerVersion.V1_8);
                final int identifier;
                final String stringId = compound.getString("id");
                if (stringId != null) {
                    identifier = NAME_TO_ID.getOrDefault(Key.stripMinecraftNamespace(stringId), compound.getShort("id"));
                } else {
                    identifier = compound.getShort("id");
                }
                final CompoundTag itemTag = compound.getCompoundTag("tag");

                final Item dataItem = new DataItem(identifier, (byte) 1, (short) 0, itemTag);
                protocol.getItemRewriter().handleItemToClient(connection, dataItem);

                compound.putInt("id", dataItem.identifier());
                if (dataItem.tag() != itemTag) {
                    compound.put("tag", dataItem.tag());
                }
                hoverEvent.addProperty("value", SerializerVersion.V1_7.toSNBT(compound));
            } else if (obj.has("extra")) {
                toClient(protocol, connection, obj.get("extra"));
            } else if (obj.has("translate") && obj.has("with")) {
                toClient(protocol, connection, obj.get("with"));
            }
        } else if (element instanceof final JsonArray array) {
            for (JsonElement value : array) {
                toClient(protocol, connection, value);
            }
        }
    }

}
