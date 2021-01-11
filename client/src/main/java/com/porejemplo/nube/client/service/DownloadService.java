package com.porejemplo.nube.client.service;

import java.io.IOException;
import java.nio.file.Path;

public interface DownloadService {
    boolean download(Path path) throws IOException;
}
