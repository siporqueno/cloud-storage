package com.porejemplo.nube.client.command;

import com.porejemplo.nube.common.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class RenameInCloudCommand implements Command {

    private final DataOutputStream out;
    private final DataInputStream in;

    public RenameInCloudCommand(DataOutputStream out, DataInputStream in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        renameFileInCloud(arguments.get(0), arguments.get(1));
    }

    private void renameFileInCloud(String fileName, String newFileName) throws IOException {
        out.writeByte(Signal.RMCL.getSignalByte());
        int fileNameLength = fileName.length();
        int newFileNameLength = newFileName.length();
        out.writeInt(fileNameLength);
        out.writeInt(newFileNameLength);
        out.write(fileName.getBytes());
        out.write(newFileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == Signal.RMCL.getSignalByte()) {
            System.out.println("Great. Such file has been found and just renamed");
        } else if (signalByte == Signal.RMCL.getFailureByte()) {
            System.out.println("No such file in the Cloud. Please double check file name.");
        }
    }
}
