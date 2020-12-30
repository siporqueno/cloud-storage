package com.porejemplo.nube.client;

import com.porejemplo.nube.client.service.DownloadService;
import com.porejemplo.nube.client.service.IODownloadService;
import com.porejemplo.nube.client.service.IOUploadService;
import com.porejemplo.nube.client.service.UploadService;
import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleClient {

    public static final int PORT = 8189;
    public static final String HOST = "localhost";

    private final Scanner scanner = new Scanner(System.in);

    private UploadService uploadService;
    private DownloadService downloadService;

    public ConsoleClient() {
        try (Socket socket = new Socket(HOST, PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());) {

            uploadService = new IOUploadService(out);
            downloadService = new IODownloadService(out, in);
            scanner.useDelimiter("\\n");
            authenticate();
            useClient(out, in);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private void authenticate() {
        /*while (true) {

        }*/
    }

    ;

    private void useClient(DataOutputStream outputStream, DataInputStream inputStream) throws IOException {
        System.out.println("Welcome to Cloud storage client.");
        while (true) {
            System.out.println("Enter a command or press \\q to quit");
            String response = scanner.next();
            String[] respTokens = response.split(" ");
            Command command = Command.valueOf(respTokens[0]);
            List<String> arguments = Arrays.stream(respTokens).skip(1).collect(Collectors.toList());

            switch (respTokens[0]) {
                case "lslc":
                    if (respTokens.length > 1) {
                        System.out.println("Wrong format of the command lslc");
                        break;
                    }
                    Path pathStrorage = Paths.get("client_storage");
                    Files.list(pathStrorage).forEach((p) -> System.out.println(p.getFileName()));
                    break;
                case "lscl":
                    if (respTokens.length > 1) {
                        System.out.println("Wrong format of the command lscl");
                        break;
                    }
                    obtainCloudFileNames(outputStream, inputStream);
                    break;
                case "upld":
                    if (respTokens.length > 2) {
                        System.out.println("Wrong format of the command upld");
                        break;
                    }
                    Path path = Paths.get("client_storage", respTokens[1]);
                    if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
                        System.out.println("Such file does not exist");
                        break;
                    } else {
                        uploadFile(outputStream, path);
                        break;
                    }
                case "dnld":
                    if (respTokens.length > 2) {
                        System.out.println("Wrong format of the command dnld");
                        break;
                    }
                    downloadFile(outputStream, inputStream, respTokens[1]);
                    break;
                case "rmlc":
                    if (respTokens.length > 3) {
                        System.out.println("Wrong format of the command rmlc");
                        break;
                    }
                    Path pathToFileToBeRenamed = Paths.get("client_storage", respTokens[1]);
                    Path pathToRenamedFile = Paths.get("client_storage", respTokens[2]);
                    Files.move(pathToFileToBeRenamed, pathToRenamedFile);
                    break;
                case "rmcl":
                    if (respTokens.length > 3) {
                        System.out.println("Wrong format of the command rmlc");
                        break;
                    }
                    renameFileInCloud(outputStream, inputStream, respTokens[1], respTokens[2]);
                    break;
                case "dellc":
                    if (respTokens.length > 2) {
                        System.out.println("Wrong format of the command dellc");
                        break;
                    }
                    Path pathToFileToBeDeleted = Paths.get("client_storage", respTokens[1]);
                    Files.deleteIfExists(pathToFileToBeDeleted);
                    break;
                case "delcl":
                    if (respTokens.length > 2) {
                        System.out.println("Wrong format of the command delcl");
                        break;
                    }
                    deleteFileInCloud(outputStream, inputStream, respTokens[1]);
                    break;
                case "\\q":
                    break;
                default:
                    System.out.println("Such command does not exist.");
            }
            if (response.equals("\\q")) break;
        }
    }

    private void handle(DataOutputStream outputStream, DataInputStream inputStream, Command command, List<String> arguments) throws ArgumentException, IOException {
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
                    break;
                } else {
                    uploadService.upload(path);
                    break;
                }
            case DNLD:
                downloadService.download(Paths.get("server_storage", arguments.get(0)));
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
            case QUIT:
                break;
            default:
                System.out.println("Such command does not exist.");
        }
    }

    private void uploadFile(DataOutputStream out, Path path) throws IOException {
        out.writeByte(15);
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
        System.out.println("Клиент: файл отправлен");
    }

    private void downloadFile(DataOutputStream out, DataInputStream in, String fileName) throws IOException {
        out.writeByte(16);
        int fileNameLength = fileName.length();
        out.writeInt(fileNameLength);
        out.write(fileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == 16) {
            System.out.println("Great. Such file found.");
        } else if (signalByte == 17) {
            System.out.println("No such file in the Cloud. Please double check file name.");
            return;
        }
        long fileSize = in.readLong();
        Path pathToFileToBeDownloaded = Paths.get("client_storage", fileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pathToFileToBeDownloaded.toFile()))) {
            for (long i = 0; i < fileSize; i++) {
                bos.write(in.readByte());
            }
        }
        System.out.println("The file has been successfully downloaded.");
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

    public static void main(String[] args) {
        new ConsoleClient();
    }
}

