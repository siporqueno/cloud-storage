package com.porejemplo.nube.client;

import com.porejemplo.nube.common.ArgumentException;
import com.porejemplo.nube.common.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleClient {

    public static final int PORT = 8189;
    public static final String HOST = "localhost";
    final String STORAGE_ROOT = "client_storage";

    private final Scanner scanner = new Scanner(System.in);

    private boolean authOk = false;
    private CommandHandler authRegCommandHandler;
    private CommandHandler mainCommandHandler;
    private String username;

    public ConsoleClient() {
        try (Socket socket = new Socket(HOST, PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());) {

            authRegCommandHandler = new CommonCommandHandlerDecorator(new AuthRegCommandHandler(this, out, in));
            scanner.useDelimiter("\\n");
            runClient(out, in);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public CommandHandler getMainCommandHandler() {
        return mainCommandHandler;
    }

    public void setMainCommandHandler(CommandHandler mainCommandHandler) {
        this.mainCommandHandler = mainCommandHandler;
    }

    public boolean isAuthOk() {
        return authOk;
    }

    public void setAuthOk(boolean authOk) {
        this.authOk = authOk;
    }

    private void runClient(DataOutputStream outputStream, DataInputStream inputStream) throws IOException {
        System.out.println("Welcome to Cloud storage client!");
        while (true) {
            System.out.printf("Enter a command (You are logged %s now).\n", (authOk) ? "in as " + username : "out");
            String response = scanner.next();
            String[] respTokens = response.split(" ");
            try {
                Command command = Command.valueOf(respTokens[0].toUpperCase());
                List<String> arguments = Arrays.stream(respTokens).skip(1).collect(Collectors.toList());
                if (authOk) mainCommandHandler.handle(outputStream, inputStream, command, arguments);
                else authRegCommandHandler.handle(outputStream, inputStream, command, arguments);

            } catch (IllegalArgumentException e) {
                System.out.printf("The command %s does not exist!\n", respTokens[0]);
            } catch (ArgumentException e) {
                System.out.println(e.getMessage());
            }

            if (response.equals("exit")) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        new ConsoleClient();
    }
}

