package com.porejemplo.nube.server.netty.main_handler_state;

import com.porejemplo.nube.common.Signal;
import com.porejemplo.nube.server.netty.MainHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class NoCommandState implements State {

    private final MainHandler mH;

    public NoCommandState(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        if (mH.getCurrentPhase() == Phase.IDLE) {
            byte signalByte = buf.readByte();
            Signal signal = null;
            try {
                signal = Signal.findSignalBySignalByte(signalByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (signal) {
                case LSCL:
                    mH.setCurrentPhase(Phase.FILES_LIST);
                    MainHandler.getLOGGER().info("STATE: Start to obtain file list in server");
                    return mH.getListCommandReceivedStateOfMainHandler();
                case UPLD:
                    mH.setCurrentPhase(Phase.NAME_LENGTH);
                    MainHandler.getLOGGER().info("STATE: Start of file upload");
                    return mH.getUploadCommandReceivedStateOfMainHandler();
                case DNLD:
                    mH.setCurrentPhase(Phase.NAME_LENGTH);
                    MainHandler.getLOGGER().info("STATE: Start of file download");
                    return mH.getDownloadCommandReceivedStateOfMainHandler();
                case RMCL:
                    mH.setCurrentPhase(Phase.NAME_LENGTH);
                    MainHandler.getLOGGER().info("STATE: Start of file renaming");
                    return mH.getRenameCommandReceivedStateOfMainHandler();
                case DELCL:
                    mH.setCurrentPhase(Phase.NAME_LENGTH);
                    MainHandler.getLOGGER().info("STATE: Start of file deleting");
                    return mH.getDeleteCommandReceivedStateOfMainHandler();
                case LOGOUT:
                    mH.setCurrentPhase(Phase.LOGOUT);
                    MainHandler.getLOGGER().info("STATE: Start of logging out");
                    return mH.getLogoutCommandReceivedStateOfMainHandler();
                default:
                    MainHandler.getLOGGER().info("ERROR: Invalid first byte - " + signalByte);
                    return mH.getNoCommandReceivedStateOfMainHandler();
            }
        }
        return mH.getCurrentState();
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) {
        MainHandler.getLOGGER().info("No command received. Nothing to process.");
        return false;
    }
}
