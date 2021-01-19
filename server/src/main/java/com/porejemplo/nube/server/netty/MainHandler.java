package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.server.netty.main_handler_state.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainHandler.class);

    private AuthHandler aH;

    private final State noCommandReceivedStateOfMainHandler;
    private final State listCommandReceivedStateOfMainHandler;
    private final State uploadCommandReceivedStateOfMainHandler;
    private final State downloadCommandReceivedStateOfMainHandler;
    private final State renameCommandReceivedStateOfMainHandler;
    private final State deleteCommandReceivedStateOfMainHandler;
    private final State logoutCommandReceivedStateOfMainHandler;
    private State currentState;

    private Phase currentPhase = Phase.IDLE;
    private ByteBuf buf, bufOut;

    public MainHandler(AuthHandler authHandler) {
        this.aH = authHandler;
        this.noCommandReceivedStateOfMainHandler = new NoCommandState(this);
        this.listCommandReceivedStateOfMainHandler = new ListCommandState(this);
        this.uploadCommandReceivedStateOfMainHandler = new UploadCommandState(this);
        this.downloadCommandReceivedStateOfMainHandler = new DownloadCommandState(this);
        this.renameCommandReceivedStateOfMainHandler = new RenameCommandState(this);
        this.deleteCommandReceivedStateOfMainHandler = new DeleteCommandState(this);
        this.logoutCommandReceivedStateOfMainHandler = new LogoutCommandState(this);
        this.currentState = noCommandReceivedStateOfMainHandler;
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public AuthHandler getAH() {
        return aH;
    }

    public State getNoCommandReceivedStateOfMainHandler() {
        return noCommandReceivedStateOfMainHandler;
    }

    public State getListCommandReceivedStateOfMainHandler() {
        return listCommandReceivedStateOfMainHandler;
    }

    public State getUploadCommandReceivedStateOfMainHandler() {
        return uploadCommandReceivedStateOfMainHandler;
    }

    public State getDownloadCommandReceivedStateOfMainHandler() {
        return downloadCommandReceivedStateOfMainHandler;
    }

    public State getRenameCommandReceivedStateOfMainHandler() {
        return renameCommandReceivedStateOfMainHandler;
    }

    public State getDeleteCommandReceivedStateOfMainHandler() {
        return deleteCommandReceivedStateOfMainHandler;
    }

    public State getLogoutCommandReceivedStateOfMainHandler() {
        return logoutCommandReceivedStateOfMainHandler;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Phase currentPhase) {
        this.currentPhase = currentPhase;
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

        currentState = receiveCommand(buf, bufOut);
        processCommand(ctx, buf, bufOut);
    }

    private State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        return currentState.receiveCommand(buf, bufOut);
    }

    private boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) throws IOException {
        return currentState.processCommand(ctx, buf, bufOut);
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
