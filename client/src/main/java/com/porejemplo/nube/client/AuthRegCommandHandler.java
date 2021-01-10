package com.porejemplo.nube.client;

import com.porejemplo.nube.client.service.IODownloadService;
import com.porejemplo.nube.client.service.IOUploadService;
import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AuthRegCommandHandler {

    private final ConsoleClient consoleClient;
    private DataOutputStream out;
    private DataInputStream in;

    public AuthRegCommandHandler(ConsoleClient consoleClient, DataOutputStream out, DataInputStream in) {
        this.consoleClient = consoleClient;
        this.out = out;
        this.in = in;
    }

    void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
        command.checkArguments(arguments);
        switch (command) {
            case LOGIN:
                if (areUsernameAndPasswordCorrectForCloud(outputStream, inputStream, arguments.get(0), arguments.get(1))) {
                    consoleClient.setUsername(arguments.get(0));
                    Path path = Paths.get(consoleClient.STORAGE_ROOT, consoleClient.getUsername());
                    if (Files.notExists(path)) Files.createDirectory(path);
                    consoleClient.setMainCommandHandler(new MainCommandHandler(consoleClient, out, in));
                    consoleClient.setAuthOk(true);
                }
                break;
            case REG:
                System.out.println("The command register is under development.");
                break;
            case EXIT:
                break;
            default:
                System.out.println("Such command is not available (You are logged out).");
        }
    }

    private boolean areUsernameAndPasswordCorrectForCloud(DataOutputStream out, DataInputStream in, String username, String password) throws IOException {
        out.writeByte(Command.LOGIN.getSignalByte());
        int usernameLength = username.length();
        int passwordLength = password.length();
        out.writeInt(usernameLength);
        out.writeInt(passwordLength);
        out.write(username.getBytes());
        out.write(password.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == Command.LOGIN.getSignalByte()) {
            System.out.println("You have successfully logged in. Enjoy!");
        } else if (signalByte == Command.LOGIN.getFailureByte()) {
            System.out.println("Your username and/or password for the Cloud are wrong. Please try again.");
            return false;
        }
        return true;
    }
}
