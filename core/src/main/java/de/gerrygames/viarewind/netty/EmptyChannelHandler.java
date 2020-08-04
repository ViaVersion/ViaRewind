package de.gerrygames.viarewind.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

public class EmptyChannelHandler implements ChannelHandler {
	@Override
	public void handlerAdded(ChannelHandlerContext channelHandlerContext) {
		// Do nothing
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext channelHandlerContext) {
		// Do nothing
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
		channelHandlerContext.fireExceptionCaught(throwable);
	}
}
