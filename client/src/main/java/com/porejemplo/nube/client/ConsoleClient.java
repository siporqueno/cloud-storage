package com.porejemplo.nube.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class ConsoleClient {
    private Scanner scanner = new Scanner(System.in);

    public ConsoleClient() {
        try (Socket socket = new Socket("localhost", 8189);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());) {
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
                    System.out.println("Command lscl is under development. Waiting for Netty.");
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
                    System.out.println("Command rmcl is under development. Waiting for Netty.");
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
                    System.out.println("Command delcl is under development. Waiting for Netty.");
                    break;
                case "\\q":
                    break;
                default:
                    System.out.println("Such command does not exist.");
            }
            if (response.equals("\\q")) break;
        }
    }

    public void uploadFile(DataOutputStream out, Path path) throws IOException {
        out.write((byte) 15);
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

    public void downloadFile(DataOutputStream out, DataInputStream in, String fileName) throws IOException {
//        out.write((byte)16);
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
        System.out.println(fileSize);
        Path pathToFileToBeDownloaded = Paths.get("client_storage", fileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pathToFileToBeDownloaded.toFile()))) {
            for (long i = 0; i < fileSize; i++) {
                bos.write(in.readByte());
            }
        }
    }

}
