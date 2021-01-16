package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RenameCommandReceivedStateOfMainHandler implements State {

    private final MainHandler mH;

    public RenameCommandReceivedStateOfMainHandler(MainHandler mH) {
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
                mH.currentPhase = Phase.NEW_NAME_LENGTH;
            } else return false;
        }

        if (mH.currentPhase == Phase.NEW_NAME_LENGTH) {
            if (mH.buf.readableBytes() >= 4) {
                MainHandler.LOGGER.info("STATE: Getting new filename length");
                mH.newNameLength = mH.buf.readInt();
                mH.currentPhase = Phase.NAME_AND_NEW_NAME;
            } else return false;
        }

        if (mH.currentPhase == Phase.NAME_AND_NEW_NAME) {
            if (mH.buf.readableBytes() >= mH.nameLength + mH.newNameLength) {
                byte[] fileNameBytes = new byte[mH.nameLength];
                mH.buf.readBytes(fileNameBytes);
                mH.fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                MainHandler.LOGGER.info("STATE: Filename received - " + mH.fileName);
                mH.path = Paths.get(mH.aH.pathToUserDir.toString(), mH.fileName);

                byte[] newFileNameBytes = new byte[mH.newNameLength];
                mH.buf.readBytes(newFileNameBytes);
                mH.newFileName = new String(newFileNameBytes, StandardCharsets.UTF_8);
                MainHandler.LOGGER.info("STATE: New filename received - " + mH.newFileName);
                mH.newPath = Paths.get(mH.aH.pathToUserDir.toString(), mH.newFileName);

                mH.currentPhase = Phase.VERIFY_FILE_PRESENCE;
            } else return false;
        }

        if (mH.currentPhase == Phase.VERIFY_FILE_PRESENCE) {
            MainHandler.LOGGER.info("STATE: File presence verification ");
            if (Files.exists(mH.path)) {
                MainHandler.LOGGER.info("File name verified");
                mH.currentPhase = Phase.RENAME_FILE;
            } else {
                mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                mH.bufOut.writeByte(Command.RMCL.getFailureByte());
                ctx.writeAndFlush(mH.bufOut);
                MainHandler.LOGGER.info("File name not verified. No such file");
                mH.currentPhase = Phase.IDLE;
                mH.currentState = mH.noCommandReceivedStateOfMainHandler;
                return false;
            }
        }

        if (mH.currentPhase == Phase.RENAME_FILE) {
            MainHandler.LOGGER.info("STATE: File renaming");
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
