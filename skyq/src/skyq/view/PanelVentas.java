package skyq.view;

import skyq.dao.VueloDAO;
import skyq.model.Pasajero;
import skyq.model.Vuelo;
import skyq.services.VentasService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PanelVentas extends JPanel {
    private static final long serialVersionUID = 1L;

    private JComboBox<String> comboVuelos;
    private JTextField txtNombrePasajero;
    private JComboBox<String> comboTipoPasajero;
    private JComboBox<String> comboClasePasajero;
    private JTable tablaPasajeros;
    private DefaultTableModel modeloTablaPasajeros;
    private JButton btnAgregarPasajero;
    private JButton btnEliminarPasajero;
    private JButton btnProcesarVenta;
    private JPanel contenedorMapa;

    private transient List<Vuelo> vuelosCargados;
    private final transient VueloDAO vueloDAO = new VueloDAO();
    private MapaAsientosPanel planoRealTime = null;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelVentas() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(20, 20));
        initComponents();
        recargarVuelos();
    }

    private void initComponents() {
        // ==========================================
        // 👈 PANEL IZQUIERDO: SELECCIÓN, FORMULARIO Y TABLA TEMPORAL
        // ==========================================
        JPanel panelIzquierdo = new JPanel(new BorderLayout(15, 15));
        panelIzquierdo.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        panelIzquierdo.setPreferredSize(new Dimension(550, 700));

        // 1. Selector de Vuelo
        JPanel panelVuelo = new JPanel(new BorderLayout(10, 5));
        panelVuelo.setBackground(EstiloUI.FONDO_TARJETA);
        panelVuelo.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_TARJETA, new EmptyBorder(12, 15, 12, 15)));

        JLabel lblSelector = new JLabel("SELECCIONE EL VUELO:");
        lblSelector.setForeground(Color.CYAN);
        lblSelector.setFont(EstiloUI.FUENTE_SUBTITULO);

        comboVuelos = new JComboBox<>();
        comboVuelos.setPreferredSize(new Dimension(320, 32));
        comboVuelos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        comboVuelos.setForeground(EstiloUI.TEXTO_BLANCO);
        comboVuelos.addActionListener(e -> alSeleccionarVuelo());

        JButton btnActualizarVuelos = new JButton("🔄 Actualizar");
        btnActualizarVuelos.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        btnActualizarVuelos.setForeground(EstiloUI.TEXTO_BLANCO);
        btnActualizarVuelos.setFont(EstiloUI.FUENTE_LABEL);
        btnActualizarVuelos.setBorderPainted(false);
        EstiloUI.aplicarHover(btnActualizarVuelos, EstiloUI.GRIS_BOTON_PASIVO, new Color(55, 62, 71));
        btnActualizarVuelos.addActionListener(e -> recargarVuelos());

        JPanel vueloComboPanel = new JPanel(new BorderLayout(10, 0));
        vueloComboPanel.setBackground(EstiloUI.FONDO_TARJETA);
        vueloComboPanel.add(comboVuelos, BorderLayout.CENTER);
        vueloComboPanel.add(btnActualizarVuelos, BorderLayout.EAST);

        panelVuelo.add(lblSelector, BorderLayout.NORTH);
        panelVuelo.add(vueloComboPanel, BorderLayout.CENTER);
        panelIzquierdo.add(panelVuelo, BorderLayout.NORTH);

        // 2. Contenedor del Formulario de Pasajero y Tabla
        JPanel panelPasajeros = new JPanel(new BorderLayout(10, 10));
        panelPasajeros.setBackground(EstiloUI.FONDO_TARJETA);
        panelPasajeros.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_TARJETA, new EmptyBorder(15, 15, 15, 15)));

        JLabel lblPasajerosTitle = new JLabel("🛒 PASAJEROS EN ESTA RESERVA");
        lblPasajerosTitle.setForeground(EstiloUI.TEXTO_BLANCO);
        lblPasajerosTitle.setFont(EstiloUI.FUENTE_SUBTITULO);
        panelPasajeros.add(lblPasajerosTitle, BorderLayout.NORTH);

        // Formulario para agregar pasajero
        JPanel formAgregar = new JPanel(new GridBagLayout());
        formAgregar.setBackground(EstiloUI.FONDO_TARJETA);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombrePasajero = crearCampoTexto();
        comboTipoPasajero = new JComboBox<>(new String[]{"Adulto", "Niño"});
        estilizarCombo(comboTipoPasajero);
        comboClasePasajero = new JComboBox<>(new String[]{"VIP", "Ejecutiva", "Económica"});
        estilizarCombo(comboClasePasajero);

        btnAgregarPasajero = new JButton("✚ AGREGAR PASAJERO");
        btnAgregarPasajero.setBackground(EstiloUI.AZUL_ACCENT);
        btnAgregarPasajero.setForeground(EstiloUI.TEXTO_BLANCO);
        btnAgregarPasajero.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnAgregarPasajero.setBorderPainted(false);
        EstiloUI.aplicarHover(btnAgregarPasajero, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());
        btnAgregarPasajero.addActionListener(e -> agregarPasajeroALista());

        gbc.gridx = 0; gbc.gridy = 0; formAgregar.add(crearLabel("Nombre Pasajero:"), gbc);
        gbc.gridx = 1; formAgregar.add(txtNombrePasajero, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formAgregar.add(crearLabel("Tipo:"), gbc);
        gbc.gridx = 1; formAgregar.add(comboTipoPasajero, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formAgregar.add(crearLabel("Clase / Prioridad:"), gbc);
        gbc.gridx = 1; formAgregar.add(comboClasePasajero, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 6, 6, 6);
        formAgregar.add(btnAgregarPasajero, gbc);

        // Tabla temporal
        modeloTablaPasajeros = new DefaultTableModel(new Object[]{"Nombre", "Tipo", "Clase", "Asiento Pre-asignado"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPasajeros = new JTable(modeloTablaPasajeros);
        tablaPasajeros.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tablaPasajeros.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaPasajeros.setGridColor(new Color(48, 54, 61));
        tablaPasajeros.getTableHeader().setBackground(EstiloUI.FONDO_TARJETA);
        tablaPasajeros.getTableHeader().setForeground(EstiloUI.TEXTO_MUTED);
        tablaPasajeros.setRowHeight(24);
        tablaPasajeros.setSelectionBackground(EstiloUI.AZUL_ACCENT);

        JScrollPane scrollTabla = new JScrollPane(tablaPasajeros);
        scrollTabla.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.setPreferredSize(new Dimension(0, 180));

        btnEliminarPasajero = new JButton("✕ ELIMINAR SELECCIONADO");
        btnEliminarPasajero.setBackground(EstiloUI.ROJO_ALERTA);
        btnEliminarPasajero.setForeground(EstiloUI.TEXTO_BLANCO);
        btnEliminarPasajero.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnEliminarPasajero.setBorderPainted(false);
        EstiloUI.aplicarHover(btnEliminarPasajero, EstiloUI.ROJO_ALERTA, EstiloUI.ROJO_ALERTA.brighter());
        btnEliminarPasajero.addActionListener(e -> eliminarPasajeroDeLista());

        JPanel listadoPanel = new JPanel(new BorderLayout(5, 5));
        listadoPanel.setBackground(EstiloUI.FONDO_TARJETA);
        listadoPanel.add(formAgregar, BorderLayout.NORTH);
        listadoPanel.add(scrollTabla, BorderLayout.CENTER);
        listadoPanel.add(btnEliminarPasajero, BorderLayout.SOUTH);

        panelPasajeros.add(listadoPanel, BorderLayout.CENTER);
        panelIzquierdo.add(panelPasajeros, BorderLayout.CENTER);

        // 3. Botón de Procesar Venta al Sur
        btnProcesarVenta = new JButton("🛒 COMPLETAR COMPRA Y GENERAR PNR");
        btnProcesarVenta.setBackground(EstiloUI.VERDE_NEON);
        btnProcesarVenta.setForeground(EstiloUI.TEXTO_BLANCO);
        btnProcesarVenta.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnProcesarVenta.setPreferredSize(new Dimension(0, 50));
        btnProcesarVenta.setBorderPainted(false);
        EstiloUI.aplicarHover(btnProcesarVenta, EstiloUI.VERDE_NEON, EstiloUI.VERDE_NEON.brighter());
        btnProcesarVenta.addActionListener(e -> procesarReservaVenta());
        panelIzquierdo.add(btnProcesarVenta, BorderLayout.SOUTH);

        add(panelIzquierdo, BorderLayout.WEST);

        // ==========================================
        // 👉 PANEL DERECHO: INTERFAZ DINÁMICA DE CABINA / MAPA
        // ==========================================
        contenedorMapa = new JPanel(new BorderLayout());
        contenedorMapa.setBackground(EstiloUI.FONDO_TARJETA);
        contenedorMapa.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        JLabel lblPlaceholder = new JLabel("Seleccione un vuelo para cargar el mapa de asientos", SwingConstants.CENTER);
        lblPlaceholder.setForeground(EstiloUI.TEXTO_MUTED);
        lblPlaceholder.setFont(EstiloUI.FUENTE_SUBTITULO);
        contenedorMapa.add(lblPlaceholder, BorderLayout.CENTER);

        add(contenedorMapa, BorderLayout.CENTER);
    }

    private void recargarVuelos() {
        comboVuelos.removeAllItems();
        vuelosCargados = vueloDAO.obtenerTodosLosVuelos();
        for (Vuelo v : vuelosCargados) {
            String modeloAvion = v.getModeloAvion() != null ? v.getModeloAvion() : "Avión";
            String fechaSalidaStr = v.getFechaSalida() != null ? v.getFechaSalida().format(FMT) : "Sin fecha";
            String etiqueta = String.format("Vuelo #%d - %s (%s) - Salida: %s",
                    v.getIdVuelo(), v.getMatricula(), modeloAvion, fechaSalidaStr);
            comboVuelos.addItem(etiqueta);
        }
        if (vuelosCargados.isEmpty()) {
            comboVuelos.addItem("No hay vuelos programados en el sistema");
        }
    }

    private void alSeleccionarVuelo() {
        modeloTablaPasajeros.setRowCount(0);
        refrescarPlanoAvion();
    }

    private void refrescarPlanoAvion() {
        contenedorMapa.removeAll();
        planoRealTime = null;

        if (vuelosCargados == null || vuelosCargados.isEmpty() || comboVuelos.getSelectedIndex() < 0) {
            JLabel lblPlaceholder = new JLabel("Seleccione un vuelo para cargar el mapa de asientos", SwingConstants.CENTER);
            lblPlaceholder.setForeground(EstiloUI.TEXTO_MUTED);
            lblPlaceholder.setFont(EstiloUI.FUENTE_SUBTITULO);
            contenedorMapa.add(lblPlaceholder, BorderLayout.CENTER);
            contenedorMapa.revalidate();
            contenedorMapa.repaint();
            return;
        }

        Vuelo vuelo = vuelosCargados.get(comboVuelos.getSelectedIndex());
        String matricula = vuelo.getMatricula();

        // Inicializar el mapa con soporte para multi-selección, límite inicial 0 (cambia al agregar pasajeros)
        planoRealTime = new MapaAsientosPanel(matricula, true, modeloTablaPasajeros.getRowCount(), codigoAsiento -> {
            actualizarAsientosEnTabla();
        });

        contenedorMapa.add(planoRealTime, BorderLayout.CENTER);
        contenedorMapa.revalidate();
        contenedorMapa.repaint();
    }

    private void agregarPasajeroALista() {
        String nombre = txtNombrePasajero.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del pasajero.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipo = (String) comboTipoPasajero.getSelectedItem();
        String clase = (String) comboClasePasajero.getSelectedItem();

        modeloTablaPasajeros.addRow(new Object[]{nombre, tipo, clase, ""});
        txtNombrePasajero.setText("");

        // Actualizar el límite en el plano
        if (planoRealTime != null) {
            planoRealTime.setLimiteSeleccion(modeloTablaPasajeros.getRowCount());
        }
    }

    private void eliminarPasajeroDeLista() {
        int filaSel = tablaPasajeros.getSelectedRow();
        if (filaSel < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un pasajero de la lista para eliminar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTablaPasajeros.removeRow(filaSel);

        // Al cambiar el número de pasajeros, limpiamos las selecciones de asientos por seguridad y consistencia
        if (planoRealTime != null) {
            planoRealTime.limpiarSeleccion();
            planoRealTime.setLimiteSeleccion(modeloTablaPasajeros.getRowCount());
        }
        
        actualizarAsientosEnTabla();
    }

    private void actualizarAsientosEnTabla() {
        if (planoRealTime == null) return;
        List<String> seleccionados = planoRealTime.getAsientosSeleccionados();
        for (int i = 0; i < modeloTablaPasajeros.getRowCount(); i++) {
            if (i < seleccionados.size()) {
                modeloTablaPasajeros.setValueAt(seleccionados.get(i), i, 3);
            } else {
                modeloTablaPasajeros.setValueAt("", i, 3);
            }
        }
    }

    private void procesarReservaVenta() {
        if (vuelosCargados == null || vuelosCargados.isEmpty() || comboVuelos.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un vuelo válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vuelo vuelo = vuelosCargados.get(comboVuelos.getSelectedIndex());
        int cantPasajeros = modeloTablaPasajeros.getRowCount();

        if (cantPasajeros == 0) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un pasajero a la reserva.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (planoRealTime == null) {
            JOptionPane.showMessageDialog(this, "El mapa de asientos no está cargado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> asientos = planoRealTime.getAsientosSeleccionados();
        if (asientos.size() != cantPasajeros) {
            JOptionPane.showMessageDialog(this, 
                    "Debe seleccionar exactamente " + cantPasajeros + " asientos en el mapa (Ha seleccionado " + asientos.size() + ").", 
                    "Validación de Asientos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Construir lista de pasajeros
        List<Pasajero> pasajeros = new ArrayList<>();
        for (int i = 0; i < cantPasajeros; i++) {
            String nombre = (String) modeloTablaPasajeros.getValueAt(i, 0);
            String clase = (String) modeloTablaPasajeros.getValueAt(i, 2);
            String asiento = (String) modeloTablaPasajeros.getValueAt(i, 3);

            // Mapeo de clase a prioridad (VIP -> 1, Ejecutiva -> 2, Económica -> 3)
            int prioridad = switch (clase) {
                case "VIP" -> 1;
                case "Ejecutiva" -> 2;
                default -> 3;
            };

            Pasajero p = new Pasajero(0, nombre, asiento, prioridad, null, vuelo.getMatricula(), "");
            pasajeros.add(p);
        }

        try {
            // Invocar el servicio de ventas
            String pnr = VentasService.procesarVenta(vuelo, pasajeros);

            JOptionPane.showMessageDialog(this,
                    "¡Venta procesada con éxito!\nCódigo PNR único: " + pnr + "\nAsientos pre-asignados guardados.",
                    "Reserva Completada",
                    JOptionPane.INFORMATION_MESSAGE);

            // Limpiar la interfaz
            modeloTablaPasajeros.setRowCount(0);
            refrescarPlanoAvion();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Procesamiento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloUI.TEXTO_MUTED);
        l.setFont(EstiloUI.FUENTE_LABEL);
        return l;
    }

    private JTextField crearCampoTexto() {
        JTextField f = new JTextField(15);
        f.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        f.setForeground(EstiloUI.TEXTO_BLANCO);
        f.setCaretColor(EstiloUI.TEXTO_BLANCO);
        f.setBorder(EstiloUI.BORDE_COMPONENTE);
        return f;
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        combo.setForeground(EstiloUI.TEXTO_BLANCO);
        combo.setBorder(EstiloUI.BORDE_COMPONENTE);
    }
}
