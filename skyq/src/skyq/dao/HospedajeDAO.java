package skyq.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import skyq.database.ConexionBD;
import skyq.model.Hospedaje;

/**
 * DAO para la tabla 'hospedaje_piloto'.
 * Versión básica demostrativa para la App Manager.
 * La App Piloto también puede usar estos métodos de consulta.
 */
public class HospedajeDAO {

    /**
     * Registra un nuevo hospedaje para un piloto.
     *
     * Objeto con idPiloto, hotel, ciudad y fechas.
     * Retorna true si el registro fue exitoso.
     */
    public boolean insertarHospedaje(Hospedaje hospedaje) {
        String sql = "INSERT INTO hospedaje_piloto (idPiloto, hotel, ciudad, fechaIngreso, fechaSalida) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hospedaje.getIdPiloto());
            stmt.setString(2, hospedaje.getHotel());
            stmt.setString(3, hospedaje.getCiudad());
            stmt.setTimestamp(4, Timestamp.valueOf(hospedaje.getFechaIngreso()));
            stmt.setTimestamp(5, Timestamp.valueOf(hospedaje.getFechaSalida()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Obtiene todos los hospedajes registrados de un piloto específico.
     * Ordenados por fecha de ingreso descendente (el más reciente primero).
     *
     * ID del piloto a consultar.
     * Retorna la lista de hospedajes del piloto.
     */
    public List<Hospedaje> obtenerHospedajesPorPiloto(int idPiloto) {
        List<Hospedaje> lista = new ArrayList<>();
        String sql = "SELECT idHospedaje, idPiloto, hotel, ciudad, fechaIngreso, fechaSalida " +
                "FROM hospedaje_piloto WHERE idPiloto = ? ORDER BY fechaIngreso DESC";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPiloto);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Hospedaje h = new Hospedaje();
                    h.setIdHospedaje(rs.getInt("idHospedaje"));
                    h.setIdPiloto(rs.getInt("idPiloto"));
                    h.setHotel(rs.getString("hotel"));
                    h.setCiudad(rs.getString("ciudad"));

                    Timestamp tsIngreso = rs.getTimestamp("fechaIngreso");
                    if (tsIngreso != null) {
                        h.setFechaIngreso(tsIngreso.toLocalDateTime());
                    }

                    Timestamp tsSalida = rs.getTimestamp("fechaSalida");
                    if (tsSalida != null) {
                        h.setFechaSalida(tsSalida.toLocalDateTime());
                    }

                    lista.add(h);
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    /**
     * Elimina un registro de hospedaje por su ID.
     *
     * ID del hospedaje a eliminar.
     * retorna true si la eliminación fue exitosa.
     */
    public boolean eliminarHospedaje(int idHospedaje) {
        String sql = "DELETE FROM hospedaje_piloto WHERE idHospedaje = ?";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idHospedaje);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }
}
