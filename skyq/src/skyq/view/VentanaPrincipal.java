package skyq.view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class VentanaPrincipal extends JFrame {

    private JTabbedPane tabbedPane;

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("SkyQ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Gestión Gerente", new PanelGerente());
        tabbedPane.addTab("Check-In", new PanelCheckIn());
        tabbedPane.addTab("Abordaje", new PanelAbordaje());
        tabbedPane.addTab("Desembarque", new PanelDesembarque());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VentanaPrincipal().setVisible(true);
            }
        });
    }
}