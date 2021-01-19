package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public interface State {
    State receiveCommand(ByteBuf buf, ByteBuf bufOut);
    boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) throws IOException;
}
