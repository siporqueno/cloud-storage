package com.porejemplo.nube.client.handler;

import com.porejemplo.nube.client.ConsoleClient;
import com.porejemplo.nube.client.command.Command;
import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Signal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class Handler {

    Handler next;
    Map<Signal, Command> commands;
    ConsoleClient consoleClient;

    public void handle(Signal signal, List<String> arguments) throws ArgumentException, IOException {
        signal.checkArguments(arguments);
        Command command = commands.get(signal);
        if (command != null) {
            command.execute(arguments);
        } else if (next != null) {
            next.handle(signal, arguments);
        } else {
            System.out.printf("Such command is not available (You are logged %s).\n", (consoleClient.isAuthOk() ? "in" : "out"));
        }
    }

    public void link(Handler next) {
        this.next = next;
    }
}
