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

public class RenameCommandState implements State {

    private int nameLength, newNameLength;
    private Path path, newPath;

    private final MainHandler mH;

    public RenameCommandState(MainHandler mH) {
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
                mH.setCurrentPhase(Phase.NEW_NAME_LENGTH);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.NEW_NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                MainHandler.getLOGGER().info("STATE: Getting new filename length");
                newNameLength = buf.readInt();
                mH.setCurrentPhase(Phase.NAME_AND_NEW_NAME);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.NAME_AND_NEW_NAME) {
            if (buf.readableBytes() >= nameLength + newNameLength) {
                byte[] fileNameBytes = new byte[nameLength];
                buf.readBytes(fileNameBytes);
                String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                MainHandler.getLOGGER().info("STATE: Filename received - " + fileName);
                path = Paths.get(mH.getAH().getPathToUserDir().toString(), fileName);

                byte[] newFileNameBytes = new byte[newNameLength];
                buf.readBytes(newFileNameBytes);
                String newFileName = new String(newFileNameBytes, StandardCharsets.UTF_8);
                MainHandler.getLOGGER().info("STATE: New filename received - " + newFileName);
                newPath = Paths.get(mH.getAH().getPathToUserDir().toString(), newFileName);

                mH.setCurrentPhase(Phase.VERIFY_FILE_PRESENCE);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.VERIFY_FILE_PRESENCE) {
            MainHandler.getLOGGER().info("STATE: File presence verification ");
            if (Files.exists(path)) {
                MainHandler.getLOGGER().info("File name verified");
                mH.setCurrentPhase(Phase.RENAME_FILE);
            } else {
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte(Signal.RMCL.getFailureByte());
                ctx.writeAndFlush(bufOut);
                MainHandler.getLOGGER().info("File name not verified. No such file");
                mH.setCurrentPhase(Phase.IDLE);
                mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
                return false;
            }
        }

        if (mH.getCurrentPhase() == Phase.RENAME_FILE) {
            MainHandler.getLOGGER().info("STATE: File renaming");
            Files.move(path, newPath);
            bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            bufOut.writeByte(Signal.RMCL.getSignalByte());
            ctx.writeAndFlush(bufOut);
            mH.setCurrentPhase(Phase.IDLE);
        }

        mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
        return true;
    }
}
