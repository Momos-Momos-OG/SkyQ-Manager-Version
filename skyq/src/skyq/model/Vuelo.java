package skyq.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa un vuelo asignado en el sistema SkyQ.
 * Relaciona un avión (matrícula) con un piloto (idPiloto) y un periodo de operación.
 * Esta entidad es compartida por las 3 aplicaciones del ecosistema SkyQ.
 */
public class Vuelo {

    private int idVuelo;
    private String matricula;       // FK → aviones
    private int idPiloto;           // FK → pilotos
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaRegreso;
    private String estado;          // 'Programado' | 'En Vuelo' | 'Completado' | 'Cancelado'
    private String origen;
    private String destino;

    // Campos auxiliares para joins (no mapeados directamente a columnas propias)
    private String nombrePiloto;
    private String modeloAvion;

    public Vuelo() {}

    public Vuelo(int idVuelo, String matricula, int idPiloto,
                 LocalDateTime fechaSalida, LocalDateTime fechaRegreso, String estado) {
        this.idVuelo = idVuelo;
        this.matricula = matricula;
        this.idPiloto = idPiloto;
        this.fechaSalida = fechaSalida;
        this.fechaRegreso = fechaRegreso;
        this.estado = estado;
        this.origen = "";
        this.destino = "";
    }

    public Vuelo(int idVuelo, String matricula, int idPiloto,
                 LocalDateTime fechaSalida, LocalDateTime fechaRegreso, String estado,
                 String origen, String destino) {
        this.idVuelo = idVuelo;
        this.matricula = matricula;
        this.idPiloto = idPiloto;
        this.fechaSalida = fechaSalida;
        this.fechaRegreso = fechaRegreso;
        this.estado = estado;
        this.origen = origen;
        this.destino = destino;
    }

    // --- Getters y Setters ---

    public int getIdVuelo() { return idVuelo; }
    public void setIdVuelo(int idVuelo) { this.idVuelo = idVuelo; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public int getIdPiloto() { return idPiloto; }
    public void setIdPiloto(int idPiloto) { this.idPiloto = idPiloto; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public LocalDateTime getFechaRegreso() { return fechaRegreso; }
    public void setFechaRegreso(LocalDateTime fechaRegreso) { this.fechaRegreso = fechaRegreso; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getNombrePiloto() { return nombrePiloto; }
    public void setNombrePiloto(String nombrePiloto) { this.nombrePiloto = nombrePiloto; }

    public String getModeloAvion() { return modeloAvion; }
    public void setModeloAvion(String modeloAvion) { this.modeloAvion = modeloAvion; }
}