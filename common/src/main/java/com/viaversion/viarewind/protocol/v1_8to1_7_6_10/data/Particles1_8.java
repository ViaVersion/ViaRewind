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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data;

import java.util.HashMap;

public enum Particles1_8 {
	EXPLOSION_NORMAL("explode"),
	EXPLOSION_LARGE("largeexplode"),
	EXPLOSION_HUGE("hugeexplosion"),
	FIREWORKS_SPARK("fireworksSpark"),
	WATER_BUBBLE("bubble"),
	WATER_SPLASH("splash"),
	WATER_WAKE("wake"),
	SUSPENDED("suspended"),
	SUSPENDED_DEPTH("depthsuspend"),
	CRIT("crit"),
	CRIT_MAGIC("magicCrit"),
	SMOKE_NORMAL("smoke"),
	SMOKE_LARGE("largesmoke"),
	SPELL("spell"),
	SPELL_INSTANT("instantSpell"),
	SPELL_MOB("mobSpell"),
	SPELL_MOB_AMBIENT("mobSpellAmbient"),
	SPELL_WITCH("witchMagic"),
	DRIP_WATER("dripWater"),
	DRIP_LAVA("dripLava"),
	VILLAGER_ANGRY("angryVillager"),
	VILLAGER_HAPPY("happyVillager"),
	TOWN_AURA("townaura"),
	NOTE("note"),
	PORTAL("portal"),
	ENCHANTMENT_TABLE("enchantmenttable"),
	FLAME("flame"),
	LAVA("lava"),
	FOOTSTEP("footstep"),
	CLOUD("cloud"),
	REDSTONE("reddust"),
	SNOWBALL("snowballpoof"),
	SNOW_SHOVEL("snowshovel"),
	SLIME("slime"),
	HEART("heart"),
	BARRIER("barrier"),
	ICON_CRACK("iconcrack", 2),
	BLOCK_CRACK("blockcrack", 1),
	BLOCK_DUST("blockdust", 1),
	WATER_DROP("droplet"),
	ITEM_TAKE("take"),
	MOB_APPEARANCE("mobappearance");

	public final String name;
	public final int extra;
	private static final HashMap<String, Particles1_8> particleMap = new HashMap<>();

	Particles1_8(String name) {
		this(name, 0);
	}

	Particles1_8(String name, int extra) {
		this.name = name;
		this.extra = extra;
	}

	public static Particles1_8 find(String part) {
		return particleMap.get(part);
	}

	public static Particles1_8 find(int id) {
		if (id < 0) return null;
		Particles1_8[] values = Particles1_8.values();
		return id >= values.length ? null : values[id];
	}

	static {
		Particles1_8[] particles = values();

		for (Particles1_8 particle : particles) {
			particleMap.put(particle.name, particle);
		}
	}
}