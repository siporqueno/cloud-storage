package com.porejemplo.nube.client;

import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.*;
import java.util.List;

public class AuthRegCommandHandler {

    private final ConsoleClient consoleClient;

    public AuthRegCommandHandler(ConsoleClient consoleClient) {
        this.consoleClient = consoleClient;
    }

    void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
        command.checkArguments(arguments);
        switch (command) {
            case LOGIN:
                if (areUsernameAndPasswordCorrectForCloud(outputStream, inputStream, arguments.get(0), arguments.get(1)))
                    consoleClient.setAuthOk(true);
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
