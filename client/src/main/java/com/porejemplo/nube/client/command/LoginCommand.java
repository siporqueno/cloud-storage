package com.porejemplo.nube.client.command;

import com.porejemplo.nube.client.ConsoleClient;
import com.porejemplo.nube.common.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LoginCommand implements Command {

    private final ConsoleClient consoleClient;
    private final DataOutputStream out;
    private final DataInputStream in;

    public LoginCommand(ConsoleClient consoleClient, DataOutputStream out, DataInputStream in) {
        this.consoleClient = consoleClient;
        this.out = out;
        this.in = in;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        if (areUsernameAndPasswordCorrectForCloud(arguments.get(0), arguments.get(1))) {
            consoleClient.setUsername(arguments.get(0));
            Path path = Paths.get(consoleClient.getSTORAGE_ROOT(), consoleClient.getUsername());
            if (Files.notExists(path)) Files.createDirectory(path);
            consoleClient.initMainChain(consoleClient, out, in);
            consoleClient.setAuthOk(true);
        }
    }

    private boolean areUsernameAndPasswordCorrectForCloud(String username, String password) throws IOException {
        out.writeByte(Signal.LOGIN.getSignalByte());
        int usernameLength = username.length();
        int passwordLength = password.length();
        out.writeInt(usernameLength);
        out.writeInt(passwordLength);
        out.write(username.getBytes());
        out.write(password.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == Signal.LOGIN.getSignalByte()) {
            System.out.println("You have successfully logged in. Enjoy!");
        } else if (signalByte == Signal.LOGIN.getFailureByte()) {
            System.out.println("Your username and/or password for the Cloud are wrong. Please try again.");
            return false;
        }
        return true;
    }
}
