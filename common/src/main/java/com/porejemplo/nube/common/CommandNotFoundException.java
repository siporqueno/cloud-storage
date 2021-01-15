package com.porejemplo.nube.common;

public class CommandNotFoundException extends Exception{
    public CommandNotFoundException(byte receivedSignalByte) {
        super(String.format("Oops. Exception has been thrown during call of method Command.findCommandBySignalBythe with argument %d", receivedSignalByte));
    }
}
