package skyq.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import skyq.model.Pasajero;

/**
 * Motor de Abordaje — Estructura de Datos: Cola de Prioridad (PriorityQueue).
 *
 * Implementa dos reglas de prioridad según el SRS:
 *
 * US-02 — Reglas de prioridad individuales:
 *   1. Silla de ruedas → Máxima prioridad absoluta
 *   2. Nivel de clase  → 1=VIP/Upgrade > 2=Ejecutiva > 3=Económica
 *   3. Desempate FIFO  → quien llegó (hizo check-in) primero aborda primero
 *
 * US-03 — Agrupación Familiar (mismo PNR):
 *   Los pasajeros con el mismo PNR deben abordar JUNTOS.
 *   Todos los miembros del grupo heredan la MÁXIMA prioridad del miembro
 *   con mayor jerarquía dentro del grupo.
 *   La jerarquía se determina por:
 *     a) Silla de ruedas (si alguno la necesita, todo el grupo la hereda)
 *     b) Menor nivelPrioridad numérico del grupo
 *     c) Para el timestamp de desempate, se usa el más antiguo del grupo
 *
 * Algoritmo:
 *   1. Agrupar pasajeros por PNR
 *   2. Para cada grupo, calcular la prioridad máxima del grupo
 *   3. Asignar esa prioridad efectiva a todos los miembros del grupo
 *   4. Insertar todos los pasajeros (con prioridad efectiva) en la PriorityQueue
 */
public class AbordajeService {

    /**
     * Crea la cola de abordaje aplicando US-02 y US-03.
     *
     * Pasajeros: Lista de pasajeros registrados en el vuelo (con check-in).
     * Retorna: PriorityQueue ordenada según reglas de prioridad del SRS.
     */
    public static PriorityQueue<Pasajero> crearColaAbordaje(List<Pasajero> pasajeros) {

        // ─────────────────────────────────────────────────
        // US-03: FASE 1 — Agrupación por PNR
        // ─────────────────────────────────────────────────
        List<Pasajero> pasajerosConPrioridadGrupal = aplicarAgrupacionFamiliar(pasajeros);

        // ─────────────────────────────────────────────────
        // US-02: FASE 2 — Insertar en PriorityQueue con comparador
        // ─────────────────────────────────────────────────
        int capacidad = pasajerosConPrioridadGrupal.isEmpty() ? 11 : pasajerosConPrioridadGrupal.size();

        PriorityQueue<Pasajero> queue = new PriorityQueue<>(capacidad, (p1, p2) -> {

            // Regla 1: Silla de ruedas (efectiva) tiene máxima prioridad absoluta
            if (p1.isSillaRuedas() != p2.isSillaRuedas()) {
                return p1.isSillaRuedas() ? -1 : 1;
            }

            // Regla 2: Jerarquía de clases (el menor número es mayor prioridad)
            // Nivel 1 (VIP/Upgrade) > Nivel 2 (Ejecutiva) > Nivel 3 (Económica)
            int class1 = (p1.getNivelPrioridad() == 1 || p1.isUpgrade()) ? 1 : p1.getNivelPrioridad();
            int class2 = (p2.getNivelPrioridad() == 1 || p2.isUpgrade()) ? 1 : p2.getNivelPrioridad();

            if (class1 != class2) {
                return Integer.compare(class1, class2);
            }

            // Regla 3: Desempate FIFO (timestamp de llegada — quien llegó primero aborda primero)
            if (p1.getTimestampLlegada() == null && p2.getTimestampLlegada() == null) {
                return 0;
            }
            if (p1.getTimestampLlegada() == null) {
                return 1;   // null va al final
            }
            if (p2.getTimestampLlegada() == null) {
                return -1;
            }
            return p1.getTimestampLlegada().compareTo(p2.getTimestampLlegada());
        });

        queue.addAll(pasajerosConPrioridadGrupal);
        return queue;
    }

    /**
     * Implementa US-03: Agrupación Familiar por PNR.
     *
     * Itera sobre los pasajeros, agrupa por PNR y calcula la prioridad
     * máxima del grupo. Devuelve una nueva lista de pasajeros donde cada
     * miembro lleva la prioridad efectiva de su grupo.
     *
     * Si el PNR es null o individual (no compartido), el pasajero conserva
     * su propia prioridad original.
     *
     * Pasajeros: Lista original de pasajeros.
     * Retorna: Lista con prioridades efectivas asignadas por grupo familiar.
     */
    private static List<Pasajero> aplicarAgrupacionFamiliar(List<Pasajero> pasajeros) {

        // Agrupar por PNR
        Map<String, List<Pasajero>> grupos = new HashMap<>();
        List<Pasajero> sinPnr = new ArrayList<>();

        for (Pasajero p : pasajeros) {
            String pnr = p.getPnr();
            if (pnr == null || pnr.isBlank()) {
                sinPnr.add(p);
            } else {
                grupos.computeIfAbsent(pnr, k -> new ArrayList<>()).add(p);
            }
        }

        List<Pasajero> resultado = new ArrayList<>(sinPnr);

        // Para cada grupo familiar, calcular prioridad máxima y propagarla
        for (Map.Entry<String, List<Pasajero>> entry : grupos.entrySet()) {
            List<Pasajero> grupo = entry.getValue();

            if (grupo.size() == 1) {
                // Grupo de 1 persona: no aplica US-03, conserva su prioridad
                resultado.add(grupo.get(0));
                continue;
            }

            // ── Calcular prioridad máxima del grupo ──

            // a) ¿Algún miembro necesita silla de ruedas?
            boolean grupoSillaRuedas = grupo.stream().anyMatch(Pasajero::isSillaRuedas);

            // b) Menor nivelPrioridad del grupo (1=VIP es mayor jerarquía)
            int mejorNivel = grupo.stream()
                    .mapToInt(p -> (p.getNivelPrioridad() == 1 || p.isUpgrade()) ? 1 : p.getNivelPrioridad())
                    .min()
                    .orElse(3);

            // c) Timestamp más antiguo del grupo (para FIFO correcto)
            java.time.LocalDateTime timestampGrupal = grupo.stream()
                    .filter(p -> p.getTimestampLlegada() != null)
                    .map(Pasajero::getTimestampLlegada)
                    .min(java.time.LocalDateTime::compareTo)
                    .orElse(null);

            // ── Propagar prioridad grupal a todos los miembros ──
            for (Pasajero miembro : grupo) {
                // Crear pasajero "efectivo" con prioridad grupal propagada
                Pasajero efectivo = new Pasajero(
                        miembro.getIdPasajero(),
                        miembro.getNombre(),
                        miembro.getNumAsiento(),
                        mejorNivel,                 // prioridad efectiva del grupo
                        timestampGrupal,            // timestamp del más antiguo del grupo
                        miembro.getMatricula(),
                        miembro.getPnr(),
                        grupoSillaRuedas,           // hereda si alguien necesita silla
                        miembro.isUpgrade()
                );
                resultado.add(efectivo);
            }
        }

        return resultado;
    }
}
