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
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.zip.Inflater;

public class CompressionDecoder extends MessageToMessageDecoder<ByteBuf> {
	private final Inflater inflater = new Inflater();

	private final int threshold;

	public CompressionDecoder(final int threshold) {
		this.threshold = threshold;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (!in.isReadable()) return;

		int outLength = Types.VAR_INT.readPrimitive(in);
		if (outLength == 0) {
			out.add(in.readBytes(in.readableBytes()));
			return;
		}

		if (outLength < this.threshold) {
			throw new DecoderException("Badly compressed packet - size of " + outLength + " is below server threshold of " + this.threshold);
		} else if (outLength > 2097152) {
			throw new DecoderException("Badly compressed packet - size of " + outLength + " is larger than protocol maximum of " + 2097152);
		}

		ByteBuf temp = in;
		if (!in.hasArray()) {
			temp = ByteBufAllocator.DEFAULT.heapBuffer().writeBytes(in);
		} else {
			in.retain();
		}
		ByteBuf output = ByteBufAllocator.DEFAULT.heapBuffer(outLength, outLength);
		try {
			this.inflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
			output.writerIndex(output.writerIndex() + this.inflater.inflate(output.array(), output.arrayOffset(), outLength));
			out.add(output.retain());
		} finally {
			output.release();
			temp.release();
			this.inflater.reset();
		}
	}
}
