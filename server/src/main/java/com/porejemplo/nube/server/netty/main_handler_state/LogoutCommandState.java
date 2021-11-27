package com.porejemplo.nube.server.netty.main_handler_state;

import com.porejemplo.nube.common.Signal;
import com.porejemplo.nube.server.netty.MainHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class LogoutCommandState implements State {

    private final MainHandler mH;

    public LogoutCommandState(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) throws IOException {

        if (mH.getCurrentPhase() == Phase.LOGOUT) {
            mH.getAH().setCurrentState(mH.getAH().getUnauthNoCommandReceivedStateOfAuthHandler());
            bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            bufOut.writeByte(Signal.LOGOUT.getSignalByte());
            ctx.writeAndFlush(bufOut.retain());
            MainHandler.getLOGGER().info("STATE: Logging out");
            ctx.pipeline().remove(mH);
        }

        mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
        return true;
    }
}
