package com.porejemplo.nube.client.service;

import com.porejemplo.nube.common.Command;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.porejemplo.nube.common.Command.DNLD;

public class IODownloadService implements DownloadService {
    private final DataOutput out;
    private final DataInput in;

    public IODownloadService(DataOutput out, DataInput in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public boolean download(Path path) throws IOException {
        out.writeByte(DNLD.getSignalByte());
        String fileName = path.getFileName().toString();
        int fileNameLength = fileName.length();
        out.writeInt(fileNameLength);
        out.write(fileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == DNLD.getSignalByte()) {
            System.out.println("Great. Such file found.");
        } else if (signalByte == Command.DELLC.getFailureByte()) {
            System.out.println("No such file in the Cloud. Please double check file name.");
            return false;
        }
        long fileSize = in.readLong();
        Path pathToFileToBeDownloaded = Paths.get("client_storage", fileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pathToFileToBeDownloaded.toFile()))) {
            for (long i = 0; i < fileSize; i++) {
                bos.write(in.readByte());
            }
            System.out.printf("File %s has been downloaded.\n", fileName);
        }
        return true;
    }
}
