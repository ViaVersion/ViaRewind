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
package com.viaversion.viarewind.protocol.v1_9to1_8.rewriter;

import com.viaversion.viabackwards.api.rewriters.LegacyEnchantmentRewriter;
import com.viaversion.viarewind.api.rewriter.VRBlockItemRewriter;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.data.PotionIdMappings1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.WindowTracker;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.nbt.tag.*;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.EntityIds1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.PotionIdMappings1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.util.Key;

import java.util.HashSet;
import java.util.Set;

public class BlockItemPacketRewriter1_9 extends VRBlockItemRewriter<ClientboundPackets1_9, ServerboundPackets1_8, Protocol1_9To1_8> {

	public final Set<String> VALID_ATTRIBUTES = new HashSet<>();

	private LegacyEnchantmentRewriter enchantmentRewriter;

	public BlockItemPacketRewriter1_9(Protocol1_9To1_8 protocol) {
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
		registerBlockChange(ClientboundPackets1_9.BLOCK_UPDATE);
		registerMultiBlockChange(ClientboundPackets1_9.CHUNK_BLOCKS_UPDATE);
		registerSetCreativeModeSlot(ServerboundPackets1_8.SET_CREATIVE_MODE_SLOT);

		protocol.registerClientbound(ClientboundPackets1_9.CONTAINER_CLOSE, wrapper -> {
			final short windowId = wrapper.passthrough(Types.UNSIGNED_BYTE);

			final WindowTracker tracker = wrapper.user().get(WindowTracker.class);

			final String windowType = tracker.get(windowId);
			if (windowType != null && windowType.equalsIgnoreCase("minecraft:enchanting_table")) {
				tracker.clearEnchantmentProperties();
			}
			tracker.remove(windowId);
		});

		protocol.registerClientbound(ClientboundPackets1_9.OPEN_SCREEN, wrapper -> {
			final short windowId = wrapper.passthrough(Types.UNSIGNED_BYTE);
			final String windowType = wrapper.passthrough(Types.STRING);

			wrapper.user().get(WindowTracker.class).put(windowId, windowType);
		});

		protocol.registerClientbound(ClientboundPackets1_9.CONTAINER_SET_CONTENT, wrapper -> {
			final short windowId = wrapper.passthrough(Types.UNSIGNED_BYTE);

			Item[] items = wrapper.read(Types.ITEM1_8_SHORT_ARRAY);
			for (int i = 0; i < items.length; i++) {
				items[i] = handleItemToClient(wrapper.user(), items[i]);
			}

			if (windowId == 0 && items.length == 46) {
				Item[] old = items;
				items = new Item[45];
				System.arraycopy(old, 0, items, 0, 45);
			} else {
				final String type = wrapper.user().get(WindowTracker.class).get(windowId);
				if (type != null && type.equalsIgnoreCase("minecraft:brewing_stand")) {
					System.arraycopy(items, 0, wrapper.user().get(WindowTracker.class).getBrewingItems(windowId), 0, 4);
					WindowTracker.updateBrewingStand(wrapper.user(), items[4], windowId);
					Item[] old = items;
					items = new Item[old.length - 1];
					System.arraycopy(old, 0, items, 0, 4);
					System.arraycopy(old, 5, items, 4, old.length - 5);
				}
			}

			wrapper.write(Types.ITEM1_8_SHORT_ARRAY, items);
		});

		protocol.registerClientbound(ClientboundPackets1_9.CONTAINER_SET_SLOT, wrapper -> {
			final byte windowId = wrapper.passthrough(Types.UNSIGNED_BYTE).byteValue();
			final short slot = wrapper.passthrough(Types.SHORT);
			final Item item = wrapper.passthrough(Types.ITEM1_8);

			handleItemToClient(wrapper.user(), item);
			if (windowId == 0 && slot == 45) {
				wrapper.cancel();
				return;
			}
			final WindowTracker windowTracker = wrapper.user().get(WindowTracker.class);
			final String windowType = windowTracker.get(windowId);
			if (windowType != null && windowType.equalsIgnoreCase("minecraft:brewing_stand")) {
				if (slot > 4) {
					wrapper.set(Types.SHORT, 0, (short) (slot - 1));
				} else if (slot == 4) {
					wrapper.cancel();
					WindowTracker.updateBrewingStand(wrapper.user(), wrapper.get(Types.ITEM1_8, 0), windowId);
				} else {
					windowTracker.getBrewingItems(windowId)[slot] = wrapper.get(Types.ITEM1_8, 0);
				}
			}
		});

		protocol.registerServerbound(ServerboundPackets1_8.CONTAINER_CLOSE, wrapper -> {
			final short windowId = wrapper.passthrough(Types.UNSIGNED_BYTE);
			wrapper.user().get(WindowTracker.class).remove(windowId);
		});

		protocol.registerServerbound(ServerboundPackets1_8.CONTAINER_CLICK, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.UNSIGNED_BYTE); // Window id
				map(Types.SHORT); // Slot
				map(Types.BYTE); // Button
				map(Types.SHORT); // Action number
				map(Types.BYTE, Types.VAR_INT); // Mode
				map(Types.ITEM1_8); // Clicked item
				handler(wrapper -> handleItemToServer(wrapper.user(), wrapper.get(Types.ITEM1_8, 0)));
				handler(wrapper -> {
					final short windowId = wrapper.get(Types.UNSIGNED_BYTE, 0);
					final String windowType = wrapper.user().get(WindowTracker.class).get(windowId);

					if (windowType != null && windowType.equalsIgnoreCase("minecraft:brewing_stand")) {
						final short slot = wrapper.get(Types.SHORT, 0);
						if (slot > 3) {
							wrapper.set(Types.SHORT, 0, (short) (slot + 1));
						}
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.CONTAINER_SET_DATA, wrapper -> {
			final short windowId = wrapper.passthrough(Types.UNSIGNED_BYTE);
			short key = wrapper.read(Types.SHORT);
			short value = wrapper.read(Types.SHORT);

			final WindowTracker tracker = wrapper.user().get(WindowTracker.class);
			final String windowType = tracker.get(windowId);
			if (windowType != null && windowType.equalsIgnoreCase("minecraft:enchanting_table")) {
				if (key >= 4 && key <= 6) { // Store enchantment ids
					tracker.putEnchantmentProperty(key, value);
					wrapper.cancel();
				} else if (key >= 7 && key <= 9) { // Mix levels with tracked ids
					key -= 3;
					final short property = tracker.getEnchantmentValue(key);
					value = (short) (property | (value << 8));
				}
			}
			wrapper.write(Types.SHORT, key);
			wrapper.write(Types.SHORT, value);
		});
	}

	@Override
	protected void registerRewrites() {
		enchantmentRewriter = new LegacyEnchantmentRewriter(nbtTagName());
		enchantmentRewriter.registerEnchantment(9, "§7Frost Walker");
		enchantmentRewriter.registerEnchantment(70, "§7Mending");
	}

	@Override
	public Item handleItemToClient(UserConnection connection, Item item) {
		if (item == null) return null;
		super.handleItemToClient(connection, item);

		CompoundTag tag = item.tag();
		enchantmentRewriter.handleToClient(item);

		CompoundTag displayTag = tag == null ? null : tag.getCompoundTag("display");
		if (item.data() != 0) {
			final ByteTag unbreakableTag = tag == null ? null : tag.getByteTag("Unbreakable");
			if (unbreakableTag != null && unbreakableTag.asByte() != 0) {
				tag.put(nbtTagName() + "|Unbreakable", new ByteTag(unbreakableTag.asByte()));
				tag.remove("Unbreakable");

				if (displayTag == null) { // Remove the display tag again if it was only added to emulate unbreakable
					tag.put("display", displayTag = new CompoundTag());
					tag.put(nbtTagName() + "|noDisplay", new ByteTag(true));
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
			final CompoundTag entityTag = tag == null ? null : tag.getCompoundTag("EntityTag");
			if (entityTag != null) {
				final StringTag idTag = entityTag.getStringTag("id");
				if (idTag != null) {
					final String id = idTag.getValue();
					if (EntityIds1_8.ENTITY_NAME_TO_ID.containsKey(id)) {
						data = EntityIds1_8.ENTITY_NAME_TO_ID.get(id);
					} else if (displayTag == null) {
						tag.put("display", displayTag = new CompoundTag());
						tag.put(nbtTagName() + "|noDisplay", new ByteTag(true));
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
			final StringTag potionTag = tag == null ? null : tag.getStringTag("Potion");
			if (potionTag != null) {
				String potionName = Key.stripMinecraftNamespace(potionTag.getValue());
				if (PotionIdMappings1_8.POTION_NAME_TO_ID.containsKey(potionName)) {
					data = PotionIdMappings1_8.POTION_NAME_TO_ID.get(potionName);
				}
				if (splashPotion) {
					potionName += "_splash";
				} else if (lingeringPotion) {
					potionName += "_lingering";
				}
				// Don't override custom name
				if ((displayTag == null || !displayTag.contains("Name")) && PotionIdMappings1_8.POTION_NAME_INDEX.containsKey(potionName)) {
					tag.put("display", displayTag = new CompoundTag());
					tag.put(nbtTagName() + "|noDisplay", new ByteTag(true));
					displayTag.put("Name", new StringTag(PotionIdMappings1_8.POTION_NAME_INDEX.get(potionName)));
				}
			}
			if (splashPotion || lingeringPotion) { // Convert splash and lingering potions to normal potions
				item.setIdentifier(373);
				data += 8192;
			}

			item.setData((short) data);
		}

		final ListTag<CompoundTag> attributeModifiers = tag == null ? null : tag.getListTag("AttributeModifiers", CompoundTag.class);
		if (attributeModifiers != null) {
			tag.put(nbtTagName() + "|AttributeModifiers", attributeModifiers.copy());
			attributeModifiers.getValue().removeIf(entries -> {
				final StringTag nameTag = entries.getStringTag("AttributeName");
				return nameTag != null && !VALID_ATTRIBUTES.contains(nameTag.getValue());
			});
		}

		return item;
	}

	@Override
	public Item handleItemToServer(UserConnection connection, Item item) {
		if (item == null) return null;
		super.handleItemToServer(connection, item);

		CompoundTag tag = item.tag();

		enchantmentRewriter.handleToServer(item);

		if (item.identifier() == 383 && item.data() != 0) { // Spawn eggs
			if ((tag == null || !tag.contains("EntityTag")) && EntityIds1_8.ENTITY_ID_TO_NAME.containsKey((int) item.data())) {
				if (tag == null) {
					item.setTag(tag = new CompoundTag());
				}
				final CompoundTag entityTag = new CompoundTag();
				entityTag.put("id", new StringTag(EntityIds1_8.ENTITY_ID_TO_NAME.get((int) item.data())));
				tag.put("EntityTag", entityTag);
			}
			item.setData((short) 0);
		}

		if (item.identifier() == 373 && (tag == null || !tag.contains("Potion"))) { // Potions
            if (item.data() >= 16384) {
				item.setIdentifier(438);
				item.setData((short) (item.data() - 8192));
			}

			if (tag == null) {
				item.setTag(tag = new CompoundTag());

				final String name = item.data() == 8192 ? "water" : PotionIdMappings1_9.potionNameFromDamage(item.data());
				tag.put("Potion", new StringTag("minecraft:" + name));
			}
			item.setData((short) 0);
		}

		if (tag == null) {
			return item;
		}
		final Tag noDisplayTag = tag.remove(nbtTagName() + "|noDisplay");
		if (noDisplayTag != null) {
			tag.remove("display");
			if (tag.isEmpty()) {
				item.setTag(null);
			}
		}
		final Tag unbreakableTag = tag.remove(nbtTagName() + "|Unbreakable");
		if (unbreakableTag != null) {
			tag.put("Unbreakable", unbreakableTag);
		}
		final Tag attributeModifiersTag = tag.remove(nbtTagName() + "|AttributeModifiers");
		if (attributeModifiersTag != null) {
			tag.put("AttributeModifiers", attributeModifiersTag);
		}

		return item;
	}
}
