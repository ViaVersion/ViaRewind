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

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.minecraft.netty.EmptyChannelHandler;
import com.viaversion.viarewind.api.minecraft.netty.ForwardMessageToByteEncoder;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.CompressionHandlerProvider;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.velocity.VelocityCompressionDecoder;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.velocity.VelocityCompressionEncoder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class TrackingCompressionHandlerProvider extends CompressionHandlerProvider {

    // Use Velocity's compression backend if ViaRewind is loaded on a Velocity server or a custom server software
    // is shipping them in the classpath.
    private static boolean velocityNatives;

    static {
        try {
            Class.forName("com.velocitypowered.natives.compression.VelocityCompressor");
            velocityNatives = true;
        } catch (final ClassNotFoundException ignored) {
            velocityNatives = false;
        }
    }

    @Override
    public void setCompressionThreshold(UserConnection user, int threshold) {
        final ChannelPipeline pipeline = user.getChannel().pipeline();
        if (!user.isClientSide() || threshold < 0) {
            setRemoveCompression(user, true); // We need to remove compression for 1.7 clients
            return;
        }

        Object compressor = null;
        if (velocityNatives) {
            compressor = com.velocitypowered.natives.util.Natives.compress.get().create(-1);
        }

        final String compressHandlerName = ViaRewind.getPlatform().compressHandlerName();
        final CompressionEncoder encoder = (CompressionEncoder) pipeline.get(compressHandlerName);
        if (encoder != null) {
            encoder.setThreshold(threshold);
        } else {
            pipeline.addBefore(Via.getManager().getInjector().getEncoderName(), compressHandlerName, getEncoder(compressor, threshold));
        }

        final String decompressHandlerName = ViaRewind.getPlatform().decompressHandlerName();
        final CompressionDecoder decoder = (CompressionDecoder) pipeline.get(decompressHandlerName);
        if (decoder != null) {
            decoder.setThreshold(threshold);
        } else {
            pipeline.addBefore(Via.getManager().getInjector().getDecoderName(), decompressHandlerName, getDecoder(compressor, threshold));
        }
    }

    @Override
    public void onTransformPacket(UserConnection user) {
        if (isRemoveCompression(user)) {
            final ChannelPipeline pipeline = user.getChannel().pipeline();

            String compressor = null;
            String decompressor = null;
            if (pipeline.get(ViaRewind.getPlatform().compressHandlerName()) != null) {
                compressor = ViaRewind.getPlatform().compressHandlerName();
                decompressor = ViaRewind.getPlatform().decompressHandlerName();
            }

            if (compressor != null) { // We can neutralize the effect of compressor to the client
                pipeline.replace(decompressor, decompressor, new EmptyChannelHandler());
                pipeline.replace(compressor, compressor, new ForwardMessageToByteEncoder());
            } else {
                throw new IllegalStateException("Couldn't remove compression for 1.7!");
            }

            setRemoveCompression(user, false);
        }
    }

    @Override
    public ChannelHandler getEncoder(final Object compressor, int threshold) {
        if (velocityNatives) {
            return new VelocityCompressionEncoder(threshold, (com.velocitypowered.natives.compression.VelocityCompressor) compressor);
        } else {
            return new CompressionEncoder(threshold);
        }
    }

    @Override
    public ChannelHandler getDecoder(final Object compressor, int threshold) {
        if (velocityNatives) {
            return new VelocityCompressionDecoder(threshold, (com.velocitypowered.natives.compression.VelocityCompressor) compressor);
        } else {
            return new CompressionDecoder(threshold);
        }
    }

}
