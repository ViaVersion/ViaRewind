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

import com.viaversion.viarewind.api.minecraft.netty.EmptyChannelHandler;
import com.viaversion.viarewind.api.minecraft.netty.ForwardMessageToByteEncoder;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.CompressionHandlerProvider;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class TrackingCompressionHandlerProvider extends CompressionHandlerProvider {
	public static final String COMPRESS_HANDLER_NAME = "compress";
	public static final String DECOMPRESS_HANDLER_NAME = "decompress";

	@Override
	public void onHandleLoginCompressionPacket(UserConnection user, int threshold) {
		final ChannelPipeline pipeline = user.getChannel().pipeline();
		if (user.isClientSide()) {
			pipeline.addBefore(Via.getManager().getInjector().getEncoderName(), COMPRESS_HANDLER_NAME, getEncoder(threshold));
			pipeline.addBefore(Via.getManager().getInjector().getDecoderName(), DECOMPRESS_HANDLER_NAME, getDecoder(threshold));
		} else {
			setCompressionEnabled(user, true); // We need to remove compression for 1.7 clients
		}
	}

	@Override
	public void onTransformPacket(UserConnection user) {
		if (isCompressionEnabled(user)) {
			final ChannelPipeline pipeline = user.getChannel().pipeline();

			String compressor = null;
			String decompressor = null;
			if (pipeline.get(COMPRESS_HANDLER_NAME) != null) { // ViaVersion
				compressor = COMPRESS_HANDLER_NAME;
				decompressor = DECOMPRESS_HANDLER_NAME;
			} else if (pipeline.get("compression-encoder") != null) { // Velocity
				compressor = "compression-encoder";
				decompressor = "compression-decoder";
			}

			if (compressor != null) { // We can neutralize the effect of compressor to the client
				pipeline.replace(decompressor, decompressor, new EmptyChannelHandler());
				pipeline.replace(compressor, compressor, new ForwardMessageToByteEncoder());
			} else {
				throw new IllegalStateException("Couldn't remove compression for 1.7!");
			}

			setCompressionEnabled(user, false);
		}
	}

	@Override
	public ChannelHandler getEncoder(int threshold) {
		return new CompressionEncoder(threshold);
	}

	@Override
	public ChannelHandler getDecoder(int threshold) {
		return new CompressionDecoder(threshold);
	}
}
