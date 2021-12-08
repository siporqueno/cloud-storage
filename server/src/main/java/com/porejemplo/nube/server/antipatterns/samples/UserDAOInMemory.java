package com.porejemplo.nube.server.antipatterns.samples;

import com.porejemplo.nube.server.auth.entity.User;
import com.porejemplo.nube.server.auth.repository.UserDAO;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserDAOInMemory implements UserDAO {

    private final Map<String, User> users;

    public UserDAOInMemory() {
        this.users = new HashMap<>();
        users.put("user1", new User(0, "user1", "pass1", "nick1"));
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return Optional.of(users.get(username));
    }

    @Override
    public void connect() throws SQLException {

    }

    @Override
    public void disconnect() {

    }
}
