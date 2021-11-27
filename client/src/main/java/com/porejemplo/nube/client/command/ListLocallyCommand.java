package com.porejemplo.nube.client.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ListLocallyCommand implements Command {

    private final Path pathToUserDir;

    public ListLocallyCommand(Path pathToUserDir) {
        this.pathToUserDir = pathToUserDir;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        String fileNamesString = Files.list(pathToUserDir)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.joining(" "));
        System.out.println(fileNamesString);
    }
}
