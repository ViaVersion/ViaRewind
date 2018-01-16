package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.PartialType;

public class CustomIntType extends PartialType<Integer[], Integer> {

	public CustomIntType(Integer param) {
		super(param, Integer[].class);
	}

	public Integer[] read(ByteBuf buffer, Integer size) throws Exception {
		if (buffer.readableBytes() < size/4) {
			throw new RuntimeException("Readable bytes does not match expected!");
		} else {
			Integer[] array = new Integer[size];
			for (int i = 0; i<size; i++) {
				array[i] = buffer.readInt();
			}
			return array;
		}
	}

	public void write(ByteBuf buffer, Integer size, Integer[] ints) throws Exception {
		for (Integer integer : ints) buffer.writeInt(integer);
	}
}
