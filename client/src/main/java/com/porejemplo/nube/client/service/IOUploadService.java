package com.porejemplo.nube.client.service;

import com.porejemplo.nube.common.Command;

import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class IOUploadService implements UploadService {
    private final DataOutput out;

    public IOUploadService(DataOutput out) {
        this.out = out;
    }

    @Override
    public boolean upload(Path path) throws IOException {
        out.writeByte(Command.UPLD.getSignalByte());
        String fileName = path.getFileName().toString();
        int fileNameLength = fileName.length();
        out.writeInt(fileNameLength);
        out.write(fileName.getBytes());
        long fileSize = Files.size(path);
        out.writeLong(fileSize);
        byte[] buf = new byte[256];
        try (InputStream inputStream = new FileInputStream(path.toFile())) {
            int n;
            while ((n = inputStream.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
        return true;
    }
}
