package skyq.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PanelCabinaPreview extends JPanel {
    private String distribucion;

    public PanelCabinaPreview(String distribucion) {
        this.distribucion = distribucion;
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(15, 15, 15, 15));
    }

    public void actualizarDistribucion(String distribucion) {
        this.distribucion = distribucion;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (distribucion == null || distribucion.isEmpty()) {
            g2.setColor(EstiloUI.TEXTO_MUTED);
            g2.setFont(EstiloUI.FUENTE_LABEL);
            g2.drawString("Sin configuración de cabina", 20, 30);
            return;
        }

        int x = 20;
        int y = 30;
        String[] clases = distribucion.split("\\|");

        for (String clase : clases) {
            String[] partes = clase.split(":");
            if (partes.length != 3)
                continue;

            String nombreClase = partes[0];
            String distribucionAsientos = partes[1];
            int filas = Integer.parseInt(partes[2]);

            Color colorClase = obtenerColorClase(nombreClase);
            dibujarSeccionCabina(g2, nombreClase, distribucionAsientos, filas, colorClase, x, y);
            y += filas * 22 + 30;
        }
    }

    private void dibujarSeccionCabina(Graphics2D g2, String nombre, String distribucion, int filas, Color color, int x,
            int y) {
        g2.setColor(EstiloUI.TEXTO_BLANCO);
        g2.setFont(EstiloUI.FUENTE_LABEL);
        g2.drawString(nombre, x, y);

        String[] columnas = distribucion.split("-");
        int posY = y + 20;

        for (int fila = 0; fila < filas; fila++) {
            int posX = x;
            int bloqueNum = 0;

            for (String colStr : columnas) {
                int cols = Integer.parseInt(colStr);

                if (bloqueNum > 0) {
                    posX += 15;
                }

                for (int col = 0; col < cols; col++) {
                    g2.setColor(color);
                    g2.fillRect(posX, posY, 16, 16);
                    g2.setColor(EstiloUI.FONDO_DARK_PRINCIPAL);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRect(posX, posY, 16, 16);
                    posX += 18;
                }

                bloqueNum++;
            }

            posY += 20;
        }
    }

    private Color obtenerColorClase(String clase) {
        return switch (clase) {
            case "VIP" -> new Color(255, 215, 0);
            case "EJEC" -> new Color(135, 206, 250);
            default -> new Color(128, 128, 128);
        };
    }
}
