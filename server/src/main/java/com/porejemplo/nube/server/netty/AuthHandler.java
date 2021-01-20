package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.server.auth.service.AuthService;
import com.porejemplo.nube.server.netty.auth_handler_state.AuthState;
import com.porejemplo.nube.server.netty.auth_handler_state.LoginCommandUnauthState;
import com.porejemplo.nube.server.netty.auth_handler_state.NoCommandUnauthState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthHandler.class);

    private final String STORAGE_ROOT = "server_storage";

    private final State unauthNoCommandReceivedStateOfAuthHandler;
    private final State unauthLoginCommandReceivedStateOfAuthHandler;
    private final State authStateOfAuthHandler;
    private State currentState;

    private ByteBuf buf, bufOut;
    private Phase currentPhase = Phase.IDLE;
    private Path pathToUserDir;

    public AuthHandler(AuthService authService) {
        this.unauthNoCommandReceivedStateOfAuthHandler = new NoCommandUnauthState(this);
        this.unauthLoginCommandReceivedStateOfAuthHandler = new LoginCommandUnauthState(this, authService);
        this.authStateOfAuthHandler = new AuthState(this);
        this.currentState = unauthNoCommandReceivedStateOfAuthHandler;
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public String getSTORAGE_ROOT() {
        return STORAGE_ROOT;
    }

    public State getUnauthNoCommandReceivedStateOfAuthHandler() {
        return unauthNoCommandReceivedStateOfAuthHandler;
    }

    public State getUnauthLoginCommandReceivedStateOfAuthHandler() {
        return unauthLoginCommandReceivedStateOfAuthHandler;
    }

    public State getAuthStateOfAuthHandler() {
        return authStateOfAuthHandler;
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

    public Path getPathToUserDir() {
        return pathToUserDir;
    }

    public void setPathToUserDir(Path pathToUserDir) {
        this.pathToUserDir = pathToUserDir;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("AuthHandler added.");
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
        LOGGER.info("AuthHandler removed.");
        buf.release();
        buf = null;
        if (bufOut.refCnt() > 0) bufOut.release();
        bufOut = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
