/*
 * Copyright (C) 2018-2023 Velocity Contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// Based on: https://github.com/PaperMC/Velocity/blob/dev/3.0.0/native/src/main/java/com/velocitypowered/natives/compression/JavaVelocityCompressor.java
// This fallback exists so that velocity-natives is not a shaded dependency

package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.compressor;

import static com.google.common.base.Preconditions.checkArgument;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Implements deflate compression by wrapping {@link Deflater} and {@link Inflater}.
 */
public class JavaCompressor implements Compressor {
    static final int ZLIB_BUFFER_SIZE = 8192;

    private final Deflater deflater;
    private final Inflater inflater;

    public JavaCompressor() {
        this.deflater = new Deflater();
        this.inflater = new Inflater();
    }

    @Override
    public void inflate(ByteBuf source, ByteBuf destination, int uncompressedSize)
            throws DataFormatException {
        // We (probably) can't nicely deal with >=1 buffer nicely, so let's scream loudly.
        checkArgument(source.nioBufferCount() == 1, "source has multiple backing buffers");
        checkArgument(destination.nioBufferCount() == 1, "destination has multiple backing buffers");

        final int origIdx = source.readerIndex();
        inflater.setInput(source.nioBuffer());

        try {
            final int readable = source.readableBytes();
            while (!inflater.finished() && inflater.getBytesRead() < readable) {
                if (!destination.isWritable()) {
                    destination.ensureWritable(ZLIB_BUFFER_SIZE);
                }

                ByteBuffer destNioBuf = destination.nioBuffer(destination.writerIndex(),
                        destination.writableBytes());
                int produced = inflater.inflate(destNioBuf);
                destination.writerIndex(destination.writerIndex() + produced);
            }

            if (!inflater.finished()) {
                throw new DataFormatException("Received a deflate stream that was too large, wanted "
                        + uncompressedSize);
            }
            source.readerIndex(origIdx + inflater.getTotalIn());
        } finally {
            inflater.reset();
        }
    }

    @Override
    public void deflate(ByteBuf source, ByteBuf destination) throws DataFormatException {
        // We (probably) can't nicely deal with >=1 buffer nicely, so let's scream loudly.
        checkArgument(source.nioBufferCount() == 1, "source has multiple backing buffers");
        checkArgument(destination.nioBufferCount() == 1, "destination has multiple backing buffers");

        final int origIdx = source.readerIndex();
        deflater.setInput(source.nioBuffer());
        deflater.finish();

        while (!deflater.finished()) {
            if (!destination.isWritable()) {
                destination.ensureWritable(ZLIB_BUFFER_SIZE);
            }

            ByteBuffer destNioBuf = destination.nioBuffer(destination.writerIndex(),
                    destination.writableBytes());
            int produced = deflater.deflate(destNioBuf);
            destination.writerIndex(destination.writerIndex() + produced);
        }

        source.readerIndex(origIdx + deflater.getTotalIn());
        deflater.reset();
    }
}
