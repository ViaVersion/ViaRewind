package de.gerrygames.viarewind.replacement;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public class ReplacementRegistry {
	private final Int2ObjectMap<Replacement> itemReplacements = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<Replacement> blockReplacements = new Int2ObjectOpenHashMap<>();


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
		itemReplacements.put(combine(id, data), replacement);
	}

	public void registerBlock(int id, int data, Replacement replacement) {
		blockReplacements.put(combine(id, data), replacement);
	}

	public void registerItemBlock(int id, int data, Replacement replacement) {
		registerItem(id, data, replacement);
		registerBlock(id, data, replacement);
	}

	public Item replace(Item item) {
		Replacement replacement = itemReplacements.get(combine(item.identifier(), item.data()));
		if (replacement==null) replacement = itemReplacements.get(combine(item.identifier(), -1));
		return replacement==null ? item : replacement.replace(item);
	}

	public Replacement replace(int id, int data) {
		Replacement replacement = blockReplacements.get(combine(id, data));
		if (replacement == null) {
			replacement = blockReplacements.get(combine(id, -1));
		}
		return replacement;
	}

	public static int combine(int id, int data) {
		return (id << 16) | (data & 0xFFFF);
	}
}
