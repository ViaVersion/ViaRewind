/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.item;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.io.NBTIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.*;

public class NBTType extends Type<CompoundTag> {
	public NBTType() {
		super(CompoundTag.class);
	}

	@Override
	public CompoundTag read(ByteBuf buffer) {
		short length = buffer.readShort();
		if (length < 0) {
			return null;
		}
		ByteBufInputStream byteBufInputStream = new ByteBufInputStream(buffer);
		DataInputStream dataInputStream = new DataInputStream(byteBufInputStream);
		try {
			return NBTIO.reader(CompoundTag.class).named().read((DataInput) dataInputStream);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		} finally {
			try {
				dataInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void write(ByteBuf buffer, CompoundTag nbt) throws Exception {
		if (nbt == null) {
			buffer.writeShort(-1);
		} else {
			ByteBuf buf = buffer.alloc().buffer();
			ByteBufOutputStream byteBufStream = new ByteBufOutputStream(buf);
			DataOutputStream dataOutputStream = new DataOutputStream(byteBufStream);
			NBTIO.writer().named().write((DataOutput) dataOutputStream, nbt);
			dataOutputStream.close();
			buffer.writeShort(buf.readableBytes());
			buffer.writeBytes(buf);
			buf.release();
		}
	}
}