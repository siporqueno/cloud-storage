package com.porejemplo.nube.client.handler;

import com.porejemplo.nube.client.command.HelpCommand;
import com.porejemplo.nube.common.Signal;

import java.util.HashMap;

public class CommonHandler extends Handler {

    public CommonHandler() {
        commands = new HashMap<>();
        commands.put(Signal.HELP, new HelpCommand());
    }
}
