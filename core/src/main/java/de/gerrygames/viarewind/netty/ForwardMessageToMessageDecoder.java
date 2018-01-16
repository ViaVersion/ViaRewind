package de.gerrygames.viarewind.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ForwardMessageToMessageDecoder extends MessageToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
		out.add(msg);
	}
}
