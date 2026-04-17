package org.example.isdcmproject.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class videoRepository {
    private static final Logger LOGGER = Logger.getLogger(videoRepository.class.getName());

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
                    categoria VARCHAR(120) NOT NULL,
                    resolucion VARCHAR(40) NOT NULL
                )
                """;
        try (Connection connection = DatabaseProvider.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                if (!"X0Y32".equals(e.getSQLState())) {
                    throw e;
                }
            }
            dropTagColumnIfExists(connection);
            ensureColumn(connection, "VIDEOS", "RESOLUCION", "VARCHAR(40) NOT NULL DEFAULT ''");
            createIndexIfMissing(connection, "IDX_VIDEOS_TITULO", "CREATE INDEX IDX_VIDEOS_TITULO ON videos (titulo)");
            createIndexIfMissing(connection, "IDX_VIDEOS_CATEGORIA", "CREATE INDEX IDX_VIDEOS_CATEGORIA ON videos (categoria)");
            createIndexIfMissing(connection, "IDX_VIDEOS_FECHA", "CREATE INDEX IDX_VIDEOS_FECHA ON videos (fecha_creacion)");
            createIndexIfMissing(connection, "IDX_VIDEOS_DURACION", "CREATE INDEX IDX_VIDEOS_DURACION ON videos (duracion)");
            createIndexIfMissing(connection, "IDX_VIDEOS_RESOLUCION", "CREATE INDEX IDX_VIDEOS_RESOLUCION ON videos (resolucion)");
        }
    }

    public SaveVideoResult save(video video) throws SQLException {
        String sql = "INSERT INTO videos (id, titulo, fecha_creacion, duracion, reproducciones, descripcion, formato, url, categoria, resolucion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            statement.setString(10, video.getResolucion());
            statement.executeUpdate();
            return SaveVideoResult.SUCCESS;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return SaveVideoResult.DUPLICATE_ID;
            }
            LOGGER.log(Level.SEVERE, "Error al guardar video", e);
            throw e;
        }
    }

    public List<video> findAll() throws SQLException {
        String sql = "SELECT id, titulo, fecha_creacion, duracion, reproducciones, descripcion, formato, url, categoria, resolucion FROM videos ORDER BY fecha_creacion DESC, titulo ASC";
        List<video> videos = new ArrayList<>();
        try (Connection connection = DatabaseProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            mapRows(videos, resultSet);
        }
        return videos;
    }

    public boolean existsById(String videoId) throws SQLException {
        String sql = "SELECT 1 FROM videos WHERE id = ?";
        try (Connection connection = DatabaseProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, videoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<video> findByFilters(videoSearchCriteria criteria) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, titulo, fecha_creacion, duracion, reproducciones, descripcion, formato, url, categoria, resolucion FROM videos WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(criteria, sql, params);
        sql.append(" ORDER BY fecha_creacion DESC FETCH FIRST 500 ROWS ONLY");
        List<video> videos = new ArrayList<>();
        try (Connection connection = DatabaseProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                mapRows(videos, resultSet);
            }
        }
        return videos;
    }

    public List<String> suggestTitles(String term, int limit) throws SQLException {
        String sql = "SELECT titulo FROM videos WHERE LOWER(titulo) LIKE ? ORDER BY titulo ASC";
        List<String> suggestions = new ArrayList<>();
        try (Connection connection = DatabaseProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setMaxRows(Math.max(1, limit));
            statement.setString(1, "%" + term.toLowerCase() + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suggestions.add(resultSet.getString("titulo"));
                }
            }
        }
        return suggestions;
    }

    private void appendFilters(videoSearchCriteria criteria, StringBuilder sql, List<Object> params) {
        if (!criteria.getTitulo().isBlank()) {
            sql.append(" AND LOWER(titulo) LIKE ?");
            params.add("%" + criteria.getTitulo().toLowerCase() + "%");
        }
        if (!criteria.getCategoria().isBlank()) {
            sql.append(" AND LOWER(categoria) LIKE ?");
            params.add("%" + criteria.getCategoria().toLowerCase() + "%");
        }
        if (!criteria.getResolucion().isBlank()) {
            sql.append(" AND LOWER(resolucion) LIKE ?");
            params.add("%" + criteria.getResolucion().toLowerCase() + "%");
        }
        if (criteria.getFechaDesde() != null) {
            sql.append(" AND fecha_creacion >= ?");
            params.add(Date.valueOf(criteria.getFechaDesde()));
        }
        if (criteria.getFechaHasta() != null) {
            sql.append(" AND fecha_creacion <= ?");
            params.add(Date.valueOf(criteria.getFechaHasta()));
        }
        if (criteria.getDuracionMin() != null) {
            sql.append(" AND duracion >= ?");
            params.add(criteria.getDuracionMin());
        }
        if (criteria.getDuracionMax() != null) {
            sql.append(" AND duracion <= ?");
            params.add(criteria.getDuracionMax());
        }
        if (!criteria.getConsultaLibre().isBlank()) {
            sql.append(" AND (LOWER(titulo) LIKE ? OR LOWER(descripcion) LIKE ? OR LOWER(categoria) LIKE ?)");
            String token = "%" + criteria.getConsultaLibre().toLowerCase() + "%";
            params.add(token);
            params.add(token);
            params.add(token);
        }
    }

    private void bindParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer number) {
                statement.setInt(i + 1, number);
            } else if (value instanceof Date date) {
                statement.setDate(i + 1, date);
            } else {
                statement.setString(i + 1, String.valueOf(value));
            }
        }
    }

    private void mapRows(List<video> videos, ResultSet resultSet) throws SQLException {
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
            video.setResolucion(resultSet.getString("resolucion"));
            videos.add(video);
        }
    }

    private void dropTagColumnIfExists(Connection connection) throws SQLException {
        if (!hasColumn(connection, "VIDEOS", "TAGS")) {
            return;
        }
        try (PreparedStatement alter = connection.prepareStatement("ALTER TABLE videos DROP COLUMN tags")) {
            alter.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "No se pudo eliminar la columna TAGS: {0}", e.getMessage());
        }
    }

    private void ensureColumn(Connection connection, String tableName, String columnName, String definition) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                return;
            }
        }
        try (PreparedStatement alter = connection.prepareStatement("ALTER TABLE videos ADD COLUMN " + columnName + " " + definition)) {
            alter.executeUpdate();
        }
    }

    private void createIndexIfMissing(Connection connection, String indexName, String createSql) throws SQLException {
        if (hasIndex(connection, indexName)) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(createSql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, "No se pudo crear índice {0}: {1}", new Object[]{indexName, e.getMessage()});
        }
    }

    private boolean hasIndex(Connection connection, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(null, null, "VIDEOS", false, false)) {
            while (indexes.next()) {
                String currentIndex = indexes.getString("INDEX_NAME");
                if (indexName.equalsIgnoreCase(currentIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }
}
