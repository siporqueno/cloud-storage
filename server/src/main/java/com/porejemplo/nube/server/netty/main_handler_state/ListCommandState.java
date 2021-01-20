package com.porejemplo.nube.server.netty.main_handler_state;

import com.porejemplo.nube.common.Command;
import com.porejemplo.nube.server.netty.MainHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class ListCommandState implements State {

    private final MainHandler mH;

    public ListCommandState(MainHandler mH) {
        this.mH = mH;
    }

    @Override
    public State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) throws IOException {

        if (mH.getCurrentPhase() == Phase.FILES_LIST) {
            MainHandler.getLOGGER().info("STATE: Forming and sending to client files list");
            bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
            bufOut.writeByte(Command.LSCL.getSignalByte());
            ctx.writeAndFlush(bufOut);
            String fileNamesString = Files.list(mH.getAH().getPathToUserDir())
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.joining(" "));
            int listLength = fileNamesString.length();
            bufOut = ByteBufAllocator.DEFAULT.directBuffer(4);
            bufOut.writeInt(listLength);
            ctx.writeAndFlush(bufOut);

            bufOut = ByteBufAllocator.DEFAULT.directBuffer(listLength * 2);
            bufOut.writeCharSequence(fileNamesString, StandardCharsets.UTF_8);
            ctx.writeAndFlush(bufOut);
            mH.setCurrentPhase(Phase.IDLE);
        }

        mH.setCurrentState(mH.getNoCommandReceivedStateOfMainHandler());
        return true;
    }
}
