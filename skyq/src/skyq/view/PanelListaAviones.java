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

    // Timer de sincronización en tiempo real — 3 segundos, no bloquea el EDT
    private final Timer timerSync = new Timer(3000, e -> recargarDatosAviones());
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

        // Contenedor intermedio que empuja el gridPanel al norte, evitando que se estiren las celdas verticalmente
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        wrapperPanel.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapperPanel);
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

    /** Arranca el timer cuando el panel entra al árbol de componentes visible. */
    @Override
    public void addNotify() {
        super.addNotify();
        timerSync.start();
    }

    /** Detiene el timer cuando el panel deja de estar en el árbol (evita conexiones ociosas). */
    @Override
    public void removeNotify() {
        timerSync.stop();
        super.removeNotify();
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
        int cardWidth = 240; // Ancho de tarjeta (220) + espacio
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
        card.setPreferredSize(new Dimension(220, 220));
        card.setMinimumSize(new Dimension(220, 220));
        card.setMaximumSize(new Dimension(220, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_TARJETA, new EmptyBorder(10, 10, 10, 10)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Mapeo exacto de colores según estado
        Color colorEstado;
        switch (av.getEstado()) {
            case EN_TERMINAL -> colorEstado = EstiloUI.VERDE_ESMERALDA;
            case EN_VUELO -> colorEstado = EstiloUI.AZUL_BRILLANTE;
            case EN_MANTENIMIENTO -> colorEstado = EstiloUI.ROJO_ALERTA;
            default -> colorEstado = EstiloUI.ASIENTO_OCUPADO;
        }

        // Header decorativo con icono
        JPanel visualHeader = new JPanel(new BorderLayout());
        visualHeader.setBackground(new Color(13, 17, 23));
        visualHeader.setPreferredSize(new Dimension(150, 80));
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

        JLabel lblModelo = new JLabel(av.getModelo());
        lblModelo.setForeground(EstiloUI.TEXTO_MUTED);
        lblModelo.setFont(EstiloUI.FUENTE_LABEL);
        lblModelo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCapacidad = new JLabel("Capacidad: " + av.getCapacidad() + " pax");
        lblCapacidad.setForeground(EstiloUI.TEXTO_MUTED);
        lblCapacidad.setFont(EstiloUI.FUENTE_LABEL);
        lblCapacidad.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEstado = new JLabel("Estado: " + av.getEstado());
        lblEstado.setForeground(colorEstado);
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(lblMatricula);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblModelo);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblCapacidad);
        infoPanel.add(Box.createVerticalStrut(4));
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
                        new EmptyBorder(10, 10, 10, 10)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(EstiloUI.FONDO_TARJETA);
                infoPanel.setBackground(EstiloUI.FONDO_TARJETA);
                card.setBorder(BorderFactory.createCompoundBorder(
                        EstiloUI.BORDE_TARJETA, new EmptyBorder(10, 10, 10, 10)));
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
