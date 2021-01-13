package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.channel.ChannelHandlerContext;

public class UnauthNoCommandReceivedStateOfAuthHandler implements State {

    private final AuthHandler aH;

    public UnauthNoCommandReceivedStateOfAuthHandler(AuthHandler aH) {
        this.aH = aH;
    }

    @Override
    public State receiveCommand() {
        if (aH.currentPhase == Phase.IDLE) {
            aH.signalByte = aH.buf.readByte();
            Command command = null;
            try {
                command = Command.findCommandBySignalByte(aH.signalByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (command) {
                case LOGIN:
                    aH.currentPhase = Phase.USERNAME_LENGTH;
                    AuthHandler.LOGGER.info("STATE: Start to check username and password.");
                    return aH.unauthLoginCommandReceivedStateOfAuthHandler;
                default:
                    AuthHandler.LOGGER.info("ERROR: Invalid first byte - " + aH.signalByte);
                    return aH.unauthNoCommandReceivedStateOfAuthHandler;
            }
        }
        return aH.currentState;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        AuthHandler.LOGGER.info("No command received. Nothing to process.");
        return false;
    }
}
