package de.gerrygames.viarewind.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ForwardMessageToByteEncoder extends MessageToByteEncoder<ByteBuf> {
	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		out.writeBytes(msg);
	}
}
