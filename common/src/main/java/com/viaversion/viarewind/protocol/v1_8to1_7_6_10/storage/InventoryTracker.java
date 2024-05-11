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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

import java.util.HashMap;
import java.util.Map;

public class InventoryTracker extends StoredObject {
	public static final Map<String, Integer> WINDOW_TYPE_REGISTRY = new HashMap<>();

	static {
		WINDOW_TYPE_REGISTRY.put("minecraft:container", 0);
		WINDOW_TYPE_REGISTRY.put("minecraft:chest", 0);
		WINDOW_TYPE_REGISTRY.put("minecraft:crafting_table", 1);
		WINDOW_TYPE_REGISTRY.put("minecraft:furnace", 2);
		WINDOW_TYPE_REGISTRY.put("minecraft:dispenser", 3);
		WINDOW_TYPE_REGISTRY.put("minecraft:enchanting_table", 4);
		WINDOW_TYPE_REGISTRY.put("minecraft:brewing_stand", 5);
		WINDOW_TYPE_REGISTRY.put("minecraft:villager", 6);
		WINDOW_TYPE_REGISTRY.put("minecraft:beacon", 7);
		WINDOW_TYPE_REGISTRY.put("minecraft:anvil", 8);
		WINDOW_TYPE_REGISTRY.put("minecraft:hopper", 9);
		WINDOW_TYPE_REGISTRY.put("minecraft:dropper", 10);
		WINDOW_TYPE_REGISTRY.put("EntityHorse", 11);
	}

	private final HashMap<Short, Short> windowTypeMap = new HashMap<>();
	private final HashMap<Short, FurnaceData> furnaceData = new HashMap<>();

	public short levelCost = 0;
	public short anvilId = -1;

	public InventoryTracker(UserConnection user) {
		super(user);
	}

	public short get(short windowId) {
		return windowTypeMap.getOrDefault(windowId, (short) -1);
	}

	public void remove(short windowId) {
		windowTypeMap.remove(windowId);
		furnaceData.remove(windowId);
	}

	public static short getInventoryType(String name) {
		return WINDOW_TYPE_REGISTRY.getOrDefault(name, -1).shortValue();
	}

	public HashMap<Short, Short> getWindowTypeMap() {
		return windowTypeMap;
	}

	public HashMap<Short, FurnaceData> getFurnaceData() {
		return furnaceData;
	}

	public static class FurnaceData {

		public short fuelLeft = 0;
		public short maxFuel = 0;
		public short progress = 0;
		public short maxProgress = 200;
	}

}
