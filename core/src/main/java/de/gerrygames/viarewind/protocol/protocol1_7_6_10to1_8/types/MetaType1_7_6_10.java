package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import us.myles.ViaVersion.api.type.Type;

public enum MetaType1_7_6_10 implements MetaType {
	Byte(0, Type.BYTE),
	Short(1, Type.SHORT),
	Int(2, Type.INT),
	Float(3, Type.FLOAT),
	String(4, Type.STRING),
	Slot(5, Types1_7_6_10.COMPRESSED_NBT_ITEM),
	Position(6, Type.VECTOR),
	NonExistent(-1, Type.NOTHING);

	private final int typeID;
	private final Type type;

	public static MetaType1_7_6_10 byId(int id) {
		return values()[id];
	}

	MetaType1_7_6_10(int typeID, Type type) {
		this.typeID = typeID;
		this.type = type;
	}

	public int getTypeID() {
		return this.typeID;
	}

	public Type getType() {
		return this.type;
	}
}
