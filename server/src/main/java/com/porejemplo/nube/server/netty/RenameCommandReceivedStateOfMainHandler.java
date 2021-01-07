package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RenameCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public RenameCommandReceivedStateOfMainHandler(MainHandler mH) {
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
                mH.currentPhase = Phase.NEW_NAME_LENGTH;
            } else return false;
        }

        if (mH.currentPhase == Phase.NEW_NAME_LENGTH) {
            System.out.println("inside if NEW_NAME_LENGTH ");
            if (mH.buf.readableBytes() >= 4) {
                System.out.println("STATE: Getting new filename length");
                mH.newNameLength = mH.buf.readInt();
                mH.currentPhase = Phase.NAME_AND_NEW_NAME;
            } else return false;
        }

        if (mH.currentPhase == Phase.NAME_AND_NEW_NAME) {
            if (mH.buf.readableBytes() >= mH.nameLength + mH.newNameLength) {
                byte[] fileName = new byte[mH.nameLength];
                mH.buf.readBytes(fileName);
                System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                mH.path = Paths.get("server_storage", new String(fileName));

                byte[] newFileName = new byte[mH.newNameLength];
                mH.buf.readBytes(newFileName);
                System.out.println("STATE: New filename received - " + new String(newFileName, "UTF-8"));
                mH.newPath = Paths.get("server_storage", new String(newFileName));

                mH.currentPhase = Phase.VERIFY_FILE_PRESENCE;
            } else return false;
        }

        if (mH.currentPhase == Phase.VERIFY_FILE_PRESENCE) {
            System.out.println("STATE: File presence verification ");
            if (Files.exists(mH.path)) {
                System.out.println("File name verified");
                mH.currentPhase = Phase.RENAME_FILE;
            } else {
                mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                mH.bufOut.writeByte((byte) 17);
                ctx.writeAndFlush(mH.bufOut);
                System.out.println("File name not verified. No such file");
                mH.currentPhase = Phase.IDLE;
                mH.currentState = mH.noCommandReceivedStateOfMainHandler;
                return false;
            }
        }

        if (mH.currentPhase == Phase.RENAME_FILE) {
            System.out.println("STATE: File renaming");
            Files.move(mH.path, mH.newPath);
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            mH.bufOut.writeByte(Command.RMCL.getSignalByte());
            ctx.writeAndFlush(mH.bufOut);
            mH.currentPhase = Phase.IDLE;
        }

        mH.currentState = mH.noCommandReceivedStateOfMainHandler;
        return true;
    }
}
