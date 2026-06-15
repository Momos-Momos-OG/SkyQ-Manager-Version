package skyq.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=skyq_db;encrypt=true;trustServerCertificate=true";
    private static final String USER = getEnvOrDefault("SQLSERVER_USER", "SA");
    private static final String PASSWORD = getEnvOrDefault("SQLSERVER_PASSWORD", "Momos@123");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            }
        }
    }

    private static String getEnvOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value;
    }
}