package skyq.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import skyq.model.Equipaje;

public class EquipajeService {

    public static Stack<Equipaje> cargarBodega(List<Equipaje> equipajes) {
        Stack<Equipaje> bodega = new Stack<>();
        for (Equipaje eq : equipajes) {
            bodega.push(eq);
        }
        return bodega;
    }

    public static List<Equipaje> descargarBodega(Stack<Equipaje> bodega) {
        List<Equipaje> descargados = new ArrayList<>();
        while (!bodega.isEmpty()) {
            descargados.add(bodega.pop());
        }
        return descargados;
    }
}
