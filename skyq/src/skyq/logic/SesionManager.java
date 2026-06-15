package skyq.logic;

import skyq.model.Usuario;

public class SesionManager {
    private static volatile SesionManager instancia;
    private Usuario usuarioActual;

    private SesionManager() {
    }

    public static SesionManager getInstance() {
        if (instancia == null) {
            synchronized (SesionManager.class) {
                if (instancia == null) {
                    instancia = new SesionManager();
                }
            }
        }
        return instancia;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    public boolean isAutenticado() {
        return usuarioActual != null;
    }

    public boolean isGerente() {
        return usuarioActual != null && usuarioActual.isGerente();
    }

    public boolean isOperario() {
        return usuarioActual != null && usuarioActual.isOperario();
    }
}
