package com.porejemplo.nube.server.auth.service;

import com.porejemplo.nube.server.auth.entity.User;
import com.porejemplo.nube.server.auth.repository.UserDAO;

import java.sql.SQLException;

public class AuthService {
    public static void connect() {
        try {
            UserDAO.connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Attempt to connect to database failed.");
        }
    }

    public static boolean verifyUsernameAndPassword(String givenUsername, String givenPassword) {
        User user = UserDAO.findUserByUsername(givenUsername).orElse(new User(314159, givenUsername+"difference","mockpass", "mocknick"));
        return user.getUsername().equals(givenUsername) && user.getPassword().equals(givenPassword);
    }

    public static void disconnect() {
        UserDAO.disconnect();
    }
}
