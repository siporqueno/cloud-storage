package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeleteCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public DeleteCommandReceivedStateOfMainHandler(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand() {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (mH.currentPhase == Phase.NAME_LENGTH) {
            if (mH.buf.readableBytes() >= 4) {
                MainHandler.LOGGER.info("STATE: Getting filename length");
                mH.nameLength = mH.buf.readInt();
                mH.currentPhase = Phase.NAME;
            } else return false;
        }

        if (mH.currentPhase == Phase.NAME) {
            if (mH.buf.readableBytes() >= mH.nameLength) {
                byte[] fileNameBytes = new byte[mH.nameLength];
                mH.buf.readBytes(fileNameBytes);
                mH.fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                MainHandler.LOGGER.info("STATE: Filename received - " + mH.fileName);
                mH.path = Paths.get(mH.aH.pathToUserDir.toString(), mH.fileName);
                mH.currentPhase = Phase.VERIFY_FILE_PRESENCE;
            } else return false;
        }

        if (mH.currentPhase == Phase.VERIFY_FILE_PRESENCE) {
            MainHandler.LOGGER.info("STATE: File presence verification ");
            if (Files.exists(mH.path)) {
                MainHandler.LOGGER.info("File name verified");
                mH.currentPhase = Phase.DELETE_FILE;
            } else {
                mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                mH.bufOut.writeByte(Command.DELCL.getFailureByte());
                ctx.writeAndFlush(mH.bufOut);
                MainHandler.LOGGER.info("File name not verified. No such file");
                mH.currentPhase = Phase.IDLE;
                mH.currentState = mH.noCommandReceivedStateOfMainHandler;
                return false;
            }
        }

        if (mH.currentPhase == Phase.DELETE_FILE) {
            MainHandler.LOGGER.info("STATE: File deleting");
            Files.delete(mH.path);
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            mH.bufOut.writeByte(Command.DELCL.getSignalByte());
            ctx.writeAndFlush(mH.bufOut);
            mH.currentPhase = Phase.IDLE;
        }

        mH.currentState = mH.noCommandReceivedStateOfMainHandler;
        return true;
    }
}
