package com.porejemplo.nube.client;

import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public abstract class CommandHandler {

    // This flag is set to to true if the command is found here, in Command Handler.
    boolean inCommandHandler;

    void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
        command.checkArguments(arguments);
        switch (command) {
            case HELP:
                inCommandHandler = true;
                help();
                break;
            default:
        }
    }

    void help() {
        System.out.println("Here are console commands of Cloud Storage client. Please don't use file names which contain spaces.");
        Arrays.stream(Command.values()).map(Command::getDescription).forEach(System.out::println);
    }

}
