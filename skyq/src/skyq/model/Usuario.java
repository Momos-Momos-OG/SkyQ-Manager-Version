package skyq.model;

public class Usuario {
    private int idUsuario;
    private String username;
    private String rol;
    private String estado;

    public Usuario() {}

    public Usuario(int idUsuario, String username, String rol, String estado) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.rol = rol;
        this.estado = estado;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isGerente() { return "GERENTE".equals(rol); }
    public boolean isOperario() { return "OPERARIO".equals(rol); }
}
