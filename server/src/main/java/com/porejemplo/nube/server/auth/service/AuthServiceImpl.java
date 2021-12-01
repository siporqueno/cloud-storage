package com.porejemplo.nube.server.auth.service;

import com.porejemplo.nube.common.dto.UserAuthDto;
import com.porejemplo.nube.server.auth.entity.User;
import com.porejemplo.nube.server.auth.repository.UserDAO;
import com.porejemplo.nube.server.auth.repository.UserIdentityMap;

import java.util.Objects;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
//    public boolean verifyUsernameAndPassword(String givenUsername, String givenPassword) {
    public boolean verifyUsernameAndPassword(UserAuthDto givenUser) {
        String givenUsername = givenUser.getUsername();
        String givenPassword = givenUser.getPassword();

        Objects.requireNonNull(givenUsername);
        Objects.requireNonNull(givenPassword);

        User userCached = UserIdentityMap.getInstance().getUser(givenUsername);
        if (userCached != null) {
            return userCached.getPassword().equals(givenPassword);
        }

        Optional<User> userOptionalFromDataSource = userDAO.findUserByUsername(givenUsername);
        if (userOptionalFromDataSource.isPresent()) {
            User userFromDataSource = userOptionalFromDataSource.get();
            if (userFromDataSource.getPassword().equals(givenPassword)) {
                UserIdentityMap.getInstance().addUser(userFromDataSource);
                return true;
            }
        }
        return false;
    }

}
