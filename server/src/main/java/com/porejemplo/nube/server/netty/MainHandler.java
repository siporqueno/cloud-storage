package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class MainHandler extends ChannelInboundHandlerAdapter {

    State noCommandReceivedState;
    State listCommandReceivedState;
    State uploadCommandReceivedState;
    State downloadCommandReceivedState;
    State renameCommandReceivedState;
    State deleteCommandReceivedState;
    State currentState;

    Phase currentPhase = Phase.IDLE;
    int nameLength;
    int newNameLength;
    long fileLength;
    long receivedFileLength;
    BufferedOutputStream out;
    Path path;
    Path newPath;
    byte signalByte;
    ByteBuf buf;
    ByteBuf bufOut;

    public MainHandler() {
        this.noCommandReceivedState = new NoCommandReceivedState(this);
        this.listCommandReceivedState = new ListCommandReceivedState(this);
        this.uploadCommandReceivedState = new UploadCommandReceivedState(this);
        this.downloadCommandReceivedState = new DownloadCommandReceivedState(this);
        this.renameCommandReceivedState = new RenameCommandReceivedState(this);
        this.deleteCommandReceivedState = new DeleteCommandReceivedState(this);
        this.currentState = noCommandReceivedState;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("MainHandler.channelRead is called " + buf.capacity() + " " + buf.readerIndex() + " " + buf.writerIndex());
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m);
        m.release();
        System.out.println("ReadableBytes: " + buf.readableBytes());

        currentState = receiveCommand();
        processCommand(ctx);
    }

    private State receiveCommand() {
        return currentState.receiveCommand(signalByte, currentPhase, buf, receivedFileLength);
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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
