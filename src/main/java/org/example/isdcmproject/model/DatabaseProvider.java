package org.example.isdcmproject.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLException;

public final class DatabaseProvider {
    private static final String CLIENT_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String NETWORK_URL = "jdbc:derby://localhost:1527/pr2;create=true";
    private static final String EMBEDDED_URL = "jdbc:derby:/Users/zhiweilin/MEI/ISDCM/Project/pr2;create=true";

    static {
        try {
            Class.forName(CLIENT_DRIVER);
            Class.forName(EMBEDDED_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo cargar el driver de Derby.", e);
        }
    }

    private DatabaseProvider() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(NETWORK_URL);
        } catch (SQLNonTransientConnectionException e) {
            return DriverManager.getConnection(EMBEDDED_URL);
        }
    }
}
