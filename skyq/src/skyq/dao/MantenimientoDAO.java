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
            ps.setDate(3, m.getFechaFin() != null ? Date.valueOf(m.getFechaFin()) : null);
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
                    Mantenimiento m = new Mantenimiento(
                            rs.getInt("idMantenimiento"),
                            rs.getString("matricula"),
                            rs.getDate("fechaInicio").toLocalDate(),
                            rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null,
                            rs.getString("descripcion"),
                            rs.getString("estado")
                    );
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
                Mantenimiento m = new Mantenimiento(
                        rs.getInt("idMantenimiento"),
                        rs.getString("matricula"),
                        rs.getDate("fechaInicio").toLocalDate(),
                        rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null,
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
}

