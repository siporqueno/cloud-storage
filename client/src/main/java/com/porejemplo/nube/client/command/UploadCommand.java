package com.porejemplo.nube.client.command;

import com.porejemplo.nube.client.service.UploadService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UploadCommand implements Command {

    private final Path pathToUserDir;
    private final UploadService uploadService;

    public UploadCommand(Path pathToUserDir, UploadService uploadService) {
        this.pathToUserDir = pathToUserDir;
        this.uploadService = uploadService;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        Path path = Paths.get(pathToUserDir.toString(), arguments.get(0));
        if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
            System.out.println("Such file does not exist");
        } else performUpload(path);
    }

    private boolean performUpload(Path path) throws IOException {
        return uploadService.upload(path);
    }
}
