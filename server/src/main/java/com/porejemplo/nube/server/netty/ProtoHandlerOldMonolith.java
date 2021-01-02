package com.porejemplo.nube.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProtoHandlerOldMonolith extends ChannelInboundHandlerAdapter {

    private Phase currentPhase = Phase.IDLE;
    private int nameLength;
    private int newNameLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path path;
    private Path newPath;
    byte signalByte;
    private ByteBuf buf;
    private ByteBuf bufOut;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m);
        m.release();

        while (buf.readableBytes() > 0) {
            if (currentPhase == Phase.IDLE) {
                signalByte = buf.readByte();
                switch (signalByte) {
                    case 14:
                        currentPhase = Phase.FILES_LIST;
                        System.out.println("STATE: Start to obtain file list in server");
                        break;
                    case 15:
                        currentPhase = Phase.NAME_LENGTH;
                        receivedFileLength = 0L;
                        System.out.println("STATE: Start of file upload");
                        break;
                    case 16:
                        currentPhase = Phase.NAME_LENGTH;
                        receivedFileLength = 0L;
                        System.out.println("STATE: Start of file download");
                        break;
                    case 18:
//                        currentState = State.NAME_AND_NEW_NAME_LENGTH;
                        currentPhase = Phase.NAME_LENGTH;
                        System.out.println("STATE: Start of file renaming");
                        break;
                    case 19:
                        currentPhase = Phase.NAME_LENGTH;
                        System.out.println("STATE: Start of file deleting");
                        break;
                    default:
                        System.out.println("ERROR: Invalid first byte - " + signalByte);
                }
            }


            if (currentPhase == Phase.NAME_LENGTH) {
                System.out.println("inside if NAME_LENGTH " + buf.readableBytes());
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Getting filename length");
                    nameLength = buf.readInt();
                    if (signalByte == 18) currentPhase = Phase.NEW_NAME_LENGTH;
                    else currentPhase = Phase.NAME;
                } else break;
            }

            if (currentPhase == Phase.NEW_NAME_LENGTH) {
                System.out.println("inside if NEW_NAME_LENGTH " + buf.readableBytes());
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Getting new filename length");
                    newNameLength = buf.readInt();
                    currentPhase = Phase.NAME_AND_NEW_NAME;
                } else break;
            }

            /*if (currentState == State.NAME_AND_NEW_NAME_LENGTH) {
                System.out.println(buf.readableBytes());
                if (buf.readableBytes() >= 8) {
                    System.out.println("STATE: Getting filename and new filename lengths");
                    nameLength = buf.readInt();
                    newNameLength = buf.readInt();
                    currentState = State.NAME_AND_NEW_NAME;
                }
            }*/

            if (currentPhase == Phase.NAME) {
                if (buf.readableBytes() >= nameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                    path = Paths.get("server_storage", new String(fileName));
                    if (signalByte == 15) {
                        out = new BufferedOutputStream(new FileOutputStream(path.toFile()));
                        currentPhase = Phase.FILE_LENGTH;
                    } else if (signalByte == 16 || signalByte == 19) {
                        currentPhase = Phase.VERIFY_FILE_PRESENCE;
                    }
                } else break;
            }

            if (currentPhase == Phase.NAME_AND_NEW_NAME) {
                if (buf.readableBytes() >= nameLength + newNameLength) {
                    byte[] fileName = new byte[nameLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                    path = Paths.get("server_storage", new String(fileName));

                    byte[] newFileName = new byte[newNameLength];
                    buf.readBytes(newFileName);
                    System.out.println("STATE: New filename received - " + new String(newFileName, "UTF-8"));
                    newPath = Paths.get("server_storage", new String(newFileName));

                    currentPhase = Phase.VERIFY_FILE_PRESENCE;
                } else break;
            }

            if (currentPhase == Phase.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentPhase = Phase.FILE;
                } else break;
            }

            if (currentPhase == Phase.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    System.out.println(receivedFileLength);
                    if (fileLength == receivedFileLength) {
                        currentPhase = Phase.IDLE;
                        System.out.println("File received");
                        out.close();
                        break;
                    }
                }
            }

            if (currentPhase == Phase.VERIFY_FILE_PRESENCE) {
                System.out.println("STATE: File presence verification ");
                if (Files.exists(path)) {
                    bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                    if (signalByte == 16) {
                        System.out.println("File name verified");
                        bufOut.writeByte((byte) 16);
                        ctx.writeAndFlush(bufOut);
                        currentPhase = Phase.FILE_DESPATCH;
                    } else if (signalByte == 18) {
                        System.out.println("File name verified");
                        currentPhase = Phase.RENAME_FILE;
                    } else if (signalByte == 19) {
                        System.out.println("File name verified");
                        currentPhase = Phase.DELETE_FILE;
                    }
                } else {
                    bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                    bufOut.writeByte((byte) 17);
                    ctx.writeAndFlush(bufOut);
                    System.out.println("File name not verified. No such file");
                    currentPhase = Phase.IDLE;
                }
            }

            if (currentPhase == Phase.FILE_DESPATCH) {
                System.out.println("STATE: File download"+Files.size(path));
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(8);
                bufOut.writeLong(Files.size(path));
                ctx.writeAndFlush(bufOut);
                // Despatch of the file from server to client
                FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
                ctx.writeAndFlush(region);
                currentPhase = Phase.IDLE;
                break;
            }

            if (currentPhase == Phase.FILES_LIST) {
                System.out.println("STATE: Forming and sending to client files list");
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte((byte) 14);
                ctx.writeAndFlush(bufOut);
                Path pathStrorage = Paths.get("server_storage");
                StringBuilder stringHelper = new StringBuilder();
                Files.list(pathStrorage).forEach((p) -> stringHelper.append(p.getFileName().toString()).append(" "));
                int listLength = stringHelper.length();

                bufOut = ByteBufAllocator.DEFAULT.directBuffer(4);
                bufOut.writeInt(listLength);
                ctx.writeAndFlush(bufOut);
                System.out.println(listLength);

                bufOut = ByteBufAllocator.DEFAULT.directBuffer(stringHelper.length() * 2);
                bufOut.writeCharSequence(stringHelper, StandardCharsets.UTF_8);
                ctx.writeAndFlush(bufOut);
                currentPhase = Phase.IDLE;
                break;
            }

            if (currentPhase == Phase.RENAME_FILE) {
                System.out.println("STATE: File renaming");
                Files.move(path, newPath);
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte((byte) 18);
                ctx.writeAndFlush(bufOut);
                currentPhase = Phase.IDLE;
                break;
            }

            if (currentPhase == Phase.DELETE_FILE) {
                System.out.println("STATE: File deleting");
                Files.delete(path);
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte((byte) 19);
                ctx.writeAndFlush(bufOut);
                currentPhase = Phase.IDLE;
                break;
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        buf.release();
        buf = null;
        bufOut.release();
        bufOut = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}