package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class MainHandler extends ChannelInboundHandlerAdapter {

    AuthHandler aH;

    State noCommandReceivedStateOfMainHandler;
    State listCommandReceivedStateOfMainHandler;
    State uploadCommandReceivedStateOfMainHandler;
    State downloadCommandReceivedStateOfMainHandler;
    State renameCommandReceivedStateOfMainHandler;
    State deleteCommandReceivedStateOfMainHandler;
    State logoutCommandReceivedStateOfMainHandler;
    State currentState;

    Phase currentPhase = Phase.IDLE;
    int nameLength;
    int newNameLength;
    long fileLength;
    String fileName;
    long receivedFileLength;
    BufferedOutputStream out;
    Path path;
    Path newPath;
    byte signalByte;
    ByteBuf buf;
    ByteBuf bufOut;

    public MainHandler(AuthHandler authHandler) {
        this.aH = authHandler;
        this.noCommandReceivedStateOfMainHandler = new NoCommandReceivedStateOfMainHandler(this);
        this.listCommandReceivedStateOfMainHandler = new ListCommandReceivedStateOfMainHandler(this);
        this.uploadCommandReceivedStateOfMainHandler = new UploadCommandReceivedStateOfMainHandler(this);
        this.downloadCommandReceivedStateOfMainHandler = new DownloadCommandReceivedStateOfMainHandler(this);
        this.renameCommandReceivedStateOfMainHandler = new RenameCommandReceivedStateOfMainHandler(this);
        this.deleteCommandReceivedStateOfMainHandler = new DeleteCommandReceivedStateOfMainHandler(this);
        this.logoutCommandReceivedStateOfMainHandler = new LogoutCommandReceivedStateOfMainHandler(this);
        this.currentState = noCommandReceivedStateOfMainHandler;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("MainHandler added.");
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
        buf.release();
        buf = null;
        bufOut.release();
        bufOut = null;
        System.out.println("MainHandler removed.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
