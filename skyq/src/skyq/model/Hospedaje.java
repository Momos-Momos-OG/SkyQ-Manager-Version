package skyq.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa el hospedaje asignado a un piloto.
 * Versión básica demostrativa: registra hotel, ciudad y fechas de estadía.
 * Usada por App Manager (asignación) y App Piloto (consulta personal).
 */
public class Hospedaje {

    private int idHospedaje;
    private int idPiloto;           // FK → pilotos
    private String hotel;
    private String ciudad;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;

    // Campo auxiliar para mostrar el nombre del piloto en la UI sin join extra
    private String nombrePiloto;

    public Hospedaje() {}

    public Hospedaje(int idHospedaje, int idPiloto, String hotel, String ciudad,
                     LocalDateTime fechaIngreso, LocalDateTime fechaSalida) {
        this.idHospedaje = idHospedaje;
        this.idPiloto = idPiloto;
        this.hotel = hotel;
        this.ciudad = ciudad;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
    }

    // --- Getters y Setters ---

    public int getIdHospedaje() { return idHospedaje; }
    public void setIdHospedaje(int idHospedaje) { this.idHospedaje = idHospedaje; }

    public int getIdPiloto() { return idPiloto; }
    public void setIdPiloto(int idPiloto) { this.idPiloto = idPiloto; }

    public String getHotel() { return hotel; }
    public void setHotel(String hotel) { this.hotel = hotel; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public String getNombrePiloto() { return nombrePiloto; }
    public void setNombrePiloto(String nombrePiloto) { this.nombrePiloto = nombrePiloto; }
}