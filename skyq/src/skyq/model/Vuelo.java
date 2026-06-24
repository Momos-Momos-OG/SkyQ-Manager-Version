package skyq.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa un vuelo del sistema SkyQ (Aeropuerto Único).
 * Relaciona un avión (matrícula) con un periodo de operación y ruta.
 */
public class Vuelo {

    private int idVuelo;
    private String matricula;       // FK → aviones
    private String codigoVuelo;     // Ej: "UIO-305", "GYE-820"
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaArribo;  // Antes: fechaRegreso
    private String estado;          // 'Programado' | 'En Vuelo' | 'Completado' | 'Cancelado'
    private String origen;
    private String destino;

    // Campo auxiliar para joins (no mapeado directamente a columna propia)
    private String modeloAvion;

    public Vuelo() {}

    public Vuelo(int idVuelo, String matricula, String codigoVuelo,
                LocalDateTime fechaSalida, LocalDateTime fechaArribo,
                String estado) {
        this.idVuelo = idVuelo;
        this.matricula = matricula;
        this.codigoVuelo = codigoVuelo;
        this.fechaSalida = fechaSalida;
        this.fechaArribo = fechaArribo;
        this.estado = estado;
        this.origen = "";
        this.destino = "";
    }

    public Vuelo(int idVuelo, String matricula, String codigoVuelo,
                LocalDateTime fechaSalida, LocalDateTime fechaArribo,
                String estado, String origen, String destino) {
        this.idVuelo = idVuelo;
        this.matricula = matricula;
        this.codigoVuelo = codigoVuelo;
        this.fechaSalida = fechaSalida;
        this.fechaArribo = fechaArribo;
        this.estado = estado;
        this.origen = origen;
        this.destino = destino;
    }

    // --- Getters y Setters ---

    public int getIdVuelo() { return idVuelo; }
    public void setIdVuelo(int idVuelo) { this.idVuelo = idVuelo; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCodigoVuelo() { return codigoVuelo; }
    public void setCodigoVuelo(String codigoVuelo) { this.codigoVuelo = codigoVuelo; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public LocalDateTime getFechaArribo() { return fechaArribo; }
    public void setFechaArribo(LocalDateTime fechaArribo) { this.fechaArribo = fechaArribo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    // Devuelve "Salida" si el vuelo parte del aeropuerto local, "Llegada" si no.
    public String getTipoVuelo() {
        if ("Aeropuerto Local".equalsIgnoreCase(origen)) {
            return "Salida";
        } else {
            return "Llegada";
        }
    }

    public String getModeloAvion() { return modeloAvion; }
    public void setModeloAvion(String modeloAvion) { this.modeloAvion = modeloAvion; }
}