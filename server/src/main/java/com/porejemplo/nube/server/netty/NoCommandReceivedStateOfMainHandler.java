package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class NoCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public NoCommandReceivedStateOfMainHandler(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        if (mH.currentPhase == Phase.IDLE) {
            mH.signalByte = buf.readByte();
            Command command = null;
            try {
                command = Command.findCommandBySignalByte(mH.signalByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (command) {
                case LSCL:
                    mH.currentPhase = Phase.FILES_LIST;
                    System.out.println("STATE: Start to obtain file list in server");
                    return mH.listCommandReceivedStateOfMainHandler;
                case UPLD:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    mH.receivedFileLength = 0L;
                    System.out.println("STATE: Start of file upload");
                    return mH.uploadCommandReceivedStateOfMainHandler;
                case DNLD:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file download");
                    return mH.downloadCommandReceivedStateOfMainHandler;
                case RMCL:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file renaming");
                    return mH.renameCommandReceivedStateOfMainHandler;
                case DELCL:
                    mH.currentPhase = Phase.NAME_LENGTH;
                    System.out.println("STATE: Start of file deleting");
                    return mH.deleteCommandReceivedStateOfMainHandler;
                case LOGOUT:
                    mH.currentPhase = Phase.LOGOUT;
                    System.out.println("STATE: Start of logging out");
                    return mH.logoutCommandReceivedStateOfMainHandler;
                default:
                    System.out.println("ERROR: Invalid first byte - " + mH.signalByte);
                    return mH.noCommandReceivedStateOfMainHandler;
            }
        }
        return mH.currentState;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        System.out.println("No command received. Nothing to process.");
        return false;
    }
}
