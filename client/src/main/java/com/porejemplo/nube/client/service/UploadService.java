package com.porejemplo.nube.client.service;

import java.io.IOException;
import java.nio.file.Path;

public interface UploadService {
    boolean upload(Path path) throws IOException;
}
