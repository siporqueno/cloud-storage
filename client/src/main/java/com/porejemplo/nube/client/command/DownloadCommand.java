package com.porejemplo.nube.client.command;

import com.porejemplo.nube.client.service.DownloadService;

import java.io.IOException;
import java.util.List;

public class DownloadCommand implements Command {

    private final DownloadService downloadService;

    public DownloadCommand(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        performDownload(arguments.get(0));
    }

    private boolean performDownload(String fileName) throws IOException {
        return downloadService.download(fileName);
    }
}
