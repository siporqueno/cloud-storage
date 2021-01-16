package com.porejemplo.nube.server.auth.repository;

public class UserDAOFactory {

    public static UserDAO getUserDAO(StorageType type) {
        switch (type){
            case SQ_LITE:
                throw new UnsupportedOperationException();
            case IN_MEMORY:
                return new UserDAOInMemory();
            default:
                throw new UnsupportedOperationException();
        }
    }
}
