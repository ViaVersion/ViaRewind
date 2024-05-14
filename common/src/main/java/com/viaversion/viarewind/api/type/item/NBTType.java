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
package com.viaversion.viarewind.api.type.item;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.io.NBTIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NBTType extends Type<CompoundTag> {
	public NBTType() {
		super(CompoundTag.class);
	}

	@Override
	public CompoundTag read(ByteBuf buffer) {
		short length = buffer.readShort();
		if (length <= 0) {
			return null;
		}

		ByteBuf compressed = buffer.readSlice(length);

		try (GZIPInputStream gzipStream = new GZIPInputStream(new ByteBufInputStream(compressed))) {
			return NBTIO.reader(CompoundTag.class).named().read(gzipStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void write(ByteBuf buffer, CompoundTag nbt) {
		if (nbt == null) {
			buffer.writeShort(-1);
			return;
		}

		ByteBuf compressedBuf = buffer.alloc().buffer();
		try {
			try (GZIPOutputStream gzipStream = new GZIPOutputStream(new ByteBufOutputStream(compressedBuf))) {
				NBTIO.writer().named().write(gzipStream, nbt);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			buffer.writeShort(compressedBuf.readableBytes());
			buffer.writeBytes(compressedBuf);
		} finally {
			compressedBuf.release();
		}
	}
}
