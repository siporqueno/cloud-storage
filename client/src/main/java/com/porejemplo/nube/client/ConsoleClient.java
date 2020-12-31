package com.porejemplo.nube.client;

import com.porejemplo.nube.client.service.IODownloadService;
import com.porejemplo.nube.client.service.IOUploadService;
import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleClient {

    public static final int PORT = 8189;
    public static final String HOST = "localhost";

    private final Scanner scanner = new Scanner(System.in);

    private CommandHandler commandHandler;

    public ConsoleClient() {
        try (Socket socket = new Socket(HOST, PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());) {

            commandHandler = new CommandHandler(new IOUploadService(out), new IODownloadService(out, in));
            scanner.useDelimiter("\\n");
            authenticate();
            runClient(out, in);

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

    private void runClient(DataOutputStream outputStream, DataInputStream inputStream) throws IOException {
        System.out.println("Welcome to Cloud storage client.");
        while (true) {
            System.out.println("Enter a command");
            String response = scanner.next();
            String[] respTokens = response.split(" ");
            Command command = Command.valueOf(respTokens[0].toUpperCase());
            List<String> arguments = Arrays.stream(respTokens).skip(1).collect(Collectors.toList());

            try {
                commandHandler.handle(outputStream, inputStream, command, arguments);
            } catch (ArgumentException e) {
                e.printStackTrace();
            }
            if (response.equals("exit")) break;
        }
    }

    public static void main(String[] args) {
        new ConsoleClient();
    }
}

