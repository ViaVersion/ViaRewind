package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import us.myles.ViaVersion.api.type.Type;
import us.myles.viaversion.libs.opennbt.NBTIO;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public class NBTType extends Type<CompoundTag> {
	public NBTType() {
		super(CompoundTag.class);
	}

	@Override
	public CompoundTag read(ByteBuf buffer) throws Exception {
		short length = buffer.readShort();
		if (length < 0) return null;
		try (DataInputStream in = new DataInputStream(new ByteBufInputStream(buffer))) {
			return (CompoundTag) NBTIO.readTag((DataInput) in);
		}
	}

	@Override
	public void write(ByteBuf buffer, CompoundTag nbt) throws Exception {
		if (nbt == null) {
			buffer.writeShort(-1);
		} else {
			ByteBuf buf = buffer.alloc().buffer();
			try (DataOutputStream out = new DataOutputStream(new ByteBufOutputStream(buf))) {
				NBTIO.writeTag((DataOutput) out, nbt);
				out.flush();
				buffer.writeShort(buf.readableBytes());
				buffer.writeBytes(buf);
			} finally {
				buf.release();
			}
		}
	}
}