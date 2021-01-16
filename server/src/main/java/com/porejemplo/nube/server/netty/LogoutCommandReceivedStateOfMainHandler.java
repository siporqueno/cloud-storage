package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class LogoutCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public LogoutCommandReceivedStateOfMainHandler(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand() {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (mH.currentPhase == Phase.LOGOUT) {
            mH.aH.currentState = mH.aH.unauthNoCommandReceivedStateOfAuthHandler;
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            mH.bufOut.writeByte(Command.LOGOUT.getSignalByte());
            ctx.writeAndFlush(mH.bufOut.retain());
            MainHandler.LOGGER.info("STATE: Logging out");
            ctx.pipeline().remove(mH);
        }

        mH.currentState = mH.noCommandReceivedStateOfMainHandler;
        return true;
    }
}
