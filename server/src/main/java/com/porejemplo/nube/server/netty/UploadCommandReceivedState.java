package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class UploadCommandReceivedState implements State {

    private final MainHandler mH;

    public UploadCommandReceivedState(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (mH.currentPhase == Phase.NAME_LENGTH) {
            System.out.println("inside if NAME_LENGTH " + mH.buf.readableBytes());
            if (mH.buf.readableBytes() >= 4) {
                System.out.println("STATE: Getting filename length");
                mH.nameLength = mH.buf.readInt();
                mH.currentPhase = Phase.NAME;
            } else return false;
        }

        if (mH.currentPhase == Phase.NAME) {
            if (mH.buf.readableBytes() >= mH.nameLength) {
                byte[] fileName = new byte[mH.nameLength];
                mH.buf.readBytes(fileName);
                System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                mH.path = Paths.get("server_storage", new String(fileName));
                mH.out = new BufferedOutputStream(new FileOutputStream(mH.path.toFile()));
                mH.currentPhase = Phase.FILE_LENGTH;
            } else return false;
        }

        if (mH.currentPhase == Phase.FILE_LENGTH) {
            if (mH.buf.readableBytes() >= 8) {
                mH.fileLength = mH.buf.readLong();
                System.out.println("STATE: File length received - " + mH.fileLength);
                mH.currentPhase = Phase.FILE;
            } else return false;
        }

        if (mH.currentPhase == Phase.FILE) {
            while (mH.buf.readableBytes() > 0) {
                mH.out.write(mH.buf.readByte());
                mH.receivedFileLength++;
                System.out.println(mH.receivedFileLength);
                if (mH.fileLength == mH.receivedFileLength) {
                    mH.currentPhase = Phase.IDLE;
                    System.out.println("File received");
                    mH.out.close();
                    break;
                }
            }
        }
        mH.currentState = mH.noCommandReceivedState;
        return true;
    }
}
