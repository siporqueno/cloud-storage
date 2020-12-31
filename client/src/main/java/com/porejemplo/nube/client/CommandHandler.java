package com.porejemplo.nube.client;

import com.porejemplo.nube.client.service.DownloadService;
import com.porejemplo.nube.client.service.UploadService;
import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CommandHandler {

    private final UploadService uploadService;
    private final DownloadService downloadService;

    public CommandHandler(UploadService uploadService, DownloadService downloadService) {
        this.uploadService = uploadService;
        this.downloadService = downloadService;
    }

    void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
        command.checkArguments(arguments);
        switch (command) {
            case LSLC:
                Path pathStrorage = Paths.get("client_storage");
                Files.list(pathStrorage).forEach((p) -> System.out.println(p.getFileName()));
                break;
            case LSCL:
                obtainCloudFileNames(outputStream, inputStream);
                break;
            case UPLD:
                Path path = Paths.get("client_storage", arguments.get(0));
                if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
                    System.out.println("Such file does not exist");
                } else performUpload(path);
                break;
            case DNLD:
                performDownload(Paths.get("server_storage", arguments.get(0)));
                break;
            case RMLC:
                Path pathToFileToBeRenamed = Paths.get("client_storage", arguments.get(0));
                Path pathToRenamedFile = Paths.get("client_storage", arguments.get(1));
                Files.move(pathToFileToBeRenamed, pathToRenamedFile);
                break;
            case RMCL:
                renameFileInCloud(outputStream, inputStream, arguments.get(0), arguments.get(1));
                break;
            case DELLC:
                Path pathToFileToBeDeleted = Paths.get("client_storage", arguments.get(0));
                Files.deleteIfExists(pathToFileToBeDeleted);
                break;
            case DELCL:
                deleteFileInCloud(outputStream, inputStream, arguments.get(0));
                break;
            case EXIT:
                break;
            default:
                System.out.println("Such command does not exist.");
        }
    }

    private boolean performUpload(Path path) throws IOException {
        return uploadService.upload(path);
    }

    private boolean performDownload(Path path) throws IOException {
        return downloadService.download(path);
    }

    private void obtainCloudFileNames(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeByte(14);
        byte signalByte = in.readByte();
        if (signalByte == 14) {
            int listLength = in.readInt();
            byte[] bytes = new byte[listLength];
            for (int i = 0; i < listLength; i++) {
                bytes[i] = (byte) in.read();
            }
            String result = new String(bytes, StandardCharsets.UTF_8);
            System.out.println(result);
        }
        System.out.println(in.available());
    }

    private void renameFileInCloud(DataOutputStream out, DataInputStream in, String fileName, String newFileName) throws IOException {
        out.writeByte(18);
        int fileNameLength = fileName.length();
        int newFileNameLength = newFileName.length();
        out.writeInt(fileNameLength);
        out.writeInt(newFileNameLength);
        out.write(fileName.getBytes());
        out.write(newFileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == 18) {
            System.out.println("Great. Such file has been found and just renamed");
        } else if (signalByte == 17) {
            System.out.println("No such file in the Cloud. Please double check file name.");
        }
    }

    private void deleteFileInCloud(DataOutputStream out, DataInputStream in, String fileName) throws IOException {
        out.writeByte(19);
        int fileNameLength = fileName.length();
        out.writeInt(fileNameLength);
        out.write(fileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == 19) {
            System.out.println("Great. Such file has been found and just deleted");
        } else if (signalByte == 17) {
            System.out.println("No such file in the Cloud. Please double check file name.");
        }
    }
}
