package skyq.model;

import java.time.LocalDate;

public class Mantenimiento {
    private int idMantenimiento;
    private String matricula;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String descripcion;
    private String estado;

    public Mantenimiento() {}

    public Mantenimiento(int idMantenimiento, String matricula, LocalDate fechaInicio, 
                        LocalDate fechaFin, String descripcion, String estado) {
        this.idMantenimiento = idMantenimiento;
        this.matricula = matricula;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdMantenimiento() { return idMantenimiento; }
    public void setIdMantenimiento(int idMantenimiento) { this.idMantenimiento = idMantenimiento; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
