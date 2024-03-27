package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viabackwards.api.rewriters.LegacyEnchantmentRewriter;
import com.viaversion.viarewind.api.rewriter.VRBlockItemRewriter;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.FurnaceData;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.InventoryTracker;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerSessionStorage;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.UUID;

public class BlockItemPackets extends VRBlockItemRewriter<ClientboundPackets1_8, ServerboundPackets1_7_2_5, Protocol1_7_6_10To1_8> {

	private LegacyEnchantmentRewriter enchantmentRewriter;

	public BlockItemPackets(Protocol1_7_6_10To1_8 protocol) {
		super(protocol, "1.8");
	}

	@Override
	protected void registerPackets() {
		protocol.registerClientbound(ClientboundPackets1_8.OPEN_WINDOW, wrapper -> {
			final InventoryTracker windowTracker = wrapper.user().get(InventoryTracker.class);

			final short windowId = wrapper.passthrough(Type.UNSIGNED_BYTE); // window id
			final short windowTypeId = InventoryTracker.getInventoryType(wrapper.read(Type.STRING)); // window type

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

			wrapper.user().get(InventoryTracker.class).remove(windowId);
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_SLOT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE); // window id
				map(Type.SHORT); // slot

				handler(wrapper -> {
					final short windowType = wrapper.user().get(InventoryTracker.class).get(wrapper.get(Type.UNSIGNED_BYTE, 0));
					final short slot = wrapper.get(Type.SHORT, 0);
					if (windowType == 4) { // Enchantment Table
						if (slot == 1) {
							wrapper.cancel();
						} else if (slot >= 2) {
							wrapper.set(Type.SHORT, 0, (short) (slot - 1));
						}
					}
				});

				map(Type.ITEM1_8, Types1_7_6_10.COMPRESSED_NBT_ITEM); // item

				// remap item
				handler(wrapper -> {
					final Item item = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);
					handleItemToClient(item);

					wrapper.set(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0, item);
				});

				handler(wrapper -> {
					final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (windowId != 0) return;

					final short slot = wrapper.get(Type.SHORT, 0);
					if (slot < 5 || slot > 8) return;

					final PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);
					final Item item = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM, 0);

					playerSession.setPlayerEquipment(wrapper.user().getProtocolInfo().getUuid(), item, 8 - slot);
					if (playerSession.isSpectator()) { // Spectator mode didn't exist in 1.7.10
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
					final short windowType = wrapper.user().get(InventoryTracker.class).get(wrapper.get(Type.UNSIGNED_BYTE, 0));

					Item[] items = wrapper.read(Type.ITEM1_8_SHORT_ARRAY);
					if (windowType == 4) { // Enchantment Table
						Item[] old = items;
						items = new Item[old.length - 1];
						items[0] = old[0];
						System.arraycopy(old, 2, items, 1, old.length - 3);
					}
					for (int i = 0; i < items.length; i++) {
						items[i] = handleItemToClient(items[i]);
					}
					wrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, items); // items
				});

				handler(wrapper -> {
					final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
					if (windowId != 0) return;

					final UUID userId = wrapper.user().getProtocolInfo().getUuid();
					final PlayerSessionStorage playerSession = wrapper.user().get(PlayerSessionStorage.class);

					final Item[] items = wrapper.get(Types1_7_6_10.COMPRESSED_NBT_ITEM_ARRAY, 0);
					for (int i = 5; i < 9; i++) {
						playerSession.setPlayerEquipment(userId, items[i], 8 - i);
						if (playerSession.isSpectator()) {
							items[i] = null;
						}
					}
					if (playerSession.isSpectator()) {
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
					final InventoryTracker windowTracker = wrapper.user().get(InventoryTracker.class);

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

			wrapper.user().get(InventoryTracker.class).remove(windowId);
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CLICK_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.BYTE, Type.UNSIGNED_BYTE); // window id
				map(Type.SHORT); // slot
				handler(wrapper -> {
					final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);  //Window Id
					final short slot = wrapper.get(Type.SHORT, 0);

					final short windowType = wrapper.user().get(InventoryTracker.class).get(windowId);
					if (windowType == 4) { // Enchantment Table
						if (slot > 0) {
							wrapper.set(Type.SHORT, 0, (short) (slot + 1));
						}
					}
				});
				map(Type.BYTE); // button
				map(Type.SHORT); // action number
				map(Type.BYTE); // mode
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM1_8); // clicked item

				// remap item
				handler(wrapper -> {
					final Item item = wrapper.get(Type.ITEM1_8, 0);
					handleItemToServer(item);
					wrapper.set(Type.ITEM1_8, 0, item);
				});
			}
		});

		protocol.registerServerbound(ServerboundPackets1_7_2_5.CREATIVE_INVENTORY_ACTION, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.SHORT); // slot
				map(Types1_7_6_10.COMPRESSED_NBT_ITEM, Type.ITEM1_8); // item

				// remap item
				handler(wrapper -> {
					final Item item = wrapper.get(Type.ITEM1_8, 0);
					handleItemToServer(item);
					wrapper.set(Type.ITEM1_8, 0, item);
				});
			}
		});
	}

	@Override
	protected void registerRewrites() {
		enchantmentRewriter = new LegacyEnchantmentRewriter(getNbtTagName());
		enchantmentRewriter.registerEnchantment(8, "§r§7Depth Strider");
	}

	@Override
	public Item handleItemToClient(Item item) {
		if (item == null) return null;
		super.handleItemToClient(item);

		CompoundTag tag = item.tag();
		if (tag == null) {
			item.setTag(tag = new CompoundTag());
		}

		if (tag.getListTag("ench") != null) {
			enchantmentRewriter.rewriteEnchantmentsToClient(tag, false);
		}
		if (tag.getListTag("StoredEnchantments") != null) {
			enchantmentRewriter.rewriteEnchantmentsToClient(tag, true);
		}
		
		if (item.identifier() == 387) {
			final ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
			if (pages == null) return item;
			
			final ListTag<StringTag> oldPages = new ListTag<>(StringTag.class);
			tag.put(getNbtTagName() + "|pages", oldPages);

			for (StringTag page : pages) {
				final String value = page.getValue();
				oldPages.add(new StringTag(value));
				page.setValue(ChatUtil.jsonToLegacy(value));
			}
		}
		return item;
	}

	@Override
	public Item handleItemToServer(Item item) {
		if (item == null) return null;
		super.handleItemToServer(item);

		final CompoundTag tag = item.tag();
		if (tag == null) return item;

		if (tag.getListTag(getNbtTagName() + "|ench") != null) {
			enchantmentRewriter.rewriteEnchantmentsToServer(tag, false);
		}
		if (tag.getListTag(getNbtTagName() + "|StoredEnchantments") != null) {
			enchantmentRewriter.rewriteEnchantmentsToServer(tag, true);
		}

		if (item.identifier() == 387) {
			final ListTag<StringTag> oldPages = tag.get(getNbtTagName() + "|pages");
			if (oldPages != null) {
				tag.remove("pages");
				tag.put("pages", oldPages);
			}
		}
		
		return item;
	}
}
