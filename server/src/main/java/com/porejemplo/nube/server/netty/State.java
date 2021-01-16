package com.porejemplo.nube.server.netty;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public interface State {
    State receiveCommand();
    boolean processCommand(ChannelHandlerContext ctx) throws IOException;
}
