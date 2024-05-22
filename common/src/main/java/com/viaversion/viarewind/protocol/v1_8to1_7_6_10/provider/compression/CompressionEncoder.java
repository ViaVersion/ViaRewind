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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression;

import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.zip.Deflater;

public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
	private final Deflater deflater = new Deflater();

	private final int threshold;

	public CompressionEncoder(final int threshold) {
		this.threshold = threshold;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
		int frameLength = in.readableBytes();
		if (frameLength < this.threshold) {
			out.writeByte(0); // VarInt
			out.writeBytes(in);
			return;
		}

		Types.VAR_INT.writePrimitive(out, frameLength);

		ByteBuf temp = in;
		if (!in.hasArray()) {
			temp = ByteBufAllocator.DEFAULT.heapBuffer().writeBytes(in);
		} else {
			in.retain();
		}
		ByteBuf output = ByteBufAllocator.DEFAULT.heapBuffer();
		try {
			this.deflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
			deflater.finish();

			while (!deflater.finished()) {
				output.ensureWritable(4096);
				output.writerIndex(output.writerIndex() + this.deflater.deflate(output.array(), output.arrayOffset() + output.writerIndex(), output.writableBytes()));
			}
			out.writeBytes(output);
		} finally {
			output.release();
			temp.release();
			this.deflater.reset();
		}
	}
}