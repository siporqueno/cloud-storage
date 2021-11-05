package com.porejemplo.nube.client.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RenameLocallyCommand implements Command {

    private final Path pathToUserDir;

    public RenameLocallyCommand(Path pathToUserDir) {
        this.pathToUserDir = pathToUserDir;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        Path pathToFileToBeRenamed = Paths.get(pathToUserDir.toString(), arguments.get(0));
        Path pathToRenamedFile = Paths.get(pathToUserDir.toString(), arguments.get(1));
        Files.move(pathToFileToBeRenamed, pathToRenamedFile);
    }
}
