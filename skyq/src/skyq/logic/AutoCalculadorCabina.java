package skyq.logic;

public class AutoCalculadorCabina {

    public static String calcularDistribucion(int capacidad) {
        if (capacidad < 150) {
            return calcularUnPasillo(capacidad);
        } else {
            return calcularDosPasillos(capacidad);
        }
    }

    private static String calcularUnPasillo(int capacidad) {
        double asientosVIP = capacidad * 0.10;
        int filasVIP = (int) Math.round(asientosVIP / 4.0);
        int realVIP = filasVIP * 4;
        int asientosRestantes = capacidad - realVIP;
        int filasECON = (int) Math.round(asientosRestantes / 6.0);
        if (filasVIP == 0 && filasECON == 0) {
            filasECON = 1;
        }
        return String.format("VIP:2-2:%d|ECON:3-3:%d", filasVIP, filasECON);
    }

    private static String calcularDosPasillos(int capacidad) {
        double asientosVIP = capacidad * 0.10;
        int filasVIP = (int) Math.round(asientosVIP / 6.0);
        int realVIP = filasVIP * 6;

        double asientosEJEC = capacidad * 0.20;
        int filasEJEC = (int) Math.round(asientosEJEC / 8.0);
        int realEJEC = filasEJEC * 8;

        int asientosRestantes = capacidad - realVIP - realEJEC;
        int filasECON = (int) Math.round(asientosRestantes / 10.0);

        return String.format("VIP:2-2-2:%d|EJEC:2-4-2:%d|ECON:3-4-3:%d", filasVIP, filasEJEC, filasECON);
    }

    public static int calcularCapacidadTotal(String distribucionClases) {
        if (distribucionClases == null || distribucionClases.trim().isEmpty()) {
            return 0;
        }
        int total = 0;
        String[] clases = distribucionClases.split("\\|");
        for (String clase : clases) {
            String[] partes = clase.split(":");
            if (partes.length != 3) continue;
            String distribucionAsientos = partes[1];
            int filas = Integer.parseInt(partes[2]);

            int asientosPorFila = 0;
            String[] bloques = distribucionAsientos.split("-");
            for (String b : bloques) {
                asientosPorFila += Integer.parseInt(b.trim());
            }
            total += (asientosPorFila * filas);
        }
        return total;
    }
}
