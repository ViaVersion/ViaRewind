package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.Type;

public class ItemArrayType extends Type<Item[]> {
	private final boolean compressed;

	public ItemArrayType(boolean compressed) {
		super(Item[].class);
		this.compressed = compressed;
	}

	@Override
	public Item[] read(ByteBuf buffer) throws Exception {
		int amount = Type.SHORT.read(buffer);
		Item[] items = new Item[amount];

		for(int i = 0; i < amount; ++i) {
			items[i] = (compressed ? Types1_7_6_10.COMPRESSED_NBT_ITEM : Types1_7_6_10.ITEM).read(buffer);
		}
		return items;
	}

	@Override
	public void write(ByteBuf buffer, Item[] items) throws Exception {
		Type.SHORT.write(buffer, (short)items.length);
		for (Item item : items) {
			(compressed ? Types1_7_6_10.COMPRESSED_NBT_ITEM : Types1_7_6_10.ITEM).write(buffer, item);
		}
	}
}
