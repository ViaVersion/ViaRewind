package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Windows;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.utils.ChatUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.viaversion.libs.gson.JsonElement;

import java.util.UUID;

public class InventoryPackets {

	public static void register(Protocol protocol) {

		/*  OUTGOING  */

		//Open Window
		protocol.registerOutgoing(State.PLAY, 0x2D, 0x2D, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
					}
				});
			}
		});

		//Close Window
		protocol.registerOutgoing(State.PLAY, 0x2E, 0x2E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
						packetWrapper.user().get(Windows.class).remove(windowsId);
					}
				});
			}
		});

		//Set Slot
		protocol.registerOutgoing(State.PLAY, 0x2F, 0x2F, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowId = packetWrapper.read(Type.BYTE);  //Window Id
						short windowType = packetWrapper.user().get(Windows.class).get(windowId);
						packetWrapper.write(Type.BYTE, (byte) windowId);
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
					}
				});
				map(Type.ITEM, Types1_7_6_10.COMPRESSED_NBT_ITEM);  //Item
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
						ItemRewriter.toClient(item);
						packetWrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowId = packetWrapper.get(Type.BYTE, 0);
						if (windowId != 0) return;
						short slot = packetWrapper.get(Type.SHORT, 0);
						if (slot < 5 || slot > 8) return;
						Item item = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						UUID uuid = packetWrapper.user().get(ProtocolInfo.class).getUuid();
						Item[] equipment = tracker.getPlayerEquipment(uuid);
						if (equipment == null) {
							tracker.setPlayerEquipment(uuid, equipment = new Item[5]);
						}
						equipment[9 - slot] = item;
						if (tracker.getGamemode() == 3) packetWrapper.cancel();
					}
				});
			}
		});

		//Window Items
		protocol.registerOutgoing(State.PLAY, 0x30, 0x30, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
						if (windowId != 0) return;
						Item[] items = packetWrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, 0);
						EntityTracker tracker = packetWrapper.user().get(EntityTracker.class);
						UUID uuid = packetWrapper.user().get(ProtocolInfo.class).getUuid();
						Item[] equipment = tracker.getPlayerEquipment(uuid);
						if (equipment == null) {
							tracker.setPlayerEquipment(uuid, equipment = new Item[5]);
						}
						for (int i = 5; i < 9; i++) {
							equipment[9 - i] = items[i];
							if (tracker.getGamemode() == 3) items[i] = null;
						}
						if (tracker.getGamemode() == 3) {
							GameProfileStorage.GameProfile profile = packetWrapper.user().get(GameProfileStorage.class).get(uuid);
							if (profile != null) items[5] = profile.getSkull();
						}
					}
				});
			}
		});

		//Window Data
		protocol.registerOutgoing(State.PLAY, 0x31, 0x31, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.SHORT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
								return;
							}
						} else if (windowType == 8) {
							windows.levelCost = value;
							windows.anvilId = windowId;
						}
					}
				});
			}
		});

		/*  INCOMING  */

		//Close Window
		protocol.registerIncoming(State.PLAY, 0x0D, 0x0D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.UNSIGNED_BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
						packetWrapper.user().get(Windows.class).remove(windowsId);
					}
				});
			}
		});

		//Click Window
		protocol.registerIncoming(State.PLAY, 0x0E, 0x0E, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
					}
				});
				map(Type.BYTE);  //Button
				map(Type.SHORT);  //Action Number
				map(Type.BYTE);  //Mode
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Item item = packetWrapper.get(Type.ITEM, 0);
						ItemRewriter.toServer(item);
						packetWrapper.set(Type.ITEM, 0, item);
					}
				});
			}
		});

		//Confirm Transaction
		protocol.registerIncoming(State.PLAY, 0x0F, 0x0F, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.BOOLEAN);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int action = packetWrapper.get(Type.SHORT, 0);
						if (action == -89) packetWrapper.cancel();
					}
				});
			}
		});

		//Creative Inventory Action
		protocol.registerIncoming(State.PLAY, 0x10, 0x10, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.SHORT);  //Slot
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM);  //Item
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						Item item = packetWrapper.get(Type.ITEM, 0);
						ItemRewriter.toServer(item);
						packetWrapper.set(Type.ITEM, 0, item);
					}
				});
			}
		});

	}
}
