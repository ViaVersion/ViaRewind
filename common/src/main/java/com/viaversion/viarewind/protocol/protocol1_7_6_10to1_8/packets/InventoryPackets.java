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

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.FurnaceData;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.WindowTracker;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.UUID;

public class InventoryPackets {

	public static void register(Protocol1_7_6_10To1_8 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.OPEN_WINDOW, wrapper -> {
			final WindowTracker windowTracker = wrapper.user().get(WindowTracker.class);

			final short windowId = wrapper.passthrough(Type.UNSIGNED_BYTE); // window id
			final short windowTypeId = WindowTracker.getInventoryType(wrapper.read(Type.STRING)); // window type

			windowTracker.getWindowTypeMap().put(windowId, windowTypeId);
			wrapper.write(Type.UNSIGNED_BYTE, windowTypeId); // window type id

			final JsonElement titleComponent = wrapper.read(Type.COMPONENT); // Title
			String title = ChatUtil.jsonToLegacy(titleComponent);
			title = ChatUtil.removeUnusedColor(title, '8');
			if (title.length() > 32) {
				title = title.substring(0, 32);
			}
			wrapper.write(Type.STRING, title); // window title

			wrapper.passthrough(Type.UNSIGNED_BYTE); // slots count
			wrapper.write(Type.BOOLEAN, true); // use provided window title

			if (windowTypeId == 11) { // Horse
				wrapper.passthrough(Type.INT); // entity id
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.CLOSE_WINDOW, wrapper -> {
			final short windowId = wrapper.passthrough(Type.UNSIGNED_BYTE); // window id

			wrapper.user().get(WindowTracker.class).remove(windowId);
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_SLOT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE); // window id
				map(Type.SHORT); // slot

				handler(wrapper -> {
					final short windowType = wrapper.user().get(WindowTracker.class).get(wrapper.get(Type.UNSIGNED_BYTE, 0));
					final short slot = wrapper.get(Type.SHORT, 0);
					if (windowType == 4) { // Enchantment Table
						if (slot == 1) {
							wrapper.cancel();
						} else if (slot >= 2) {
							wrapper.set(Type.SHORT, 0, (short) (slot - 1));
						}
					}
				});

				map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM); // item

				// remap item
				handler(wrapper -> {
					final Item item = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					protocol.getItemRewriter().handleItemToClient(item);

					wrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
				});

				handler(wrapper -> {
					final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (windowId != 0) return;

					final short slot = wrapper.get(Type.SHORT, 0);
					if (slot < 5 || slot > 8) return;

					final EntityTracker tracker = wrapper.user().get(EntityTracker.class);
					final Item item = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);

					tracker.setPlayerEquipment(wrapper.user().getProtocolInfo().getUuid(), item, 8 - slot);
					if (tracker.getGamemode() == 3) { // Spectator mode didn't exist in 1.7.10
						wrapper.cancel();
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.WINDOW_ITEMS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE); // window id

				// remap enchantment table items
				handler(wrapper -> {
					final short windowType = wrapper.user().get(WindowTracker.class).get(wrapper.get(Type.UNSIGNED_BYTE, 0));

					Item[] items = wrapper.read(Type.ITEM_ARRAY);
					if (windowType == 4) { // Enchantment Table
						Item[] old = items;
						items = new Item[old.length - 1];
						items[0] = old[0];
						System.arraycopy(old, 2, items, 1, old.length - 3);
					}
					for (int i = 0; i < items.length; i++) {
						items[i] = protocol.getItemRewriter().handleItemToClient(items[i]);
					}
					wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, items); // items
				});

				handler(wrapper -> {
					final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (windowId != 0) return;

					final UUID userId = wrapper.user().getProtocolInfo().getUuid();
					final EntityTracker tracker = wrapper.user().get(EntityTracker.class);

					final Item[] items = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, 0);
					for (int i = 5; i < 9; i++) {
						tracker.setPlayerEquipment(userId, items[i], 8 - i);
						if (tracker.getGamemode() == 3) {
							items[i] = null;
						}
					}
					if (tracker.getGamemode() == 3) {
						final GameProfileStorage.GameProfile profile = wrapper.user().get(GameProfileStorage.class).get(userId);
						if (profile != null) {
							items[5] = profile.getSkull();
						}
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.WINDOW_PROPERTY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE); // window id
				map(Type.SHORT); // progress bar id
				map(Type.SHORT); // progress bar value

				handler(wrapper -> {
					final WindowTracker windowTracker = wrapper.user().get(WindowTracker.class);

					final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					final short windowType = windowTracker.get(windowId);

					final short progressBarId = wrapper.get(Type.SHORT, 0);
					short progress = wrapper.get(Type.SHORT, 1);

					if (windowType == 2) { // Furnace
						final FurnaceData furnace = windowTracker.getFurnaceData().computeIfAbsent(windowId, x -> new FurnaceData());
						if (progressBarId == 0 || progressBarId == 1) {
							if (progressBarId == 0) {
								furnace.fuelLeft = progress;
							} else {
								furnace.maxFuel = progress;
							}
							if (furnace.maxFuel == 0) {
								wrapper.cancel();
								return;
							}
							progress = (short) (200 * furnace.fuelLeft / furnace.maxFuel);
							wrapper.set(Type.SHORT, 0, (short) 1);
							wrapper.set(Type.SHORT, 1, progress);
						} else if (progressBarId == 2 || progressBarId == 3) {
							if (progressBarId == 2) {
								furnace.progress = progress;
							} else {
								furnace.maxProgress = progress;
							}
							if (furnace.maxProgress == 0) {
								wrapper.cancel();
								return;
							}
							progress = (short) (200 * furnace.progress / furnace.maxProgress);
							wrapper.set(Type.SHORT, 0, (short) 0);
							wrapper.set(Type.SHORT, 1, progress);
						}
					} else if (windowType == 4 && progressBarId > 2) { // Enchanting Table
						wrapper.cancel();
					} else if (windowType == 8) { // Anvil
						windowTracker.levelCost = progress;
						windowTracker.anvilId = windowId;
					}
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CLOSE_WINDOW, wrapper -> {
			final short windowId = wrapper.passthrough(Type.UNSIGNED_BYTE);

			wrapper.user().get(WindowTracker.class).remove(windowId);
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CLICK_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.BYTE, Type.UNSIGNED_BYTE); // window id
				map(Type.SHORT); // slot
				handler(wrapper -> {
					final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);  //Window Id
					final short slot = wrapper.get(Type.SHORT, 0);

					final short windowType = wrapper.user().get(WindowTracker.class).get(windowId);
					if (windowType == 4) { // Enchantment Table
						if (slot > 0) {
							wrapper.set(Type.SHORT, 0, (short) (slot + 1));
						}
					}
				});
				map(Type.BYTE); // button
				map(Type.SHORT); // action number
				map(Type.BYTE); // mode
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM); // clicked item

				// remap item
				handler(wrapper -> {
					final Item item = wrapper.get(Type.ITEM, 0);
					protocol.getItemRewriter().handleItemToServer(item);
					wrapper.set(Type.ITEM, 0, item);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CREATIVE_INVENTORY_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.SHORT); // slot
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM); // item

				// remap item
				handler(wrapper -> {
					final Item item = wrapper.get(Type.ITEM, 0);
					protocol.getItemRewriter().handleItemToServer(item);
					wrapper.set(Type.ITEM, 0, item);
				});
			}
		});
	}
}
