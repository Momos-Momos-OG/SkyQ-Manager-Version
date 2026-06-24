package skyq.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import skyq.database.ConexionBD;
import skyq.model.Vuelo;

/**
 * DAO para la tabla 'vuelos'.
 * Aeropuerto Único - SRS v4.0: sin dependencia de pilotos.
 */
public class VueloDAO {

    /**
     * Inserta un nuevo vuelo en la base de datos.
     *
     * @param vuelo Objeto Vuelo con matrícula, codigoVuelo, fechas y estado inicial.
     * @return true si la inserción fue exitosa.
     */
    public boolean insertarVuelo(Vuelo vuelo) {
        String sql = "INSERT INTO vuelos (matricula, codigoVuelo, fechaSalida, fechaArribo, estado, origen, destino) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vuelo.getMatricula());
            stmt.setString(2, vuelo.getCodigoVuelo());
            stmt.setTimestamp(3, Timestamp.valueOf(vuelo.getFechaSalida()));
            stmt.setTimestamp(4, Timestamp.valueOf(vuelo.getFechaArribo()));
            stmt.setString(5, vuelo.getEstado());
            stmt.setString(6, vuelo.getOrigen());
            stmt.setString(7, vuelo.getDestino());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL al insertar vuelo", e);
            return false;
        }
    }

    /**
     * Obtiene todos los vuelos asignados a una matrícula de avión específica.
     *
     * @param matricula Matrícula del avión.
     * @return Lista de vuelos del avión.
     */
    public List<Vuelo> obtenerVuelosPorMatricula(String matricula) {
        List<Vuelo> lista = new ArrayList<>();
        String sql = "SELECT v.idVuelo, v.matricula, v.codigoVuelo, v.fechaSalida, v.fechaArribo, " +
                "v.estado, v.origen, v.destino " +
                "FROM vuelos v " +
                "WHERE v.matricula = ? " +
                "ORDER BY v.fechaSalida DESC";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    /**
     * Obtiene todos los vuelos del sistema con información del avión (join con aviones).
     *
     * @return Lista completa de vuelos.
     */
    public List<Vuelo> obtenerTodosLosVuelos() {
        List<Vuelo> lista = new ArrayList<>();
        String sql = "SELECT v.idVuelo, v.matricula, v.codigoVuelo, v.fechaSalida, v.fechaArribo, " +
                "v.estado, v.origen, v.destino, a.modelo AS modeloAvion " +
                "FROM vuelos v " +
                "INNER JOIN aviones a ON v.matricula = a.matricula " +
                "ORDER BY v.fechaSalida DESC";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vuelo v = mapearResultSet(rs);
                v.setModeloAvion(rs.getString("modeloAvion"));
                lista.add(v);
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    /**
     * Actualiza el estado de un vuelo.
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
     * Elimina un vuelo del sistema.
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
     * Actualiza todos los campos editables de un vuelo.
     *
     * @param vuelo Vuelo con datos actualizados.
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizarVuelo(Vuelo vuelo) {
        String sql = "UPDATE vuelos SET matricula = ?, codigoVuelo = ?, fechaSalida = ?, fechaArribo = ?, estado = ?, origen = ?, destino = ? WHERE idVuelo = ?";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vuelo.getMatricula());
            stmt.setString(2, vuelo.getCodigoVuelo());
            stmt.setTimestamp(3, Timestamp.valueOf(vuelo.getFechaSalida()));
            stmt.setTimestamp(4, Timestamp.valueOf(vuelo.getFechaArribo()));
            stmt.setString(5, vuelo.getEstado());
            stmt.setString(6, vuelo.getOrigen());
            stmt.setString(7, vuelo.getDestino());
            stmt.setInt(8, vuelo.getIdVuelo());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL al actualizar vuelo", e);
            return false;
        }
    }

    /**
     * Verifica si un avión tiene algún vuelo en estado activo.
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
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
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
        v.setEstado(rs.getString("estado"));

        try {
            v.setCodigoVuelo(rs.getString("codigoVuelo"));
        } catch (SQLException ignored) {}

        try {
            v.setOrigen(rs.getString("origen"));
            v.setDestino(rs.getString("destino"));
        } catch (SQLException ignored) {}

        Timestamp tsSalida = rs.getTimestamp("fechaSalida");
        if (tsSalida != null) {
            v.setFechaSalida(tsSalida.toLocalDateTime());
        }

        Timestamp tsArribo = rs.getTimestamp("fechaArribo");
        if (tsArribo != null) {
            v.setFechaArribo(tsArribo.toLocalDateTime());
        }

        return v;
    }
}
