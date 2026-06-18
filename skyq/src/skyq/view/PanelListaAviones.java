package skyq.view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import skyq.dao.AvionDAO;
import skyq.model.Avion;

public final class PanelListaAviones extends JPanel {
    private static final long serialVersionUID = 1L;

    private final transient AvionDAO avionDAO = new AvionDAO();
    private transient List<Avion> avionesFlota = new ArrayList<>();
    private final JPanel gridPanel;

    public PanelListaAviones() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título de la sección
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        headerPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        JLabel lblTitulo = new JLabel("✈  MONITOR DE FLOTA  —  Seleccione una aeronave para editar");
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_TITULO);
        headerPanel.add(lblTitulo);
        add(headerPanel, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Component listener para adaptar las columnas dinámicamente
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                reorganizarTarjetas();
            }
        });

        recargarDatosAviones();
    }

    public void recargarDatosAviones() {
        avionesFlota = avionDAO.obtenerAvionesFlota();
        reorganizarTarjetas();
    }

    private void reorganizarTarjetas() {
        gridPanel.removeAll();
        int width = getWidth();
        if (width <= 0) {
            width = 800; // Ancho base de fallback
        }
        int cardWidth = 230; // Ancho promedio de la tarjeta
        int cols = Math.max(1, (width - 40) / cardWidth);

        gridPanel.setLayout(new GridLayout(0, cols, 20, 20));

        for (Avion av : avionesFlota) {
            gridPanel.add(crearTarjetaAvion(av));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel crearTarjetaAvion(Avion av) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(EstiloUI.FONDO_TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_TARJETA, new EmptyBorder(12, 12, 12, 12)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Color según estado
        Color colorEstado;
        switch (av.getEstado()) {
            case "Disponible" -> colorEstado = EstiloUI.VERDE_NEON;
            case "En Vuelo" -> colorEstado = EstiloUI.AZUL_ACCENT;
            case "En mantenimiento" -> colorEstado = new Color(250, 176, 5); // Naranja
            default -> colorEstado = EstiloUI.ROJO_ALERTA;
        }

        // Header decorativo con icono
        JPanel visualHeader = new JPanel(new BorderLayout());
        visualHeader.setBackground(new Color(13, 17, 23));
        visualHeader.setPreferredSize(new Dimension(150, 75));
        visualHeader.setBorder(EstiloUI.BORDE_COMPONENTE);
        
        JLabel lblIcono = new JLabel("✈", SwingConstants.CENTER);
        lblIcono.setFont(new Font("SansSerif", Font.BOLD, 42));
        lblIcono.setForeground(colorEstado);
        visualHeader.add(lblIcono, BorderLayout.CENTER);

        card.add(visualHeader, BorderLayout.NORTH);

        // Información de la aeronave
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(EstiloUI.FONDO_TARJETA);
        infoPanel.setBorder(new EmptyBorder(10, 2, 2, 2));

        JLabel lblMatricula = new JLabel(av.getMatricula());
        lblMatricula.setForeground(EstiloUI.TEXTO_BLANCO);
        lblMatricula.setFont(EstiloUI.FUENTE_SUBTITULO);
        lblMatricula.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblModelo = new JLabel(av.getModelo() + " (" + av.getCapacidad() + " pax)");
        lblModelo.setForeground(EstiloUI.TEXTO_MUTED);
        lblModelo.setFont(EstiloUI.FUENTE_LABEL);
        lblModelo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEstado = new JLabel("● " + av.getEstado());
        lblEstado.setForeground(colorEstado);
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(lblMatricula);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblModelo);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(lblEstado);

        card.add(infoPanel, BorderLayout.CENTER);

        // Efecto hover sobre la tarjeta
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(30, 36, 45));
                infoPanel.setBackground(new Color(30, 36, 45));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(56, 139, 253), 1),
                        new EmptyBorder(12, 12, 12, 12)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(EstiloUI.FONDO_TARJETA);
                infoPanel.setBackground(EstiloUI.FONDO_TARJETA);
                card.setBorder(BorderFactory.createCompoundBorder(
                        EstiloUI.BORDE_TARJETA, new EmptyBorder(12, 12, 12, 12)));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                DialogoEditarAvion dialogo = new DialogoEditarAvion(
                        (Frame) SwingUtilities.getWindowAncestor(PanelListaAviones.this), av);
                dialogo.setVisible(true);
                recargarDatosAviones();
            }
        });

        return card;
    }
}
