package org.example.isdcmproject.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class videoRepository {
    public enum SaveVideoResult {
        SUCCESS,
        DUPLICATE_ID,
        ERROR
    }

    public void initializeTable() throws SQLException {
        String sql = """
                CREATE TABLE videos (
                    id VARCHAR(80) PRIMARY KEY,
                    titulo VARCHAR(150) NOT NULL,
                    fecha_creacion DATE NOT NULL,
                    duracion INT NOT NULL,
                    reproducciones INT NOT NULL,
                    descripcion VARCHAR(800) NOT NULL,
                    formato VARCHAR(40) NOT NULL,
                    url VARCHAR(400) NOT NULL,
                    categoria VARCHAR(120) NOT NULL
                )
                """;
        try (Connection connection = DatabaseProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                throw e;
            }
        }
    }

    public SaveVideoResult save(video video) throws SQLException {
        String sql = "INSERT INTO videos (id, titulo, fecha_creacion, duracion, reproducciones, descripcion, formato, url, categoria) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, video.getIdentificador());
            statement.setString(2, video.getTitulo());
            statement.setDate(3, Date.valueOf(video.getFechaCreacion()));
            statement.setInt(4, video.getDuracion());
            statement.setInt(5, video.getReproducciones());
            statement.setString(6, video.getDescripcion());
            statement.setString(7, video.getFormato());
            statement.setString(8, video.getUrl());
            statement.setString(9, video.getCategoria());
            statement.executeUpdate();
            return SaveVideoResult.SUCCESS;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return SaveVideoResult.DUPLICATE_ID;
            }
            throw e;
        }
    }

    public List<video> findAll() throws SQLException {
        String sql = "SELECT id, titulo, fecha_creacion, duracion, reproducciones, descripcion, formato, url, categoria FROM videos ORDER BY fecha_creacion DESC, titulo ASC";
        List<video> videos = new ArrayList<>();
        try (Connection connection = DatabaseProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                video video = new video();
                video.setIdentificador(resultSet.getString("id"));
                video.setTitulo(resultSet.getString("titulo"));
                video.setFechaCreacion(resultSet.getDate("fecha_creacion").toLocalDate());
                video.setDuracion(resultSet.getInt("duracion"));
                video.setReproducciones(resultSet.getInt("reproducciones"));
                video.setDescripcion(resultSet.getString("descripcion"));
                video.setFormato(resultSet.getString("formato"));
                video.setUrl(resultSet.getString("url"));
                video.setCategoria(resultSet.getString("categoria"));
                videos.add(video);
            }
        }
        return videos;
    }
}
