package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.Type;

public class ItemType extends Type<Item> {
	private final boolean compressed;

	public ItemType(boolean compressed) {
		super(Item.class);
		this.compressed = compressed;
	}

	@Override
	public Item read(ByteBuf buffer) throws Exception {
		int readerIndex = buffer.readerIndex();
		short id = buffer.readShort();
		if (id < 0) {
			return null;
		}
		Item item = new Item();
		item.setIdentifier(id);
		item.setAmount(buffer.readByte());
		item.setData(buffer.readShort());
		item.setTag((compressed ? Types1_7_6_10.COMPRESSED_NBT : Types1_7_6_10.NBT).read(buffer));
		return item;
	}

	@Override
	public void write(ByteBuf buffer, Item item) throws Exception {
		if (item == null) {
			buffer.writeShort(-1);
		} else {
			buffer.writeShort(item.getIdentifier());
			buffer.writeByte(item.getAmount());
			buffer.writeShort(item.getData());
			(compressed ? Types1_7_6_10.COMPRESSED_NBT : Types1_7_6_10.NBT).write(buffer, item.getTag());
		}
	}
}
