package skyq.services;

import java.time.LocalDateTime;
import skyq.dao.EquipajeDAO;
import skyq.dao.PasajeroDAO;
import skyq.model.Equipaje;
import skyq.model.Pasajero;

public class CheckInService {
    private static final PasajeroDAO pasajeroDAO = new PasajeroDAO();
    private static final EquipajeDAO equipajeDAO = new EquipajeDAO();

    public static void realizarCheckIn(Pasajero pasajero, double pesoEquipaje) throws Exception {
        if (pasajero == null) {
            throw new IllegalArgumentException("Debe seleccionar un pasajero válido.");
        }
        if (pasajero.getNumAsiento() == null || pasajero.getNumAsiento().trim().isEmpty()) {
            throw new IllegalArgumentException("El pasajero no tiene un asiento asignado.");
        }

        int prioridad = pasajero.getNivelPrioridad();
        double pesoMaximo = switch (prioridad) {
            case 1 -> 32.0;
            case 2 -> 23.0;
            default -> 15.0;
        };

        if (pesoEquipaje > pesoMaximo) {
            throw new Exception("El equipaje excede el peso permitido (" + pesoMaximo + "kg) para prioridad " + prioridad);
        }

        // Realizar Check-In: actualizar timestamp de llegada en BD
        LocalDateTime fechaLlegada = LocalDateTime.now();
        boolean okCheckIn = pasajeroDAO.realizarCheckIn(pasajero.getIdPasajero(), pasajero.getNumAsiento(), fechaLlegada);

        if (!okCheckIn) {
            throw new Exception("Error al procesar el check-in en la base de datos.");
        }

        // Registrar Equipaje
        Equipaje equipaje = new Equipaje(0, pasajero.getIdPasajero(), pesoEquipaje, "Aceptado");
        boolean okEquipaje = equipajeDAO.registrarEquipaje(equipaje);

        if (!okEquipaje) {
            throw new Exception("Check-in realizado, pero error al registrar el equipaje.");
        }
    }
}
