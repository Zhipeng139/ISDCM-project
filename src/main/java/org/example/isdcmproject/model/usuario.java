package org.example.isdcmproject.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLException;

public class usuario {
    private static final String CLIENT_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String NETWORK_URL = "jdbc:derby://localhost:1527/pr2;create=true";
    private static final String EMBEDDED_URL = "jdbc:derby:pr2;create=true";

    static {
        try {
            Class.forName(CLIENT_DRIVER);
            Class.forName(EMBEDDED_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo cargar el driver de Derby.", e);
        }
    }

    public usuario() {
    }

    private Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(NETWORK_URL);
        } catch (SQLNonTransientConnectionException e) {
            return DriverManager.getConnection(EMBEDDED_URL);
        }
    }

    public void initializeTable() throws SQLException {
        String sql = """
                CREATE TABLE usuarios (
                    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    nombre VARCHAR(80) NOT NULL,
                    apellido VARCHAR(80) NOT NULL,
                    email VARCHAR(120) NOT NULL,
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

    public boolean createUser(String nombre, String apellido, String email, String username, String rawPassword) throws SQLException {
        if (existsByUsername(username)) {
            return false;
        }
        String sql = "INSERT INTO usuarios (nombre, apellido, email, username, password_hash) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, email);
            statement.setString(4, username);
            statement.setString(5, hashPassword(rawPassword));
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return false;
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
}
