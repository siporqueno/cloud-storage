package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ListCommandReceivedState implements State {

    private final MainHandler pH;

    public ListCommandReceivedState(MainHandler pH) {
        this.pH = pH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (pH.currentPhase == Phase.FILES_LIST) {
            System.out.println("STATE: Forming and sending to client files list");
            pH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            pH.bufOut.writeByte(Command.LSCL.getSignalByte());
            ctx.writeAndFlush(pH.bufOut);
            Path pathStrorage = Paths.get("server_storage");
            String fileNamesString=Files.list(pathStrorage)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining(" "));
            int listLength = fileNamesString.length();
            pH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(4);
            pH.bufOut.writeInt(listLength);
            ctx.writeAndFlush(pH.bufOut);
            System.out.println(listLength);

            pH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(listLength * 2);
            pH.bufOut.writeCharSequence(fileNamesString, StandardCharsets.UTF_8);
            ctx.writeAndFlush(pH.bufOut);
            pH.currentPhase = Phase.IDLE;
        }

        pH.currentState = pH.noCommandReceivedState;
        return true;
    }
}
