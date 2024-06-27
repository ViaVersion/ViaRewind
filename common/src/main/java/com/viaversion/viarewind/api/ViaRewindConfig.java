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
package com.viaversion.viarewind.api;

import com.viaversion.viaversion.api.configuration.Config;

/**
 * This class is used to get the platform specific config
 */
public interface ViaRewindConfig extends Config {

	enum CooldownIndicator {
		TITLE, ACTION_BAR, BOSS_BAR, DISABLED
	}

	/**
	 * Specifies how 1.8.x clients should see the cooldown indicator
	 * You can choose between TITLE, ACTION_BAR, BOSS_BAR and DISABLED
	 * ONLY DISABLE IF YOU HAVE 1.9 COOLDOWN DISABLED ON YOUR SERVER
	 * 1.8 PLAYERS MAY ASK WHY PVP IS NOT WORKING OTHERWISE
	 *
	 * @return the cooldown indicator
	 */
	CooldownIndicator getCooldownIndicator();

	/**
	 * Replaces Adventure mode with Survival mode for 1.7.x clients
	 * Enable this option if your server is using the 'CanDestroy'
	 * or 'CanPlaceOn' flags on items
	 *
	 * @return true if enabled
	 */
	boolean isReplaceAdventureMode();

	/**
	 * Whether 1.9 particles should be replaced by similar ones in
	 * 1.8 and lower
	 *
	 * @return true if enabled
	 */
	boolean isReplaceParticles();

	/**
	 * Max amount of pages for written books before a client gets kicked
	 *
	 * @return the max amount of pages
	 */
	int getMaxBookPages();

	/**
	 * Max amount of characters in the json (!) string of a book page before a client gets kicked
	 *
	 * @return the max amount of characters
	 */
	int getMaxBookPageSize();

	/**
	 * Whether to emulate the 1.8+ world border for 1.7.x clients
	 *
	 * @return true if enabled
	 */
	boolean isEmulateWorldBorder();

	/**
	 * Always shows the original mob's name instead of only when hovering over them with the cursor.
	 *
	 * @return true if enabled
	 */
	boolean alwaysShowOriginalMobName();

	/**
	 * The particle to show the world border for the 1.8+ world border for 1.7.x clients
	 *
	 * @return the particle name (see <a href="https://wiki.vg/index.php?title=Protocol&oldid=7368#Particle_2">Particle registry for Packet</a>)
	 */
	String getWorldBorderParticle();

	/**
	 * If enabled, 1.8 players on 1.9+ servers can use /offhand to switch items between their main hand and offhand.
	 *
	 * @return true if enabled
	 */
	boolean isEnableOffhand();

	/**
	 * Allows to define the offhand command
	 *
	 * @return the offhand command
	 */
	String getOffhandCommand();

	/**
	 * If enabled, 1.8 players on 1.9+ servers will also experience the levitation effect by sending velocity packets.
	 *
	 * @return true if enabled
	 */
	boolean emulateLevitationEffect();
}
