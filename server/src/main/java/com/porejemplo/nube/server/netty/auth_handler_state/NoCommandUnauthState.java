package com.porejemplo.nube.server.netty.auth_handler_state;

import com.porejemplo.nube.common.Signal;
import com.porejemplo.nube.server.netty.AuthHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class NoCommandUnauthState implements State {

    private final AuthHandler aH;

    public NoCommandUnauthState(AuthHandler aH) {
        this.aH = aH;
    }

    @Override
    public State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        if (aH.getCurrentPhase() == Phase.IDLE) {
            byte signalByte = buf.readByte();
            Signal signal = null;
            try {
                signal = Signal.findSignalBySignalByte(signalByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (signal) {
                case LOGIN:
                    aH.setCurrentPhase(Phase.USERNAME_LENGTH);
                    AuthHandler.getLOGGER().info("STATE: Start to check username and password.");
                    return aH.getUnauthLoginCommandReceivedStateOfAuthHandler();
                default:
                    AuthHandler.getLOGGER().info("ERROR: Invalid first byte - " + signalByte);
                    return aH.getUnauthNoCommandReceivedStateOfAuthHandler();
            }
        }
        return aH.getCurrentState();
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) {
        AuthHandler.getLOGGER().info("No command received. Nothing to process.");
        return false;
    }
}
