package skyq.model;

public class Equipaje {

    private int idMaleta;
    private int idPasajero;
    private double peso;
    private String estado;

    public Equipaje() {
    }

    public Equipaje(int idMaleta, int idPasajero, double peso, String estado) {
        this.idMaleta = idMaleta;
        this.idPasajero = idPasajero;
        this.peso = peso;
        this.estado = estado;
    }

    public int getIdMaleta() {
        return idMaleta;
    }

    public void setIdMaleta(int idMaleta) {
        this.idMaleta = idMaleta;
    }

    public int getIdPasajero() {
        return idPasajero;
    }

    public void setIdPasajero(int idPasajero) {
        this.idPasajero = idPasajero;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}