package com.porejemplo.nube.server.netty.main_handler_state;

import com.porejemplo.nube.common.Signal;
import com.porejemplo.nube.server.netty.MainHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DeleteCommandState implements State {

    private int nameLength;
    private Path path;

    private final MainHandler mH;

    public DeleteCommandState(MainHandler mH) {
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
                String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                MainHandler.getLOGGER().info("STATE: Filename received - " + fileName);
                path = Paths.get(mH.getAH().getPathToUserDir().toString(), fileName);
                mH.setCurrentPhase(Phase.VERIFY_FILE_PRESENCE);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.VERIFY_FILE_PRESENCE) {
            MainHandler.getLOGGER().info("STATE: File presence verification ");
            if (Files.exists(path)) {
                MainHandler.getLOGGER().info("File name verified");
                mH.setCurrentPhase(Phase.DELETE_FILE);
            } else {
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte(Signal.DELCL.getFailureByte());
                ctx.writeAndFlush(bufOut);
                MainHandler.getLOGGER().info("File name not verified. No such file");
                mH.setCurrentPhase(Phase.IDLE);
                mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
                return false;
            }
        }

        if (mH.getCurrentPhase() == Phase.DELETE_FILE) {
            MainHandler.getLOGGER().info("STATE: File deleting");
            Files.delete(path);
            bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            bufOut.writeByte(Signal.DELCL.getSignalByte());
            ctx.writeAndFlush(bufOut);
            mH.setCurrentPhase(Phase.IDLE);
        }

        mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
        return true;
    }
}
