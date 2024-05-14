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
package com.viaversion.viarewind.protocol.v1_9to1_8.storage;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentSerializer;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;

import java.util.HashMap;
import java.util.Map;

public class WindowTracker extends StoredObject {
	private final HashMap<Short, String> types = new HashMap<>();
	private final HashMap<Short, Item[]> brewingItems = new HashMap<>();
	private final Map<Short, Short> enchantmentProperties = new HashMap<>();

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

	public short getEnchantmentValue(final short key) {
		if (!enchantmentProperties.containsKey(key)) {
			return 0;
		}
		return enchantmentProperties.remove(key);
	}

	public void putEnchantmentProperty(short key, short value) {
		enchantmentProperties.put(key, value);
	}

	public void clearEnchantmentProperties() {
		enchantmentProperties.clear();
	}

	public static void updateBrewingStand(UserConnection user, Item blazePowder, short windowId) {
		if (blazePowder != null && blazePowder.identifier() != 377) {
			return;
		}
		int amount = blazePowder == null ? 0 : blazePowder.amount();

		PacketWrapper openWindow = PacketWrapper.create(ClientboundPackets1_8.OPEN_SCREEN, user);
		openWindow.write(Types.UNSIGNED_BYTE, windowId);
		openWindow.write(Types.STRING, "minecraft:brewing_stand");

		ATextComponent title = new StringComponent().
			append(new TranslationComponent("container.brewing")).
			append(new StringComponent(": " + TextFormatting.DARK_GRAY)).
			append(new StringComponent(amount + " " + TextFormatting.DARK_RED)).
			append(new TranslationComponent("item.blazePowder.name", TextFormatting.DARK_RED));

		openWindow.write(Types.COMPONENT, TextComponentSerializer.V1_8.serializeJson(title));
		openWindow.write(Types.UNSIGNED_BYTE, (short) 420);
		openWindow.scheduleSend(Protocol1_9To1_8.class);

		Item[] items = user.get(WindowTracker.class).getBrewingItems(windowId);
		for (int i = 0; i < items.length; i++) {
			PacketWrapper setSlot = PacketWrapper.create(ClientboundPackets1_8.CONTAINER_SET_SLOT, user);
			setSlot.write(Types.UNSIGNED_BYTE, windowId);
			setSlot.write(Types.SHORT, (short) i);
			setSlot.write(Types.ITEM1_8, items[i]);
			setSlot.scheduleSend(Protocol1_9To1_8.class);
		}
	}
}
