package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class NoCommandReceivedStateMain implements State {

    private final MainHandler mH;

    public NoCommandReceivedStateMain(MainHandler mH) {
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
                    return mH.listCommandReceivedStateMain;
                case UPLD:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    mH.receivedFileLength = 0L;
                    System.out.println("STATE: Start of file upload");
                    return mH.uploadCommandReceivedStateMain;
                case DNLD:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file download");
                    return mH.downloadCommandReceivedStateMain;
                case RMCL:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file renaming");
                    return mH.renameCommandReceivedStateMain;
                case DELCL:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file deleting");
                    return mH.deleteCommandReceivedStateMain;
                case LOGOUT:
                    mH.currentPhase = Phase.LOGOUT;
                    System.out.println("STATE: Start of logging out");
                    return mH.logoutCommandReceivedStateMain;
                default:
                    System.out.println("ERROR: Invalid first byte - " + signalByte);
                    return mH.noCommandReceivedStateMain;
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
