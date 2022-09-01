package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Windows;

public class InventoryPackets {

	public static void register(Protocol<ClientboundPackets1_9, ClientboundPackets1_8,
			ServerboundPackets1_9, ServerboundPackets1_8> protocol) {
		/*  OUTGOING  */

		//Confirm Transaction

		//Close Window
		protocol.registerClientbound(ClientboundPackets1_9.CLOSE_WINDOW, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					packetWrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		//Open Window
		protocol.registerClientbound(ClientboundPackets1_9.OPEN_WINDOW, new PacketRemapper() {
			@Override
			public void registerMap() {
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
		protocol.registerClientbound(ClientboundPackets1_9.WINDOW_ITEMS, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					Item[] items = packetWrapper.read(Type.ITEM_ARRAY);
					for (int i = 0; i < items.length; i++) {
						items[i] = ItemRewriter.toClient(items[i]);
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
					packetWrapper.write(Type.ITEM_ARRAY, items);
				});
			}
		});

		//Window Property

		//Set Slot
		protocol.registerClientbound(ClientboundPackets1_9.SET_SLOT, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.ITEM);
				handler(packetWrapper -> {
					packetWrapper.set(Type.ITEM, 0, ItemRewriter.toClient(packetWrapper.get(Type.ITEM, 0)));
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
							Windows.updateBrewingStand(packetWrapper.user(), packetWrapper.get(Type.ITEM, 0), windowId);
						} else {
							packetWrapper.user().get(Windows.class).getBrewingItems(windowId)[slot] = packetWrapper.get(Type.ITEM, 0);
						}
					}
				});
			}
		});

		/*  INCMOING  */

		//Close Window
		protocol.registerServerbound(ServerboundPackets1_8.CLOSE_WINDOW, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					packetWrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		//Click Window
		protocol.registerServerbound(ServerboundPackets1_8.CLICK_WINDOW, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.BYTE, Type.VAR_INT);
				map(Type.ITEM);
				handler(packetWrapper -> packetWrapper.set(Type.ITEM, 0, ItemRewriter.toServer(packetWrapper.get(Type.ITEM, 0))));
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

		//Creative Inventory Action
		protocol.registerServerbound(ServerboundPackets1_8.CREATIVE_INVENTORY_ACTION, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.SHORT);
				map(Type.ITEM);
				handler(packetWrapper -> packetWrapper.set(Type.ITEM, 0, ItemRewriter.toServer(packetWrapper.get(Type.ITEM, 0))));
			}
		});

		//Enchant Item
	}
}
