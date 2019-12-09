package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

public class IntArrayType extends Type<int[]> {

	public IntArrayType() {
		super(int[].class);
	}

	@Override
	public int[] read(ByteBuf byteBuf) throws Exception {
		byte size = byteBuf.readByte();
		int[] array = new int[size];
		for (byte i = 0; i < size; i++) {
			array[i] = byteBuf.readInt();
		}
		return array;
	}

	@Override
	public void write(ByteBuf byteBuf, int[] array) throws Exception {
		byteBuf.writeByte(array.length);
		for (int i : array) byteBuf.writeInt(i);
	}
}
