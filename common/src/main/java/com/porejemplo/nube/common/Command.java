package com.porejemplo.nube.common;

import java.util.List;

public enum Command {

    UPLD("upld", "upld file_name\nUploads file named file_name to cloud storage. File name shall not contain spaces.", 1, (byte) 15),
    DNLD("dnld", "dnld file_name\nDownloads file named file_name from cloud storage. File name shall not contain spaces.", 1, (byte) 16),
    LSLC("lslc", "lslc\nLists files in the local storage.", 0, (byte) -1),
    LSCL("lscl", "lscl\nLists files in the cloud storage.", 0, (byte) 14),
    RMLC("rmlc", "rmlc file_name_one file_name_two\nRenames file file_name_one as file_name_two in the local storage", 2, (byte) -1),
    RMCL("rmcl", "rmcl file_name_one file_name_two\nRenames file file_name_one as file_name_two in the cloud storage", 2, (byte) 18),
    DELLC("dellc", "dellc file_name\nDeletes file file_name in the local storage", 1, (byte) -1),
    DELCL("delcl", "delcl file_name\nDeletes file file_name in the cloud storage", 1, (byte) 19),
    QUIT("\\q", "\\q\nExit", 0, (byte) -1);

    private final String name;
    private final String description;
    private final int requiredArgumentsNumber;
    private final byte signalByte;

    Command(String name, String description, int requiredArgumentsNumber, byte signalByte) {
        this.name = name;
        this.description = description;
        this.requiredArgumentsNumber = requiredArgumentsNumber;
        this.signalByte = signalByte;
    }

    public byte getSignalByte() {
        return signalByte;
    }

    public boolean checkArguments(List<String> arguments) throws ArgumentException {
        if (arguments.size() != requiredArgumentsNumber) {
            throw new ArgumentException(String.format("Wrong format of the command %s", name));
        }
        return true;
    }
}
