package skyq.logic;

import java.util.regex.Pattern;

public class ValidadorFormulario {

    private static final Pattern MATRICULA_PATTERN = Pattern.compile("^[A-Z]{2}-[A-Z0-9]{3,4}$");

    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean esNumeroPositivo(String numero) {
        try {
            int valor = Integer.parseInt(numero.trim());
            return valor > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean esDecimalPositivo(String numero) {
        try {
            double valor = Double.parseDouble(numero.trim());
            return valor > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean esMatriculaValida(String matricula) {
        return matricula != null && MATRICULA_PATTERN.matcher(matricula.trim().toUpperCase()).matches();
    }

    public static boolean esDistribucionValida(String distribucion) {
        if (distribucion == null) return false;
        String regex = "^[1-9][0-9]*(-[1-9][0-9]*)+$";
        return Pattern.matches(regex, distribucion.trim());
    }
}
