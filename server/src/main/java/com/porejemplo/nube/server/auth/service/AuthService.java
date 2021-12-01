package com.porejemplo.nube.server.auth.service;

import com.porejemplo.nube.common.dto.UserAuthDto;

public interface AuthService {

    boolean verifyUsernameAndPassword(UserAuthDto givenUser);


}
