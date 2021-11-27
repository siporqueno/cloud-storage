package com.porejemplo.nube.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Signal {

    UPLD("upld", "upld file_name\nUploads file named file_name to cloud storage. File name shall not contain spaces.", 1, (byte) 15, (byte) -1),
    DNLD("dnld", "dnld file_name\nDownloads file named file_name from cloud storage. File name shall not contain spaces.", 1, (byte) 16, (byte) 17),
    LSLC("lslc", "lslc\nLists files in the local storage.", 0, (byte) -1, (byte) -1),
    LSCL("lscl", "lscl\nLists files in the cloud storage.", 0, (byte) 14, (byte) -1),
    RMLC("rmlc", "rmlc file_name_one file_name_two\nRenames file file_name_one as file_name_two in the local storage", 2, (byte) -1, (byte) -1),
    RMCL("rmcl", "rmcl file_name_one file_name_two\nRenames file file_name_one as file_name_two in the cloud storage", 2, (byte) 18, (byte) 17),
    DELLC("dellc", "dellc file_name\nDeletes file file_name in the local storage", 1, (byte) -1, (byte) -1),
    DELCL("delcl", "delcl file_name\nDeletes file file_name in the cloud storage", 1, (byte) 19, (byte) 17),
    EXIT("exit", "exit\nExits the client.", 0, (byte) -1, (byte) -1),
    LOGIN("login", "login username password\nLogs in.", 2, (byte) 20, (byte) 21),
    LOGOUT("logout", "logout\nLogs out.", 0, (byte) 22, (byte) 23),
    REG("register", "register username password nickname\nRegisters new user.", 0, (byte) 24, (byte) 25),
    HELP("help", "help\nPrints all the commands.", 0, (byte) -1, (byte) -1);

    private static final Map<Byte, Signal> signals = new HashMap<>();

    static {
        for (Signal value : Signal.values()) signals.put(value.getSignalByte(), value);
    }

    private final String name;
    private final String description;
    private final int requiredArgumentsNumber;
    private final byte signalByte;
    private final byte failureByte;

    Signal(String name, String description, int requiredArgumentsNumber, byte signalByte, byte failureByte) {
        this.name = name;
        this.description = description;
        this.requiredArgumentsNumber = requiredArgumentsNumber;
        this.signalByte = signalByte;
        this.failureByte = failureByte;
    }

    public byte getSignalByte() {
        return signalByte;
    }

    public byte getFailureByte() {
        return failureByte;
    }

    public String getDescription() {
        return description;
    }

    public boolean checkArguments(List<String> arguments) throws ArgumentException {
        if (arguments.size() != requiredArgumentsNumber) {
            throw new ArgumentException(String.format("Wrong format of the command %s.", name));
        }
        return true;
    }

    public static Signal findSignalBySignalByte(byte receivedSignalByte) throws Exception {
        Signal signal = signals.get(receivedSignalByte);
        if (signal != null) return signal;
        throw new SignalNotFoundException(receivedSignalByte);
    }
}