package skyq.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import skyq.database.ConexionBD;
import skyq.logic.LoggerManager;

public class AuditoriaDAO {

    public boolean registrarAccion(String username, String accion, String detalle) {
        String sql = "INSERT INTO auditoria (username, accion, detalle) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, accion);
            ps.setString(3, detalle);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerManager.getInstance().logError("AuditoriaDAO.registrarAccion", e);
            return false;
        }
    }

    public List<Object[]> obtenerHistorial() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT idAuditoria, username, accion, detalle, fecha_hora FROM auditoria ORDER BY fecha_hora DESC";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] fila = new Object[5];
                fila[0] = rs.getInt("idAuditoria");
                fila[1] = rs.getString("username");
                fila[2] = rs.getString("accion");
                fila[3] = rs.getString("detalle");
                fila[4] = rs.getTimestamp("fecha_hora");
                filas.add(fila);
            }
        } catch (SQLException e) {
            LoggerManager.getInstance().logError("AuditoriaDAO.obtenerHistorial", e);
        }
        return filas;
    }

    public List<Object[]> obtenerHistorialPorUsuario(String username) {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT idAuditoria, username, accion, detalle, fecha_hora FROM auditoria WHERE username = ? ORDER BY fecha_hora DESC";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[5];
                    fila[0] = rs.getInt("idAuditoria");
                    fila[1] = rs.getString("username");
                    fila[2] = rs.getString("accion");
                    fila[3] = rs.getString("detalle");
                    fila[4] = rs.getTimestamp("fecha_hora");
                    filas.add(fila);
                }
            }
        } catch (SQLException e) {
            LoggerManager.getInstance().logError("AuditoriaDAO.obtenerHistorialPorUsuario", e);
        }
        return filas;
    }
}

