package skyq.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class PanelBarraTitulo extends JPanel {
    private static final long serialVersionUID = 1L;
    private Point initialClick;

    public PanelBarraTitulo(Window window, String titulo, boolean mostrarMinMax) {
        setBackground(EstiloUI.FONDO_TARJETA);
        setPreferredSize(new Dimension(window.getWidth(), 36));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(48, 54, 61)));

        // Arrastrar ventana
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = window.getLocation().x;
                int thisY = window.getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                window.setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        // Título a la izquierda
        JLabel lblTitulo = new JLabel("  " + titulo);
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(lblTitulo, BorderLayout.WEST);

        // Panel de botones a la derecha
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotones.setBackground(EstiloUI.FONDO_TARJETA);

        // Botón Minimizar / Maximizar
        if (mostrarMinMax && window instanceof Frame) {
            JButton btnMin = crearBotonBarra("—", false);
            btnMin.addActionListener(e -> ((Frame) window).setExtendedState(Frame.ICONIFIED));
            panelBotones.add(btnMin);

            JButton btnMax = crearBotonBarra("□", false);
            btnMax.addActionListener(e -> {
                Frame frame = (Frame) window;
                if (frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    frame.setExtendedState(Frame.NORMAL);
                } else {
                    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                }
            });
            panelBotones.add(btnMax);
        }

        // Botón Cerrar
        JButton btnCerrar = crearBotonBarra("✕", true);
        btnCerrar.addActionListener(e -> {
            if (window instanceof JFrame jframe) {
                if (jframe.getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
                    System.exit(0);
                } else {
                    jframe.dispose();
                }
            } else {
                window.dispose();
            }
        });
        panelBotones.add(btnCerrar);

        add(panelBotones, BorderLayout.EAST);
    }

    private JButton crearBotonBarra(String texto, boolean esCerrar) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(45, 35));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(EstiloUI.TEXTO_MUTED);
        btn.setBackground(EstiloUI.FONDO_TARJETA);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (esCerrar) {
                    btn.setBackground(EstiloUI.ROJO_ALERTA);
                    btn.setForeground(EstiloUI.TEXTO_BLANCO);
                } else {
                    btn.setBackground(new Color(48, 54, 61));
                    btn.setForeground(EstiloUI.TEXTO_BLANCO);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(EstiloUI.FONDO_TARJETA);
                btn.setForeground(EstiloUI.TEXTO_MUTED);
            }
        });

        return btn;
    }
}
