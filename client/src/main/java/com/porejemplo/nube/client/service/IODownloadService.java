package com.porejemplo.nube.client.service;

import com.porejemplo.nube.common.Signal;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.porejemplo.nube.common.Signal.DNLD;

public class IODownloadService implements DownloadService {
    private final DataOutput out;
    private final DataInput in;
    private final Path pathToUserDir;

    public IODownloadService(DataOutput out, DataInput in, Path pathToUserDir) {
        this.out = out;
        this.in = in;
        this.pathToUserDir = pathToUserDir;
    }

    @Override
    public boolean download(String fileName) throws IOException {
        out.writeByte(DNLD.getSignalByte());
        int fileNameLength = fileName.length();
        out.writeInt(fileNameLength);
        out.write(fileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == DNLD.getSignalByte()) {
            System.out.println("Great. Such file found.");
        } else if (signalByte == Signal.DELLC.getFailureByte()) {
            System.out.println("No such file in the Cloud. Please double check file name.");
            return false;
        }
        long fileSize = in.readLong();
        Path pathToFileToBeDownloaded = Paths.get(pathToUserDir.toString(), fileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pathToFileToBeDownloaded.toFile()))) {
            for (long i = 0; i < fileSize; i++) {
                bos.write(in.readByte());
            }
            System.out.printf("File %s has been downloaded.\n", fileName);
        }
        return true;
    }
}
