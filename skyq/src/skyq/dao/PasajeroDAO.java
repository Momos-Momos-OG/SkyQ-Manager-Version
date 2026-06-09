package skyq.dao;

import skyq.database.ConexionBD;
import skyq.model.Pasajero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PasajeroDAO {

    public boolean insertarPasajero(Pasajero pasajero) {
        String sql = "INSERT INTO pasajero (nombre, numAsiento, nivelPrioridad, timestampLlegada) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pasajero.getNombre());
            statement.setString(2, pasajero.getNumAsiento());
            statement.setInt(3, pasajero.getNivelPrioridad());

            LocalDateTime timestampLlegada = pasajero.getTimestampLlegada();
            if (timestampLlegada != null) {
                statement.setTimestamp(4, Timestamp.valueOf(timestampLlegada));
            } else {
                statement.setTimestamp(4, null);
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int insertarPasajeroYObtenerId(Pasajero pasajero) {
        String sql = "INSERT INTO pasajero (nombre, numAsiento, nivelPrioridad, timestampLlegada) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, pasajero.getNombre());
            statement.setString(2, pasajero.getNumAsiento());
            statement.setInt(3, pasajero.getNivelPrioridad());

            LocalDateTime timestampLlegada = pasajero.getTimestampLlegada();
            if (timestampLlegada != null) {
                statement.setTimestamp(4, Timestamp.valueOf(timestampLlegada));
            } else {
                statement.setTimestamp(4, null);
            }

            int filasAfectadas = statement.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Pasajero> obtenerPasajerosVuelo() {
        List<Pasajero> pasajeros = new ArrayList<>();
        String sql = "SELECT idPasajero, nombre, numAsiento, nivelPrioridad, timestampLlegada FROM pasajero ORDER BY idPasajero";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Pasajero pasajero = new Pasajero();
                pasajero.setIdPasajero(resultSet.getInt("idPasajero"));
                pasajero.setNombre(resultSet.getString("nombre"));
                pasajero.setNumAsiento(resultSet.getString("numAsiento"));
                pasajero.setNivelPrioridad(resultSet.getInt("nivelPrioridad"));

                Timestamp timestamp = resultSet.getTimestamp("timestampLlegada");
                if (timestamp != null) {
                    pasajero.setTimestampLlegada(timestamp.toLocalDateTime());
                }

                pasajeros.add(pasajero);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pasajeros;
    }

    public boolean verificarAsientoOcupado(String asiento) {
        String sql = "SELECT COUNT(*) FROM pasajero WHERE numAsiento = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, asiento);

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