package com.porejemplo.nube.server.netty.auth_handler_state;

import com.porejemplo.nube.server.netty.AuthHandler;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class AuthState implements State {

    private final AuthHandler aH;

    public AuthState(AuthHandler aH) {
        this.aH = aH;
    }

    @Override
    public State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) {
        ctx.fireChannelRead(buf.retain());
        return true;
    }
}
