package skyq.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import skyq.dao.ConfiguracionDAO;

public class DialogoConfigurarCabina extends JDialog {
    private String matricula;
    private String distribucion;
    private ConfiguracionDAO dao = new ConfiguracionDAO();

    public DialogoConfigurarCabina(Frame parent, String matricula, String distribucion) {
        super(parent, "Configurar Cabina — " + matricula, true);
        this.matricula = matricula;
        this.distribucion = distribucion;

        getContentPane().setBackground(EstiloUI.FONDO_TARJETA);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(EstiloUI.FONDO_TARJETA);
        tabs.setForeground(EstiloUI.TEXTO_BLANCO);

        tabs.addTab("VIP", crearTabClase("VIP"));
        tabs.addTab("Ejecutiva", crearTabClase("EJEC"));
        tabs.addTab("Económica", crearTabClase("ECON"));

        add(tabs, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.setBackground(EstiloUI.FONDO_TARJETA);

        JButton btnGuardar = crearBoton("💾  GUARDAR", EstiloUI.VERDE_NEON);
        JButton btnCancelar = crearBoton("✕  CANCELAR", EstiloUI.GRIS_BOTON_PASIVO);

        btnGuardar.addActionListener(e -> {
            if (dao.actualizarConfiguracion(matricula, distribucion)) {
                JOptionPane.showMessageDialog(this, "Configuración guardada correctamente.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar configuración.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel crearTabClase(String clase) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EstiloUI.FONDO_TARJETA);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(crearLabel("Distribución:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cbDistribucion = new JComboBox<>(obtenerDistribuciones(clase));
        estilizarCombo(cbDistribucion);
        panel.add(cbDistribucion, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(crearLabel("Filas:"), gbc);
        gbc.gridx = 1;
        JSpinner spinFilas = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        spinFilas.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        panel.add(spinFilas, gbc);

        return panel;
    }

    private String[] obtenerDistribuciones(String clase) {
        return switch (clase) {
            case "VIP" -> new String[]{"2-2", "2-2-2", "1-2-1"};
            case "EJEC" -> new String[]{"2-4-2", "2-3-2", "3-3-3"};
            case "ECON" -> new String[]{"3-3", "3-4-3", "2-4-2"};
            default -> new String[]{"3-3"};
        };
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(EstiloUI.TEXTO_BLANCO);
        label.setFont(EstiloUI.FUENTE_LABEL);
        return label;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(EstiloUI.TEXTO_BLANCO);
        btn.setFont(EstiloUI.FUENTE_LABEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        combo.setForeground(EstiloUI.TEXTO_BLANCO);
        combo.setBorder(EstiloUI.BORDE_COMPONENTE);
    }
}
