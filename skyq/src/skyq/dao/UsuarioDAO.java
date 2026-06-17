package skyq.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import skyq.database.ConexionBD;
import skyq.model.Usuario;

public class UsuarioDAO {

    public Usuario autenticar(String username, String password) {
        String sql = "SELECT idUsuario, username, rol, estado FROM usuarios WHERE username = ? AND password_hash = ? AND estado = 'Activo'";

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Usuario(
                            resultSet.getInt("idUsuario"),
                            resultSet.getString("username"),
                            resultSet.getString("rol"),
                            resultSet.getString("estado"));
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL", e);
        }
        return null;
    }

    public static boolean verificarPasswordGerente(String password) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol = 'GERENTE' AND password_hash = ? AND estado = 'Activo'";
        try (Connection connection = ConexionBD.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            skyq.logic.LoggerManager.getInstance().logError("Error SQL al verificar contraseña de Gerente", e);
        }
        return false;
    }
}
