package skyq.logic;

import skyq.model.Pasajero;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DesembarqueManager {

    public List<Pasajero> ordenarPorAsiento(List<Pasajero> pasajeros) {
        if (pasajeros == null) {
            return Collections.emptyList();
        }

        pasajeros.sort(new Comparator<Pasajero>() {
            @Override
            public int compare(Pasajero primero, Pasajero segundo) {
                int filaPrimero = obtenerFila(primero.getNumAsiento());
                int filaSegundo = obtenerFila(segundo.getNumAsiento());

                int comparacionFila = Integer.compare(filaPrimero, filaSegundo);
                if (comparacionFila != 0) {
                    return comparacionFila;
                }

                String letraPrimero = obtenerLetra(primero.getNumAsiento());
                String letraSegundo = obtenerLetra(segundo.getNumAsiento());
                return letraPrimero.compareTo(letraSegundo);
            }

            private int obtenerFila(String asiento) {
                if (asiento == null || asiento.isEmpty()) {
                    return Integer.MAX_VALUE;
                }

                StringBuilder numero = new StringBuilder();
                for (int i = 0; i < asiento.length(); i++) {
                    char caracter = asiento.charAt(i);
                    if (Character.isDigit(caracter)) {
                        numero.append(caracter);
                    } else {
                        break;
                    }
                }

                if (numero.length() == 0) {
                    return Integer.MAX_VALUE;
                }

                return Integer.parseInt(numero.toString());
            }

            private String obtenerLetra(String asiento) {
                if (asiento == null || asiento.isEmpty()) {
                    return "Z";
                }

                for (int i = 0; i < asiento.length(); i++) {
                    char caracter = asiento.charAt(i);
                    if (Character.isLetter(caracter)) {
                        return String.valueOf(Character.toUpperCase(caracter));
                    }
                }

                return "Z";
            }
        });

        return pasajeros;
    }
}