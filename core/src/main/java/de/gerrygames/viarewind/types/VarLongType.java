package de.gerrygames.viarewind.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

public class VarLongType extends Type<Long> {

	public static final VarLongType VAR_LONG = new VarLongType();

	public VarLongType() {
		super("VarLong", Long.class);
	}

	@Override
	public Long read(ByteBuf byteBuf) throws Exception {
		long i = 0L;
		int j = 0;
		byte b0;
		do
		{
			b0 = byteBuf.readByte();
			i |= (b0 & 0x7F) << j++ * 7;
			if (j > 10) {
				throw new RuntimeException("VarLong too big");
			}
		} while ((b0 & 0x80) == 128);
		return i;
	}

	@Override
	public void write(ByteBuf byteBuf, Long i) throws Exception {
		while ((i & 0xFFFFFFFFFFFFFF80L) != 0L)
		{
			byteBuf.writeByte((int)(i & 0x7F) | 0x80);
			i >>>= 7;
		}
		byteBuf.writeByte(i.intValue());
	}
}
