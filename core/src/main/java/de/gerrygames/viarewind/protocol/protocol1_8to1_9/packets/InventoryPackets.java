package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Windows;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.util.GsonUtil;

public class InventoryPackets {

	public static void register(Protocol protocol) {
		/*  OUTGOING  */

		//Confirm Transaction
		protocol.registerOutgoing(State.PLAY, 0x11, 0x32);

		//Close Window
		protocol.registerOutgoing(State.PLAY, 0x12, 0x2E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				handler(wrapper -> {
					short windowsId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					wrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		//Open Window
		protocol.registerOutgoing(State.PLAY, 0x13, 0x2D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.STRING);
				map(Type.COMPONENT);
				map(Type.UNSIGNED_BYTE);

				handler(wrapper -> {
					String type = wrapper.get(Type.STRING, 0);
					if (type.equals("EntityHorse")) wrapper.passthrough(Type.INT);
				});
				handler(wrapper -> {
					short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					String windowType = wrapper.get(Type.STRING, 0);
					wrapper.user().get(Windows.class).put(windowId, windowType);
				});
				handler(wrapper -> {
					String type = wrapper.get(Type.STRING, 0);
					if (type.equalsIgnoreCase("minecraft:shulker_box")) {
						wrapper.set(Type.STRING, 0, type = "minecraft:container");
					}
					String name = wrapper.get(Type.COMPONENT, 0).toString();
					if (name.equalsIgnoreCase("{\"translate\":\"container.shulkerBox\"}")) {
						wrapper.set(Type.COMPONENT, 0, GsonUtil.getJsonParser().parse("{\"text\":\"Shulker Box\"}"));
					}
				});
			}
		});

		//Window Items
		protocol.registerOutgoing(State.PLAY, 0x14, 0x30, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.ITEM_ARRAY);

				handler(wrapper -> {
					Item[] items = wrapper.get(Type.ITEM_ARRAY, 0);
					for (Item item : items) ItemRewriter.toClient(item);

					short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (windowId == 0 && items.length == 46) {
						Item[] old = items;
						items = new Item[45];
						System.arraycopy(old, 0, items, 0, 45);
					} else {
						String type = wrapper.user().get(Windows.class).get(windowId);
						if (type != null && type.equalsIgnoreCase("minecraft:brewing_stand")) {
							System.arraycopy(items, 0, wrapper.user().get(Windows.class).getBrewingItems(windowId), 0, 4);
							Windows.updateBrewingStand(wrapper.user(), items[4], windowId);
							Item[] old = items;
							items = new Item[old.length - 1];
							System.arraycopy(old, 0, items, 0, 4);
							System.arraycopy(old, 5, items, 4, old.length - 5);
						}
					}
				});
			}
		});

		//Window Property
		protocol.registerOutgoing(State.PLAY, 0x15, 0x31);

		//Set Slot
		protocol.registerOutgoing(State.PLAY, 0x16, 0x2F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.ITEM);

				handler(wrapper -> ItemRewriter.toClient(wrapper.get(Type.ITEM, 0)));
				handler(wrapper -> {
					byte windowId = wrapper.get(Type.BYTE, 0);
					short slot = wrapper.get(Type.SHORT, 0);
					if (windowId == 0 && slot == 45) {
						wrapper.cancel();
						return;
					}
					String type = wrapper.user().get(Windows.class).get(windowId);
					if (type == null) return;
					if (type.equalsIgnoreCase("minecraft:brewing_stand")) {
						if (slot > 4) {
							wrapper.set(Type.SHORT, 0, slot -= 1);
						} else if (slot == 4) {
							wrapper.cancel();
							Windows.updateBrewingStand(wrapper.user(), wrapper.get(Type.ITEM, 0), windowId);
						} else {
							wrapper.user().get(Windows.class).getBrewingItems(windowId)[slot] = wrapper.get(Type.ITEM, 0);
						}
					}
				});
			}
		});

		/*  INCMOING  */

		//Close Window
		protocol.registerIncoming(State.PLAY, 0x08, 0x0D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				handler(wrapper -> {
					short windowsId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					wrapper.user().get(Windows.class).remove(windowsId);
				});
			}
		});

		//Click Window
		protocol.registerIncoming(State.PLAY, 0x07, 0x0E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.BYTE, Type.VAR_INT);
				map(Type.ITEM);

				handler(wrapper -> ItemRewriter.toServer(wrapper.get(Type.ITEM, 0)));
				handler(wrapper -> {
					short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					Windows windows = wrapper.user().get(Windows.class);
					String type = windows.get(windowId);
					if (type == null) return;
					if (type.equalsIgnoreCase("minecraft:brewing_stand")) {
						short slot = wrapper.get(Type.SHORT, 0);
						if (slot > 3) {
							wrapper.set(Type.SHORT, 0, slot += 1);
						}
					}
				});
			}
		});

		//Creative Inventory Action
		protocol.registerIncoming(State.PLAY, 0x18, 0x10, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.SHORT);
				map(Type.ITEM);

				handler(wrapper -> ItemRewriter.toServer(wrapper.get(Type.ITEM, 0)));
			}
		});

		//Enchant Item
		protocol.registerIncoming(State.PLAY, 0x06, 0x11);
	}
}
