package com.porejemplo.nube.client;

import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public abstract class CommandHandler {

    void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
        command.checkArguments(arguments);
        switch (command) {
            case HELP:
                help();
                break;
            default:
        }
    }

    void help() {
        Arrays.stream(Command.values()).map(Command::getDescription).forEach(System.out::println);
    }

}
