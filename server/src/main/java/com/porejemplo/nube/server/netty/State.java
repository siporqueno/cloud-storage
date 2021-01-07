package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public interface State {
//    State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength);
    State receiveCommand();
    boolean processCommand(ChannelHandlerContext ctx) throws IOException;
}
