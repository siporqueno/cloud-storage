package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import com.porejemplo.nube.server.auth.service.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class AuthStateOfAuthHandler implements State {

    private final AuthHandler aH;

    public AuthStateOfAuthHandler(AuthHandler aH) {
        this.aH = aH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        ctx.fireChannelRead(aH.buf.retain());
        return true;
    }
}
