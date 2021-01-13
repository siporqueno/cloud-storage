package com.porejemplo.nube.server.netty;

import com.porejemplo.nube.common.Command;
import com.porejemplo.nube.server.auth.service.AuthService;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            if (aH.buf.readableBytes() >= 4) {
                AuthHandler.LOGGER.info("STATE: Getting username length");
                aH.usernameLength = aH.buf.readInt();
                aH.currentPhase = Phase.PASSWORD_LENGTH;
            } else return false;
        }

        if (aH.currentPhase == Phase.PASSWORD_LENGTH) {
            if (aH.buf.readableBytes() >= 4) {
                AuthHandler.LOGGER.info("STATE: Getting password length");
                aH.passwordLength = aH.buf.readInt();
                aH.currentPhase = Phase.USERNAME_AND_PASSWORD;
            } else return false;
        }

        if (aH.currentPhase == Phase.USERNAME_AND_PASSWORD) {
            if (aH.buf.readableBytes() >= aH.usernameLength + aH.passwordLength) {
                byte[] usernameBytes = new byte[aH.usernameLength];
                aH.buf.readBytes(usernameBytes);
                aH.username = new String(usernameBytes, StandardCharsets.UTF_8);
                AuthHandler.LOGGER.info("STATE: username received - " + aH.username);

                byte[] passwordBytes = new byte[aH.passwordLength];
                aH.buf.readBytes(passwordBytes);
                aH.password = new String(passwordBytes, StandardCharsets.UTF_8);
                AuthHandler.LOGGER.info("STATE: New password received - " + aH.password);

                aH.currentPhase = Phase.VERIFY_USERNAME_AND_PASSWORD;
            } else return false;
        }

        if (aH.currentPhase == Phase.VERIFY_USERNAME_AND_PASSWORD) {
            AuthHandler.LOGGER.info("STATE: username and password verification ");
            if (AuthService.verifyUsernameAndPassword(aH.username, aH.password)) {
                aH.pathToUserDir = Paths.get(aH.STORAGE_ROOT, aH.username);
                if (Files.notExists(aH.pathToUserDir)) {
                    try {
                        Files.createDirectory(aH.pathToUserDir);
                    } catch (IOException e) {
                        System.out.println("This IO exception is caused by an attempt to create user directory.");
                        e.printStackTrace();
                    }
                }

                aH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                aH.bufOut.writeByte(Command.LOGIN.getSignalByte());
                ctx.writeAndFlush(aH.bufOut);
                aH.currentState = aH.authStateOfAuthHandler;
                ctx.pipeline().addLast(new MainHandler(aH));
                AuthHandler.LOGGER.info("Correct username and password.");
            } else {
                aH.bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                aH.bufOut.writeByte(Command.LOGIN.getFailureByte());
                ctx.writeAndFlush(aH.bufOut);
                aH.currentState = aH.unauthNoCommandReceivedStateOfAuthHandler;
                AuthHandler.LOGGER.info("Wrong username and/or password.");
            }
            aH.currentPhase = Phase.IDLE;
        }
        return true;
    }
}
