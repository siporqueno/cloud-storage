package com.porejemplo.nube.client;

import com.porejemplo.nube.client.service.DownloadService;
import com.porejemplo.nube.client.service.IODownloadService;
import com.porejemplo.nube.client.service.IOUploadService;
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
import java.util.stream.Collectors;

public class MainCommandHandler extends CommandHandler {

    private final ConsoleClient consoleClient;
    private final UploadService uploadService;
    private final DownloadService downloadService;
    private final Path pathToUserDir;

    public MainCommandHandler(ConsoleClient consoleClient, DataOutputStream out, DataInputStream in) {
        this.consoleClient = consoleClient;
        this.pathToUserDir = Paths.get(consoleClient.STORAGE_ROOT, consoleClient.getUsername());
        this.downloadService = new IODownloadService(out, in, this.pathToUserDir);
        this.uploadService = new IOUploadService(out);
    }

    @Override
    void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
        super.handle(outputStream, inputStream, command, arguments);
        command.checkArguments(arguments);
        switch (command) {
            case LSLC:
                String fileNamesString = Files.list(pathToUserDir)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.joining(" "));
                System.out.println(fileNamesString);
                break;
            case LSCL:
                obtainCloudFileNames(outputStream, inputStream);
                break;
            case UPLD:
                Path path = Paths.get(pathToUserDir.toString(), arguments.get(0));
                if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
                    System.out.println("Such file does not exist");
                } else performUpload(path);
                break;
            case DNLD:
                performDownload(arguments.get(0));
                break;
            case RMLC:
                Path pathToFileToBeRenamed = Paths.get(pathToUserDir.toString(), arguments.get(0));
                Path pathToRenamedFile = Paths.get(pathToUserDir.toString(), arguments.get(1));
                Files.move(pathToFileToBeRenamed, pathToRenamedFile);
                break;
            case RMCL:
                renameFileInCloud(outputStream, inputStream, arguments.get(0), arguments.get(1));
                break;
            case DELLC:
                Path pathToFileToBeDeleted = Paths.get(pathToUserDir.toString(), arguments.get(0));
                Files.deleteIfExists(pathToFileToBeDeleted);
                break;
            case DELCL:
                deleteFileInCloud(outputStream, inputStream, arguments.get(0));
                break;
            case EXIT:
                logout(outputStream, inputStream, "You have successfully exited.");
                break;
            case LOGOUT:
                logout(outputStream, inputStream, "You have successfully logged out");
                break;
            default:
                if (inCommandHandler) inCommandHandler = false;
                else System.out.println("Such command is not available (You are logged in).");

        }
    }

    private boolean performUpload(Path path) throws IOException {
        return uploadService.upload(path);
    }

    private boolean performDownload(String fileName) throws IOException {
        return downloadService.download(fileName);
    }

    private void obtainCloudFileNames(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeByte(Command.LSCL.getSignalByte());
        byte signalByte = in.readByte();
        if (signalByte == Command.LSCL.getSignalByte()) {
            int listLength = in.readInt();
            byte[] bytes = new byte[listLength];
            for (int i = 0; i < listLength; i++) {
                bytes[i] = (byte) in.read();
            }
            String result = new String(bytes, StandardCharsets.UTF_8);
            System.out.println(result);
        }
    }

    private void renameFileInCloud(DataOutputStream out, DataInputStream in, String fileName, String newFileName) throws IOException {
        out.writeByte(Command.RMCL.getSignalByte());
        int fileNameLength = fileName.length();
        int newFileNameLength = newFileName.length();
        out.writeInt(fileNameLength);
        out.writeInt(newFileNameLength);
        out.write(fileName.getBytes());
        out.write(newFileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == Command.RMCL.getSignalByte()) {
            System.out.println("Great. Such file has been found and just renamed");
        } else if (signalByte == Command.RMCL.getFailureByte()) {
            System.out.println("No such file in the Cloud. Please double check file name.");
        }
    }

    private void deleteFileInCloud(DataOutputStream out, DataInputStream in, String fileName) throws IOException {
        out.writeByte(Command.DELCL.getSignalByte());
        int fileNameLength = fileName.length();
        out.writeInt(fileNameLength);
        out.write(fileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == Command.DELCL.getSignalByte()) {
            System.out.println("Great. Such file has been found and just deleted");
        } else if (signalByte == Command.DELCL.getFailureByte()) {
            System.out.println("No such file in the Cloud. Please double check file name.");
        }
    }

    private void logout(DataOutput out, DataInput in, String consoleMessage) throws IOException {
        out.writeByte(Command.LOGOUT.getSignalByte());
        byte signalByte = in.readByte();
        if (signalByte == Command.LOGOUT.getSignalByte()) {
            consoleClient.setAuthOk(false);
            System.out.println(consoleMessage);
        }
    }
}
