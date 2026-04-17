package org.example.isdcmproject.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class usuario {
    public enum CreateUserResult {
        SUCCESS,
        USERNAME_TAKEN,
        EMAIL_TAKEN,
        DATABASE_ERROR,
        ERROR
    }

    public usuario() {
    }

    private Connection getConnection() throws SQLException {
        return DatabaseProvider.getConnection();
    }

    public void initializeTable() throws SQLException {
        String sql = """
                CREATE TABLE usuarios (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    nombre VARCHAR(80) NOT NULL,
                    apellido VARCHAR(80) NOT NULL,
                    email VARCHAR(120) NOT NULL UNIQUE,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(64) NOT NULL
                )
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                throw e;
            }
        }
    }

    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE username = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public CreateUserResult createUser(String nombre, String apellido, String email, String username, String rawPassword) throws SQLException {

        nombre = nombre.toLowerCase();
        apellido = apellido.toLowerCase();
        email = email.toLowerCase();
        username = username.toLowerCase();

        if (existsByUsername(username)) {
            return CreateUserResult.USERNAME_TAKEN;
        }
        if (existByEmail(email)) {
            return CreateUserResult.EMAIL_TAKEN;
        }
        String sql = "INSERT INTO usuarios (nombre, apellido, email, username, password_hash) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, email);
            statement.setString(4, username);
            statement.setString(5, hashPassword(rawPassword));
            if (statement.executeUpdate() == 1) {
                return CreateUserResult.SUCCESS;
            } return CreateUserResult.ERROR;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return CreateUserResult.DATABASE_ERROR;
            }
            throw e;
        }
    }

    public boolean validateCredentials(String username, String rawPassword) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE username = ? AND password_hash = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, hashPassword(rawPassword));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo aplicar hash a la contraseña.", e);
        }
    }

    public boolean existByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
