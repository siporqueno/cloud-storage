package com.porejemplo.nube.client.service;

import java.io.IOException;
import java.nio.file.Path;

public interface DownloadService {
    boolean download(String fileName) throws IOException;
}
