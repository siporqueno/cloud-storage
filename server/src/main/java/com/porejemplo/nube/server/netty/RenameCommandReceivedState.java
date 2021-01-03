package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RenameCommandReceivedState implements State {

    private final MainHandler pH;

    public RenameCommandReceivedState(MainHandler pH) {
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
                pH.currentPhase = Phase.NEW_NAME_LENGTH;
            } else return false;
        }

        if (pH.currentPhase == Phase.NEW_NAME_LENGTH) {
            System.out.println("inside if NEW_NAME_LENGTH ");
            if (pH.buf.readableBytes() >= 4) {
                System.out.println("STATE: Getting new filename length");
                pH.newNameLength = pH.buf.readInt();
                pH.currentPhase = Phase.NAME_AND_NEW_NAME;
            } else return false;
        }

        if (pH.currentPhase == Phase.NAME_AND_NEW_NAME) {
            if (pH.buf.readableBytes() >= pH.nameLength + pH.newNameLength) {
                byte[] fileName = new byte[pH.nameLength];
                pH.buf.readBytes(fileName);
                System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                pH.path = Paths.get("server_storage", new String(fileName));

                byte[] newFileName = new byte[pH.newNameLength];
                pH.buf.readBytes(newFileName);
                System.out.println("STATE: New filename received - " + new String(newFileName, "UTF-8"));
                pH.newPath = Paths.get("server_storage", new String(newFileName));

                pH.currentPhase = Phase.VERIFY_FILE_PRESENCE;
            } else return false;
        }

        if (pH.currentPhase == Phase.VERIFY_FILE_PRESENCE) {
            System.out.println("STATE: File presence verification ");
            if (Files.exists(pH.path)) {
                System.out.println("File name verified");
                pH.currentPhase = Phase.RENAME_FILE;
            } else {
                pH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                pH.bufOut.writeByte((byte) 17);
                ctx.writeAndFlush(pH.bufOut);
                System.out.println("File name not verified. No such file");
                pH.currentPhase = Phase.IDLE;
                pH.currentState = pH.noCommandReceivedState;
                return false;
            }
        }

        if (pH.currentPhase == Phase.RENAME_FILE) {
            System.out.println("STATE: File renaming");
            Files.move(pH.path, pH.newPath);
            pH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            pH.bufOut.writeByte(Command.RMCL.getSignalByte());
            ctx.writeAndFlush(pH.bufOut);
            pH.currentPhase = Phase.IDLE;
        }

        pH.currentState = pH.noCommandReceivedState;
        return true;
    }
}
