/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
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

import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.compressor.CompressorUtil;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

public class CompressionDecoder extends MessageToMessageDecoder<ByteBuf> {
    private int threshold;

    public CompressionDecoder(final int threshold) {
        this.threshold = threshold;
    }

    public void setThreshold(final int threshold) {
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

        ByteBuf output = ctx.alloc().buffer(outLength);
        try {
            CompressorUtil.getCompressor().inflate(in, output, outLength);
            out.add(output.retain());
        } finally {
            output.release();
        }
    }
}
