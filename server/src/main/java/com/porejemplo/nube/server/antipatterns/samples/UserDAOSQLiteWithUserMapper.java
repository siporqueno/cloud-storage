package com.porejemplo.nube.server.auth.repository;

import com.porejemplo.nube.server.auth.entity.User;

import java.nio.file.Paths;
import java.sql.*;
import java.util.Optional;

public class UserDAOSQLiteWithUserMapper implements UserDAO {
    private Connection connection;
    private UserMapper userMapper;

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userMapper.findUserByUsername(username);
    }

    @Override
    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", Paths.get("server", "mainDb.db")));
            userMapper = new UserMapper(this.connection);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
