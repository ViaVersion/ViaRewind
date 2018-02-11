package de.gerrygames.viarewind.replacement;

import de.gerrygames.viarewind.storage.BlockStorage;
import us.myles.ViaVersion.api.minecraft.item.Item;

import java.util.HashMap;

public class ReplacementRegistry {
	private Replacement[] itemReplacements = new Replacement[2267 << 4 | 0x0f];
	private Replacement[] blockReplacements = new Replacement[2267 << 4 | 0x0f];


	public void registerItem(int id, Replacement replacement) {
		registerItem(id, -1, replacement);
	}

	public void registerBlock(int id, Replacement replacement) {
		registerBlock(id, -1, replacement);
	}

	public void registerItemBlock(int id, Replacement replacement) {
		registerItemBlock(id, -1, replacement);
	}

	public void registerItem(int id, int data, Replacement replacement) {
		itemReplacements[combine(id, data)] = replacement;
	}

	public void registerBlock(int id, int data, Replacement replacement) {
		blockReplacements[combine(id, data)] = replacement;
	}

	public void registerItemBlock(int id, int data, Replacement replacement) {
		registerItem(id, data, replacement);
		registerBlock(id, data, replacement);
	}

	public Item replace(Item item) {
		Replacement replacement = itemReplacements[combine(item.getId(), item.getData())];
		if (replacement==null) replacement = itemReplacements[combine(item.getId(), -1)];
		return replacement==null ? item : replacement.replace(item);
	}

	public BlockStorage.BlockState replace(BlockStorage.BlockState block) {
		Replacement replacement = blockReplacements[combine(block.getId(), block.getData())];
		if (replacement==null) replacement = blockReplacements[combine(block.getId(), -1)];
		return replacement==null ? block : replacement.replace(block);
	}

	private static int combine(int id, int data) {
		return (id << 4) | (data & 0xF);
	}
}
