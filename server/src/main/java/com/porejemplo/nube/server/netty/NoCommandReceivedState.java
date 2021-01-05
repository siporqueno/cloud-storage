package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class NoCommandReceivedState implements State {

    private final MainHandler mH;

    public NoCommandReceivedState(MainHandler mH) {
        this.mH = mH;
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
                    mH.currentPhase = Phase.FILES_LIST;
                    System.out.println("STATE: Start to obtain file list in server");
                    return mH.listCommandReceivedState;
                case UPLD:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    mH.receivedFileLength = 0L;
                    System.out.println("STATE: Start of file upload");
                    return mH.uploadCommandReceivedState;
                case DNLD:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file download");
                    return mH.downloadCommandReceivedState;
                case RMCL:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file renaming");
                    return mH.renameCommandReceivedState;
                case DELCL:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file deleting");
                    return mH.deleteCommandReceivedState;
                default:
                    System.out.println("ERROR: Invalid first byte - " + signalByte);
                    return mH.noCommandReceivedState;
            }
        }
        return mH.currentState;
//        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        System.out.println("No command received. Nothing to process.");
        return false;
    }
}
