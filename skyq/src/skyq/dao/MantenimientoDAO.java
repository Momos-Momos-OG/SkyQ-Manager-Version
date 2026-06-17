package skyq.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import skyq.database.ConexionBD;
import skyq.model.Mantenimiento;

public class MantenimientoDAO {

    public boolean insertar(Mantenimiento m) {
        String sql = "INSERT INTO mantenimiento (matricula, fechaInicio, fechaFin, descripcion, estado) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getMatricula());
            ps.setDate(2, Date.valueOf(m.getFechaInicio()));
            if (m.getFechaFin() != null) {
                ps.setDate(3, Date.valueOf(m.getFechaFin()));
            } else {
                ps.setDate(3, null);
            }
            ps.setString(4, m.getDescripcion());
            ps.setString(5, m.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return false;
    }

    public List<Mantenimiento> obtenerPorMatricula(String matricula) {
        List<Mantenimiento> lista = new ArrayList<>();
        String sql = "SELECT idMantenimiento, matricula, fechaInicio, fechaFin, descripcion, estado " +
                "FROM mantenimiento WHERE matricula = ? ORDER BY fechaInicio DESC";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate fechaFinVal = null;
                    Date fechaFinSql = rs.getDate("fechaFin");
                    if (fechaFinSql != null) {
                        fechaFinVal = fechaFinSql.toLocalDate();
                    }
                    Mantenimiento m = new Mantenimiento(
                            rs.getInt("idMantenimiento"),
                            rs.getString("matricula"),
                            rs.getDate("fechaInicio").toLocalDate(),
                            fechaFinVal,
                            rs.getString("descripcion"),
                            rs.getString("estado"));
                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    public List<Mantenimiento> obtenerMantenimientoPorMatricula(String matricula) {
        return obtenerPorMatricula(matricula);
    }

    public List<Mantenimiento> obtenerTodos() {
        List<Mantenimiento> lista = new ArrayList<>();
        String sql = "SELECT idMantenimiento, matricula, fechaInicio, fechaFin, descripcion, estado " +
                "FROM mantenimiento ORDER BY fechaInicio DESC";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate fechaFinVal = null;
                Date fechaFinSql = rs.getDate("fechaFin");
                if (fechaFinSql != null) {
                    fechaFinVal = fechaFinSql.toLocalDate();
                }
                Mantenimiento m = new Mantenimiento(
                        rs.getInt("idMantenimiento"),
                        rs.getString("matricula"),
                        rs.getDate("fechaInicio").toLocalDate(),
                        fechaFinVal,
                        rs.getString("descripcion"),
                        rs.getString("estado")
                    );
                lista.add(m);
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return lista;
    }

    public boolean actualizarEstado(int idMantenimiento, String nuevoEstado) {
        String sql = "UPDATE mantenimiento SET estado = ? WHERE idMantenimiento = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMantenimiento);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return false;
    }

    public boolean actualizarFechaFin(int idMantenimiento, LocalDate fechaFin) {
        String sql = "UPDATE mantenimiento SET fechaFin = ? WHERE idMantenimiento = ?";
        try (Connection conn = ConexionBD.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaFin));
            ps.setInt(2, idMantenimiento);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return false;
    }

    public boolean estaEnMantenimiento(String matricula, java.util.Date fechaVuelo) {
        String sql = "SELECT COUNT(*) FROM mantenimiento WHERE matricula = ? AND ? >= fechaInicio AND (? <= fechaFin OR fechaFin IS NULL)";
        try (Connection conn = ConexionBD.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setDate(2, new java.sql.Date(fechaVuelo.getTime()));
            ps.setDate(3, new java.sql.Date(fechaVuelo.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL al verificar mantenimiento", e);
        }
        return false;
    }
}

