package skyq.view;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import skyq.dao.AuditoriaDAO;
import skyq.dao.AvionDAO;
import skyq.dao.ConfiguracionDAO;
import skyq.dao.HospedajeDAO;
import skyq.dao.MantenimientoDAO;
import skyq.dao.PilotoDAO;
import skyq.dao.VueloDAO;
import skyq.logic.AutoCalculadorCabina;
import skyq.logic.LoggerManager;
import skyq.logic.SesionManager;
import skyq.logic.ValidadorFormulario;
import skyq.model.Avion;
import skyq.model.Hospedaje;
import skyq.model.Mantenimiento;
import skyq.model.Piloto;
import skyq.model.Vuelo;

/**
 * Panel principal del Módulo A: Centro de Comando del Gerente.
 *
 * Arquitectura interna (CardLayout):
 * - PANTALLA_RADAR → PanelRadarView con animación de barrido
 * - PANTALLA_DASHBOARD → Dashboard de 3 columnas:
 * Col 1: CRUD de aviones
 * Col 2: Diseñador de mapa de asientos
 * Col 3: Gestión interactiva de pilotos (cards + diálogos de vuelo y hospedaje)
 */
public class PanelGerente extends JPanel {

    // ── Navegación principal ──
    private CardLayout cardNavigator;
    private JPanel mainDynamicContainer;
    private PanelRadarView panelRadarView;
    private JButton btnRadarView, btnRegistroView;

    // ── Formulario de aviones ──
    private JTextField txtMatricula, txtModelo, txtCapacidad;
    private JComboBox<String> comboEstado;
    private JTextArea txtDescripcion;
    private JButton btnRegistrarAvion;

    // ── Panel de cards de pilotos ──
    private JPanel panelCardsContainer;

    // ── DAOs ──
    private final PilotoDAO pilotoDAO = new PilotoDAO();
    private final VueloDAO vueloDAO = new VueloDAO();
    private final HospedajeDAO hospedajeDAO = new HospedajeDAO();
    private final MantenimientoDAO mantenimientoDAO = new MantenimientoDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    // Formateador de fechas para los diálogos
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

        btnRadarView = new JButton("✈  Radar");
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

        panelRadarView = new PanelRadarView();
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
    // DASHBOARD DE 3 COLUMNAS
    // ═══════════════════════════════════════════════════════

    private JPanel construirDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(1, 3, 20, 0));
        dashboard.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        dashboard.setBorder(new EmptyBorder(25, 25, 25, 25));

        dashboard.add(construirColumnaAviones());
        dashboard.add(construirColumnaMapeo());
        dashboard.add(construirColumnaPilotos());

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

        comboEstado = new JComboBox<>(
                new String[] { "Disponible", "En Vuelo", "En mantenimiento", "Fuera de servicio" });
        comboEstado.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        comboEstado.setForeground(EstiloUI.TEXTO_BLANCO);
        comboEstado.setBorder(EstiloUI.BORDE_COMPONENTE);

        btnRegistrarAvion = new JButton("✚  REGISTRAR EN FLOTA");
        btnRegistrarAvion.setBackground(EstiloUI.AZUL_ACCENT);
        btnRegistrarAvion.setForeground(EstiloUI.TEXTO_BLANCO);
        btnRegistrarAvion.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnRegistrarAvion.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnRegistrarAvion, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());

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
    // COLUMNA 3: Gestión Interactiva de Pilotos
    // ═══════════════════════════════════════════════════════

    private JPanel construirColumnaPilotos() {
        JPanel columna = new JPanel(new BorderLayout(0, 10));
        columna.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        // ── Encabezado ──
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(EstiloUI.FONDO_TARJETA);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(10, 15, 10, 15)));

        JLabel lblTitulo = new JLabel("👥  CREW MANAGEMENT");
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);

        JButton btnNuevoPiloto = new JButton("✚  Nuevo Piloto");
        btnNuevoPiloto.setBackground(EstiloUI.VERDE_NEON);
        btnNuevoPiloto.setForeground(EstiloUI.TEXTO_BLANCO);
        btnNuevoPiloto.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnNuevoPiloto.setBorderPainted(false);
        PanelRadarView.aplicarHover(btnNuevoPiloto, EstiloUI.VERDE_NEON, EstiloUI.VERDE_NEON.brighter());

        encabezado.add(lblTitulo, BorderLayout.WEST);
        encabezado.add(btnNuevoPiloto, BorderLayout.EAST);
        columna.add(encabezado, BorderLayout.NORTH);

        // ── Panel scrollable de cards de pilotos ──
        panelCardsContainer = new JPanel();
        panelCardsContainer.setLayout(new BoxLayout(panelCardsContainer, BoxLayout.Y_AXIS));
        panelCardsContainer.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        JScrollPane scrollCards = new JScrollPane(panelCardsContainer);
        scrollCards.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollCards.setBorder(BorderFactory.createEmptyBorder());
        scrollCards.getVerticalScrollBar().setUnitIncrement(16);
        columna.add(scrollCards, BorderLayout.CENTER);

        // Carga inicial de cards
        recargarCardsPilotos();

        // Acción de registrar nuevo piloto
        btnNuevoPiloto.addActionListener(e -> abrirDialogoRegistrarPiloto());

        return columna;
    }

    /**
     * Recarga el panel de cards destruyendo las anteriores y reconstruyendo
     * una card por cada piloto obtenido de la base de datos.
     */
    private void recargarCardsPilotos() {
        panelCardsContainer.removeAll();

        List<Piloto> pilotos = pilotoDAO.obtenerPilotos();

        if (pilotos.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay pilotos registrados en el sistema.", SwingConstants.CENTER);
            lblVacio.setForeground(EstiloUI.TEXTO_MUTED);
            lblVacio.setFont(EstiloUI.FUENTE_LABEL);
            lblVacio.setBorder(new EmptyBorder(40, 0, 0, 0));
            panelCardsContainer.add(lblVacio);
        } else {
            for (Piloto piloto : pilotos) {
                panelCardsContainer.add(construirCardPiloto(piloto));
                panelCardsContainer.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        panelCardsContainer.revalidate();
        panelCardsContainer.repaint();
    }

    /**
     * Construye una "Crew Card" interactiva para un piloto.
     * Contiene badge de estado, nombre, rango y 3 botones de acción:
     * Editar, Asignar Vuelo y Hospedaje.
     */
    private JPanel construirCardPiloto(Piloto piloto) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(EstiloUI.FONDO_TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(12, 15, 12, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // ── Sección izquierda: Badge de estado + info ──
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(EstiloUI.FONDO_TARJETA);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(1, 0, 1, 8);

        // Badge circular de estado (pintado con Graphics2D)
        Color colorEstado = obtenerColorEstado(piloto.getEstado());
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorEstado);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                // Halo exterior semitransparente
                g2.setColor(new Color(colorEstado.getRed(), colorEstado.getGreen(), colorEstado.getBlue(), 60));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
            }
        };
        badge.setPreferredSize(new Dimension(14, 14));
        badge.setBackground(EstiloUI.FONDO_TARJETA);
        badge.setToolTipText(piloto.getEstado());

        JLabel lblNombre = new JLabel(piloto.getNombre());
        lblNombre.setForeground(EstiloUI.TEXTO_BLANCO);
        lblNombre.setFont(EstiloUI.FUENTE_SUBTITULO);

        JLabel lblRango = new JLabel(piloto.getRango() + "  ·  " + piloto.getEstado());
        lblRango.setForeground(colorEstado);
        lblRango.setFont(new Font("SansSerif", Font.PLAIN, 11));

        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(badge, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        infoPanel.add(lblNombre, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        infoPanel.add(lblRango, gbc);

        card.add(infoPanel, BorderLayout.CENTER);

        // ── Sección derecha: Botones de acción ──
        JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        accionesPanel.setBackground(EstiloUI.FONDO_TARJETA);

        JButton btnEditar = crearBotonAccion("✏", EstiloUI.GRIS_BOTON_PASIVO, "Editar datos del piloto");
        JButton btnVuelo = crearBotonAccion("✈", EstiloUI.AZUL_ACCENT, "Asignar vuelo");
        JButton btnHotel = crearBotonAccion("🏨", new Color(120, 60, 180), "Gestionar hospedaje");
        JButton btnEliminar = crearBotonAccion("✕", EstiloUI.ROJO_ALERTA, "Eliminar piloto");

        // Hover effects
        PanelRadarView.aplicarHover(btnEditar, EstiloUI.GRIS_BOTON_PASIVO, new Color(55, 62, 71));
        PanelRadarView.aplicarHover(btnVuelo, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());
        PanelRadarView.aplicarHover(btnHotel, new Color(120, 60, 180), new Color(140, 80, 200));
        PanelRadarView.aplicarHover(btnEliminar, EstiloUI.ROJO_ALERTA, EstiloUI.ROJO_ALERTA.brighter());

        accionesPanel.add(btnEditar);
        accionesPanel.add(btnVuelo);
        accionesPanel.add(btnHotel);
        accionesPanel.add(btnEliminar);
        card.add(accionesPanel, BorderLayout.EAST);

        // ── Listeners de acción ──
        btnEditar.addActionListener(e -> abrirDialogoEditarPiloto(piloto));
        btnVuelo.addActionListener(e -> abrirDialogoAsignarVuelo(piloto));
        btnHotel.addActionListener(e -> abrirDialogoHospedaje(piloto));
        btnEliminar.addActionListener(e -> confirmarEliminarPiloto(piloto));

        return card;
    }

    // ═══════════════════════════════════════════════════════
    // DIÁLOGOS DE PILOTOS
    // ═══════════════════════════════════════════════════════

    /** Abre el diálogo para registrar un nuevo piloto en el sistema */
    private void abrirDialogoRegistrarPiloto() {
        JDialog dialog = crearDialogoBase("Registrar Nuevo Piloto", 420, 300);

        JTextField txtNombre = crearCampoTexto();
        JTextField txtRango = crearCampoTexto();
        JComboBox<String> cbEstado = new JComboBox<>(new String[] { "Disponible", "En Vuelo", "Licencia", "Descanso" });
        estilizarCombo(cbEstado);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(EstiloUI.FONDO_TARJETA);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(crearLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        form.add(txtNombre, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(crearLabel("Rango / Título:"), gbc);
        gbc.gridx = 1;
        form.add(txtRango, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(crearLabel("Estado inicial:"), gbc);
        gbc.gridx = 1;
        form.add(cbEstado, gbc);

        JButton btnGuardar = crearBotonPrincipal("✚  Registrar Piloto", EstiloUI.VERDE_NEON);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 8, 10);
        form.add(btnGuardar, gbc);

        dialog.add(form);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String rango = txtRango.getText().trim();
            if (nombre.isEmpty() || rango.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nombre y rango son obligatorios.", "Validación",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (pilotoDAO.insertarPiloto(new Piloto(0, nombre, rango, (String) cbEstado.getSelectedItem()))) {
                JOptionPane.showMessageDialog(dialog, "Piloto registrado correctamente en el sistema.");
                dialog.dispose();
                recargarCardsPilotos();
            }
        });

        dialog.setVisible(true);
    }

    /** Abre el diálogo para editar los datos de un piloto existente */
    private void abrirDialogoEditarPiloto(Piloto piloto) {
        JDialog dialog = crearDialogoBase("Editar Piloto — " + piloto.getNombre(), 420, 300);

        JTextField txtNombre = crearCampoTexto();
        txtNombre.setText(piloto.getNombre());
        JTextField txtRango = crearCampoTexto();
        txtRango.setText(piloto.getRango());
        JComboBox<String> cbEstado = new JComboBox<>(new String[] { "Disponible", "En Vuelo", "Licencia", "Descanso" });
        cbEstado.setSelectedItem(piloto.getEstado());
        estilizarCombo(cbEstado);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(EstiloUI.FONDO_TARJETA);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(crearLabel("Nombre completo:"), gbc);
        gbc.gridx = 1;
        form.add(txtNombre, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(crearLabel("Rango / Título:"), gbc);
        gbc.gridx = 1;
        form.add(txtRango, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(crearLabel("Estado:"), gbc);
        gbc.gridx = 1;
        form.add(cbEstado, gbc);

        JButton btnGuardar = crearBotonPrincipal("✔  Guardar Cambios", EstiloUI.AZUL_ACCENT);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 8, 10);
        form.add(btnGuardar, gbc);

        dialog.add(form);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String rango = txtRango.getText().trim();
            if (nombre.isEmpty() || rango.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Los campos no pueden estar vacíos.", "Validación",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            piloto.setNombre(nombre);
            piloto.setRango(rango);
            piloto.setEstado((String) cbEstado.getSelectedItem());

            if (pilotoDAO.actualizarPiloto(piloto)) {
                JOptionPane.showMessageDialog(dialog, "Datos del piloto actualizados correctamente.");
                dialog.dispose();
                recargarCardsPilotos();
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Abre el diálogo interactivo de asignación de vuelo a un piloto.
     * Verifica conflictos antes de guardar:
     * - Piloto no debe tener vuelos activos
     * - Avión seleccionado no debe tener vuelos activos
     */
    private void abrirDialogoAsignarVuelo(Piloto piloto) {
        JDialog dialog = crearDialogoBase("✈  Asignar Vuelo — " + piloto.getNombre(), 560, 520);
        dialog.setLayout(new BorderLayout(0, 10));

        // ── Sección superior: formulario de asignación ──
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(EstiloUI.FONDO_TARJETA);
        formPanel.setBorder(new EmptyBorder(15, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cargar aviones disponibles (sin vuelo activo)
        AvionDAO avionDAO = new AvionDAO();
        List<Avion> aviones = avionDAO.obtenerAvionesFlota();
        JComboBox<String> cbAvion = new JComboBox<>();
        for (Avion av : aviones) {
            String etiqueta = av.getMatricula() + " — " + av.getModelo() + " (" + av.getEstado() + ")";
            cbAvion.addItem(etiqueta);
        }
        estilizarCombo(cbAvion);

        JTextField txtSalida = crearCampoTexto();
        txtSalida.setText("dd/MM/yyyy HH:mm");
        JTextField txtRegreso = crearCampoTexto();
        txtRegreso.setText("dd/MM/yyyy HH:mm");
        txtSalida.setForeground(EstiloUI.TEXTO_MUTED);
        txtRegreso.setForeground(EstiloUI.TEXTO_MUTED);

        // Limpiar placeholder al hacer foco
        limpiarPlaceholder(txtSalida, "dd/MM/yyyy HH:mm");
        limpiarPlaceholder(txtRegreso, "dd/MM/yyyy HH:mm");

        JComboBox<String> cbEstadoVuelo = new JComboBox<>(new String[] { "Programado", "En Vuelo" });
        estilizarCombo(cbEstadoVuelo);

        JLabel lblTitulo = new JLabel("Nueva asignación de vuelo para: " + piloto.getNombre());
        lblTitulo.setForeground(Color.CYAN);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(lblTitulo, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(crearLabel("Aeronave:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cbAvion, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(crearLabel("Fecha Salida:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtSalida, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(crearLabel("Fecha Regreso:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtRegreso, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(crearLabel("Estado vuelo:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cbEstadoVuelo, gbc);

        JButton btnAsignar = crearBotonPrincipal("✈  Confirmar Asignación", EstiloUI.AZUL_ACCENT);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 8, 8);
        formPanel.add(btnAsignar, gbc);

        // ── Sección inferior: historial de vuelos del piloto ──
        JPanel historialPanel = new JPanel(new BorderLayout(0, 5));
        historialPanel.setBackground(EstiloUI.FONDO_TARJETA);
        historialPanel.setBorder(new EmptyBorder(0, 20, 15, 20));

        JLabel lblHistorial = new JLabel("Historial de vuelos asignados:");
        lblHistorial.setForeground(EstiloUI.TEXTO_MUTED);
        lblHistorial.setFont(EstiloUI.FUENTE_LABEL);

        // Tabla de historial de vuelos
        String[] columnas = { "ID", "Aeronave", "Modelo", "Salida", "Regreso", "Estado" };
        Object[][] filas = obtenerFilasVuelosPiloto(piloto.getIdPiloto());
        JTable tablaVuelos = new JTable(filas, columnas) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaVuelos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tablaVuelos.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaVuelos.setGridColor(new Color(48, 54, 61));
        tablaVuelos.getTableHeader().setBackground(EstiloUI.FONDO_TARJETA);
        tablaVuelos.getTableHeader().setForeground(EstiloUI.TEXTO_MUTED);
        tablaVuelos.setRowHeight(24);
        tablaVuelos.setSelectionBackground(EstiloUI.AZUL_ACCENT);

        JScrollPane scrollTabla = new JScrollPane(tablaVuelos);
        scrollTabla.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollTabla.setBorder(EstiloUI.BORDE_COMPONENTE);
        scrollTabla.setPreferredSize(new Dimension(0, 160));

        historialPanel.add(lblHistorial, BorderLayout.NORTH);
        historialPanel.add(scrollTabla, BorderLayout.CENTER);

        dialog.add(formPanel, BorderLayout.NORTH);
        dialog.add(historialPanel, BorderLayout.CENTER);

        // ── Acción de asignación con validaciones de conflicto ──
        btnAsignar.addActionListener(e -> {
            if (cbAvion.getItemCount() == 0) {
                JOptionPane.showMessageDialog(dialog, "No hay aeronaves registradas.", "Sin Aeronaves",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String matriculaSeleccionada = aviones.get(cbAvion.getSelectedIndex()).getMatricula();

            // Verificar que el piloto no tenga vuelo activo
            if (vueloDAO.pilotoTieneVueloActivo(piloto.getIdPiloto())) {
                JOptionPane.showMessageDialog(dialog,
                        piloto.getNombre()
                                + " ya tiene un vuelo activo o programado.\nNo se puede asignar otro hasta que el actual se complete o cancele.",
                        "Conflicto de Asignación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verificar que el avión no tenga vuelo activo
            if (vueloDAO.avionTieneVueloActivo(matriculaSeleccionada)) {
                JOptionPane.showMessageDialog(dialog,
                        "La aeronave " + matriculaSeleccionada
                                + " ya tiene un vuelo activo.\nSeleccione otra aeronave disponible.",
                        "Aeronave No Disponible", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDateTime fechaSalida, fechaRegreso;
            try {
                fechaSalida = LocalDateTime.parse(txtSalida.getText().trim(), FMT);
                fechaRegreso = LocalDateTime.parse(txtRegreso.getText().trim(), FMT);
                if (!fechaRegreso.isAfter(fechaSalida)) {
                    JOptionPane.showMessageDialog(dialog, "La fecha de regreso debe ser posterior a la salida.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de fecha inválido. Use dd/MM/yyyy HH:mm", "Validación",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Vuelo vuelo = new Vuelo();
            vuelo.setMatricula(matriculaSeleccionada);
            vuelo.setIdPiloto(piloto.getIdPiloto());
            vuelo.setFechaSalida(fechaSalida);
            vuelo.setFechaRegreso(fechaRegreso);
            vuelo.setEstado((String) cbEstadoVuelo.getSelectedItem());

            if (vueloDAO.insertarVuelo(vuelo)) {
                JOptionPane.showMessageDialog(dialog, "Vuelo asignado correctamente a " + piloto.getNombre() + ".");
                dialog.dispose();
                recargarCardsPilotos();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar el vuelo. Verifique la conexión.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Abre el diálogo interactivo de gestión de hospedaje de un piloto.
     * Permite registrar nuevo hospedaje y ver/eliminar hospedajes anteriores.
     */
    private void abrirDialogoHospedaje(Piloto piloto) {
        JDialog dialog = crearDialogoBase("🏨  Hospedaje — " + piloto.getNombre(), 540, 480);
        dialog.setLayout(new BorderLayout(0, 10));

        // ── Formulario de nuevo hospedaje ──
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(EstiloUI.FONDO_TARJETA);
        formPanel.setBorder(new EmptyBorder(15, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Registrar hospedaje para: " + piloto.getNombre());
        lblTitulo.setForeground(Color.CYAN);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);

        JTextField txtHotel = crearCampoTexto();
        JTextField txtCiudad = crearCampoTexto();
        JTextField txtIngreso = crearCampoTexto();
        txtIngreso.setText("dd/MM/yyyy HH:mm");
        JTextField txtSalida = crearCampoTexto();
        txtSalida.setText("dd/MM/yyyy HH:mm");
        txtIngreso.setForeground(EstiloUI.TEXTO_MUTED);
        txtSalida.setForeground(EstiloUI.TEXTO_MUTED);
        limpiarPlaceholder(txtIngreso, "dd/MM/yyyy HH:mm");
        limpiarPlaceholder(txtSalida, "dd/MM/yyyy HH:mm");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(lblTitulo, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(crearLabel("Hotel / Alojamiento:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtHotel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(crearLabel("Ciudad:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtCiudad, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(crearLabel("Check-in:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtIngreso, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(crearLabel("Check-out:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtSalida, gbc);

        JButton btnRegistrar = crearBotonPrincipal("🏨  Registrar Hospedaje", new Color(120, 60, 180));
        PanelRadarView.aplicarHover(btnRegistrar, new Color(120, 60, 180), new Color(140, 80, 200));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 8, 8);
        formPanel.add(btnRegistrar, gbc);

        // ── Lista de hospedajes previos con opción de eliminar ──
        JPanel listaPanel = new JPanel(new BorderLayout(0, 5));
        listaPanel.setBackground(EstiloUI.FONDO_TARJETA);
        listaPanel.setBorder(new EmptyBorder(0, 20, 15, 20));

        JLabel lblLista = new JLabel("Hospedajes registrados:");
        lblLista.setForeground(EstiloUI.TEXTO_MUTED);
        lblLista.setFont(EstiloUI.FUENTE_LABEL);

        // Panel interno de hospedajes (se reconstruye al registrar uno nuevo)
        JPanel[] panelHospedajesRef = { construirListaHospedajes(piloto.getIdPiloto(), dialog) };
        JScrollPane scrollHosp = new JScrollPane(panelHospedajesRef[0]);
        scrollHosp.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollHosp.setBorder(EstiloUI.BORDE_COMPONENTE);

        listaPanel.add(lblLista, BorderLayout.NORTH);
        listaPanel.add(scrollHosp, BorderLayout.CENTER);

        dialog.add(formPanel, BorderLayout.NORTH);
        dialog.add(listaPanel, BorderLayout.CENTER);

        // ── Acción de registro de hospedaje ──
        btnRegistrar.addActionListener(e -> {
            String hotel = txtHotel.getText().trim();
            String ciudad = txtCiudad.getText().trim();
            if (hotel.isEmpty() || ciudad.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Hotel y ciudad son obligatorios.", "Validación",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            LocalDateTime fechaIngreso, fechaSalida;
            try {
                fechaIngreso = LocalDateTime.parse(txtIngreso.getText().trim(), FMT);
                fechaSalida = LocalDateTime.parse(txtSalida.getText().trim(), FMT);
                if (!fechaSalida.isAfter(fechaIngreso)) {
                    JOptionPane.showMessageDialog(dialog, "El check-out debe ser posterior al check-in.", "Validación",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de fecha inválido. Use dd/MM/yyyy HH:mm", "Validación",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Hospedaje h = new Hospedaje(0, piloto.getIdPiloto(), hotel, ciudad, fechaIngreso, fechaSalida);
            if (hospedajeDAO.insertarHospedaje(h)) {
                JOptionPane.showMessageDialog(dialog, "Hospedaje registrado para " + piloto.getNombre() + ".");
                // Reconstruye la lista sin cerrar el diálogo
                JPanel nuevaLista = construirListaHospedajes(piloto.getIdPiloto(), dialog);
                scrollHosp.setViewportView(nuevaLista);
                txtHotel.setText("");
                txtCiudad.setText("");
                txtIngreso.setText("dd/MM/yyyy HH:mm");
                txtIngreso.setForeground(EstiloUI.TEXTO_MUTED);
                txtSalida.setText("dd/MM/yyyy HH:mm");
                txtSalida.setForeground(EstiloUI.TEXTO_MUTED);
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al registrar hospedaje.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Construye el panel con la lista de hospedajes de un piloto,
     * cada uno con un botón de eliminar integrado.
     */
    private JPanel construirListaHospedajes(int idPiloto, JDialog dialogPadre) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        List<Hospedaje> hospedajes = hospedajeDAO.obtenerHospedajesPorPiloto(idPiloto);

        if (hospedajes.isEmpty()) {
            JLabel lblVacio = new JLabel("  Sin hospedajes registrados.");
            lblVacio.setForeground(EstiloUI.TEXTO_MUTED);
            lblVacio.setFont(EstiloUI.FUENTE_LABEL);
            lblVacio.setBorder(new EmptyBorder(10, 5, 10, 5));
            panel.add(lblVacio);
        } else {
            for (Hospedaje h : hospedajes) {
                JPanel fila = new JPanel(new BorderLayout(10, 0));
                fila.setBackground(EstiloUI.FONDO_TARJETA);
                fila.setBorder(BorderFactory.createCompoundBorder(
                        EstiloUI.BORDE_COMPONENTE, new EmptyBorder(8, 12, 8, 12)));
                fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

                String textoHosp = String.format("<html><b style='color:#f0f6fc'>%s</b> · %s<br>" +
                        "<span style='color:#8b949e; font-size:10px'>Check-in: %s  →  Check-out: %s</span></html>",
                        h.getHotel(), h.getCiudad(),
                        h.getFechaIngreso() != null ? h.getFechaIngreso().format(FMT) : "—",
                        h.getFechaSalida() != null ? h.getFechaSalida().format(FMT) : "—");

                JLabel lblInfo = new JLabel(textoHosp);
                fila.add(lblInfo, BorderLayout.CENTER);

                JButton btnDel = crearBotonAccion("✕", EstiloUI.ROJO_ALERTA, "Eliminar hospedaje");
                PanelRadarView.aplicarHover(btnDel, EstiloUI.ROJO_ALERTA, EstiloUI.ROJO_ALERTA.brighter());
                fila.add(btnDel, BorderLayout.EAST);

                btnDel.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(dialogPadre,
                            "¿Eliminar el hospedaje en " + h.getHotel() + "?",
                            "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION && hospedajeDAO.eliminarHospedaje(h.getIdHospedaje())) {
                        panel.remove(fila);
                        panel.revalidate();
                        panel.repaint();
                    }
                });

                panel.add(fila);
                panel.add(Box.createRigidArea(new Dimension(0, 4)));
            }
        }
        return panel;
    }

    /** Pide confirmación y elimina un piloto del sistema */
    private void confirmarEliminarPiloto(Piloto piloto) {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al piloto " + piloto.getNombre() + "?\n" +
                        "Esta acción eliminará también sus vuelos y hospedajes asociados.",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            if (pilotoDAO.eliminarPiloto(piloto.getIdPiloto())) {
                recargarCardsPilotos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el piloto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO — AVIONES
    // ═══════════════════════════════════════════════════════

    private void guardarAvionBaseDatos() {
        String matricula = txtMatricula.getText().trim();
        String modelo = txtModelo.getText().trim();
        String capacidadTexto = txtCapacidad.getText().trim();
        String estado = (String) comboEstado.getSelectedItem();

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

    /** Obtiene los vuelos de un piloto como array de filas para JTable */
    private Object[][] obtenerFilasVuelosPiloto(int idPiloto) {
        List<Vuelo> vuelos = vueloDAO.obtenerVuelosPorPiloto(idPiloto);
        Object[][] filas = new Object[vuelos.size()][6];
        for (int i = 0; i < vuelos.size(); i++) {
            Vuelo v = vuelos.get(i);
            filas[i][0] = v.getIdVuelo();
            filas[i][1] = v.getMatricula();
            filas[i][2] = v.getModeloAvion() != null ? v.getModeloAvion() : "—";
            filas[i][3] = v.getFechaSalida() != null ? v.getFechaSalida().format(FMT) : "—";
            filas[i][4] = v.getFechaRegreso() != null ? v.getFechaRegreso().format(FMT) : "—";
            filas[i][5] = v.getEstado();
        }
        return filas;
    }

    /** Devuelve el color asociado al estado de un piloto */
    private Color obtenerColorEstado(String estado) {
        return switch (estado) {
            case "Disponible" -> EstiloUI.VERDE_NEON;
            case "En Vuelo" -> EstiloUI.AZUL_ACCENT;
            case "Licencia" -> new Color(250, 176, 5);
            default -> EstiloUI.TEXTO_MUTED;
        };
    }

    /** Crea un diálogo base con estilo dark mode del sistema */
    private JDialog crearDialogoBase(String titulo, int ancho, int alto) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titulo, true);
        dialog.getContentPane().setBackground(EstiloUI.FONDO_TARJETA);
        dialog.setSize(ancho, alto);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        return dialog;
    }

    /** Crea un botón de acción compacto (iconos en las cards de pilotos) */
    private JButton crearBotonAccion(String icono, Color color, String tooltip) {
        JButton btn = new JButton(icono);
        btn.setBackground(color);
        btn.setForeground(EstiloUI.TEXTO_BLANCO);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(34, 34));
        btn.setToolTipText(tooltip);
        return btn;
    }

    /** Crea un botón principal de ancho completo */
    private JButton crearBotonPrincipal(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(EstiloUI.TEXTO_BLANCO);
        btn.setFont(EstiloUI.FUENTE_COMPONENTE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        PanelRadarView.aplicarHover(btn, color, color.brighter());
        return btn;
    }

    /**
     * Aplica placeholder: limpia el campo al ganar foco si el texto era el
     * placeholder
     */
    private void limpiarPlaceholder(JTextField campo, String placeholder) {
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                    campo.setForeground(EstiloUI.TEXTO_BLANCO);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                    campo.setForeground(EstiloUI.TEXTO_MUTED);
                }
            }
        });
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

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        combo.setForeground(EstiloUI.TEXTO_BLANCO);
        combo.setBorder(EstiloUI.BORDE_COMPONENTE);
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
    // MANTENIMIENTO DE FLOTA
    // ═══════════════════════════════════════════════════════

    private void abrirDialogoRegistrarMantenimiento() {
        JDialog dialog = crearDialogoBase("🔧  Registrar Mantenimiento", 500, 400);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        AvionDAO avionDAO = new AvionDAO();
        List<Avion> aviones = avionDAO.obtenerAvionesFlota();

        if (aviones.isEmpty()) {
            JLabel lblError = new JLabel("No hay aeronaves registradas.");
            lblError.setForeground(EstiloUI.ROJO_ALERTA);
            dialog.add(lblError);
            dialog.setVisible(true);
            return;
        }

        String[] matriculas = aviones.stream().map(Avion::getMatricula).toArray(String[]::new);
        JComboBox<String> cbMatricula = new JComboBox<>(matriculas);
        estilizarCombo(cbMatricula);

        JSpinner spinFechaInicio = new JSpinner(new javax.swing.SpinnerDateModel());
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinFechaInicio, "dd/MM/yyyy");
        spinFechaInicio.setEditor(editorInicio);
        spinFechaInicio.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        JSpinner spinFechaFin = new JSpinner(new javax.swing.SpinnerDateModel());
        JSpinner.DateEditor editorFin = new JSpinner.DateEditor(spinFechaFin, "dd/MM/yyyy");
        spinFechaFin.setEditor(editorFin);
        spinFechaFin.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        JTextArea txtDescripcionManto = new JTextArea(4, 30);
        txtDescripcionManto.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        txtDescripcionManto.setForeground(EstiloUI.TEXTO_BLANCO);
        txtDescripcionManto.setBorder(EstiloUI.BORDE_COMPONENTE);
        txtDescripcionManto.setLineWrap(true);
        txtDescripcionManto.setWrapStyleWord(true);

        String[] estados = { "Programado", "En Curso", "Completado" };
        JComboBox<String> cbEstado = new JComboBox<>(estados);
        estilizarCombo(cbEstado);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(crearLabel("Matrícula:"), gbc);
        gbc.gridx = 1;
        dialog.add(cbMatricula, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dialog.add(crearLabel("Fecha Inicio:"), gbc);
        gbc.gridx = 1;
        dialog.add(spinFechaInicio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(crearLabel("Fecha Fin:"), gbc);
        gbc.gridx = 1;
        dialog.add(spinFechaFin, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(crearLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        dialog.add(new JScrollPane(txtDescripcionManto), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(crearLabel("Estado:"), gbc);
        gbc.gridx = 1;
        dialog.add(cbEstado, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JButton btnGuardar = crearBotonPrincipal("💾  GUARDAR", EstiloUI.VERDE_NEON);
        dialog.add(btnGuardar, gbc);

        btnGuardar.addActionListener(e -> {
            String matricula = (String) cbMatricula.getSelectedItem();
            java.util.Date date1 = (java.util.Date) spinFechaInicio.getValue();
            LocalDate fechaInicio = new java.sql.Date(date1.getTime()).toLocalDate();
            java.util.Date date2 = (java.util.Date) spinFechaFin.getValue();
            LocalDate fechaFin = date2 != null ? new java.sql.Date(date2.getTime()).toLocalDate() : null;
            String descripcion = txtDescripcionManto.getText().trim();
            String estado = (String) cbEstado.getSelectedItem();

            if (!ValidadorFormulario.esTextoValido(descripcion)) {
                JOptionPane.showMessageDialog(dialog, "Ingrese una descripción.", "Validación",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Mantenimiento m = new Mantenimiento(0, matricula, fechaInicio, fechaFin, descripcion, estado);
            if (mantenimientoDAO.insertar(m)) {
                JOptionPane.showMessageDialog(dialog, "Mantenimiento registrado correctamente.");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al registrar mantenimiento.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void abrirDialogoVerMantenimiento() {
        JDialog dialog = crearDialogoBase("📋  Historial de Mantenimientos", 700, 500);
        dialog.setLayout(new BorderLayout(10, 10));

        List<Mantenimiento> mantenimientos = mantenimientoDAO.obtenerTodos();

        String[] columnas = { "ID", "Matrícula", "Inicio", "Fin", "Descripción", "Estado" };
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Mantenimiento m : mantenimientos) {
            modelo.addRow(new Object[] {
                    m.getIdMantenimiento(),
                    m.getMatricula(),
                    m.getFechaInicio(),
                    m.getFechaFin() != null ? m.getFechaFin() : "—",
                    m.getDescripcion(),
                    m.getEstado()
            });
        }

        JTable tabla = new JTable(modelo);
        tabla.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tabla.setForeground(EstiloUI.TEXTO_BLANCO);
        tabla.setGridColor(new Color(48, 54, 61));
        tabla.getTableHeader().setBackground(EstiloUI.FONDO_TARJETA);
        tabla.getTableHeader().setForeground(EstiloUI.TEXTO_MUTED);
        tabla.setRowHeight(24);
        tabla.setSelectionBackground(EstiloUI.AZUL_ACCENT);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollTabla.setBorder(EstiloUI.BORDE_COMPONENTE);

        dialog.add(scrollTabla, BorderLayout.CENTER);

        JButton btnCerrar = crearBotonPrincipal("✕  CERRAR", EstiloUI.GRIS_BOTON_PASIVO);
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBotones.setBackground(EstiloUI.FONDO_TARJETA);
        panelBotones.add(btnCerrar);
        btnCerrar.addActionListener(e -> dialog.dispose());
        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.setVisible(true);
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