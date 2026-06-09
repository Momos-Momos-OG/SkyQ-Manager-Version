package skyq.dao;

import skyq.database.ConexionBD;
import skyq.model.Equipaje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EquipajeDAO {

    public boolean registrarEquipaje(Equipaje equipaje) {
        String sql = "INSERT INTO equipaje (idPasajero, peso, estado) VALUES (?, ?, ?)";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, equipaje.getIdPasajero());
            statement.setDouble(2, equipaje.getPeso());
            statement.setString(3, equipaje.getEstado());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}