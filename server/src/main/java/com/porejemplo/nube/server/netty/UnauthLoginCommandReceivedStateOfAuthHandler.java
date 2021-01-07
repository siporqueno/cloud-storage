package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import com.porejemplo.nube.server.auth.service.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class UnauthLoginCommandReceivedStateOfAuthHandler implements State {

    private final AuthHandler aH;

    public UnauthLoginCommandReceivedStateOfAuthHandler(AuthHandler aH) {
        this.aH = aH;
    }

    @Override
    public State receiveCommand() {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx) {
        if (aH.currentPhase == Phase.USERNAME_LENGTH) {
            System.out.println("inside if USERNAME_LENGTH " + aH.buf.readableBytes());
            if (aH.buf.readableBytes() >= 4) {
                System.out.println("STATE: Getting username length");
                aH.usernameLength = aH.buf.readInt();
                aH.currentPhase = Phase.PASSWORD_LENGTH;
            } else return false;
        }

        if (aH.currentPhase == Phase.PASSWORD_LENGTH) {
            System.out.println("inside if PASSWORD_LENGTH ");
            if (aH.buf.readableBytes() >= 4) {
                System.out.println("STATE: Getting password length");
                aH.passwordLength = aH.buf.readInt();
                aH.currentPhase = Phase.USERNAME_AND_PASSWORD;
            } else return false;
        }

        if (aH.currentPhase == Phase.USERNAME_AND_PASSWORD) {
            if (aH.buf.readableBytes() >= aH.usernameLength + aH.passwordLength) {
                byte[] usernameBytes = new byte[aH.usernameLength];
                aH.buf.readBytes(usernameBytes);
                aH.username = new String(usernameBytes, StandardCharsets.UTF_8);
                System.out.println("STATE: username received - " + aH.username);

                byte[] passwordBytes = new byte[aH.passwordLength];
                aH.buf.readBytes(passwordBytes);
                aH.password = new String(passwordBytes, StandardCharsets.UTF_8);
                System.out.println("STATE: New password received - " + aH.password);

                aH.currentPhase = Phase.VERIFY_USERNAME_AND_PASSWORD;
            } else return false;
        }

        if (aH.currentPhase == Phase.VERIFY_USERNAME_AND_PASSWORD) {
            System.out.println("STATE: username and password verification ");
            if (AuthService.verifyUsernameAndPassword(aH.username, aH.password)) {
                aH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                aH.bufOut.writeByte(Command.LOGIN.getSignalByte());
                ctx.writeAndFlush(aH.bufOut);
                aH.currentState = aH.authStateOfAuthHandler;
                ctx.pipeline().addLast(new MainHandler(aH));
                System.out.println("Correct username and password.");
            } else {
                aH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                aH.bufOut.writeByte(Command.LOGIN.getFailureByte());
                ctx.writeAndFlush(aH.bufOut);
                aH.currentState = aH.unauthNoCommandReceivedStateOfAuthHandler;
                System.out.println("Wrong username and/or password.");
            }
            aH.currentPhase = Phase.IDLE;
        }
        return true;
    }
}
