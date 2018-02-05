package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import lombok.Data;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;

public class Windows extends StoredObject {
	public HashMap<Short, Short> types = new HashMap<>();
	public HashMap<Short, Furnace> furnace = new HashMap<>();
	public short levelCost = 0;
	public short anvilId = -1;

	public Windows(UserConnection user) {
		super(user);
	}

	public short get(short windowId) {
		return types.getOrDefault(windowId, (short)-1);
	}

	public void remove(short windowId) {
		types.remove(windowId);
		furnace.remove(windowId);
	}

	public static int getInventoryType(String name) {
		switch(name) {
			case "minecraft:container":
				return 0;
			case "minecraft:chest":
				return 0;
			case "minecraft:crafting_table":
				return 1;
			case "minecraft:furnace":
				return 2;
			case "minecraft:dispenser":
				return 3;
			case "minecraft:enchanting_table":
				return 4;
			case "minecraft:brewing_stand":
				return 5;
			case "minecraft:villager":
				return 6;
			case "minecraft:beacon":
				return 7;
			case "minecraft:anvil":
				return 8;
			case "minecraft:hopper":
				return 9;
			case "minecraft:dropper":
				return 10;
			case "EntityHorse":
				return 11;
			default:
				throw new IllegalArgumentException("Unknown type " + name);
		}
	}

	@Data
	public static class Furnace {
		private short fuelLeft = 0;
		private short maxFuel = 0;
		private short progress = 0;
		private short maxProgress = 200;
	}
}
