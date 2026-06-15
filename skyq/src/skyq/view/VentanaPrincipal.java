package skyq.view;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import skyq.logic.SesionManager;
import skyq.model.Usuario;

public class VentanaPrincipal extends JFrame {

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
        } else if (usuarioActual != null && usuarioActual.isOperario()) {
            tabbedPane.addTab("1. Gestión Operativa de Pasajeros", new PanelCheckIn());
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}

        SwingUtilities.invokeLater(() -> new PantallaLogin().setVisible(true));
    }
}