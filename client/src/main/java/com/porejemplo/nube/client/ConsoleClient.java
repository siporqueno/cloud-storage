package com.porejemplo.nube.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ConsoleClient {
    private static Scanner scanner = new Scanner(System.in);

    public ConsoleClient() {
        try (Socket socket = new Socket("localhost", 8189);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            System.out.println("Welcome to Cloud storage client.");
            while (true) {
                System.out.println("Enter a command or press \\q to quit");
                String response = scanner.next();
                switch (response) {
                    case "send":
                        Path path = Paths.get("client_storage", "1.txt");
                        sendFile(out, path);
                        break;
                    case "\\q":
                        break;
                    default:
                        System.out.println("Such command does not exist.");
                }
                if (response.equals("\\q")) break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(DataOutputStream out, Path path) throws IOException {
        out.write(15);
        String fileName = "1.txt";
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
