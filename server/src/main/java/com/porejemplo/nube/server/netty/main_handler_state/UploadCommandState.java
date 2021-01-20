package com.porejemplo.nube.server.netty.main_handler_state;

import com.porejemplo.nube.server.netty.MainHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadCommandState implements State {

    private int nameLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    private final MainHandler mH;

    public UploadCommandState(MainHandler mH) {
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
                receivedFileLength = 0L;
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
                Path path = Paths.get(mH.getAH().getPathToUserDir().toString(), fileName);
                out = new BufferedOutputStream(new FileOutputStream(path.toFile()));
                mH.setCurrentPhase(Phase.FILE_LENGTH);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                MainHandler.getLOGGER().info("STATE: File length received - " + fileLength);
                mH.setCurrentPhase(Phase.FILE);
            } else return false;
        }

        if (mH.getCurrentPhase() == Phase.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    mH.setCurrentPhase(Phase.IDLE);
                    MainHandler.getLOGGER().info("File received");
                    out.close();
                    mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
                    return true;
                }
            }
        }

        return false;
    }
}
