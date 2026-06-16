package skyq.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PanelCabinaPreview extends JPanel {
    private String distribucion;

    public PanelCabinaPreview(String distribucion) {
        this.distribucion = distribucion;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        rebuildUI();
    }

    public void actualizarDistribucion(String distribucion) {
        this.distribucion = distribucion;
        rebuildUI();
    }

    private void rebuildUI() {
        removeAll();
        if (distribucion == null || distribucion.isEmpty()) {
            JLabel lbl = new JLabel("Sin configuración de cabina");
            lbl.setForeground(EstiloUI.TEXTO_MUTED);
            lbl.setFont(EstiloUI.FUENTE_LABEL);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lbl);
            revalidate();
            repaint();
            return;
        }

        String[] clases = distribucion.split("\\|");
        int filaActual = 1;

        for (String clase : clases) {
            String[] partes = clase.split(":");
            if (partes.length != 3) continue;

            String nombreClase = partes[0];
            String distribucionAsientos = partes[1];
            int filas = Integer.parseInt(partes[2]);

            Color colorClase = obtenerColorClase(nombreClase);

            // Cabecera de la sección (ej: "─── CLASE VIP ───")
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            headerPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
            JLabel lblHeader = new JLabel("─── CLASE " + nombreClase + " (" + distribucionAsientos + ") ───");
            lblHeader.setForeground(EstiloUI.TEXTO_MUTED);
            lblHeader.setFont(new Font("SansSerif", Font.BOLD, 10));
            headerPanel.add(lblHeader);
            add(headerPanel);
            add(Box.createRigidArea(new Dimension(0, 5)));

            String[] columnas = distribucionAsientos.split("-");

            for (int f = 0; f < filas; f++) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
                rowPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

                // Indicador de fila izquierdo
                JLabel lblFilaIzq = new JLabel(String.valueOf(filaActual), SwingConstants.RIGHT);
                lblFilaIzq.setForeground(EstiloUI.TEXTO_MUTED);
                lblFilaIzq.setFont(new Font("SansSerif", Font.BOLD, 9));
                lblFilaIzq.setPreferredSize(new Dimension(20, 16));
                rowPanel.add(lblFilaIzq);

                for (int b = 0; b < columnas.length; b++) {
                    int cols = Integer.parseInt(columnas[b]);

                    for (int c = 0; c < cols; c++) {
                        JPanel seat = new JPanel();
                        seat.setPreferredSize(new Dimension(16, 16));
                        seat.setBackground(colorClase);
                        seat.setBorder(BorderFactory.createLineBorder(EstiloUI.FONDO_DARK_PRINCIPAL, 1));
                        rowPanel.add(seat);
                    }

                    // Pasillo
                    if (b < columnas.length - 1) {
                        JPanel spacer = new JPanel();
                        spacer.setPreferredSize(new Dimension(14, 16));
                        spacer.setOpaque(false);
                        rowPanel.add(spacer);
                    }
                }

                // Indicador de fila derecho
                JLabel lblFilaDer = new JLabel(String.valueOf(filaActual), SwingConstants.LEFT);
                lblFilaDer.setForeground(EstiloUI.TEXTO_MUTED);
                lblFilaDer.setFont(new Font("SansSerif", Font.BOLD, 9));
                lblFilaDer.setPreferredSize(new Dimension(20, 16));
                rowPanel.add(lblFilaDer);

                add(rowPanel);
                add(Box.createRigidArea(new Dimension(0, 4)));
                filaActual++;
            }
            add(Box.createRigidArea(new Dimension(0, 15)));
        }
        revalidate();
        repaint();
    }

    private Color obtenerColorClase(String clase) {
        return switch (clase) {
            case "VIP" -> new Color(255, 215, 0);
            case "EJEC" -> new Color(135, 206, 250);
            default -> new Color(128, 128, 128);
        };
    }

    @Override
    public Dimension getPreferredSize() {
        int width = 0;
        int height = 0;
        for (Component comp : getComponents()) {
            Dimension d = comp.getPreferredSize();
            width = Math.max(width, d.width);
            height += d.height;
        }
        return new Dimension(width + 30, height + 30);
    }
}
