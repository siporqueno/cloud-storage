package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import com.porejemplo.nube.server.auth.service.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    ByteBuf buf;
    ByteBuf bufOut;
    private boolean authOk = false;
    Phase currentPhase = Phase.IDLE;
    int usernameLength;
    int passwordLength;
    private String username;
    private String password;

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

        if (authOk) {
            ctx.fireChannelRead(buf.retain());
            return;
        }

        while (buf.readableBytes() > 0) {

            if (currentPhase == Phase.IDLE) {
                Command command = Command.findCommandBySignalByte(buf.readByte());
                switch (command) {
                    case LOGIN:
                        currentPhase = Phase.USERNAME_LENGTH;
                        System.out.println("STATE: Start to check username and password.");
                        break;
                }
            }

            if (currentPhase == Phase.USERNAME_LENGTH) {
                System.out.println("inside if USERNAME_LENGTH " + buf.readableBytes());
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Getting username length");
                    usernameLength = buf.readInt();
                    currentPhase = Phase.PASSWORD_LENGTH;
                } else break;
            }

            if (currentPhase == Phase.PASSWORD_LENGTH) {
                System.out.println("inside if PASSWORD_LENGTH ");
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Getting password length");
                    passwordLength = buf.readInt();
                    currentPhase = Phase.USERNAME_AND_PASSWORD;
                } else break;
            }

            if (currentPhase == Phase.USERNAME_AND_PASSWORD) {
                if (buf.readableBytes() >= usernameLength + passwordLength) {
                    byte[] usernameBytes = new byte[usernameLength];
                    buf.readBytes(usernameBytes);
                    username = new String(usernameBytes, StandardCharsets.UTF_8);
                    System.out.println("STATE: username received - " + username);

                    byte[] passwordBytes = new byte[passwordLength];
                    buf.readBytes(passwordBytes);
                    password = new String(passwordBytes, StandardCharsets.UTF_8);
                    System.out.println("STATE: New password received - " + password);

                    currentPhase = Phase.VERIFY_USERNAME_AND_PASSWORD;
                } else break;
            }

            if (currentPhase == Phase.VERIFY_USERNAME_AND_PASSWORD) {
                System.out.println("STATE: username and password verification ");
                if (AuthService.verifyUsernameAndPassword(username, password)) {
                    bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                    bufOut.writeByte(Command.LOGIN.getSignalByte());
                    ctx.writeAndFlush(bufOut);
                    authOk = true;
                    ctx.pipeline().addLast(new MainHandler());
                    System.out.println("Correct username and password.");
                } else {
                    bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                    bufOut.writeByte(Command.LOGIN.getFailureByte());
                    ctx.writeAndFlush(bufOut);
                    System.out.println("Wrong username and/or password.");
                }
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
        super.exceptionCaught(ctx, cause);
    }
}
