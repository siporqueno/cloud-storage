package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public DownloadCommandReceivedStateOfMainHandler(MainHandler mH) {
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
                mH.path = Paths.get(mH.aH.pathToUserDir.toString(),
                        mH.fileName);
                mH.currentPhase = Phase.VERIFY_FILE_PRESENCE;
            } else return false;
        }

        if (mH.currentPhase == Phase.VERIFY_FILE_PRESENCE) {
            MainHandler.LOGGER.info("STATE: File presence verification ");
            if (Files.exists(mH.path)) {
                mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                MainHandler.LOGGER.info("File name verified");
                mH.bufOut.writeByte(Command.DNLD.getSignalByte());
                ctx.writeAndFlush(mH.bufOut);
                mH.currentPhase = Phase.FILE_DESPATCH;
            } else {
                mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                mH.bufOut.writeByte(Command.DNLD.getFailureByte());
                ctx.writeAndFlush(mH.bufOut);
                MainHandler.LOGGER.info("File name not verified. No such file");
                mH.currentPhase = Phase.IDLE;
                mH.currentState = mH.noCommandReceivedStateOfMainHandler;
                return false;
            }
        }

        if (mH.currentPhase == Phase.FILE_DESPATCH) {
            MainHandler.LOGGER.info("STATE: File download");
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(8);
            mH.bufOut.writeLong(Files.size(mH.path));
            ctx.writeAndFlush(mH.bufOut);
            FileRegion region = new DefaultFileRegion(mH.path.toFile(), 0, Files.size(mH.path));
            ChannelFuture channelFuture = ctx.writeAndFlush(region);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        MainHandler.LOGGER.info(String.format("File %s has been successfully sent from server to client.\n", mH.fileName));
                    } else future.cause().printStackTrace();
                }
            });
            mH.currentPhase = Phase.IDLE;
        }

        mH.currentState = mH.noCommandReceivedStateOfMainHandler;
        return true;
    }
}
