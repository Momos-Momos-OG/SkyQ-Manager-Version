package skyq.database;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConexionBD {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=skyq_db;encrypt=true;trustServerCertificate=true";
    private static final String USER = getEnvOrDefault("SQLSERVER_USER", "SA");
    private static final String PASSWORD = getEnvOrDefault("SQLSERVER_PASSWORD", "Momos@123");

    private static final int POOL_SIZE = 8;
    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final List<Connection> todasLasConexiones = new ArrayList<>();
    private static boolean inicializado = false;

    private static synchronized void inicializarPool() {
        if (inicializado) {
            return;
        }
        try {
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                pool.add(conn);
                todasLasConexiones.add(conn);
            }
            
            // Ejecutar migración automática de esquema (columna PNR y vuelos)
            if (!todasLasConexiones.isEmpty()) {
                Connection migrationConn = todasLasConexiones.get(0);
                try (java.sql.Statement stmt = migrationConn.createStatement()) {
                    // Migración pasajero
                    stmt.execute("IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.pasajero') AND name = 'pnr') " +
                                "BEGIN ALTER TABLE dbo.pasajero ADD pnr VARCHAR(20) NULL; END");
                    // Migración vuelos (origen y destino)
                    stmt.execute("IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.vuelos') AND name = 'origen') " +
                                "BEGIN ALTER TABLE dbo.vuelos ADD origen VARCHAR(100) NULL, destino VARCHAR(100) NULL; END");
                } catch (SQLException ex) {
                    skyq.logic.LoggerManager.getInstance().logError("Error ejecutando migración de esquema", ex);
                }
            }
            
            inicializado = true;
            // Registrar shutdown hook para cierre físico ordenado
            Runtime.getRuntime().addShutdownHook(new Thread(ConexionBD::cerrarTodoFisicamente));
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error inicializando pool de conexiones", e);
            throw new RuntimeException("No se pudo inicializar el pool de conexiones", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!inicializado) {
            inicializarPool();
        }

        Connection physicalConn;
        try {
            // Obtener de forma bloqueante y thread-safe una conexión del pool
            physicalConn = pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Hilo interrumpido al obtener conexión del pool", e);
        }

        // Validar salud de la conexión física antes de entregarla
        try {
            if (physicalConn.isClosed()) {
                physicalConn = DriverManager.getConnection(URL, USER, PASSWORD);
                synchronized (ConexionBD.class) {
                    todasLasConexiones.add(physicalConn);
                }
            }
        } catch (SQLException e) {
            pool.offer(physicalConn); // Retornarla en caso de error para no vaciar el pool
            throw e;
        }

        final Connection finalPhysicalConn = physicalConn;

        // Retorna un Dynamic Proxy para interceptar close() y devolver la conexión al pool
        return (Connection) Proxy.newProxyInstance(
                ConexionBD.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        releaseConnection(finalPhysicalConn);
                        return null;
                    }
                    try {
                        return method.invoke(finalPhysicalConn, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
        );
    }

    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    pool.offer(connection);
                } else {
                    // Reemplazar conexión dañada por una nueva
                    Connection nuevaConn = DriverManager.getConnection(URL, USER, PASSWORD);
                    synchronized (ConexionBD.class) {
                        todasLasConexiones.add(nuevaConn);
                    }
                    pool.offer(nuevaConn);
                }
            } catch (SQLException e) {
                skyq.logic.LoggerManager.getInstance().logError("Error al liberar/recrear conexión física", e);
            }
        }
    }

    public static synchronized void cerrarTodoFisicamente() {
        if (!inicializado) {
            return;
        }
        for (Connection conn : todasLasConexiones) {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión física en shutdown hook: " + e.getMessage());
                }
            }
        }
        todasLasConexiones.clear();
        pool.clear();
        inicializado = false;
        System.out.println("Pool de conexiones de SkyQ liberado físicamente.");
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close(); // Invoca el proxy, el cual llama a releaseConnection
            } catch (SQLException e) {
                skyq.logic.LoggerManager.getInstance().logError("Error SQL al cerrar proxy de conexión", e);
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