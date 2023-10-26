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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.Windows;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;

public class InventoryPackets {

	public static void register(final Protocol1_8To1_9 protocol) {
		protocol.registerClientbound(ClientboundPackets1_9.CLOSE_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					packetWrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		//Open Window
		protocol.registerClientbound(ClientboundPackets1_9.OPEN_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				map(Type.STRING);
				map(Type.COMPONENT);
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					String type = packetWrapper.get(Type.STRING, 0);
					if (type.equals("EntityHorse")) packetWrapper.passthrough(Type.INT);
				});
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					String windowType = packetWrapper.get(Type.STRING, 0);
					packetWrapper.user().get(Windows.class).put(windowId, windowType);
				});
				handler(packetWrapper -> {
					String type = packetWrapper.get(Type.STRING, 0);
					if (type.equalsIgnoreCase("minecraft:shulker_box")) {
						packetWrapper.set(Type.STRING, 0, type = "minecraft:container");
					}
					String name = packetWrapper.get(Type.COMPONENT, 0).toString();
					if (name.equalsIgnoreCase("{\"translate\":\"container.shulkerBox\"}")) {
						packetWrapper.set(Type.COMPONENT, 0, JsonParser.parseString("{\"text\":\"Shulker Box\"}"));
					}
				});
			}
		});

		//Window Items
		protocol.registerClientbound(ClientboundPackets1_9.WINDOW_ITEMS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					Item[] items = packetWrapper.read(Type.ITEM1_8_SHORT_ARRAY);
					for (int i = 0; i < items.length; i++) {
						items[i] = protocol.getItemRewriter().handleItemToClient(items[i]);
					}
					if (windowId == 0 && items.length == 46) {
						Item[] old = items;
						items = new Item[45];
						System.arraycopy(old, 0, items, 0, 45);
					} else {
						String type = packetWrapper.user().get(Windows.class).get(windowId);
						if (type != null && type.equalsIgnoreCase("minecraft:brewing_stand")) {
							System.arraycopy(items, 0, packetWrapper.user().get(Windows.class).getBrewingItems(windowId), 0, 4);
							Windows.updateBrewingStand(packetWrapper.user(), items[4], windowId);
							Item[] old = items;
							items = new Item[old.length - 1];
							System.arraycopy(old, 0, items, 0, 4);
							System.arraycopy(old, 5, items, 4, old.length - 5);
						}
					}
					packetWrapper.write(Type.ITEM1_8_SHORT_ARRAY, items);
				});
			}
		});

		//Window Property

		//Set Slot
		protocol.registerClientbound(ClientboundPackets1_9.SET_SLOT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.ITEM1_8);
				handler(packetWrapper -> {
					packetWrapper.set(Type.ITEM1_8, 0, protocol.getItemRewriter().handleItemToClient(packetWrapper.get(Type.ITEM1_8, 0)));
					byte windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0).byteValue();
					short slot = packetWrapper.get(Type.SHORT, 0);
					if (windowId == 0 && slot == 45) {
						packetWrapper.cancel();
						return;
					}
					String type = packetWrapper.user().get(Windows.class).get(windowId);
					if (type == null) return;
					if (type.equalsIgnoreCase("minecraft:brewing_stand")) {
						if (slot > 4) {
							packetWrapper.set(Type.SHORT, 0, slot -= 1);
						} else if (slot == 4) {
							packetWrapper.cancel();
							Windows.updateBrewingStand(packetWrapper.user(), packetWrapper.get(Type.ITEM1_8, 0), windowId);
						} else {
							packetWrapper.user().get(Windows.class).getBrewingItems(windowId)[slot] = packetWrapper.get(Type.ITEM1_8, 0);
						}
					}
				});
			}
		});

		/*  INCOMING  */

		//Close Window
		protocol.registerServerbound(ServerboundPackets1_8.CLOSE_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					packetWrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		//Click Window
		protocol.registerServerbound(ServerboundPackets1_8.CLICK_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.BYTE, Type.VAR_INT);
				map(Type.ITEM1_8);
				handler(packetWrapper -> packetWrapper.set(Type.ITEM1_8, 0, protocol.getItemRewriter().handleItemToServer(packetWrapper.get(Type.ITEM1_8, 0))));
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					Windows windows = packetWrapper.user().get(Windows.class);
					String type = windows.get(windowId);
					if (type == null) return;
					if (type.equalsIgnoreCase("minecraft:brewing_stand")) {
						short slot = packetWrapper.get(Type.SHORT, 0);
						if (slot > 3) {
							packetWrapper.set(Type.SHORT, 0, slot += 1);
						}
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.CREATIVE_INVENTORY_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.SHORT);
				map(Type.ITEM1_8);
				handler(packetWrapper -> packetWrapper.set(Type.ITEM1_8, 0, protocol.getItemRewriter().handleItemToServer(packetWrapper.get(Type.ITEM1_8, 0))));
			}
		});
	}
}
