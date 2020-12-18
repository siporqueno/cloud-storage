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
//            authenticate();
            useClient(out);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private void authenticate(){
        while (true){

        }
    };

    private void useClient(DataOutputStream outputStream) throws IOException {
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
                        sendFile(outputStream, path);
                        break;
                    }
                case "dnld":
                    if (respTokens.length > 2) {
                        System.out.println("Wrong format of the command dnld");
                        break;
                    }
                    System.out.println("Command dnld is under development. Waiting for Netty.");
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

    public void sendFile(DataOutputStream out, Path path) throws IOException {
        out.write(15);
//        String fileName = "1.txt";
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
}
