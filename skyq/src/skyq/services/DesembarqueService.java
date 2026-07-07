package skyq.services;

import java.util.ArrayList;
import java.util.List;
import skyq.model.Pasajero;

/**
 * Servicio de desembarque de pasajeros.
 * Utiliza el algoritmo de ordenamiento de burbuja (Bubble Sort)
 * para simular el egreso de los pasajeros de adelante hacia atrás.
 */
public class DesembarqueService {

    /**
     * Ordena a los pasajeros para el proceso de desembarque de la aeronave.
     * Aplica Bubble Sort manual para ordenar de forma ascendente por el número de fila.
     */
    public static List<Pasajero> ordenarDesembarque(List<Pasajero> pasajeros) {
        if (pasajeros == null) {
            return new ArrayList<>();
        }

        List<Pasajero> lista = new ArrayList<>(pasajeros);
        int n = lista.size();

        // Implementación manual de Bubble Sort
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                Pasajero p1 = lista.get(j);
                Pasajero p2 = lista.get(j + 1);

                int fila1 = obtenerFila(p1.getNumAsiento());
                int fila2 = obtenerFila(p2.getNumAsiento());

                boolean intercambiar = false;
                if (fila1 > fila2) {
                    intercambiar = true;
                } else if (fila1 == fila2) {
                    String letra1 = obtenerLetra(p1.getNumAsiento());
                    String letra2 = obtenerLetra(p2.getNumAsiento());
                    if (letra1.compareTo(letra2) > 0) {
                        intercambiar = true;
                    }
                }

                if (intercambiar) {
                    lista.set(j, p2);
                    lista.set(j + 1, p1);
                }
            }
        }

        return lista;
    }

    private static int obtenerFila(String asiento) {
        if (asiento == null || asiento.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        StringBuilder numero = new StringBuilder();
        for (int i = 0; i < asiento.length(); i++) {
            char c = asiento.charAt(i);
            if (Character.isDigit(c)) {
                numero.append(c);
            } else {
                break;
            }
        }
        if (numero.length() == 0) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(numero.toString());
    }

    private static String obtenerLetra(String asiento) {
        if (asiento == null || asiento.isEmpty()) {
            return "Z";
        }
        for (int i = 0; i < asiento.length(); i++) {
            char c = asiento.charAt(i);
            if (Character.isLetter(c)) {
                return String.valueOf(Character.toUpperCase(c));
            }
        }
        return "Z";
    }
}
