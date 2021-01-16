package com.porejemplo.nube.server.auth.service;

import com.porejemplo.nube.server.auth.entity.User;
import com.porejemplo.nube.server.auth.repository.UserDAO;

import java.util.Objects;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    @Override
    public boolean verifyUsernameAndPassword(String givenUsername, String givenPassword) {
        Objects.requireNonNull(givenUsername);
        Objects.requireNonNull(givenPassword);

        Optional<User> userOptional = userDAO.findUserByUsername(givenUsername);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getPassword().equals(givenPassword);
        }
        return false;
    }

}
