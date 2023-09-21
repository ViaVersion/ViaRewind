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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.Windows;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.ServerboundPackets1_7;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;

import java.util.UUID;

public class InventoryPackets {

	public static void register(Protocol1_7_6_10To1_8 protocol) {

		/*  OUTGOING  */

		protocol.registerClientbound(ClientboundPackets1_8.OPEN_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					short windowId = packetWrapper.passthrough(Type.UNSIGNED_BYTE);
					String windowType = packetWrapper.read(Type.STRING);
					short windowtypeId = (short) Windows.getInventoryType(windowType);
					packetWrapper.user().get(Windows.class).types.put(windowId, windowtypeId);
					packetWrapper.write(Type.UNSIGNED_BYTE, windowtypeId);

					JsonElement titleComponent = packetWrapper.read(Type.COMPONENT);  //Title
					String title = ChatUtil.jsonToLegacy(titleComponent);
					title = ChatUtil.removeUnusedColor(title, '8');
					if (title.length() > 32) {
						title = title.substring(0, 32);
					}
					packetWrapper.write(Type.STRING, title);  //Window title

					packetWrapper.passthrough(Type.UNSIGNED_BYTE);
					packetWrapper.write(Type.BOOLEAN, true);
					if (windowtypeId == 11) packetWrapper.passthrough(Type.INT);  //Entity Id
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.CLOSE_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					packetWrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_SLOT, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					short windowId = packetWrapper.read(Type.UNSIGNED_BYTE);  //Window Id
					short windowType = packetWrapper.user().get(Windows.class).get(windowId);
					packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
					short slot = packetWrapper.read(Type.SHORT);
					if (windowType == 4) {
						if (slot == 1) {
							packetWrapper.cancel();
							return;
						} else if (slot >= 2) {
							slot -= 1;
						}
					}
					packetWrapper.write(Type.SHORT, slot);  //Slot
				});
				map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM);  //Item
				handler(packetWrapper -> {
					Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					ItemRewriter.toClient(item);
					packetWrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
				});
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					if (windowId != 0) return;
					short slot = packetWrapper.get(Type.SHORT, 0);
					if (slot < 5 || slot > 8) return;
					Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					UUID myId = packetWrapper.user().getProtocolInfo().getUuid();
					tracker.setPlayerEquipment(myId, item, 8 - slot);
					if (tracker.getGamemode() == 3) packetWrapper.cancel();
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.WINDOW_ITEMS, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					short windowId = packetWrapper.read(Type.UNSIGNED_BYTE);  //Window Id
					short windowType = packetWrapper.user().get(Windows.class).get(windowId);
					packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
					Item[] items = packetWrapper.read(Type.ITEM_ARRAY);
					if (windowType == 4) {
						Item[] old = items;
						items = new Item[old.length - 1];
						items[0] = old[0];
						System.arraycopy(old, 2, items, 1, old.length - 3);
					}
					for (int i = 0; i < items.length; i++) items[i] = ItemRewriter.toClient(items[i]);
					packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, items);  //Items
				});
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					if (windowId != 0) return;
					Item[] items = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, 0);
					EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
					UUID myId = packetWrapper.user().getProtocolInfo().getUuid();
					for (int i = 5; i < 9; i++) {
						tracker.setPlayerEquipment(myId, items[i], 8 - i);
						if (tracker.getGamemode() == 3) items[i] = null;
					}
					if (tracker.getGamemode() == 3) {
						GameProfileStorage.GameProfile profile = packetWrapper.user().get(GameProfileStorage.class).get(myId);
						if (profile != null) items[5] = profile.getSkull();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.WINDOW_PROPERTY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.SHORT);
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					Windows windows = packetWrapper.user().get(Windows.class);
					short windowType = windows.get(windowId);

					short property = packetWrapper.get(Type.SHORT, 0);
					short value = packetWrapper.get(Type.SHORT, 1);

					if (windowType == -1) return;
					if (windowType == 2) {  //Furnace
						Windows.Furnace furnace = windows.furnace.computeIfAbsent(windowId, x -> new Windows.Furnace());
						if (property == 0 || property == 1) {
							if (property == 0) {
								furnace.setFuelLeft(value);
							} else {
								furnace.setMaxFuel(value);
							}
							if (furnace.getMaxFuel() == 0) {
								packetWrapper.cancel();
								return;
							}
							value = (short) (200 * furnace.getFuelLeft() / furnace.getMaxFuel());
							packetWrapper.set(Type.SHORT, 0, (short) 1);
							packetWrapper.set(Type.SHORT, 1, value);
						} else if (property == 2 || property == 3) {
							if (property == 2) {
								furnace.setProgress(value);
							} else {
								furnace.setMaxProgress(value);
							}
							if (furnace.getMaxProgress() == 0) {
								packetWrapper.cancel();
								return;
							}
							value = (short) (200 * furnace.getProgress() / furnace.getMaxProgress());
							packetWrapper.set(Type.SHORT, 0, (short) 0);
							packetWrapper.set(Type.SHORT, 1, value);
						}
					} else if (windowType == 4) {  //Enchanting Table
						if (property > 2) {
							packetWrapper.cancel();
						}
					} else if (windowType == 8) {
						windows.levelCost = value;
						windows.anvilId = windowId;
					}
				});
			}
		});

		/*  INCOMING  */

		protocol.registerServerbound(ServerboundPackets1_7.CLOSE_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					packetWrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7.CLICK_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				handler(packetWrapper -> {
					short windowId = packetWrapper.read(Type.BYTE);  //Window Id
					packetWrapper.write(Type.UNSIGNED_BYTE, windowId);
					short windowType = packetWrapper.user().get(Windows.class).get(windowId);
					short slot = packetWrapper.read(Type.SHORT);
					if (windowType == 4) {
						if (slot > 0) {
							slot += 1;
						}
					}
					packetWrapper.write(Type.SHORT, slot);
				});
				map(Type.BYTE);  //Button
				map(Type.SHORT);  //Action Number
				map(Type.BYTE);  //Mode
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);
				handler(packetWrapper -> {
					Item item = packetWrapper.get(Type.ITEM, 0);
					ItemRewriter.toServer(item);
					packetWrapper.set(Type.ITEM, 0, item);
				});
			}
		});

		//Creative Inventory Action
		protocol.registerServerbound(ServerboundPackets1_7.CREATIVE_INVENTORY_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.SHORT);  //Slot
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);  //Item
				handler(packetWrapper -> {
					Item item = packetWrapper.get(Type.ITEM, 0);
					ItemRewriter.toServer(item);
					packetWrapper.set(Type.ITEM, 0, item);
				});
			}
		});

	}
}
