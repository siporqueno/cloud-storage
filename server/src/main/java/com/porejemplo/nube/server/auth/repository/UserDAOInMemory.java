package com.porejemplo.nube.server.auth.repository;

import com.porejemplo.nube.server.auth.entity.User;

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
}
