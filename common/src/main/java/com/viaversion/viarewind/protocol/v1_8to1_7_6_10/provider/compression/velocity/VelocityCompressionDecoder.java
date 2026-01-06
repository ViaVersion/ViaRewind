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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.velocity;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.CompressionDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.zip.DataFormatException;

public final class VelocityCompressionDecoder extends CompressionDecoder {

    private final VelocityCompressor compressor;

    public VelocityCompressionDecoder(final int threshold, final VelocityCompressor compressor) {
        super(threshold);
        this.compressor = compressor;
    }

    @Override
    protected void inflate(final ChannelHandlerContext ctx, final ByteBuf source, final int outLength, final List<Object> out) throws DataFormatException {
        ByteBuf output = ctx.alloc().buffer(outLength);
        try {
            this.compressor.inflate(source, output, outLength);
            out.add(output.retain());
        } finally {
            output.release();
        }
    }

}
