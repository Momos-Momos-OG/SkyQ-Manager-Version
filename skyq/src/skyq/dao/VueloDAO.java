package skyq.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import skyq.database.ConexionBD;
import skyq.model.Vuelo;

/**
 * DAO para la tabla 'vuelos'.
 * Compartido por las 3 aplicaciones del ecosistema SkyQ.
 * Maneja la asignación de pilotos a aviones y el ciclo de vida del vuelo.
 */
public class VueloDAO {

    /**
     * Inserta un nuevo vuelo en la base de datos.
     * Antes de llamar este método, verificar que el piloto y el avión estén disponibles.
     *
     * @param vuelo Objeto Vuelo con matrícula, idPiloto, fechas y estado inicial.
     * @return true si la inserción fue exitosa.
     */
    public boolean insertarVuelo(Vuelo vuelo) {
        String sql = "INSERT INTO vuelos (matricula, idPiloto, fechaSalida, fechaRegreso, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vuelo.getMatricula());
            stmt.setInt(2, vuelo.getIdPiloto());
            stmt.setTimestamp(3, Timestamp.valueOf(vuelo.getFechaSalida()));
            stmt.setTimestamp(4, Timestamp.valueOf(vuelo.getFechaRegreso()));
            stmt.setString(5, vuelo.getEstado());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Obtiene todos los vuelos asignados a un piloto específico,
     * incluyendo el modelo del avión (join con tabla aviones).
     * Usada por App Manager y App Piloto.
     *
     * @param idPiloto ID del piloto a consultar.
     * @return Lista de vuelos del piloto ordenados por fecha de salida.
     */
    public List<Vuelo> obtenerVuelosPorPiloto(int idPiloto) {
        List<Vuelo> lista = new ArrayList<>();
        String sql = "SELECT v.idVuelo, v.matricula, v.idPiloto, v.fechaSalida, v.fechaRegreso, " +
                "v.estado, a.modelo AS modeloAvion " +
                "FROM vuelos v " +
                "INNER JOIN aviones a ON v.matricula = a.matricula " +
                "WHERE v.idPiloto = ? " +
                "ORDER BY v.fechaSalida DESC";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPiloto);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vuelo v = mapearResultSet(rs);
                    v.setModeloAvion(rs.getString("modeloAvion"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    /**
     * Obtiene todos los vuelos asignados a una matrícula de avión específica.
     * Usada por App Manager (verificar si un avión está ocupado).
     *
     * @param matricula Matrícula del avión.
     * @return Lista de vuelos del avión.
     */
    public List<Vuelo> obtenerVuelosPorMatricula(String matricula) {
        List<Vuelo> lista = new ArrayList<>();
        String sql = "SELECT v.idVuelo, v.matricula, v.idPiloto, v.fechaSalida, v.fechaRegreso, " +
                "v.estado, p.nombre AS nombrePiloto " +
                "FROM vuelos v " +
                "INNER JOIN pilotos p ON v.idPiloto = p.idPiloto " +
                "WHERE v.matricula = ? " +
                "ORDER BY v.fechaSalida DESC";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vuelo v = mapearResultSet(rs);
                    v.setNombrePiloto(rs.getString("nombrePiloto"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    /**
     * Obtiene todos los vuelos del sistema con información completa (join aviones + pilotos).
     * Usada por el panel de timeline del Manager.
     *
     * @return Lista completa de vuelos.
     */
    public List<Vuelo> obtenerTodosLosVuelos() {
        List<Vuelo> lista = new ArrayList<>();
        String sql = "SELECT v.idVuelo, v.matricula, v.idPiloto, v.fechaSalida, v.fechaRegreso, " +
                "v.estado, p.nombre AS nombrePiloto, a.modelo AS modeloAvion " +
                "FROM vuelos v " +
                "INNER JOIN pilotos p ON v.idPiloto = p.idPiloto " +
                "INNER JOIN aviones a ON v.matricula = a.matricula " +
                "ORDER BY v.fechaSalida DESC";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vuelo v = mapearResultSet(rs);
                v.setNombrePiloto(rs.getString("nombrePiloto"));
                v.setModeloAvion(rs.getString("modeloAvion"));
                lista.add(v);
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    /**
     * Actualiza el estado de un vuelo (ej: 'Programado' → 'En Vuelo' → 'Completado').
     *
     * @param idVuelo ID del vuelo a actualizar.
     * @param nuevoEstado Nuevo estado a asignar.
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizarEstadoVuelo(int idVuelo, String nuevoEstado) {
        String sql = "UPDATE vuelos SET estado = ? WHERE idVuelo = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idVuelo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Elimina un vuelo del sistema (solo disponible para estado 'Programado' o 'Cancelado').
     *
     * @param idVuelo ID del vuelo a eliminar.
     * @return true si la eliminación fue exitosa.
     */
    public boolean eliminarVuelo(int idVuelo) {
        String sql = "DELETE FROM vuelos WHERE idVuelo = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVuelo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
            return false;
        }
    }

    /**
     * Verifica si un piloto tiene algún vuelo en estado 'En Vuelo' o 'Programado'.
     * Usado para bloquear reasignaciones conflictivas.
     *
     * @param idPiloto ID del piloto.
     * @return true si el piloto tiene vuelos activos.
     */
    public boolean pilotoTieneVueloActivo(int idPiloto) {
        String sql = "SELECT COUNT(*) FROM vuelos WHERE idPiloto = ? AND estado IN ('En Vuelo', 'Programado')";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPiloto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return false;
    }

    /**
     * Verifica si un avión tiene algún vuelo en estado activo.
     * Usado antes de asignar un vuelo nuevo a una aeronave.
     *
     * @param matricula Matrícula del avión.
     * @return true si el avión está ocupado en un vuelo activo.
     */
    public boolean avionTieneVueloActivo(String matricula) {
        String sql = "SELECT COUNT(*) FROM vuelos WHERE matricula = ? AND estado IN ('En Vuelo', 'Programado')";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return false;
    }

    // ----------------------------------------------------------------
    // Método privado auxiliar: mapea un ResultSet a un objeto Vuelo
    // ----------------------------------------------------------------
    private Vuelo mapearResultSet(ResultSet rs) throws SQLException {
        Vuelo v = new Vuelo();
        v.setIdVuelo(rs.getInt("idVuelo"));
        v.setMatricula(rs.getString("matricula"));
        v.setIdPiloto(rs.getInt("idPiloto"));
        v.setEstado(rs.getString("estado"));

        Timestamp tsSalida = rs.getTimestamp("fechaSalida");
        if (tsSalida != null) v.setFechaSalida(tsSalida.toLocalDateTime());

        Timestamp tsRegreso = rs.getTimestamp("fechaRegreso");
        if (tsRegreso != null) v.setFechaRegreso(tsRegreso.toLocalDateTime());

        return v;
    }
}
