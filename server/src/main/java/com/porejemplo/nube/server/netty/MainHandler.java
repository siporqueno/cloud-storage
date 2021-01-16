package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class MainHandler extends ChannelInboundHandlerAdapter {

    static final Logger LOGGER = LoggerFactory.getLogger(MainHandler.class);

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
    int nameLength, newNameLength;
    long fileLength;
    String fileName, newFileName;
    long receivedFileLength;
    BufferedOutputStream out;
    Path path, newPath;
    byte signalByte;
    ByteBuf buf, bufOut;

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
        LOGGER.info("MainHandler added.");
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
        if (bufOut.refCnt() > 0) bufOut.release();
        bufOut = null;
        LOGGER.info("MainHandler removed.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
