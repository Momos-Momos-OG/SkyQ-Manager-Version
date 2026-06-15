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
        int vipCount = (int) Math.round(capacidad * 0.10);
        int econCount = capacidad - vipCount;

        int vipFilas = (int) Math.ceil(vipCount / 4.0);
        int econFilas = (int) Math.ceil(econCount / 3.0);

        return String.format("VIP:2-2:%d|ECON:3-3:%d", vipFilas, econFilas);
    }

    private static String calcularDosPasillos(int capacidad) {
        int vipCount = (int) Math.round(capacidad * 0.10);
        int ejecCount = (int) Math.round(capacidad * 0.20);
        int econCount = capacidad - vipCount - ejecCount;

        int vipFilas = (int) Math.ceil(vipCount / 6.0);
        int ejecFilas = (int) Math.ceil(ejecCount / 8.0);
        int econFilas = (int) Math.ceil(econCount / 10.0);

        return String.format("VIP:2-2-2:%d|EJEC:2-4-2:%d|ECON:3-4-3:%d", vipFilas, ejecFilas, econFilas);
    }
}
