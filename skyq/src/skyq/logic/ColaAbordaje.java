package skyq.logic;

import skyq.model.Pasajero;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ColaAbordaje {

    public List<Pasajero> organizarPasajeros(List<Pasajero> pasajeros) {
        if (pasajeros == null) {
            return Collections.emptyList();
        }

        pasajeros.sort(new Comparator<Pasajero>() {
            @Override
            public int compare(Pasajero primero, Pasajero segundo) {
                int prioridadComparacion = Integer.compare(primero.getNivelPrioridad(), segundo.getNivelPrioridad());
                if (prioridadComparacion != 0) {
                    return prioridadComparacion;
                }

                if (primero.getTimestampLlegada() == null && segundo.getTimestampLlegada() == null) {
                    return 0;
                }
                if (primero.getTimestampLlegada() == null) {
                    return 1;
                }
                if (segundo.getTimestampLlegada() == null) {
                    return -1;
                }

                return primero.getTimestampLlegada().compareTo(segundo.getTimestampLlegada());
            }
        });

        return pasajeros;
    }
}