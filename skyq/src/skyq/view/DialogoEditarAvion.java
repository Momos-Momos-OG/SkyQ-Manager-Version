package skyq.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import skyq.dao.AuditoriaDAO;
import skyq.dao.AvionDAO;
import skyq.dao.ConfiguracionDAO;
import skyq.dao.MantenimientoDAO;
import skyq.logic.AutoCalculadorCabina;
import skyq.logic.LoggerManager;
import skyq.logic.SesionManager;
import skyq.logic.ValidadorFormulario;
import skyq.model.Avion;
import skyq.model.Mantenimiento;
import java.time.LocalDate;
import java.util.List;

public class DialogoEditarAvion extends JDialog {
    private Avion avion;
    private JTextField txtModelo, txtCapacidad;
    private JComboBox<String> cbEstado;
    private JTable tablaManto;
    private JButton btnActualizar, btnEditarAsientos, btnRegistrarManto, btnVerManto;

    public DialogoEditarAvion(Frame padre, Avion avion) {
        super(padre, "✈  Editar Aeronave: " + avion.getMatricula(), true);
        this.avion = avion;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Crear pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(EstiloUI.FONDO_TARJETA);
        tabs.setForeground(EstiloUI.TEXTO_BLANCO);
        tabs.setUI(new javax.swing.plaf.metal.MetalTabbedPaneUI() {
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 8;
            }
        });

        tabs.addTab("📋  Información", crearPanelInformacion());
        tabs.addTab("🔧  Mantenimiento", crearPanelMantenimiento());
        tabs.addTab("📺  Asientos", crearPanelAsientos());

        add(tabs, BorderLayout.CENTER);

        // Panel de botones inferiores
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        btnActualizar = new JButton("✔  Guardar Cambios");
        btnActualizar.setBackground(EstiloUI.AZUL_ACCENT);
        btnActualizar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnActualizar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnActualizar.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnActualizar, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());
        btnActualizar.addActionListener(e -> guardarCambios());

        JButton btnCancelar = new JButton("✕  Cancelar");
        btnCancelar.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        btnCancelar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnCancelar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnCancelar.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnCancelar, EstiloUI.GRIS_BOTON_PASIVO, new Color(55, 62, 71));
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnActualizar);
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        setSize(500, 600);
        setLocationRelativeTo(padre);
    }

    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Matrícula (solo lectura)
        JLabel lblMatricula = new JLabel("Matrícula:");
        lblMatricula.setForeground(EstiloUI.TEXTO_MUTED);
        lblMatricula.setFont(EstiloUI.FUENTE_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblMatricula, gbc);

        JLabel lblMatriculaVal = new JLabel(avion.getMatricula());
        lblMatriculaVal.setForeground(Color.CYAN);
        lblMatriculaVal.setFont(EstiloUI.FUENTE_SUBTITULO);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(lblMatriculaVal, gbc);

        // Modelo
        JLabel lblModelo = new JLabel("Modelo:");
        lblModelo.setForeground(EstiloUI.TEXTO_MUTED);
        lblModelo.setFont(EstiloUI.FUENTE_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblModelo, gbc);

        txtModelo = new JTextField(avion.getModelo(), 15);
        estilizarCampo(txtModelo);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtModelo, gbc);

        // Capacidad
        JLabel lblCapacidad = new JLabel("Capacidad:");
        lblCapacidad.setForeground(EstiloUI.TEXTO_MUTED);
        lblCapacidad.setFont(EstiloUI.FUENTE_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblCapacidad, gbc);

        txtCapacidad = new JTextField(String.valueOf(avion.getCapacidad()), 15);
        estilizarCampo(txtCapacidad);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtCapacidad, gbc);

        // Estado
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setForeground(EstiloUI.TEXTO_MUTED);
        lblEstado.setFont(EstiloUI.FUENTE_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblEstado, gbc);

        cbEstado = new JComboBox<>(new String[] { "Disponible", "En Vuelo", "En mantenimiento", "Fuera de servicio" });
        cbEstado.setSelectedItem(avion.getEstado());
        cbEstado.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        cbEstado.setForeground(EstiloUI.TEXTO_BLANCO);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(cbEstado, gbc);

        // Botón Editar Asientos
        btnEditarAsientos = new JButton("🛩  Editar Configuración de Asientos");
        btnEditarAsientos.setBackground(EstiloUI.VERDE_NEON);
        btnEditarAsientos.setForeground(EstiloUI.TEXTO_BLANCO);
        btnEditarAsientos.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnEditarAsientos.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnEditarAsientos, EstiloUI.VERDE_NEON, EstiloUI.VERDE_NEON.darker());
        btnEditarAsientos.addActionListener(e -> abrirEditorAsientos());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 8, 10);
        panel.add(btnEditarAsientos, gbc);

        return panel;
    }

    private JPanel crearPanelMantenimiento() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotones.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        btnRegistrarManto = new JButton("📝  Registrar Mantenimiento");
        btnRegistrarManto.setBackground(new Color(255, 152, 0));
        btnRegistrarManto.setForeground(EstiloUI.TEXTO_BLANCO);
        btnRegistrarManto.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnRegistrarManto, new Color(255, 152, 0), new Color(255, 152, 0).darker());
        btnRegistrarManto.addActionListener(e -> abrirDialogoMantenimiento());

        btnVerManto = new JButton("📋  Ver Historial");
        btnVerManto.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        btnVerManto.setForeground(EstiloUI.TEXTO_BLANCO);
        btnVerManto.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnVerManto, EstiloUI.GRIS_BOTON_PASIVO, new Color(55, 62, 71));

        panelBotones.add(btnRegistrarManto);
        panelBotones.add(btnVerManto);
        panel.add(panelBotones, BorderLayout.NORTH);

        // Tabla de mantenimientos
        String[] columnas = { "Fecha", "Estado", "Descripción" };
        tablaManto = new JTable(new javax.swing.table.DefaultTableModel(columnas, 0));
        tablaManto.setBackground(EstiloUI.FONDO_TARJETA);
        tablaManto.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaManto.setSelectionBackground(EstiloUI.AZUL_ACCENT);
        tablaManto.getTableHeader().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tablaManto.getTableHeader().setForeground(EstiloUI.TEXTO_MUTED);

        JScrollPane scroll = new JScrollPane(tablaManto);
        scroll.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scroll.setBorder(EstiloUI.BORDE_COMPONENTE);
        panel.add(scroll, BorderLayout.CENTER);

        cargarMantenimientos();
        return panel;
    }

    private JPanel crearPanelAsientos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        final ConfiguracionDAO confDAO = new ConfiguracionDAO();
        String dist = confDAO.obtenerDistribucion(avion.getMatricula());
        if (dist == null) {
            dist = AutoCalculadorCabina.calcularDistribucion(avion.getCapacidad());
        }
        final String distribucion = dist;

        PanelCabinaPreview cabinaPreview = new PanelCabinaPreview(distribucion);
        JScrollPane scroll = new JScrollPane(cabinaPreview);
        scroll.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scroll.setBorder(EstiloUI.BORDE_COMPONENTE);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnEditar = new JButton("✏  Editar Distribución");
        btnEditar.setBackground(EstiloUI.AZUL_ACCENT);
        btnEditar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnEditar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnEditar.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnEditar, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());
        btnEditar.addActionListener(e -> {
            DialogoConfigurarCabina dialogo = new DialogoConfigurarCabina(
                    (Frame) SwingUtilities.getWindowAncestor(DialogoEditarAvion.this),
                    avion.getMatricula(),
                    distribucion);
            dialogo.setVisible(true);
            cabinaPreview.actualizarDistribucion(confDAO.obtenerDistribucion(avion.getMatricula()));
        });
        panel.add(btnEditar, BorderLayout.SOUTH);

        return panel;
    }

    private void cargarMantenimientos() {
        MantenimientoDAO mDAO = new MantenimientoDAO();
        List<Mantenimiento> mantenimientos = mDAO.obtenerMantenimientoPorMatricula(avion.getMatricula());

        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tablaManto.getModel();
        modelo.setRowCount(0);

        for (Mantenimiento m : mantenimientos) {
            modelo.addRow(new Object[] {
                    m.getFechaInicio(),
                    m.getEstado(),
                    m.getDescripcion(),
                    "—"
            });
        }
    }

    private void abrirEditorAsientos() {
        ConfiguracionDAO confDAO = new ConfiguracionDAO();
        String distribucion = confDAO.obtenerDistribucion(avion.getMatricula());
        if (distribucion == null) {
            distribucion = AutoCalculadorCabina.calcularDistribucion(avion.getCapacidad());
            confDAO.guardarConfiguracion(avion.getMatricula(), distribucion);
        }
        new DialogoConfigurarCabina(
                (Frame) SwingUtilities.getWindowAncestor(this),
                avion.getMatricula(),
                distribucion).setVisible(true);
    }

    private void abrirDialogoMantenimiento() {
        JDialog dialogo = new JDialog(this, "📝  Registrar Mantenimiento", true);
        dialogo.setLayout(new GridBagLayout());
        dialogo.getContentPane().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        dialogo.setSize(400, 300);
        dialogo.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cbEstado = new JComboBox<>(new String[] { "Programado", "En Progreso", "Completado" });
        JTextArea txtDesc = new JTextArea(3, 20);

        cbEstado.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        cbEstado.setForeground(EstiloUI.TEXTO_BLANCO);
        estilizarCampo(new JTextField());
        txtDesc.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        txtDesc.setForeground(EstiloUI.TEXTO_BLANCO);
        txtDesc.setBorder(EstiloUI.BORDE_COMPONENTE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialogo.add(crearLabel("Estado:"), gbc);
        gbc.gridx = 1;
        dialogo.add(cbEstado, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogo.add(crearLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        dialogo.add(new JScrollPane(txtDesc), gbc);

        JButton btnGuardar = new JButton("✔  Guardar");
        btnGuardar.setBackground(EstiloUI.AZUL_ACCENT);
        btnGuardar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnGuardar.setBorderPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialogo.add(btnGuardar, gbc);

        btnGuardar.addActionListener(e -> {
            Mantenimiento m = new Mantenimiento();
            m.setMatricula(avion.getMatricula());
            m.setFechaInicio(LocalDate.now());
            m.setDescripcion(txtDesc.getText());
            m.setEstado((String) cbEstado.getSelectedItem());

            MantenimientoDAO mDAO = new MantenimientoDAO();
            if (mDAO.insertar(m)) {
                LoggerManager.getInstance().logInfo("Mantenimiento registrado: " + avion.getMatricula());
                AuditoriaDAO audDAO = new AuditoriaDAO();
                audDAO.registrarAccion(
                        SesionManager.getInstance().getUsuarioActual().getUsername(),
                        "REGISTRAR_MANTENIMIENTO",
                        "Matrícula: " + avion.getMatricula() + ", Estado: " + cbEstado.getSelectedItem());
                JOptionPane.showMessageDialog(dialogo, "Mantenimiento guardado.");
                cargarMantenimientos();
                dialogo.dispose();
            }
        });

        dialogo.setVisible(true);
    }

    private void estilizarCampo(JTextField campo) {
        campo.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        campo.setForeground(EstiloUI.TEXTO_BLANCO);
        campo.setCaretColor(EstiloUI.TEXTO_BLANCO);
        campo.setBorder(EstiloUI.BORDE_COMPONENTE);
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloUI.TEXTO_MUTED);
        l.setFont(EstiloUI.FUENTE_LABEL);
        return l;
    }

    private void guardarCambios() {
        try {
            avion.setModelo(txtModelo.getText().trim());
            avion.setCapacidad(Integer.parseInt(txtCapacidad.getText().trim()));
            avion.setEstado((String) cbEstado.getSelectedItem());

            if (!ValidadorFormulario.esTextoValido(avion.getModelo())) {
                JOptionPane.showMessageDialog(this, "Modelo inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AvionDAO avDAO = new AvionDAO();
            if (avDAO.actualizarAvion(avion)) {
                LoggerManager.getInstance().logInfo("Avión actualizado: " + avion.getMatricula());
                AuditoriaDAO audDAO = new AuditoriaDAO();
                audDAO.registrarAccion(
                        SesionManager.getInstance().getUsuarioActual().getUsername(),
                        "ACTUALIZAR_AVION",
                        "Matrícula: " + avion.getMatricula() + ", Cambios: modelo, capacidad, estado");
                JOptionPane.showMessageDialog(this, "Aeronave actualizada correctamente.");
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacidad debe ser un número válido.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
