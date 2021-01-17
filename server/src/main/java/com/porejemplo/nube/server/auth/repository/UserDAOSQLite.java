package com.porejemplo.nube.server.auth.repository;

import com.porejemplo.nube.server.auth.entity.User;

import java.sql.*;
import java.util.Optional;

public class UserDAOSQLite implements UserDAO {
    private Connection connection;

    @Override
    public Optional<User> findUserByUsername(String username) {
        String sqlSelectByUsername = String.format("SELECT * FROM user WHERE username='%s';", username);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlSelectByUsername);
            if (rs.next()) {
                return Optional.of(new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            } else {
                System.out.printf("No entries with username %s\n", username);
                return Optional.empty();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server\\mainDb.db");
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
