package skyq.dao;

import skyq.database.ConexionBD;
import skyq.model.Avion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AvionDAO {

    public boolean guardarAvion(Avion avion) {
        String sql = "INSERT INTO aviones (matricula, modelo, capacidad, estado) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, avion.getMatricula());
            statement.setString(2, avion.getModelo());
            statement.setInt(3, avion.getCapacidad());
            statement.setString(4, avion.getEstado());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
            e.printStackTrace();
        }

        return false;
    }
}