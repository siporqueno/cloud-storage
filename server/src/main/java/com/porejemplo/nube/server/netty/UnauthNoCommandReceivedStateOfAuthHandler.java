package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class UnauthNoCommandReceivedStateOfAuthHandler implements State {

    private final AuthHandler aH;

    public UnauthNoCommandReceivedStateOfAuthHandler(AuthHandler aH) {
        this.aH = aH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        if (currentPhase == Phase.IDLE) {
            signalByte = buf.readByte();
            Command command = null;
            try {
                command = Command.findCommandBySignalByte(signalByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (command) {
                case LOGIN:
                    aH.currentPhase = Phase.USERNAME_LENGTH;
                    System.out.println("STATE: Start to check username and password.");
                    return aH.unauthLoginCommandReceivedStateOfAuthHandler;
                default:
                    System.out.println("ERROR: Invalid first byte - " + signalByte);
                    return aH.unauthNoCommandReceivedStateOfAuthHandler;
            }
        }
        return aH.currentState;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        System.out.println("No command received. Nothing to process.");
        return false;
    }
}
