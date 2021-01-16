package com.porejemplo.nube.server.netty;

import io.netty.channel.ChannelHandlerContext;

public class AuthStateOfAuthHandler implements State {

    private final AuthHandler aH;

    public AuthStateOfAuthHandler(AuthHandler aH) {
        this.aH = aH;
    }

    @Override
    public State receiveCommand() {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        ctx.fireChannelRead(aH.buf.retain());
        return true;
    }
}
