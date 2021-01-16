package com.porejemplo.nube.server.auth.service;

public interface AuthService {

    boolean verifyUsernameAndPassword(String givenUsername, String givenPassword);


}
