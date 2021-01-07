package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public DownloadCommandReceivedStateOfMainHandler(MainHandler mH) {
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
                mH.currentPhase = Phase.VERIFY_FILE_PRESENCE;
            } else return false;
        }

        if (mH.currentPhase == Phase.VERIFY_FILE_PRESENCE) {
            System.out.println("STATE: File presence verification ");
            if (Files.exists(mH.path)) {
                mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                System.out.println("File name verified");
                mH.bufOut.writeByte(Command.DNLD.getSignalByte());
                ctx.writeAndFlush(mH.bufOut);
                mH.currentPhase = Phase.FILE_DESPATCH;
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

        if (mH.currentPhase == Phase.FILE_DESPATCH) {
            System.out.println("STATE: File download");
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(8);
            mH.bufOut.writeLong(Files.size(mH.path));
            ctx.writeAndFlush(mH.bufOut);
            // Despatch of the file from server to client
            FileRegion region = new DefaultFileRegion(mH.path.toFile(), 0, Files.size(mH.path));
            ctx.writeAndFlush(region);
            mH.currentPhase = Phase.IDLE;
        }

        mH.currentState = mH.noCommandReceivedStateOfMainHandler;
        return true;
    }
}
