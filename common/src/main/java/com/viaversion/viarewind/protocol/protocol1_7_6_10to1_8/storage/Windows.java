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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

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
		return types.getOrDefault(windowId, (short) -1);
	}

	public void remove(short windowId) {
		types.remove(windowId);
		furnace.remove(windowId);
	}

	public static int getInventoryType(String name) {
		switch (name) {
			case "minecraft:container":
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

	public static class Furnace {
		private short fuelLeft = 0;
		private short maxFuel = 0;
		private short progress = 0;
		private short maxProgress = 200;

		public Furnace() {
		}

		public short getFuelLeft() {
			return this.fuelLeft;
		}

		public short getMaxFuel() {
			return this.maxFuel;
		}

		public short getProgress() {
			return this.progress;
		}

		public short getMaxProgress() {
			return this.maxProgress;
		}

		public void setFuelLeft(short fuelLeft) {
			this.fuelLeft = fuelLeft;
		}

		public void setMaxFuel(short maxFuel) {
			this.maxFuel = maxFuel;
		}

		public void setProgress(short progress) {
			this.progress = progress;
		}

		public void setMaxProgress(short maxProgress) {
			this.maxProgress = maxProgress;
		}

		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof Furnace))
				return false;
			final Furnace other = (Furnace) o;
			if (!other.canEqual(this)) return false;
			if (this.getFuelLeft() != other.getFuelLeft()) return false;
			if (this.getMaxFuel() != other.getMaxFuel()) return false;
			if (this.getProgress() != other.getProgress()) return false;
			return this.getMaxProgress() == other.getMaxProgress();
		}

		protected boolean canEqual(final Object other) {
			return other instanceof Furnace;
		}

		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getFuelLeft();
			result = result * PRIME + this.getMaxFuel();
			result = result * PRIME + this.getProgress();
			result = result * PRIME + this.getMaxProgress();
			return result;
		}

		public String toString() {
			return "Windows.Furnace(fuelLeft=" + this.getFuelLeft() + ", maxFuel=" + this.getMaxFuel() + ", progress=" + this.getProgress() + ", maxProgress=" + this.getMaxProgress() + ")";
		}
	}
}
