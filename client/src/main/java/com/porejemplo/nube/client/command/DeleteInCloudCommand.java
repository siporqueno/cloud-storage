package com.porejemplo.nube.client.command;

import com.porejemplo.nube.common.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class DeleteInCloudCommand implements Command {

    private final DataOutputStream out;
    private final DataInputStream in;

    public DeleteInCloudCommand(DataOutputStream out, DataInputStream in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        deleteFileInCloud(arguments.get(0));
    }

    private void deleteFileInCloud(String fileName) throws IOException {
        out.writeByte(Signal.DELCL.getSignalByte());
        int fileNameLength = fileName.length();
        out.writeInt(fileNameLength);
        out.write(fileName.getBytes());
        byte signalByte = in.readByte();
        if (signalByte == Signal.DELCL.getSignalByte()) {
            System.out.println("Great. Such file has been found and just deleted");
        } else if (signalByte == Signal.DELCL.getFailureByte()) {
            System.out.println("No such file in the Cloud. Please double check file name.");
        }
    }
}
