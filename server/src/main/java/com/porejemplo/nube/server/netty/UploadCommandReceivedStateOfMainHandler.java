package com.porejemplo.nube.server.netty;

import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class UploadCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public UploadCommandReceivedStateOfMainHandler(MainHandler mH) {
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
                mH.out = new BufferedOutputStream(new FileOutputStream(mH.path.toFile()));
                mH.currentPhase = Phase.FILE_LENGTH;
            } else return false;
        }

        if (mH.currentPhase == Phase.FILE_LENGTH) {
            if (mH.buf.readableBytes() >= 8) {
                mH.fileLength = mH.buf.readLong();
                MainHandler.LOGGER.info("STATE: File length received - " + mH.fileLength);
                mH.currentPhase = Phase.FILE;
            } else return false;
        }

        if (mH.currentPhase == Phase.FILE) {
            while (mH.buf.readableBytes() > 0) {
                mH.out.write(mH.buf.readByte());
                mH.receivedFileLength++;
                if (mH.fileLength == mH.receivedFileLength) {
                    mH.currentPhase = Phase.IDLE;
                    MainHandler.LOGGER.info("File received");
                    mH.out.close();
                    mH.currentState = mH.noCommandReceivedStateOfMainHandler;
                    return true;
                }
            }
        }
        mH.currentState = mH.uploadCommandReceivedStateOfMainHandler;
        return false;
    }
}
