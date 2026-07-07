package skyq.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import skyq.database.ConexionBD;
import skyq.model.Avion;
import skyq.model.EstadoAvion;

/**
 * DAO para gestionar la persistencia y consulta de la flota de aviones.
 */
public class AvionDAO {

    /**
     * Guarda un nuevo avión en la base de datos.
     */
    public boolean guardarAvion(Avion avion) {
        String sql = "INSERT INTO aviones (matricula, modelo, capacidad, estado) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, avion.getMatricula());
            statement.setString(2, avion.getModelo());
            statement.setInt(3, avion.getCapacidad());
            statement.setString(4, avion.getEstado().name());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Verifica si una matrícula de avión específica ya existe registrada en el sistema.
     */
    public boolean verificarMatriculaRegistrada(String matricula) {
        String sql = "SELECT COUNT(*) FROM aviones WHERE matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return false;
    }

    /**
     * Obtiene la lista completa de todos los aviones en la flota.
     */
    public List<Avion> obtenerAvionesFlota() {
        List<Avion> lista = new ArrayList<>();
        String sql = "SELECT matricula, modelo, capacidad, estado FROM aviones";
        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                lista.add(new Avion(
                        resultSet.getString("matricula"),
                        resultSet.getString("modelo"),
                        resultSet.getInt("capacidad"),
                        EstadoAvion.valueOf(resultSet.getString("estado"))
                ));
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    /**
     * Actualiza la información (modelo, capacidad, estado) de un avión existente.
     */
    public boolean actualizarAvion(Avion avion) {
        String sql = "UPDATE aviones SET modelo = ?, capacidad = ?, estado = ? WHERE matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, avion.getModelo());
            statement.setInt(2, avion.getCapacidad());
            statement.setString(3, avion.getEstado().name());
            statement.setString(4, avion.getMatricula());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Actualiza específicamente la capacidad máxima de pasajeros de un avión.
     */
    public boolean actualizarCapacidad(String matricula, int nuevaCapacidad) {
        String sql = "UPDATE aviones SET capacidad = ? WHERE matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nuevaCapacidad);
            statement.setString(2, matricula);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Obtiene el estado actual (EN_TERMINAL, EN_VUELO, EN_MANTENIMIENTO) de un avión por matrícula.
     */
    public EstadoAvion obtenerEstado(String matricula) {
        String sql = "SELECT estado FROM aviones WHERE matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EstadoAvion.valueOf(resultSet.getString("estado"));
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL al obtener estado del avion", e);
        }
        return null;
    }
}
