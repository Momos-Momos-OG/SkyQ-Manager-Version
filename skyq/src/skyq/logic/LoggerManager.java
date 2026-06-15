package skyq.logic;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerManager {
    private static volatile LoggerManager instancia;
    private static final Logger LOGGER = Logger.getLogger(LoggerManager.class.getName());

    private LoggerManager() {
        try {
            FileHandler fh = new FileHandler("skyq_errors.log", true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Error configurando FileHandler: " + e.getMessage());
        }
    }

    public static LoggerManager getInstance() {
        if (instancia == null) {
            synchronized (LoggerManager.class) {
                if (instancia == null) {
                    instancia = new LoggerManager();
                }
            }
        }
        return instancia;
    }

    public void logError(String contexto, Exception e) {
        String mensaje = String.format("[%s] %s: %s", contexto, e.getClass().getSimpleName(), e.getMessage());
        LOGGER.log(Level.SEVERE, mensaje);
        if (e.getCause() != null) {
            LOGGER.log(Level.SEVERE, "Causa: {0}", e.getCause().getMessage());
        }
    }

    public void logInfo(String mensaje) {
        LOGGER.info(mensaje);
    }

    public void logWarning(String mensaje) {
        LOGGER.warning(mensaje);
    }
}
