package skyq.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import skyq.dao.ConfiguracionDAO;
import skyq.logic.ValidadorFormulario;

public final class DialogoConfigurarCabina extends JDialog {
    private static final long serialVersionUID = 1L;
    private final String matricula;
    private final String distribucion;
    private final transient ConfiguracionDAO dao = new ConfiguracionDAO();
    private DialogoEditarAvion editorAvionParent;

    private JTextField txtVipDist, txtEjecDist, txtEconDist;
    private JSpinner spinVipFilas, spinEjecFilas, spinEconFilas;

    private String vipDist = "2-2";
    private int vipFilas = 0;
    private String ejecDist = "2-4-2";
    private int ejecFilas = 0;
    private String econDist = "3-3";
    private int econFilas = 0;

    public DialogoConfigurarCabina(Frame parent, String matricula, String distribucion) {
        super(parent, "Configurar Cabina — " + matricula, true);
        this.matricula = matricula;
        this.distribucion = distribucion;

        getContentPane().setBackground(EstiloUI.FONDO_TARJETA);
        setSize(500, 320);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        parseDistribucion();
        initComponents();
    }

    public DialogoConfigurarCabina(DialogoEditarAvion parent, String matricula, String distribucion) {
        super(parent, "Configurar Cabina — " + matricula, true);
        this.editorAvionParent = parent;
        this.matricula = matricula;
        this.distribucion = distribucion;

        getContentPane().setBackground(EstiloUI.FONDO_TARJETA);
        setSize(500, 320);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        parseDistribucion();
        initComponents();
    }

    private void parseDistribucion() {
        if (distribucion != null && !distribucion.isEmpty()) {
            String[] partesClases = distribucion.split("\\|");
            for (String pc : partesClases) {
                String[] datos = pc.split(":");
                if (datos.length == 3) {
                    String clase = datos[0];
                    String distStr = datos[1];
                    int filasVal = Integer.parseInt(datos[2]);
                    if ("VIP".equalsIgnoreCase(clase)) {
                        vipDist = distStr;
                        vipFilas = filasVal;
                    } else if ("EJEC".equalsIgnoreCase(clase)) {
                        ejecDist = distStr;
                        ejecFilas = filasVal;
                    } else if ("ECON".equalsIgnoreCase(clase)) {
                        econDist = distStr;
                        econFilas = filasVal;
                    }
                }
            }
        }
    }

    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(EstiloUI.FONDO_TARJETA);
        tabs.setForeground(EstiloUI.TEXTO_BLANCO);

        // Pestaña VIP
        txtVipDist = new JTextField(vipDist, 10);
        estilizarCampo(txtVipDist);
        spinVipFilas = new JSpinner(new SpinnerNumberModel(vipFilas, 0, 100, 1));
        tabs.addTab("VIP", crearTabClase(txtVipDist, spinVipFilas));

        // Pestaña Ejecutiva
        txtEjecDist = new JTextField(ejecDist, 10);
        estilizarCampo(txtEjecDist);
        spinEjecFilas = new JSpinner(new SpinnerNumberModel(ejecFilas, 0, 100, 1));
        tabs.addTab("Ejecutiva", crearTabClase(txtEjecDist, spinEjecFilas));

        // Pestaña Económica
        txtEconDist = new JTextField(econDist, 10);
        estilizarCampo(txtEconDist);
        spinEconFilas = new JSpinner(new SpinnerNumberModel(econFilas, 0, 100, 1));
        tabs.addTab("Económica", crearTabClase(txtEconDist, spinEconFilas));

        add(tabs, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.setBackground(EstiloUI.FONDO_TARJETA);

        JButton btnGuardar = crearBoton("💾  GUARDAR", EstiloUI.VERDE_NEON);
        JButton btnCancelar = crearBoton("✕  CANCELAR", EstiloUI.GRIS_BOTON_PASIVO);

        btnGuardar.addActionListener(e -> guardarConfiguracion());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel crearTabClase(JTextField txtDist, JSpinner spinFilas) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EstiloUI.FONDO_TARJETA);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(crearLabel("Distribución (ej: 3-3):"), gbc);
        gbc.gridx = 1;
        panel.add(txtDist, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(crearLabel("Filas:"), gbc);
        gbc.gridx = 1;
        panel.add(spinFilas, gbc);

        return panel;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        campo.setForeground(EstiloUI.TEXTO_BLANCO);
        campo.setCaretColor(EstiloUI.TEXTO_BLANCO);
        campo.setBorder(EstiloUI.BORDE_COMPONENTE);
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

    private void guardarConfiguracion() {
        String finalVipDist = txtVipDist.getText().trim();
        int finalVipFilas = (int) spinVipFilas.getValue();

        String finalEjecDist = txtEjecDist.getText().trim();
        int finalEjecFilas = (int) spinEjecFilas.getValue();

        String finalEconDist = txtEconDist.getText().trim();
        int finalEconFilas = (int) spinEconFilas.getValue();

        // Validaciones de aviación
        if (finalVipFilas > 0 && !ValidadorFormulario.esDistribucionValida(finalVipDist)) {
            JOptionPane.showMessageDialog(this,
                    "Formato VIP inválido. Use números para asientos y guiones para pasillos. Ej: 2-2. Los extremos no pueden ser pasillos.",
                    "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (finalEjecFilas > 0 && !ValidadorFormulario.esDistribucionValida(finalEjecDist)) {
            JOptionPane.showMessageDialog(this,
                    "Formato Ejecutiva inválido. Use números para asientos y guiones para pasillos. Ej: 2-4-2. Los extremos no pueden ser pasillos.",
                    "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (finalEconFilas > 0 && !ValidadorFormulario.esDistribucionValida(finalEconDist)) {
            JOptionPane.showMessageDialog(this,
                    "Formato Económica inválido. Use números para asientos y guiones para pasillos. Ej: 3-3. Los extremos no pueden ser pasillos.",
                    "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (finalVipFilas == 0 && finalEjecFilas == 0 && finalEconFilas == 0) {
            JOptionPane.showMessageDialog(this, "Debe configurar al menos una clase con más de 0 filas.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (finalVipFilas > 0) {
            sb.append("VIP:").append(finalVipDist).append(":").append(finalVipFilas);
        }
        if (finalEjecFilas > 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("EJEC:").append(finalEjecDist).append(":").append(finalEjecFilas);
        }
        if (finalEconFilas > 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("ECON:").append(finalEconDist).append(":").append(finalEconFilas);
        }

        String nuevaDistribucion = sb.toString();
        int nuevaCapacidad = skyq.logic.AutoCalculadorCabina.calcularCapacidadTotal(nuevaDistribucion);

        if (dao.actualizarConfiguracion(matricula, nuevaDistribucion)) {
            // Actualizar la capacidad en la base de datos
            skyq.dao.AvionDAO avionDAO = new skyq.dao.AvionDAO();
            avionDAO.actualizarCapacidad(matricula, nuevaCapacidad);

            // Registrar la auditoria
            skyq.dao.AuditoriaDAO auditoriaDAO = new skyq.dao.AuditoriaDAO();
            String username = "Sistema";
            if (skyq.logic.SesionManager.getInstance().getUsuarioActual() != null) {
                username = skyq.logic.SesionManager.getInstance().getUsuarioActual().getUsername();
            }
            auditoriaDAO.registrarAccion(username, "RECONFIGURAR_CABINA",
                    "Reconfiguración de cabina. Nueva capacidad: " + nuevaCapacidad);

            // Actualizar la UI del dialogo padre
            if (editorAvionParent != null) {
                editorAvionParent.actualizarCapacidadUI(nuevaCapacidad);
            }

            JOptionPane.showMessageDialog(this, "Configuración guardada correctamente. Capacidad actualizada a: " + nuevaCapacidad);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar configuración.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
