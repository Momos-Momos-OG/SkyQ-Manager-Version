package skyq.services;

import java.util.List;
import skyq.dao.AuditoriaDAO;
import skyq.dao.MantenimientoDAO;
import skyq.dao.PasajeroDAO;
import skyq.logic.LoggerManager;
import skyq.logic.SesionManager;
import skyq.model.Pasajero;
import skyq.model.Vuelo;

public class VentasService {
    private static final PasajeroDAO pasajeroDAO = new PasajeroDAO();
    private static final MantenimientoDAO mantenimientoDAO = new MantenimientoDAO();
    private static final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    public static String procesarVenta(Vuelo vuelo, List<Pasajero> pasajeros) throws Exception {
        if (vuelo == null) {
            throw new IllegalArgumentException("Debe seleccionar un vuelo válido.");
        }
        if (pasajeros == null || pasajeros.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un pasajero.");
        }

        // Validación crítica de Mantenimiento
        java.util.Date fechaVuelo = java.sql.Timestamp.valueOf(vuelo.getFechaSalida());
        boolean enManto = mantenimientoDAO.estaEnMantenimiento(vuelo.getMatricula(), fechaVuelo);

        if (enManto) {
            throw new Exception("Venta denegada. La aeronave asignada a este vuelo se encontrará en mantenimiento durante esa fecha.");
        }

        // Generar un solo PNR para todos
        String pnr = generarPNR();

        // Guardar cada pasajero
        for (Pasajero p : pasajeros) {
            p.setPnr(pnr);
            p.setMatricula(vuelo.getMatricula());
            int id = pasajeroDAO.insertarPasajeroYObtenerId(p);
            if (id <= 0) {
                throw new Exception("Error de base de datos al guardar la reserva para: " + p.getNombre());
            }
            p.setIdPasajero(id);
        }

        // Registrar en auditoría
        String userActual = "Desconocido";
        if (SesionManager.getInstance().getUsuarioActual() != null) {
            userActual = SesionManager.getInstance().getUsuarioActual().getUsername();
        }
        
        StringBuilder detalle = new StringBuilder("PNR: " + pnr + ", Vuelo: #" + vuelo.getIdVuelo() + ", Pasajeros: ");
        for (Pasajero p : pasajeros) {
            detalle.append(p.getNombre()).append(" (Asiento: ").append(p.getNumAsiento()).append("), ");
        }
        
        auditoriaDAO.registrarAccion(userActual, "VENTA_BOLETO_MULTIPLE", detalle.toString());
        LoggerManager.getInstance().logInfo("Venta procesada múltiple - PNR: " + pnr + " con " + pasajeros.size() + " pasajeros");

        return pnr;
    }

    private static String generarPNR() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("SQ-");
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 4; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }
}
