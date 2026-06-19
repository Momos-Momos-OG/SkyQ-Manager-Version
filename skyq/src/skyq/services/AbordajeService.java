package skyq.services;

import java.util.List;
import java.util.PriorityQueue;
import skyq.model.Pasajero;

public class AbordajeService {

    public static PriorityQueue<Pasajero> crearColaAbordaje(List<Pasajero> pasajeros) {
        int capacidad = pasajeros.isEmpty() ? 11 : pasajeros.size();
        PriorityQueue<Pasajero> queue = new PriorityQueue<>(capacidad, (p1, p2) -> {
            // 1. Silla de ruedas tiene máxima prioridad absoluta
            if (p1.isSillaRuedas() != p2.isSillaRuedas()) {
                return p1.isSillaRuedas() ? -1 : 1;
            }

            // 2. Jerarquía de clases (Nivel 1 (VIP/Upgrade) > Nivel 2 (Ejecutiva) > Nivel 3 (Económica))
            int class1 = (p1.getNivelPrioridad() == 1 || p1.isUpgrade()) ? 1 : p1.getNivelPrioridad();
            int class2 = (p2.getNivelPrioridad() == 1 || p2.isUpgrade()) ? 1 : p2.getNivelPrioridad();

            if (class1 != class2) {
                return Integer.compare(class1, class2);
            }

            // 3. Regla de desempate: Orden FIFO (Check-in primero)
            if (p1.getTimestampLlegada() == null && p2.getTimestampLlegada() == null) {
                return 0;
            }
            if (p1.getTimestampLlegada() == null) {
                return 1;
            }
            if (p2.getTimestampLlegada() == null) {
                return -1;
            }
            return p1.getTimestampLlegada().compareTo(p2.getTimestampLlegada());
        });

        queue.addAll(pasajeros);
        return queue;
    }
}
