package com.porejemplo.nube.client.command;

import com.porejemplo.nube.common.Signal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DeleteLocallyCommand implements Command {

    private final Path pathToUserDir;

    public DeleteLocallyCommand(Path pathToUserDir) {
        this.pathToUserDir = pathToUserDir;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        Path pathToFileToBeDeleted = Paths.get(pathToUserDir.toString(), arguments.get(0));

        if (Files.deleteIfExists(pathToFileToBeDeleted)) {
            System.out.println("Great. Such file has been found and just deleted");
        } else {
            System.out.println("No such file exists locally. Please double check file name.");
        }
    }
}
