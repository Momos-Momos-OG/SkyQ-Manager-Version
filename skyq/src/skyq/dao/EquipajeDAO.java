package skyq.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import skyq.database.ConexionBD;
import skyq.model.Equipaje;

/**
 * DAO para la gestión de la tabla 'equipaje' en la base de datos.
 */
public class EquipajeDAO {

    /**
     * Registra una nueva pieza de equipaje vinculada a un pasajero.
     */
    public boolean registrarEquipaje(Equipaje equipaje) {
        String sql = "INSERT INTO equipaje (idPasajero, peso, estado) VALUES (?, ?, ?)";

        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, equipaje.getIdPasajero());
            statement.setDouble(2, equipaje.getPeso());
            statement.setString(3, equipaje.getEstado());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Obtiene la lista de equipajes registrados para todos los pasajeros de un vuelo específico.
     */
    public List<Equipaje> obtenerEquipajePorVuelo(String matricula) {
        List<Equipaje> lista = new ArrayList<>();
        String sql = "SELECT e.idMaleta, e.idPasajero, e.peso, e.estado " +
                     "FROM equipaje e " +
                     "INNER JOIN pasajero p ON e.idPasajero = p.idPasajero " +
                     "WHERE p.matricula = ?";
        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, matricula);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Equipaje(
                        rs.getInt("idMaleta"),
                        rs.getInt("idPasajero"),
                        rs.getDouble("peso"),
                        rs.getString("estado")
                    ));
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL al obtener equipaje por vuelo", e);
        }
        return lista;
    }
}
