package com.porejemplo.nube.client;

import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CommonCommandHandlerDecorator extends CommandHandlerDecorator {

    boolean commandHandled;

    public CommonCommandHandlerDecorator(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
        command.checkArguments(arguments);
        switch (command) {
            case HELP:
                commandHandled = true;
                help();
                break;
            default:
        }

        if (commandHandled) {
            commandHandled = false;
        } else {
            commandHandler.handle(outputStream, inputStream, command, arguments);
        }
    }

    void help() {
        System.out.println("Here are console commands of Cloud Storage client. Please don't use file names which contain spaces.");
        Arrays.stream(Command.values()).map(Command::getDescription).forEach(System.out::println);
    }
}
