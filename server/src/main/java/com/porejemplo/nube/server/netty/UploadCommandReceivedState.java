package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class UploadCommandReceivedState implements State {

    private final MainHandler pH;

    public UploadCommandReceivedState(MainHandler pH) {
        this.pH = pH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (pH.currentPhase == Phase.NAME_LENGTH) {
            System.out.println("inside if NAME_LENGTH " + pH.buf.readableBytes());
            if (pH.buf.readableBytes() >= 4) {
                System.out.println("STATE: Getting filename length");
                pH.nameLength = pH.buf.readInt();
                pH.currentPhase = Phase.NAME;
            } else return false;
        }

        if (pH.currentPhase == Phase.NAME) {
            if (pH.buf.readableBytes() >= pH.nameLength) {
                byte[] fileName = new byte[pH.nameLength];
                pH.buf.readBytes(fileName);
                System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                pH.path = Paths.get("server_storage", new String(fileName));
                pH.out = new BufferedOutputStream(new FileOutputStream(pH.path.toFile()));
                pH.currentPhase = Phase.FILE_LENGTH;
            } else return false;
        }

        if (pH.currentPhase == Phase.FILE_LENGTH) {
            if (pH.buf.readableBytes() >= 8) {
                pH.fileLength = pH.buf.readLong();
                System.out.println("STATE: File length received - " + pH.fileLength);
                pH.currentPhase = Phase.FILE;
            } else return false;
        }

        if (pH.currentPhase == Phase.FILE) {
            while (pH.buf.readableBytes() > 0) {
                pH.out.write(pH.buf.readByte());
                pH.receivedFileLength++;
                System.out.println(pH.receivedFileLength);
                if (pH.fileLength == pH.receivedFileLength) {
                    pH.currentPhase = Phase.IDLE;
                    System.out.println("File received");
                    pH.out.close();
                    break;
                }
            }
        }
        pH.currentState = pH.noCommandReceivedState;
        return true;
    }
}
