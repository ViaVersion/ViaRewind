package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.compression;

import com.viaversion.viarewind.netty.EmptyChannelHandler;
import com.viaversion.viarewind.netty.ForwardMessageToByteEncoder;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.CompressionHandlerProvider;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class TrackingCompressionHandlerProvider extends CompressionHandlerProvider {
	public final static String COMPRESS_HANDLER_NAME = "compress";
	public final static String DECOMPRESS_HANDLER_NAME = "decompress";

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
