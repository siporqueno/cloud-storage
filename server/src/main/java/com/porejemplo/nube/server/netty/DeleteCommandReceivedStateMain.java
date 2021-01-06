package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeleteCommandReceivedStateMain implements State {

    private final MainHandler mH;

    public DeleteCommandReceivedStateMain(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (mH.currentPhase == Phase.NAME_LENGTH) {
            System.out.println("inside if NAME_LENGTH ");
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
                mH.currentPhase = Phase.VERIFY_FILE_PRESENCE;
            } else return false;
        }

        if (mH.currentPhase == Phase.VERIFY_FILE_PRESENCE) {
            System.out.println("STATE: File presence verification ");
            if (Files.exists(mH.path)) {
                System.out.println("File name verified");
                mH.currentPhase = Phase.DELETE_FILE;
            } else {
                mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                mH.bufOut.writeByte((byte) 17);
                ctx.writeAndFlush(mH.bufOut);
                System.out.println("File name not verified. No such file");
                mH.currentPhase = Phase.IDLE;
                mH.currentState = mH.noCommandReceivedStateMain;
                return false;
            }
        }

        if (mH.currentPhase == Phase.DELETE_FILE) {
            System.out.println("STATE: File deleting");
            Files.delete(mH.path);
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            mH.bufOut.writeByte(Command.DELCL.getSignalByte());
            ctx.writeAndFlush(mH.bufOut);
            mH.currentPhase = Phase.IDLE;
        }

        mH.currentState = mH.noCommandReceivedStateMain;
        return true;
    }
}
