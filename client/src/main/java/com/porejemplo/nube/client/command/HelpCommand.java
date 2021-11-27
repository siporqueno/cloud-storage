package com.porejemplo.nube.client.command;

import com.porejemplo.nube.common.Signal;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HelpCommand implements Command {
    @Override
    public void execute(List<String> arguments) throws IOException {
        help();
    }

    void help() {
        System.out.println("Here are console commands of Cloud Storage client. Please don't use file names which contain spaces.");
        Arrays.stream(Signal.values())
                .map(Signal::getDescription)
                .sorted()
                .forEach(System.out::println);
    }
}
