package skyq.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import skyq.database.ConexionBD;
import skyq.logic.LoggerManager;

public class ConfiguracionDAO {

    public String obtenerDistribucion(String matricula) {
        String sql = "SELECT distribucion_clases FROM configuracion_asientos WHERE matricula = ?";

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("distribucion_clases");
                }
            }
        } catch (SQLException e) {
            LoggerManager.getInstance().logError("ConfiguracionDAO.obtenerDistribucion", e);
        }
        return null;
    }

    public boolean guardarConfiguracion(String matricula, String distribucion) {
        String sql = "INSERT INTO configuracion_asientos (matricula, distribucion_clases) VALUES (?, ?)";

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
            statement.setString(2, distribucion);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.getInstance().logError("ConfiguracionDAO.guardarConfiguracion", e);
        }
        return false;
    }

    public boolean actualizarConfiguracion(String matricula, String distribucion) {
        String sql = "UPDATE configuracion_asientos SET distribucion_clases = ? WHERE matricula = ?";

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, distribucion);
            statement.setString(2, matricula);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.getInstance().logError("ConfiguracionDAO.actualizarConfiguracion", e);
        }
        return false;
    }

    public boolean existeConfiguracion(String matricula) {
        String sql = "SELECT 1 FROM configuracion_asientos WHERE matricula = ?";

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            LoggerManager.getInstance().logError("ConfiguracionDAO.existeConfiguracion", e);
        }
        return false;
    }
}
