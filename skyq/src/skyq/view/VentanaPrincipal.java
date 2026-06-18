package skyq.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import skyq.logic.SesionManager;
import skyq.model.Usuario;

public final class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTabbedPane tabbedPane;

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("SkyQ - Consola Aeroportuaria de Control Integrado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(EstiloUI.FONDO_TARJETA);
        tabbedPane.setForeground(EstiloUI.TEXTO_BLANCO);

        Usuario usuarioActual = SesionManager.getInstance().getUsuarioActual();

        if (usuarioActual != null && usuarioActual.isGerente()) {
            tabbedPane.addTab("1. Centro de Comando (Gerente)", new PanelGerente());
            tabbedPane.addTab("2. Gestión Operativa de Pasajeros", new PanelCheckIn());
            tabbedPane.addTab("3. Ventas / Booking", new PanelVentas());
            tabbedPane.addTab("4. Programación de Vuelos", new PanelVuelos());
        } else if (usuarioActual != null && usuarioActual.isOperario()) {
            tabbedPane.addTab("1. Gestión Operativa de Pasajeros", new PanelCheckIn());
            tabbedPane.addTab("2. Ventas / Booking", new PanelVentas());
            tabbedPane.addTab("3. Programación de Vuelos", new PanelVuelos());
        }

        // --- BARRA SUPERIOR DE SESIÓN ---
        JPanel panelSesion = new JPanel(new BorderLayout());
        panelSesion.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        panelSesion.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblUsuario = new JLabel("Sesión activa: " + (usuarioActual != null ? usuarioActual.getUsername() + " (" + usuarioActual.getRol() + ")" : "Anónimo"));
        lblUsuario.setForeground(EstiloUI.TEXTO_MUTED);
        lblUsuario.setFont(EstiloUI.FUENTE_LABEL);

        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setBackground(EstiloUI.ROJO_ALERTA);
        btnCerrarSesion.setForeground(EstiloUI.TEXTO_BLANCO);
        btnCerrarSesion.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        EstiloUI.aplicarHover(btnCerrarSesion, EstiloUI.ROJO_ALERTA, EstiloUI.ROJO_ALERTA.brighter());
        
        btnCerrarSesion.addActionListener(e -> {
            SesionManager.getInstance().cerrarSesion();
            new PantallaLogin().setVisible(true);
            dispose();
        });

        panelSesion.add(lblUsuario, BorderLayout.WEST);
        panelSesion.add(btnCerrarSesion, BorderLayout.EAST);

        add(panelSesion, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {
            
        }

        SwingUtilities.invokeLater(() -> new PantallaLogin().setVisible(true));
    }
}