package com.porejemplo.nube.server.auth.repository;

import com.porejemplo.nube.server.auth.entity.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserMapper {
    private final Connection connection;

    public UserMapper(Connection connection) {
        this.connection = connection;
    }

    Optional<User> findUserByUsername(String username){
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

        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return Optional.empty();
    }
}
