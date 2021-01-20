package com.porejemplo.nube.server.auth.repository;

import com.porejemplo.nube.server.auth.entity.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {

    Optional<User> findUserByUsername(String username);

    void connect() throws SQLException;

    void disconnect();

}
