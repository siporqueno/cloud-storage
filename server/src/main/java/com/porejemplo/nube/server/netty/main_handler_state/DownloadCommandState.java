package com.porejemplo.nube.server.netty.main_handler_state;

import com.porejemplo.nube.common.Command;
import com.porejemplo.nube.server.netty.MainHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadCommandState implements State {

    private int nameLength, newNameLength;
    private long fileLength;
    private String fileName, newFileName;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path path, newPath;
    private byte signalByte;

    private final MainHandler mH;

    public DownloadCommandState(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) throws IOException {

        if (mH.getCurrentPhase() == Phase.NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                MainHandler.getLOGGER().info("STATE: Getting filename length");
                nameLength = buf.readInt();
                mH.setCurrentPhase(Phase.NAME);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.NAME) {
            if (buf.readableBytes() >= nameLength) {
                byte[] fileNameBytes = new byte[nameLength];
                buf.readBytes(fileNameBytes);
                fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                MainHandler.getLOGGER().info("STATE: Filename received - " + fileName);
                path = Paths.get(mH.getAH().getPathToUserDir().toString(), fileName);
                mH.setCurrentPhase(Phase.VERIFY_FILE_PRESENCE);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.VERIFY_FILE_PRESENCE) {
            MainHandler.getLOGGER().info("STATE: File presence verification ");
            if (Files.exists(path)) {
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                MainHandler.getLOGGER().info("File name verified");
                bufOut.writeByte(Command.DNLD.getSignalByte());
                ctx.writeAndFlush(bufOut);
                mH.setCurrentPhase(Phase.FILE_DESPATCH);
            } else {
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte(Command.DNLD.getFailureByte());
                ctx.writeAndFlush(bufOut);
                MainHandler.getLOGGER().info("File name not verified. No such file");
                mH.setCurrentPhase(Phase.IDLE);
                mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
                return false;
            }
        }

        if (mH.getCurrentPhase() == Phase.FILE_DESPATCH) {
            MainHandler.getLOGGER().info("STATE: File download");
            bufOut = ByteBufAllocator.DEFAULT.directBuffer(8);
            bufOut.writeLong(Files.size(path));
            ctx.writeAndFlush(bufOut);
            FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
            ChannelFuture channelFuture = ctx.writeAndFlush(region);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        MainHandler.getLOGGER().info(String.format("File %s has been successfully sent from server to client.\n", fileName));
                    } else future.cause().printStackTrace();
                }
            });
            mH.setCurrentPhase(Phase.IDLE);
        }

        mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
        return true;
    }
}
