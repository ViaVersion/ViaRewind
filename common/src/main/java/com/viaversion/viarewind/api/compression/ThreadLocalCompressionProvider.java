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
package com.viaversion.viarewind.api.compression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * A ThreadLocal-based compression provider that either uses Velocity's native compression
 * or falls back to Java's built-in compression (Deflater/Inflater).
 */
public final class ThreadLocalCompressionProvider {

    private static final boolean VELOCITY_NATIVES_AVAILABLE;

    static {
        boolean velocityAvailable = false;
        try {
            Class.forName("com.velocitypowered.natives.compression.VelocityCompressor");
            velocityAvailable = true;
        } catch (final ClassNotFoundException ignored) {
        }
        VELOCITY_NATIVES_AVAILABLE = velocityAvailable;
    }

    // ThreadLocal for Java's Deflater (used when Velocity natives are not available)
    private static final ThreadLocal<Deflater> JAVA_DEFLATER = ThreadLocal.withInitial(Deflater::new);

    // ThreadLocal for Java's Inflater (used when Velocity natives are not available)
    private static final ThreadLocal<Inflater> JAVA_INFLATER = ThreadLocal.withInitial(Inflater::new);

    private ThreadLocalCompressionProvider() {
    }

    /**
     * Compresses data from the source buffer into the destination buffer.
     *
     * @param source      the source buffer containing uncompressed data
     * @param destination the destination buffer to write compressed data to
     * @throws DataFormatException if compression fails
     */
    public static void deflate(final ByteBuf source, final ByteBuf destination) throws DataFormatException {
        if (VELOCITY_NATIVES_AVAILABLE) {
            deflateVelocity(source, destination);
        } else {
            deflateJava(source, destination);
        }
    }


    /**
     * Decompresses data from the source buffer into the destination buffer.
     *
     * @param source        the source buffer containing compressed data
     * @param destination   the destination buffer to write decompressed data to
     * @param expectedSize  the expected size of the decompressed data
     * @throws DataFormatException if decompression fails
     */
    public static void inflate(final ByteBuf source, final ByteBuf destination, final int expectedSize) throws DataFormatException {
        if (VELOCITY_NATIVES_AVAILABLE) {
            inflateVelocity(source, destination, expectedSize);
        } else {
            inflateJava(source, destination, expectedSize);
        }
    }

    // Java native implementation

    private static void deflateJava(final ByteBuf source, final ByteBuf destination) throws DataFormatException {
        ByteBuf temp = source;
        if (!source.hasArray()) {
            temp = ByteBufAllocator.DEFAULT.heapBuffer().writeBytes(source);
        } else {
            source.retain();
        }
        ByteBuf output = ByteBufAllocator.DEFAULT.heapBuffer();
        try {
            final Deflater deflater = JAVA_DEFLATER.get();
            deflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
            deflater.finish();

            while (!deflater.finished()) {
                output.ensureWritable(4096);
                output.writerIndex(output.writerIndex() + deflater.deflate(output.array(), output.arrayOffset() + output.writerIndex(), output.writableBytes()));
            }
            destination.writeBytes(output);
        } finally {
            output.release();
            temp.release();
            JAVA_DEFLATER.get().reset();
        }
    }


    private static void inflateJava(final ByteBuf source, final ByteBuf destination, final int expectedSize) throws DataFormatException {
        ByteBuf temp = source;
        if (!source.hasArray()) {
            temp = ByteBufAllocator.DEFAULT.heapBuffer().writeBytes(source);
        } else {
            source.retain();
        }
        ByteBuf output = ByteBufAllocator.DEFAULT.heapBuffer(expectedSize, expectedSize);
        try {
            final Inflater inflater = JAVA_INFLATER.get();
            inflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
            output.writerIndex(output.writerIndex() + inflater.inflate(output.array(), output.arrayOffset(), expectedSize));
            destination.writeBytes(output);
        } finally {
            output.release();
            temp.release();
            JAVA_INFLATER.get().reset();
        }
    }

    // Velocity native implementation
    private static void deflateVelocity(final ByteBuf source, final ByteBuf destination) throws DataFormatException {
        VelocityHolder.deflate(source, destination);
    }

    private static void inflateVelocity(final ByteBuf source, final ByteBuf destination, final int expectedSize) throws DataFormatException {
        VelocityHolder.inflate(source, destination, expectedSize);
    }

    private static final class VelocityHolder {

        private static final ThreadLocal<com.velocitypowered.natives.compression.VelocityCompressor> COMPRESSOR =
                ThreadLocal.withInitial(() -> com.velocitypowered.natives.util.Natives.compress.get().create(-1));

        static void deflate(final ByteBuf source, final ByteBuf destination) throws DataFormatException {
            COMPRESSOR.get().deflate(source, destination);
        }

        static void inflate(final ByteBuf source, final ByteBuf destination, final int expectedSize) throws DataFormatException {
            COMPRESSOR.get().inflate(source, destination, expectedSize);
        }
    }
}

