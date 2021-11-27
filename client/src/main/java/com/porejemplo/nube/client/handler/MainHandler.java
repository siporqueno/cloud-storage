package com.porejemplo.nube.client.handler;

import com.porejemplo.nube.client.ConsoleClient;
import com.porejemplo.nube.client.command.*;
import com.porejemplo.nube.client.service.DownloadService;
import com.porejemplo.nube.client.service.IODownloadService;
import com.porejemplo.nube.client.service.IOUploadService;
import com.porejemplo.nube.client.service.UploadService;
import com.porejemplo.nube.common.Signal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class MainHandler extends Handler {

    private final UploadService uploadService;
    private final DownloadService downloadService;
    private final Path pathToUserDir;
    private final DataOutputStream out;
    private final DataInputStream in;

    public MainHandler(ConsoleClient consoleClient, DataOutputStream out, DataInputStream in) {
        this.consoleClient = consoleClient;
        this.pathToUserDir = Paths.get(consoleClient.getSTORAGE_ROOT(), consoleClient.getUsername());
        this.out = out;
        this.in = in;
        this.downloadService = new IODownloadService(this.out, this.in, this.pathToUserDir);
        this.uploadService = new IOUploadService(this.out);

        commands = new HashMap<>();
        commands.put(Signal.LSLC, new ListLocallyCommand(this.pathToUserDir));
        commands.put(Signal.LSCL, new ListInCloudCommand(this.out, this.in));
        commands.put(Signal.UPLD, new UploadCommand(this.pathToUserDir, this.uploadService));
        commands.put(Signal.DNLD, new DownloadCommand(this.downloadService));
        commands.put(Signal.RMLC, new RenameLocallyCommand(this.pathToUserDir));
        commands.put(Signal.RMCL, new RenameInCloudCommand(this.out, this.in));
        commands.put(Signal.DELLC, new DeleteLocallyCommand(this.pathToUserDir));
        commands.put(Signal.DELCL, new DeleteInCloudCommand(this.out, this.in));
        commands.put(Signal.LOGOUT, new LogoutOrExitAsLoggedInCommand(this.consoleClient, this.out, this.in, "You have successfully logged out"));
        commands.put(Signal.EXIT, new LogoutOrExitAsLoggedInCommand(this.consoleClient, this.out, this.in, "You have successfully exited"));
    }
}
