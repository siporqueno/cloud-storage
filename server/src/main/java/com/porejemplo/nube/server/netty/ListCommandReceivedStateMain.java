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

public class ListCommandReceivedStateMain implements State {

    private final MainHandler mH;

    public ListCommandReceivedStateMain(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(byte signalByte, Phase currentPhase, ByteBuf buf, long receivedFileLength) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) throws IOException {

        if (mH.currentPhase == Phase.FILES_LIST) {
            System.out.println("STATE: Forming and sending to client files list");
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            mH.bufOut.writeByte(Command.LSCL.getSignalByte());
            ctx.writeAndFlush(mH.bufOut);
            Path pathStorage = Paths.get("server_storage");
            String fileNamesString=Files.list(pathStorage)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining(" "));
            int listLength = fileNamesString.length();
            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(4);
            mH.bufOut.writeInt(listLength);
            ctx.writeAndFlush(mH.bufOut);
            System.out.println(listLength);

            mH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(listLength * 2);
            mH.bufOut.writeCharSequence(fileNamesString, StandardCharsets.UTF_8);
            ctx.writeAndFlush(mH.bufOut);
            mH.currentPhase = Phase.IDLE;
        }

        mH.currentState = mH.noCommandReceivedStateMain;
        return true;
    }
}
