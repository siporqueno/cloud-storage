package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LogoutCommandReceivedStateMain implements State {

    private final MainHandler mH;

    public LogoutCommandReceivedStateMain(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (mH.currentPhase == Phase.LOGOUT) {
            System.out.println("inside if LOGOUT");
            // The below commented line is compatible with AuthHandlerOldMonolith class where there are no states and field authOk is present.
//            mH.aH.authOk = false;
            mH.aH.currentState = mH.aH.unauthNoCommandReceivedStateOfAuthHandler;
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            mH.bufOut.writeByte(Command.LOGOUT.getSignalByte());
            ctx.writeAndFlush(mH.bufOut.retain());
            System.out.println("STATE: Logging out");
            ctx.pipeline().remove(mH);
        }

        mH.currentState = mH.noCommandReceivedStateMain;
        return true;
    }
}
