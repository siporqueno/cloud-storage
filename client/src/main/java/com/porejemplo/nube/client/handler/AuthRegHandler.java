package com.porejemplo.nube.client.handler;

import com.porejemplo.nube.client.ConsoleClient;
import com.porejemplo.nube.client.command.ExitAsLoggedOutCommand;
import com.porejemplo.nube.client.command.LoginCommand;
import com.porejemplo.nube.client.command.RegisterCommand;
import com.porejemplo.nube.common.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;

public class AuthRegHandler extends Handler {

    private final DataOutputStream out;
    private final DataInputStream in;

    public AuthRegHandler(ConsoleClient consoleClient, DataOutputStream out, DataInputStream in) {
        this.consoleClient = consoleClient;
        this.out = out;
        this.in = in;

        commands = new HashMap<>();
        commands.put(Signal.LOGIN, new LoginCommand(this.consoleClient, this.out, this.in));
        commands.put(Signal.REG, new RegisterCommand());
        commands.put(Signal.EXIT, new ExitAsLoggedOutCommand());
    }
}
