package skyq.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import skyq.dao.AuditoriaDAO;
import skyq.dao.AvionDAO;
import skyq.dao.ConfiguracionDAO;

import skyq.logic.AutoCalculadorCabina;
import skyq.logic.LoggerManager;
import skyq.logic.SesionManager;
import skyq.logic.ValidadorFormulario;
import skyq.model.Avion;
import skyq.model.EstadoAvion;

/**
 * Panel principal del Módulo A: Centro de Comando del Gerente.
 * SRS v4.0: Aeropuerto Único. Pilotos y Hospedaje fuera de alcance.
 *
 * Arquitectura interna (CardLayout):
 * - PANTALLA_RADAR    → PanelListaAviones con cuadrícula adaptativa
 * - PANTALLA_DASHBOARD → Dashboard de 2 columnas:
 *   Col 1: CRUD de aviones
 *   Col 2: Diseñador de mapa de asientos (Cabina Preview)
 */
public final class PanelGerente extends JPanel {
    private static final long serialVersionUID = 1L;

    // ── Navegación principal ──
    private CardLayout cardNavigator;
    private JPanel mainDynamicContainer;
    private PanelListaAviones panelRadarView;
    private JButton btnRadarView, btnRegistroView;

    // ── Formulario de aviones ──
    private JTextField txtMatricula, txtModelo, txtCapacidad;
    private JComboBox<EstadoAvion> comboEstado;
    private JTextArea txtDescripcion;
    private JButton btnRegistrarAvion;

    // ── DAOs ──
    private final transient AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    public PanelGerente() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setLayout(new BorderLayout());
        initComponents();
    }

    // ═══════════════════════════════════════════════════════
    // INICIALIZACIÓN PRINCIPAL
    // ═══════════════════════════════════════════════════════

    private void initComponents() {
        // ── Sidebar de navegación ──
        JPanel sidebar = new JPanel();
        sidebar.setBackground(EstiloUI.FONDO_TARJETA);
        sidebar.setPreferredSize(new Dimension(180, 800));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(48, 54, 61)));
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 25));

        btnRadarView = new JButton("✈  Terminal");
        btnRegistroView = new JButton("⚙  Registro");
        JButton btnAuditoria = new JButton("📊  Auditoría");
        aplicarEstiloBotonMenu(btnRadarView, true);
        aplicarEstiloBotonMenu(btnRegistroView, false);
        aplicarEstiloBotonMenu(btnAuditoria, false);
        sidebar.add(btnRadarView);
        sidebar.add(btnRegistroView);
        sidebar.add(btnAuditoria);
        add(sidebar, BorderLayout.WEST);

        // ── Contenedor dinámico ──
        cardNavigator = new CardLayout();
        mainDynamicContainer = new JPanel(cardNavigator);
        mainDynamicContainer.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        panelRadarView = new PanelListaAviones();
        mainDynamicContainer.add(panelRadarView, "PANTALLA_RADAR");
        mainDynamicContainer.add(construirDashboard(), "PANTALLA_DASHBOARD");
        add(mainDynamicContainer, BorderLayout.CENTER);

        // ── Listeners de navegación ──
        btnRadarView.addActionListener(e -> {
            aplicarEstiloBotonMenu(btnRadarView, true);
            aplicarEstiloBotonMenu(btnRegistroView, false);
            aplicarEstiloBotonMenu(btnAuditoria, false);
            panelRadarView.recargarDatosAviones();
            cardNavigator.show(mainDynamicContainer, "PANTALLA_RADAR");
        });

        btnRegistroView.addActionListener(e -> {
            aplicarEstiloBotonMenu(btnRadarView, false);
            aplicarEstiloBotonMenu(btnRegistroView, true);
            aplicarEstiloBotonMenu(btnAuditoria, false);
            cardNavigator.show(mainDynamicContainer, "PANTALLA_DASHBOARD");
        });

        btnAuditoria.addActionListener(e -> {
            aplicarEstiloBotonMenu(btnRadarView, false);
            aplicarEstiloBotonMenu(btnRegistroView, false);
            aplicarEstiloBotonMenu(btnAuditoria, true);
            abrirDialogoAuditoria();
        });
    }

    // ═══════════════════════════════════════════════════════
    // DASHBOARD DE 2 COLUMNAS (SRS v4.0)
    // ═══════════════════════════════════════════════════════

    private JPanel construirDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(1, 2, 20, 0));
        dashboard.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        dashboard.setBorder(new EmptyBorder(25, 25, 25, 25));

        dashboard.add(construirColumnaAviones());
        dashboard.add(construirColumnaMapeo());

        return dashboard;
    }

    // ═══════════════════════════════════════════════════════
    // COLUMNA 1: CRUD de Aviones
    // ═══════════════════════════════════════════════════════

    private JPanel construirColumnaAviones() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(EstiloUI.FONDO_TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        // Placeholder de imagen de aeronave
        JPanel fotoPlaceholder = new JPanel(new BorderLayout());
        fotoPlaceholder.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        fotoPlaceholder.setPreferredSize(new Dimension(110, 110));
        fotoPlaceholder.setBorder(EstiloUI.BORDE_COMPONENTE);
        JLabel lblPlus = new JLabel("✈", SwingConstants.CENTER);
        lblPlus.setForeground(EstiloUI.TEXTO_MUTED);
        lblPlus.setFont(new Font("SansSerif", Font.BOLD, 42));
        fotoPlaceholder.add(lblPlus, BorderLayout.CENTER);

        txtMatricula = crearCampoTexto();
        txtModelo = crearCampoTexto();
        txtCapacidad = crearCampoTexto();
        txtDescripcion = new JTextArea(3, 10);
        txtDescripcion.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        txtDescripcion.setForeground(EstiloUI.TEXTO_BLANCO);
        txtDescripcion.setBorder(EstiloUI.BORDE_COMPONENTE);
        txtDescripcion.setLineWrap(true);

        comboEstado = new JComboBox<>(EstadoAvion.values());
        comboEstado.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        comboEstado.setForeground(EstiloUI.TEXTO_BLANCO);
        comboEstado.setBorder(EstiloUI.BORDE_COMPONENTE);

        btnRegistrarAvion = new JButton("✚  REGISTRAR EN FLOTA");
        btnRegistrarAvion.setBackground(EstiloUI.AZUL_ACCENT);
        btnRegistrarAvion.setForeground(EstiloUI.TEXTO_BLANCO);
        btnRegistrarAvion.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnRegistrarAvion.setBorderPainted(false);
        EstiloUI.aplicarHover(btnRegistrarAvion, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.NONE;
        card.add(fotoPlaceholder, g);

        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = 1;
        card.add(crearLabel("Matrícula:"), g);
        g.gridx = 1;
        card.add(txtMatricula, g);

        g.gridx = 0;
        g.gridy = 2;
        card.add(crearLabel("Modelo:"), g);
        g.gridx = 1;
        card.add(txtModelo, g);

        g.gridx = 0;
        g.gridy = 3;
        card.add(crearLabel("Capacidad:"), g);
        g.gridx = 1;
        card.add(txtCapacidad, g);

        g.gridx = 0;
        g.gridy = 4;
        card.add(crearLabel("Estado:"), g);
        g.gridx = 1;
        card.add(comboEstado, g);

        g.gridx = 0;
        g.gridy = 5;
        card.add(crearLabel("Notas:"), g);
        g.gridx = 1;
        card.add(new JScrollPane(txtDescripcion), g);

        g.gridx = 0;
        g.gridy = 6;
        g.gridwidth = 2;
        g.insets = new Insets(15, 5, 5, 5);
        card.add(btnRegistrarAvion, g);

        btnRegistrarAvion.addActionListener(e -> guardarAvionBaseDatos());

        return card;
    }

    // ═══════════════════════════════════════════════════════
    // COLUMNA 2: Previsualización de Asientos (Scrolleable)
    // ═══════════════════════════════════════════════════════

    private JPanel construirColumnaMapeo() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(EstiloUI.FONDO_TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTitulo = new JLabel("📺  VISTA PREVIA DE CABINA", SwingConstants.CENTER);
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);
        card.add(lblTitulo, BorderLayout.NORTH);

        PanelCabinaPreview cabinaPreview = new PanelCabinaPreview("");
        JScrollPane scrollCabina = new JScrollPane(cabinaPreview);
        scrollCabina.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollCabina.setBorder(EstiloUI.BORDE_COMPONENTE);
        scrollCabina.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollCabina.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollCabina.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scrollCabina, BorderLayout.CENTER);

        JButton btnEditar = new JButton("✏  EDITAR CONFIGURACIÓN");
        btnEditar.setBackground(EstiloUI.AZUL_ACCENT);
        btnEditar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnEditar.setBorderPainted(false);
        card.add(btnEditar, BorderLayout.SOUTH);

        btnEditar.addActionListener(e -> {
            String matriculaActual = txtMatricula.getText().trim();
            if (matriculaActual.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un avión del radar para editar su configuración.",
                        "Sin Selección", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AvionDAO avDAO = new AvionDAO();
            if (!avDAO.verificarMatriculaRegistrada(matriculaActual)) {
                JOptionPane.showMessageDialog(this, "Registre primero el avión en la flota.", "Avión No Registrado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());
            ConfiguracionDAO confDAO = new ConfiguracionDAO();
            String distribucion = confDAO.obtenerDistribucion(matriculaActual);
            if (distribucion == null) {
                distribucion = AutoCalculadorCabina.calcularDistribucion(capacidad);
                confDAO.guardarConfiguracion(matriculaActual, distribucion);
            }
            DialogoConfigurarCabina dialogo = new DialogoConfigurarCabina(
                    (Frame) SwingUtilities.getWindowAncestor(this), matriculaActual, distribucion);
            dialogo.setVisible(true);
            cabinaPreview.actualizarDistribucion(confDAO.obtenerDistribucion(matriculaActual));
        });

        return card;
    }

    // ═══════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO — AVIONES
    // ═══════════════════════════════════════════════════════

    private void guardarAvionBaseDatos() {
        String matricula = txtMatricula.getText().trim();
        String modelo = txtModelo.getText().trim();
        String capacidadTexto = txtCapacidad.getText().trim();
        EstadoAvion estado = (EstadoAvion) comboEstado.getSelectedItem();

        if (!ValidadorFormulario.esTextoValido(matricula)) {
            JOptionPane.showMessageDialog(this, "Ingrese una matrícula válida.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidadorFormulario.esMatriculaValida(matricula)) {
            JOptionPane.showMessageDialog(this, "Formato de matrícula inválido (ej: HC-BXA).", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidadorFormulario.esTextoValido(modelo)) {
            JOptionPane.showMessageDialog(this, "Ingrese un modelo válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidadorFormulario.esNumeroPositivo(capacidadTexto)) {
            JOptionPane.showMessageDialog(this, "Capacidad debe ser un número mayor a 0.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int capacidadEstimada = Integer.parseInt(capacidadTexto);
        String distribucion = skyq.logic.AutoCalculadorCabina.calcularDistribucion(capacidadEstimada);
        int capacidadReal = skyq.logic.AutoCalculadorCabina.calcularCapacidadTotal(distribucion);

        AvionDAO avionDAO = new AvionDAO();
        if (avionDAO.verificarMatriculaRegistrada(matricula)) {
            JOptionPane.showMessageDialog(this, "Esta matrícula ya está registrada en la flota.", "Duplicado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (avionDAO.guardarAvion(new Avion(matricula, modelo, capacidadReal, estado))) {
            ConfiguracionDAO confDAO = new ConfiguracionDAO();
            confDAO.guardarConfiguracion(matricula, distribucion);

            LoggerManager.getInstance().logInfo("Avión registrado: " + matricula + ". Capacidad real calculada: " + capacidadReal);
            AuditoriaDAO audDAO = new AuditoriaDAO();
            audDAO.registrarAccion(
                    SesionManager.getInstance().getUsuarioActual().getUsername(),
                    "REGISTRAR_AVION",
                    "Matrícula: " + matricula + ", Modelo: " + modelo + ", Capacidad Estimada: " + capacidadEstimada + ", Capacidad Real: " + capacidadReal);

            JOptionPane.showMessageDialog(this, "Avión registrado. Capacidad ajustada a " + capacidadReal + " para cuadrar con la geometría de las filas.");
            panelRadarView.recargarDatosAviones();
        }
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS Y UTILIDADES
    // ═══════════════════════════════════════════════════════

    /** Crea un diálogo base con estilo dark mode del sistema */
    private JDialog crearDialogoBase(String titulo, int ancho, int alto) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titulo, true);
        dialog.getContentPane().setBackground(EstiloUI.FONDO_TARJETA);
        dialog.setSize(ancho, alto);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        return dialog;
    }

    /** Crea un botón principal de ancho completo */
    private JButton crearBotonPrincipal(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(EstiloUI.TEXTO_BLANCO);
        btn.setFont(EstiloUI.FUENTE_COMPONENTE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        EstiloUI.aplicarHover(btn, color, color.brighter());
        return btn;
    }



    private JTextField crearCampoTexto() {
        JTextField f = new JTextField(14);
        f.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        f.setForeground(EstiloUI.TEXTO_BLANCO);
        f.setCaretColor(EstiloUI.TEXTO_BLANCO);
        f.setBorder(EstiloUI.BORDE_COMPONENTE);
        return f;
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloUI.TEXTO_MUTED);
        l.setFont(EstiloUI.FUENTE_LABEL);
        return l;
    }



    private void aplicarEstiloBotonMenu(JButton b, boolean activo) {
        b.setPreferredSize(new Dimension(150, 42));
        b.setFont(EstiloUI.FUENTE_SUBTITULO);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBackground(activo ? EstiloUI.AZUL_ACCENT : EstiloUI.GRIS_BOTON_PASIVO);
        b.setForeground(activo ? EstiloUI.TEXTO_BLANCO : EstiloUI.TEXTO_MUTED);
    }

    // ═══════════════════════════════════════════════════════
    // AUDITORÍA
    // ═══════════════════════════════════════════════════════

    private void abrirDialogoAuditoria() {
        JDialog dialog = crearDialogoBase("📊  Auditoría del Sistema", 850, 550);
        dialog.setLayout(new BorderLayout(10, 10));

        List<Object[]> registros = auditoriaDAO.obtenerHistorial();

        String[] columnas = { "ID", "Usuario", "Acción", "Detalle", "Fecha/Hora" };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Object[] fila : registros) {
            modelo.addRow(fila);
        }

        JTable tabla = new JTable(modelo);
        tabla.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tabla.setForeground(EstiloUI.TEXTO_BLANCO);
        tabla.setGridColor(new Color(48, 54, 61));
        tabla.getTableHeader().setBackground(EstiloUI.FONDO_TARJETA);
        tabla.getTableHeader().setForeground(EstiloUI.TEXTO_MUTED);
        tabla.setRowHeight(24);
        tabla.setSelectionBackground(EstiloUI.AZUL_ACCENT);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(180);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollTabla.setBorder(EstiloUI.BORDE_COMPONENTE);

        JPanel panelEncabezado = new JPanel(new BorderLayout());
        panelEncabezado.setBackground(EstiloUI.FONDO_TARJETA);
        panelEncabezado.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel lblTitulo = new JLabel("📋  Historial de Operaciones del Sistema");
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);
        panelEncabezado.add(lblTitulo, BorderLayout.WEST);

        JLabel lblTotal = new JLabel("Total: " + registros.size() + " registros");
        lblTotal.setForeground(EstiloUI.TEXTO_MUTED);
        lblTotal.setFont(EstiloUI.FUENTE_LABEL);
        panelEncabezado.add(lblTotal, BorderLayout.EAST);

        dialog.add(panelEncabezado, BorderLayout.NORTH);
        dialog.add(scrollTabla, BorderLayout.CENTER);

        JButton btnCerrar = crearBotonPrincipal("✕  CERRAR", EstiloUI.GRIS_BOTON_PASIVO);
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBotones.setBackground(EstiloUI.FONDO_TARJETA);
        panelBotones.add(btnCerrar);
        btnCerrar.addActionListener(e -> dialog.dispose());
        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}