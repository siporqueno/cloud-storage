package com.porejemplo.nube.client.command;

import com.porejemplo.nube.common.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListInCloudCommand implements Command {

    private final DataOutputStream out;
    private final DataInputStream in;

    public ListInCloudCommand(DataOutputStream out, DataInputStream in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void execute(List<String> arguments) throws IOException {
        obtainCloudFileNames(out, in);
    }

    private void obtainCloudFileNames(DataOutputStream out, DataInputStream in) throws IOException {
        out.writeByte(Signal.LSCL.getSignalByte());
        byte signalByte = in.readByte();
        if (signalByte == Signal.LSCL.getSignalByte()) {
            int listLength = in.readInt();
            byte[] bytes = new byte[listLength];
            for (int i = 0; i < listLength; i++) {
                bytes[i] = (byte) in.read();
            }
            String result = new String(bytes, StandardCharsets.UTF_8);
            System.out.println(result);
        }
    }
}
