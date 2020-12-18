package com.porejemplo.nube.server.io_server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainServerApp {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запущен");
            try (Socket socket = serverSocket.accept();
                 DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                byte signalByte = in.readByte();
                int fileNameLength = in.readInt();
                byte[] fileNameBytes = new byte[fileNameLength];
                in.read(fileNameBytes);
                String fileName = new String(fileNameBytes);
                Path path = Paths.get("server_storage", fileName);
                long fileSize = in.readLong();

                try (OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
                    for (long i = 0; i < fileSize; i++) {
                        fileOutputStream.write(in.read());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
