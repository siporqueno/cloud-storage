package com.porejemplo.nube.server.netty;

public enum State {
    IDLE,
    NAME_LENGTH,
    NAME,
    FILE_LENGTH,
    FILE,
    VERIFY_FILE_PRESENCE,
    FILE_DESPATCH,
    FILES_LIST,
    NAME_AND_NEW_NAME_LENGTH,
    NAME_AND_NEW_NAME,
    RENAME_FILE,
    NEW_NAME_LENGTH,
    DELETE_FILE
}
