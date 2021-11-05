package com.porejemplo.nube.client.command;

import java.io.IOException;
import java.util.List;

public interface Command {

    void execute(List<String> arguments) throws IOException;

}
