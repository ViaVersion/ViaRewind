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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viabackwards.api.rewriters.LegacyEnchantmentRewriter;
import com.viaversion.viarewind.api.rewriter.VRBlockItemRewriter;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.rewriter.PotionMappings;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.Windows;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.*;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.util.Key;

import java.util.HashSet;
import java.util.Set;

import static com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter.potionNameFromDamage;

public class BlockItemPackets extends VRBlockItemRewriter<ClientboundPackets1_9, ServerboundPackets1_8, Protocol1_8To1_9> {

	public final Set<String> VALID_ATTRIBUTES = new HashSet<>();

	private LegacyEnchantmentRewriter enchantmentRewriter;

	public BlockItemPackets(Protocol1_8To1_9 protocol) {
		super(protocol, "1.9");

		VALID_ATTRIBUTES.add("generic.maxHealth");
		VALID_ATTRIBUTES.add("generic.followRange");
		VALID_ATTRIBUTES.add("generic.knockbackResistance");
		VALID_ATTRIBUTES.add("generic.movementSpeed");
		VALID_ATTRIBUTES.add("generic.attackDamage");
		VALID_ATTRIBUTES.add("horse.jumpStrength");
		VALID_ATTRIBUTES.add("zombie.spawnReinforcements");
	}

	@Override
	protected void registerPackets() {
		registerBlockChange(ClientboundPackets1_9.BLOCK_CHANGE);
		registerMultiBlockChange(ClientboundPackets1_9.MULTI_BLOCK_CHANGE);

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

		protocol.registerClientbound(ClientboundPackets1_9.WINDOW_ITEMS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				handler(packetWrapper -> {
					short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
					Item[] items = packetWrapper.read(Type.ITEM1_8_SHORT_ARRAY);
					for (int i = 0; i < items.length; i++) {
						items[i] = handleItemToClient(items[i]);
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

		protocol.registerClientbound(ClientboundPackets1_9.SET_SLOT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.ITEM1_8);
				handler(packetWrapper -> {
					packetWrapper.set(Type.ITEM1_8, 0, handleItemToClient(packetWrapper.get(Type.ITEM1_8, 0)));
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

		protocol.registerServerbound(ServerboundPackets1_8.CLICK_WINDOW, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.UNSIGNED_BYTE);
				map(Type.SHORT);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Type.BYTE, Type.VAR_INT);
				map(Type.ITEM1_8);
				handler(packetWrapper -> packetWrapper.set(Type.ITEM1_8, 0, handleItemToServer(packetWrapper.get(Type.ITEM1_8, 0))));
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
				handler(packetWrapper -> packetWrapper.set(Type.ITEM1_8, 0, handleItemToServer(packetWrapper.get(Type.ITEM1_8, 0))));
			}
		});
	}

	@Override
	protected void registerRewrites() {
		enchantmentRewriter = new LegacyEnchantmentRewriter(getNbtTagName());
		enchantmentRewriter.registerEnchantment(9, "§r§7Frost Walker");
		enchantmentRewriter.registerEnchantment(70, "§r§7Mending");
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

		CompoundTag displayTag = tag.get("display");
		if (item.data() != 0 && tag.contains("Unbreakable")) {
			final ByteTag unbreakableTag = tag.get("Unbreakable");
			if (unbreakableTag != null && unbreakableTag.asByte() != 0) {
				tag.put(getNbtTagName() + "|Unbreakable", new ByteTag(unbreakableTag.asByte()));
				tag.remove("Unbreakable");

				if (displayTag == null) { // Remove the display tag again if it was only added to emulate unbreakable
					tag.put("display", displayTag = new CompoundTag());
					tag.put(getNbtTagName() + "|noDisplay", new ByteTag());
				}

				ListTag<StringTag> loreTag = displayTag.getListTag("Lore", StringTag.class);
				if (loreTag == null) {
					displayTag.put("Lore", loreTag = new ListTag<>(StringTag.class));
				}
				loreTag.add(new StringTag("§9Unbreakable"));
			}
		}

		if (item.identifier() == 383 && item.data() == 0) { // Spawn eggs
			int data = 0;
			final CompoundTag entityTag = tag.getCompoundTag("EntityTag");
			if (entityTag != null) {
				final StringTag idTag = entityTag.getStringTag("id");
				if (idTag != null) {
					final String id = idTag.getValue();
					if (ItemRewriter.ENTITY_NAME_TO_ID.containsKey(id)) {
						data = ItemRewriter.ENTITY_NAME_TO_ID.get(id);
					} else if (displayTag == null) {
						tag.put("display", displayTag = new CompoundTag());
						tag.put(getNbtTagName() + "|noDisplay", new ByteTag());
						displayTag.put("Name", new StringTag("§rSpawn " + id));
					}
				}
			}
			item.setData((short) data);
		}

		// Potion, splash potion, lingering potion
		final boolean potion = item.identifier() == 373;
		final boolean splashPotion = item.identifier() == 438;
		final boolean lingeringPotion = item.identifier() == 441;

		if (potion || splashPotion || lingeringPotion) {
			int data = 0;
			final StringTag potionTag = tag.getStringTag("Potion");
			if (potionTag != null) {
				String potionName = Key.stripMinecraftNamespace(potionTag.getValue());
				if (PotionMappings.POTION_NAME_TO_ID.containsKey(potionName)) {
					data = PotionMappings.POTION_NAME_TO_ID.get(potionName);
				}
				if (splashPotion) {
					potionName += "_splash";
				} else if (lingeringPotion) {
					potionName += "_lingering";
				}
				// Don't override custom name
				if ((displayTag == null || !displayTag.contains("Name")) && PotionMappings.POTION_NAME_INDEX.containsKey(potionName)) {
					tag.put("display", displayTag = new CompoundTag());
					tag.put(getNbtTagName() + "|noDisplay", new ByteTag());
					displayTag.put("Name", new StringTag(PotionMappings.POTION_NAME_INDEX.get(potionName)));
				}
			}
			if (splashPotion || lingeringPotion) { // Convert splash and lingering potions to normal potions
				item.setIdentifier(373);
				data += 8192;
			}

			item.setData((short) data);
		}

		final ListTag<CompoundTag> attributeModifiers = tag.getListTag("AttributeModifiers", CompoundTag.class);
		if (attributeModifiers != null) {
			tag.put(getNbtTagName() + "|AttributeModifiers", attributeModifiers.copy());
			attributeModifiers.getValue().removeIf(entries -> {
				final StringTag nameTag = entries.getStringTag("AttributeName");
				return nameTag != null && !VALID_ATTRIBUTES.contains(nameTag.getValue());
			});
		}

		return item;
	}

	@Override
	public Item handleItemToServer(Item item) {
		if (item == null) return null;
		super.handleItemToServer(item);

		CompoundTag tag = item.tag();
		if (tag == null) {
			item.setTag(tag = new CompoundTag());
		}
		if (tag.getListTag(getNbtTagName() + "|ench") != null) {
			enchantmentRewriter.rewriteEnchantmentsToServer(tag, false);
		}
		if (tag.getListTag(getNbtTagName() + "|StoredEnchantments") != null) {
			enchantmentRewriter.rewriteEnchantmentsToServer(tag, true);
		}

		if (item.identifier() == 383 && item.data() != 0) { // Spawn eggs
			if (!tag.contains("EntityTag") && ItemRewriter.ENTITY_ID_TO_NAME.containsKey((int) item.data())) {
				final CompoundTag entityTag = new CompoundTag();
				entityTag.put("id", new StringTag(ItemRewriter.ENTITY_ID_TO_NAME.get((int) item.data())));
				tag.put("EntityTag", entityTag);
			}
			item.setData((short) 0);
		}

		if (item.identifier() == 373 && !tag.contains("Potion")) { // Potions
            if (item.data() >= 16384) {
				item.setIdentifier(438);
				item.setData((short) (item.data() - 8192));
			}

			final String name = item.data() == 8192 ? "water" : potionNameFromDamage(item.data());
			tag.put("Potion", new StringTag("minecraft:" + name));
			item.setData((short) 0);
		}

		final Tag noDisplayTag = tag.remove(getNbtTagName() + "|noDisplay");
		if (noDisplayTag != null) {
			tag.remove("display");
		}
		final Tag unbreakableTag = tag.remove(getNbtTagName() + "|Unbreakable");
		if (unbreakableTag != null) {
			tag.put("Unbreakable", unbreakableTag);
		}
		final Tag attributeModifiersTag = tag.remove(getNbtTagName() + "|AttributeModifiers");
		if (attributeModifiersTag != null) {
			tag.put("AttributeModifiers", attributeModifiersTag);
		}

		return item;
	}
}
