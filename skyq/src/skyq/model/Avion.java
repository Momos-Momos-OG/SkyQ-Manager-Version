package skyq.model;

public class Avion {

    private String matricula;
    private String modelo;
    private int capacidad;
    private String estado;

    public Avion() {
    }

    public Avion(String matricula, String modelo, int capacidad, String estado) {
        this.matricula = matricula;
        this.modelo = modelo;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}