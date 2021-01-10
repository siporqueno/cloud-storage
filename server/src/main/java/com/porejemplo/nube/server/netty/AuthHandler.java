package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.file.Path;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    final String STORAGE_ROOT = "server_storage";

    State unauthNoCommandReceivedStateOfAuthHandler;
    State unauthLoginCommandReceivedStateOfAuthHandler;
    State authStateOfAuthHandler;
    State currentState;

    ByteBuf buf;
    ByteBuf bufOut;
    Phase currentPhase = Phase.IDLE;
    byte signalByte;
    int usernameLength;
    int passwordLength;
    String username;
    String password;
    Path pathToUserDir;

    public AuthHandler() {
        this.unauthNoCommandReceivedStateOfAuthHandler = new UnauthNoCommandReceivedStateOfAuthHandler(this);
        this.unauthLoginCommandReceivedStateOfAuthHandler = new UnauthLoginCommandReceivedStateOfAuthHandler(this);
        this.authStateOfAuthHandler = new AuthStateOfAuthHandler(this);
        this.currentState = unauthNoCommandReceivedStateOfAuthHandler;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("AuthHandler added.");
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m);
        m.release();

        currentState = receiveCommand();
        processCommand(ctx);

    }

    private State receiveCommand() {
        return currentState.receiveCommand();
    }

    private boolean processCommand(ChannelHandlerContext ctx) throws IOException {
        return currentState.processCommand(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("AuthHandler removed.");
        buf.release();
        buf = null;
        if (bufOut.refCnt() > 0) {
            bufOut.release();
        }
        bufOut = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
