package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;

import java.util.List;

public class Types1_7_6_10 {
	public static final Type<CompoundTag> COMPRESSED_NBT = new CompressedNBTType();
	public static final Type<Item[]> ITEM_ARRAY = new ItemArrayType(false);
	public static final Type<Item[]> COMPRESSED_NBT_ITEM_ARRAY = new ItemArrayType(true);
	public static final Type<Item> ITEM = new ItemType(false);
	public static final Type<Item> COMPRESSED_NBT_ITEM = new ItemType(true);
	public static final Type<List<Metadata>> METADATA_LIST = new MetadataListType();
	public static final Type<Metadata> METADATA = new MetadataType();
	public static final Type<CompoundTag> NBT = new NBTType();
	/**
	 * An int array prefixed with byte representing the size
	 */
	public static final Type<int[]> INT_ARRAY = new IntArrayType();
}
