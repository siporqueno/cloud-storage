package com.porejemplo.nube.server.netty.auth_handler_state;

import com.porejemplo.nube.common.Command;
import com.porejemplo.nube.common.dto.UserAuthDto;
import com.porejemplo.nube.server.auth.service.AuthService;
import com.porejemplo.nube.server.netty.AuthHandler;
import com.porejemplo.nube.server.netty.MainHandler;
import com.porejemplo.nube.server.netty.Phase;
import com.porejemplo.nube.server.netty.State;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginCommandUnauthState implements State {

    private int usernameLength;
    private int passwordLength;
    //    private String username;
//    private String password;
    private UserAuthDto user;
    private final AuthHandler aH;
    private final AuthService authService;

    public LoginCommandUnauthState(AuthHandler aH, AuthService authService) {
        this.aH = aH;
        this.authService = authService;
    }

    @Override
    public State receiveCommand(ByteBuf buf, ByteBuf bufOut) {
        return this;
    }

    @Override
    public boolean processCommand(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf bufOut) {
        System.out.println(aH.getCurrentPhase());
        if (aH.getCurrentPhase() == Phase.USERNAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                AuthHandler.getLOGGER().info("STATE: Getting username length");
                usernameLength = buf.readInt();
                aH.setCurrentPhase(Phase.PASSWORD_LENGTH);
            } else return false;
        }

        if (aH.getCurrentPhase() == Phase.PASSWORD_LENGTH) {
            if (buf.readableBytes() >= 4) {
                AuthHandler.getLOGGER().info("STATE: Getting password length");
                passwordLength = buf.readInt();
                aH.setCurrentPhase(Phase.USERNAME_AND_PASSWORD);
            } else return false;
        }

        if (aH.getCurrentPhase() == Phase.USERNAME_AND_PASSWORD) {
            if (buf.readableBytes() >= usernameLength + passwordLength) {
                byte[] usernameBytes = new byte[usernameLength];
                buf.readBytes(usernameBytes);
                user.setUsername(new String(usernameBytes, StandardCharsets.UTF_8));
                AuthHandler.getLOGGER().info("STATE: username received - " + user.getUsername());

                byte[] passwordBytes = new byte[passwordLength];
                buf.readBytes(passwordBytes);
                user.setPassword(new String(passwordBytes, StandardCharsets.UTF_8));
                AuthHandler.getLOGGER().info("STATE: New password received - " + user.getPassword());

                aH.setCurrentPhase(Phase.VERIFY_USERNAME_AND_PASSWORD);
            } else return false;
        }

        if (aH.getCurrentPhase() == Phase.VERIFY_USERNAME_AND_PASSWORD) {
            AuthHandler.getLOGGER().info("STATE: username and password verification ");
            if (authService.verifyUsernameAndPassword(user)) {
                aH.setPathToUserDir(Paths.get(aH.getSTORAGE_ROOT(), user.getUsername()));
                if (Files.notExists(aH.getPathToUserDir())) {
                    try {
                        Files.createDirectory(aH.getPathToUserDir());
                    } catch (IOException e) {
                        System.out.println("This IO exception is caused by an attempt to create user directory.");
                        e.printStackTrace();
                    }
                }

                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte(Command.LOGIN.getSignalByte());
                ctx.writeAndFlush(bufOut);
                aH.setCurrentState(aH.getAuthStateOfAuthHandler());
                ctx.pipeline().addLast(new MainHandler(aH));
                AuthHandler.getLOGGER().info("Correct username and password.");
            } else {
                bufOut = ByteBufAllocator.DEFAULT.directBuffer(1);
                bufOut.writeByte(Command.LOGIN.getFailureByte());
                ctx.writeAndFlush(bufOut);
                aH.setCurrentState(aH.getUnauthNoCommandReceivedStateOfAuthHandler());
                AuthHandler.getLOGGER().info("Wrong username and/or password.");
            }
            aH.setCurrentPhase(Phase.IDLE);
        }
        return true;
    }
}
