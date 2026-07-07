package skyq.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import skyq.model.Equipaje;

/**
 * Servicio encargado de gestionar la carga y descarga de equipaje en la bodega del avión.
 * Utiliza una estructura de datos tipo Pila (Stack) para simular el comportamiento LIFO
 * (Last In, First Out), donde la última maleta en entrar es la primera en salir al descargar.
 */
public class EquipajeService {

    /**
     * Carga las maletas en la bodega apilándolas una sobre otra (push).
     */
    public static Stack<Equipaje> cargarBodega(List<Equipaje> equipajes) {
        Stack<Equipaje> bodega = new Stack<>();
        for (Equipaje eq : equipajes) {
            bodega.push(eq); // Apila la maleta en la bodega
        }
        return bodega;
    }

    /**
     * Descarga las maletas desapilándolas una a una (pop).
     * Esto invierte el orden original, simulando el desembarque físico de carga.
     */
    public static List<Equipaje> descargarBodega(Stack<Equipaje> bodega) {
        List<Equipaje> descargados = new ArrayList<>();
        while (!bodega.isEmpty()) {
            descargados.add(bodega.pop()); // Saca la maleta del tope de la pila
        }
        return descargados;
    }
}
