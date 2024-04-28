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
package com.viaversion.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentSerializer;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.HashMap;
import java.util.Map;

public class WindowTracker extends StoredObject {
	private final HashMap<Short, String> types = new HashMap<>();
	private final HashMap<Short, Item[]> brewingItems = new HashMap<>();
	private final Map<Short, WindowProperties> enchantmentProperties = new HashMap<>();

	public WindowTracker(UserConnection user) {
		super(user);
	}

	public String get(short windowId) {
		return types.get(windowId);
	}

	public void put(short windowId, String type) {
		types.put(windowId, type);
	}

	public void remove(short windowId) {
		types.remove(windowId);
		brewingItems.remove(windowId);
	}

	public Item[] getBrewingItems(short windowId) {
		return brewingItems.computeIfAbsent(windowId, key -> new Item[]{
				new DataItem(),
				new DataItem(),
				new DataItem(),
				new DataItem()
		});
	}

	public void storeEnchantmentTableProperty(short windowId, int key, int value) {
		enchantmentProperties.computeIfAbsent(windowId, aShort -> new WindowProperties()).put(key, value);
	}

	public short getEnchantmentTableProperty(short windowId, int key) {
		final Integer value = enchantmentProperties.get(windowId).get(key);
		if (value == null) {
			return 0; // Assume default value, nothing we can do about
		}
		if (enchantmentProperties.get(windowId).properties.isEmpty()) { // Remove from list if nothing to track
			enchantmentProperties.remove(windowId);
		}
		return value.shortValue();
	}

	public static void updateBrewingStand(UserConnection user, Item blazePowder, short windowId) throws Exception {
		if (blazePowder != null && blazePowder.identifier() != 377) {
			return;
		}
		int amount = blazePowder == null ? 0 : blazePowder.amount();

		PacketWrapper openWindow = PacketWrapper.create(ClientboundPackets1_8.OPEN_WINDOW, user);
		openWindow.write(Type.UNSIGNED_BYTE, windowId);
		openWindow.write(Type.STRING, "minecraft:brewing_stand");

		ATextComponent title = new StringComponent().
			append(new TranslationComponent("container.brewing")).
			append(new StringComponent(": " + TextFormatting.DARK_GRAY)).
			append(new StringComponent(amount + " " + TextFormatting.DARK_RED)).
			append(new TranslationComponent("item.blazePowder.name", TextFormatting.DARK_RED));

		openWindow.write(Type.COMPONENT, TextComponentSerializer.V1_8.serializeJson(title));
		openWindow.write(Type.UNSIGNED_BYTE, (short) 420);
		openWindow.scheduleSend(Protocol1_8To1_9.class);

		Item[] items = user.get(WindowTracker.class).getBrewingItems(windowId);
		for (int i = 0; i < items.length; i++) {
			PacketWrapper setSlot = PacketWrapper.create(ClientboundPackets1_8.SET_SLOT, user);
			setSlot.write(Type.UNSIGNED_BYTE, windowId);
			setSlot.write(Type.SHORT, (short) i);
			setSlot.write(Type.ITEM1_8, items[i]);
			setSlot.scheduleSend(Protocol1_8To1_9.class);
		}
	}

	public static class WindowProperties {

		private final Map<Integer, Integer> properties = new HashMap<>();

		public Integer get(int key) {
			if (!properties.containsKey(key)) {
				return null;
			}
			return properties.remove(key);
		}

		public void put(int key, int value) {
			properties.put(key, value);
		}
	}
}
