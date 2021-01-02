package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class NoCommandReceivedState implements State {

    private final ProtoHandler pH;

    public NoCommandReceivedState(ProtoHandler pH) {
        this.pH = pH;
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
                case LSCL:
                    pH.currentPhase = Phase.FILES_LIST;
                    System.out.println("STATE: Start to obtain file list in server");
                    return pH.listCommandReceivedState;
                case UPLD:
                    pH.currentPhase = Phase.NAME_LENGTH;
                    pH.receivedFileLength = 0L;
                    System.out.println("STATE: Start of file upload");
                    return pH.uploadCommandReceivedState;
                case DNLD:
                    pH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file download");
                    return pH.downloadCommandReceivedState;
                case RMCL:
                    pH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file renaming");
                    return pH.renameCommandReceivedState;
                case DELCL:
                    pH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file deleting");
                    return pH.deleteCommandReceivedState;
                default:
                    System.out.println("ERROR: Invalid first byte - " + signalByte);
                    return pH.noCommandReceivedState;
            }
        }
        return pH.currentState;
//        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        System.out.println("No command received. Nothing to process.");
        return false;
    }
}
