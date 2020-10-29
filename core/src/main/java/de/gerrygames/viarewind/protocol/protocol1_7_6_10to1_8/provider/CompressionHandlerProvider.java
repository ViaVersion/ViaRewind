package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider;

import de.gerrygames.viarewind.netty.EmptyChannelHandler;
import de.gerrygames.viarewind.netty.ForwardMessageToByteEncoder;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.CompressionSendStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.api.type.Type;

import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionHandlerProvider implements Provider {
	public void handleSetCompression(UserConnection user, int threshold) {
		ChannelPipeline pipeline = user.getChannel().pipeline();
		if (!Via.getPlatform().getConnectionManager().isFrontEnd(user)) {
			// This looks like client-side
			pipeline.addBefore(Via.getManager().getInjector().getEncoderName(), "viarewind-compressor", getEncoder(threshold));
			pipeline.addBefore(Via.getManager().getInjector().getDecoderName(), "viarewind-decompressor", getDecoder(threshold));
		} else {
			// Server-side
			CompressionSendStorage storage = user.get(CompressionSendStorage.class);
			storage.setNeutralizeCompression(true);
		}
	}

	public void handlePostCompression(UserConnection user) {
		CompressionSendStorage storage = user.get(CompressionSendStorage.class);
		if (storage.isNeutralizeCompression()) {
			ChannelPipeline pipeline = user.getChannel().pipeline();

			String compressor = null;
			String decompressor = null;
			if (pipeline.get("compress") != null) {
				compressor = "compress";
				decompressor = "decompress";
			} else if (pipeline.get("compression-encoder") != null) { // Velocity
				compressor = "compression-encoder";
				decompressor = "compression-decoder";
			}
			if (compressor != null) { // We can neutralize the effect of compressor to the client
				System.out.println("stop decompressing");
				pipeline.replace(decompressor, decompressor, new EmptyChannelHandler());
				pipeline.replace(compressor, compressor, new ForwardMessageToByteEncoder());
			} else {
				throw new IllegalStateException("Couldn't neutralize compression for 1.7!");
			}

			storage.setNeutralizeCompression(false);
		}
	}

	protected ChannelHandler getEncoder(int threshold) {
		return new GenericCompressor(threshold);
	}

	protected ChannelHandler getDecoder(int threshold) {
		return new GenericDecompressor(threshold);
	}

	private static class GenericDecompressor extends MessageToMessageDecoder<ByteBuf> {
		// https://github.com/Gerrygames/ClientViaVersion/blob/master/src/main/java/de/gerrygames/the5zig/clientviaversion/netty/CompressionEncoder.java
		private final Inflater inflater;
		private final int threshold;

		public GenericDecompressor(int var1) {
			this.threshold = var1;
			this.inflater = new Inflater();
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			if (in.readableBytes() != 0) {
				int outLength = Type.VAR_INT.readPrimitive(in);
				if (outLength == 0) {
					out.add(in.readBytes(in.readableBytes()));
				} else {
					if (outLength < this.threshold) {
						throw new DecoderException("Badly compressed packet - size of " + outLength + " is below server threshold of " + this.threshold);
					}

					if (outLength > 2097152) {
						throw new DecoderException("Badly compressed packet - size of " + outLength + " is larger than protocol maximum of " + 2097152);
					}

					byte[] temp = new byte[in.readableBytes()];
					in.readBytes(temp);
					this.inflater.setInput(temp);
					byte[] output = new byte[outLength];
					this.inflater.inflate(output);
					out.add(Unpooled.wrappedBuffer(output));
					this.inflater.reset();
				}
			}
		}
	}

	private static class GenericCompressor extends MessageToByteEncoder<ByteBuf> {
		// https://github.com/Gerrygames/ClientViaVersion/blob/master/src/main/java/de/gerrygames/the5zig/clientviaversion/netty/CompressionEncoder.java
		private final byte[] buffer = new byte[8192];
		private final Deflater deflater;
		private final int threshold;

		public GenericCompressor(int var1) {
			this.threshold = var1;
			this.deflater = new Deflater();
		}

		protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
			int frameLength = in.readableBytes();
			if (frameLength < this.threshold) {
				Type.VAR_INT.writePrimitive(out, 0);
				out.writeBytes(in);
			} else {
				Type.VAR_INT.writePrimitive(out, frameLength);

				byte[] inBytes = new byte[frameLength];
				in.readBytes(inBytes);
				this.deflater.setInput(inBytes, 0, frameLength);
				this.deflater.finish();

				while (!this.deflater.finished()) {
					int written = this.deflater.deflate(this.buffer);
					out.writeBytes(this.buffer, 0, written);
				}

				this.deflater.reset();
			}
		}
	}
}
