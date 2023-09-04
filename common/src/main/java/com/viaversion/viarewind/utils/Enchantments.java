/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

package com.viaversion.viarewind.utils;

import java.util.HashMap;
import java.util.Map;

public class Enchantments {
	public static final Map<Short, String> ENCHANTMENTS = new HashMap<>();

	static {
		ENCHANTMENTS.put((short) 1, "I");
		ENCHANTMENTS.put((short) 2, "II");
		ENCHANTMENTS.put((short) 3, "III");
		ENCHANTMENTS.put((short) 4, "IV");
		ENCHANTMENTS.put((short) 5, "V");
		ENCHANTMENTS.put((short) 6, "VI");
		ENCHANTMENTS.put((short) 7, "VII");
		ENCHANTMENTS.put((short) 8, "VIII");
		ENCHANTMENTS.put((short) 9, "IX");
		ENCHANTMENTS.put((short) 10, "X");
	}
}
