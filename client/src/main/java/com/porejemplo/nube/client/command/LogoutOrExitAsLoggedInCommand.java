package com.porejemplo.nube.client.command;

import com.porejemplo.nube.client.ConsoleClient;
import com.porejemplo.nube.common.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class LogoutOrExitAsLoggedInCommand implements Command {

    private final ConsoleClient consoleClient;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final String consoleMessage;

    public LogoutOrExitAsLoggedInCommand(ConsoleClient consoleClient, DataOutputStream out, DataInputStream in, String consoleMessage) {
        this.consoleClient = consoleClient;
        this.out = out;
        this.in = in;
        this.consoleMessage = consoleMessage;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        logout(consoleMessage);
    }

    private void logout(String consoleMessage) throws IOException {
        out.writeByte(Signal.LOGOUT.getSignalByte());
        byte signalByte = in.readByte();
        if (signalByte == Signal.LOGOUT.getSignalByte()) {
            consoleClient.setAuthOk(false);
            System.out.println(consoleMessage);
        }
    }
}
