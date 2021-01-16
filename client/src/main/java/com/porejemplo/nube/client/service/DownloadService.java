package com.porejemplo.nube.client.service;

import java.io.IOException;

public interface DownloadService {
    boolean download(String fileName) throws IOException;
}
