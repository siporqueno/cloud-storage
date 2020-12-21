package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProtoHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private int nameLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path path;
    byte signalByte;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        ByteBuf bufOut = null;

        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                signalByte = buf.readByte();
                switch (signalByte) {
                    case 15:
                        currentState = State.NAME_LENGTH;
                        receivedFileLength = 0L;
                        System.out.println("STATE: Start of file upload");
                        break;
                    case 16:
                        currentState = State.NAME_LENGTH;
                        receivedFileLength = 0L;
                        System.out.println("STATE: Start of file download");
                        break;
                    default:
                        System.out.println("ERROR: Invalid first byte - " + signalByte);
                }
            }


            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Getting filename length");
                    nameLength = buf.readInt();
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                    path = Paths.get("server_storage", new String(fileName));
                    if (signalByte == 15) {
                        out = new BufferedOutputStream(new FileOutputStream(path.toFile()));
                        currentState = State.FILE_LENGTH;
                    } else if (signalByte == 16) {
                        currentState = State.VERIFY_FILE_PRESENCE;
                    }
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    System.out.println(receivedFileLength);
                    if (fileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("File received");
                        out.close();
                        break;
                    }
                }
            }

            if (currentState == State.VERIFY_FILE_PRESENCE) {
                System.out.println("STATE: File presence verification ");
                if (Files.exists(path)) {
                    bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                    bufOut.writeByte((byte) 16);
                    ctx.writeAndFlush(bufOut);
                    System.out.println("File name verified");
                    currentState = State.FILE_DESPATCH;
                } else {
                    bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                    bufOut.writeByte((byte) 17);
                    ctx.writeAndFlush(bufOut);
                    System.out.println("File name not verified. No such file");
                    currentState = State.IDLE;
                }
            }

            if (currentState == State.FILE_DESPATCH) {
                System.out.println("STATE: File download");
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(8);
                bufOut.writeLong(Files.size(path));
                ctx.writeAndFlush(bufOut);
                // Despatch of the file from server to client
                FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
                ctx.writeAndFlush(region);
                currentState = State.IDLE;
                break;
            }
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
