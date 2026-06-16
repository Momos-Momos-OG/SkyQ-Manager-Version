package skyq.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import skyq.dao.AvionDAO;
import skyq.model.Avion;

/**
 * Panel de radar operativo con:
 * - Animación de barrido rotatoria usando javax.swing.Timer
 * - Aviones dibujados con posiciones matemáticas estables (hash de matrícula)
 * - Hitboxes clicables para editar cada aeronave
 * - Hover effect sobre los íconos de avión
 */
public final class PanelRadarView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final transient AvionDAO avionDAO = new AvionDAO();
    private transient List<Avion> avionesFlota = new ArrayList<>();
    private final transient List<Rectangle> hitboxes = new ArrayList<>();

    // Estado de la animación de barrido del radar
    private double anguloBarrido = 0.0;
    private int avionHover = -1; // Índice del avión sobre el que está el cursor
    private final Timer timerBarrido;

    public PanelRadarView() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        recargarDatosAviones();

        // Timer de animación: refresca el barrido cada 30ms (~33 FPS)
        timerBarrido = new Timer(30, e -> {
            anguloBarrido = (anguloBarrido + 1.5) % 360;
            repaint();
        });
        timerBarrido.start();

        // Escucha de clics sobre los elementos del Radar
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < hitboxes.size(); i++) {
                    if (hitboxes.get(i).contains(e.getPoint())) {
                        abrirEditorAvionFlotante(avionesFlota.get(i));
                        break;
                    }
                }
            }
        });

        // Escucha de movimiento para el efecto hover
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int anteriorHover = avionHover;
                avionHover = -1;
                for (int i = 0; i < hitboxes.size(); i++) {
                    if (hitboxes.get(i).contains(e.getPoint())) {
                        avionHover = i;
                        break;
                    }
                }
                // Solo repinta si cambió el estado de hover para no sobrecargar
                if (avionHover != anteriorHover) {
                    repaint();
                }
                if (avionHover >= 0) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    @Override
    public void removeNotify() {
        if (timerBarrido != null && timerBarrido.isRunning()) {
            timerBarrido.stop();
        }
        super.removeNotify();
    }

    public final void recargarDatosAviones() {
        avionesFlota = avionDAO.obtenerAvionesFlota();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int radioMaximo = Math.min(getWidth(), getHeight()) / 2 - 30;

        // ── Anillos del radar ──
        g2.setColor(new Color(48, 54, 61));
        g2.setStroke(new BasicStroke(1f));
        for (int radio = radioMaximo / 4; radio <= radioMaximo; radio += radioMaximo / 4) {
            g2.drawOval(cx - radio, cy - radio, radio * 2, radio * 2);
        }

        // Líneas de cruz
        g2.drawLine(0, cy, getWidth(), cy);
        g2.drawLine(cx, 0, cx, getHeight());

        // ── Cono de barrido (sweep) con gradiente de opacidad ──
        double anguloRad = Math.toRadians(anguloBarrido);
        int abanico = 60; // Grados de amplitud del cono de luz
        for (int i = abanico; i >= 0; i--) {
            float alpha = (abanico - i) / (float) abanico * 0.35f;
            g2.setColor(new Color(31, 111, 235, (int) (alpha * 255)));
            double angulo = Math.toRadians(anguloBarrido - i);
            int x2 = cx + (int) (radioMaximo * Math.cos(angulo));
            int y2 = cy + (int) (radioMaximo * Math.sin(angulo));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(cx, cy, x2, y2);
        }

        // Línea de barrido principal (más brillante)
        g2.setColor(new Color(31, 111, 235, 200));
        g2.setStroke(new BasicStroke(2f));
        int xBarrido = cx + (int) (radioMaximo * Math.cos(anguloRad));
        int yBarrido = cy + (int) (radioMaximo * Math.sin(anguloRad));
        g2.drawLine(cx, cy, xBarrido, yBarrido);

        // ── Renderizado de aviones ──
        hitboxes.clear();
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));

        for (int i = 0; i < avionesFlota.size(); i++) {
            Avion av = avionesFlota.get(i);

            // Posición estable basada en el hash único de la matrícula
            int semilla = Math.abs(av.getMatricula().hashCode());
            int margenX = Math.max(200, getWidth() - 300);
            int margenY = Math.max(150, getHeight() - 200);
            int x = 150 + (semilla % margenX);
            int y = 100 + ((semilla / 3) % margenY);

            // Hitbox de 30x30 centrada en el ícono
            Rectangle hitbox = new Rectangle(x - 8, y - 18, 30, 30);
            hitboxes.add(hitbox);

            // Color según estado del avión
            Color colorAvion;
            switch (av.getEstado()) {
                case "Disponible" -> colorAvion = EstiloUI.VERDE_NEON;
                case "En Vuelo" -> colorAvion = EstiloUI.AZUL_ACCENT;
                case "En mantenimiento" -> colorAvion = new Color(250, 176, 5);
                default -> colorAvion = EstiloUI.ROJO_ALERTA;
            }

            // Efecto hover: halo de selección alrededor del avión
            if (i == avionHover) {
                g2.setColor(new Color(colorAvion.getRed(), colorAvion.getGreen(), colorAvion.getBlue(), 60));
                g2.fillOval(x - 18, y - 28, 50, 50);
                g2.setColor(colorAvion);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x - 18, y - 28, 50, 50);
            }

            // Ícono del avión con sombra suave
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2.setColor(new Color(0, 0, 0, 80));
            g2.drawString("✈", x + 1, y + 1); // Sombra
            g2.setColor(colorAvion);
            g2.drawString("✈", x, y);

            // Etiqueta de matrícula y estado bajo el ícono
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(EstiloUI.TEXTO_MUTED);
            g2.drawString(av.getMatricula(), x - 15, y + 16);
            g2.setColor(colorAvion);
            g2.drawString("● " + av.getEstado(), x - 15, y + 27);
        }

        // ── Título del panel ──
        g2.setColor(EstiloUI.TEXTO_BLANCO);
        g2.setFont(EstiloUI.FUENTE_SUBTITULO);
        g2.drawString("RADAR OPERATIVO  —  Clic en aeronave para editar", 25, 28);

        // Leyenda de colores en esquina inferior izquierda
        dibujarLeyenda(g2);
    }

    /** Dibuja la leyenda de estados de aviones en la esquina inferior izquierda */
    private void dibujarLeyenda(Graphics2D g2) {
        int xL = 20;
        int yL = getHeight() - 90;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

        Color[] colores = { EstiloUI.VERDE_NEON, EstiloUI.AZUL_ACCENT, new Color(250, 176, 5), EstiloUI.ROJO_ALERTA };
        String[] estados = { "Disponible", "En Vuelo", "En mantenimiento", "Fuera de servicio" };

        for (int i = 0; i < estados.length; i++) {
            g2.setColor(colores[i]);
            g2.fillOval(xL, yL + i * 16 - 7, 8, 8);
            g2.setColor(EstiloUI.TEXTO_MUTED);
            g2.drawString(estados[i], xL + 14, yL + i * 16);
        }
    }

    /**
     * Abre el diálogo flotante de edición de una aeronave seleccionada en el radar
     */
    private void abrirEditorAvionFlotante(Avion avion) {
        DialogoEditarAvion dialogo = new DialogoEditarAvion((Frame) SwingUtilities.getWindowAncestor(this), avion);
        dialogo.setVisible(true);
        recargarDatosAviones();
    }

    // ── Helpers de estilo ──

    /**
     * Aplica un efecto hover a un botón cambiando su color de fondo
     * cuando el cursor entra y sale usando MouseAdapter.
     */
    public static void aplicarHover(JButton btn, Color colorNormal, Color colorHover) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(colorHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(colorNormal);
            }
        });
    }
}