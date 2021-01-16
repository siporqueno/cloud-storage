package com.porejemplo.nube.server.auth.repository;

import com.porejemplo.nube.server.auth.entity.User;

import java.sql.*;
import java.util.Optional;

public class UserDAO {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement pstmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server\\mainDb.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Optional<User> findUserByUsername(String usernm) {
        String sqlSelectByUsername = String.format("SELECT * FROM user WHERE username='%s';", usernm);
        try {
            ResultSet rs = stmt.executeQuery(sqlSelectByUsername);
            if (rs.next()) {
                return Optional.of(new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            } else {
                System.out.printf("No entries with username %s\n", usernm);
                return Optional.empty();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
