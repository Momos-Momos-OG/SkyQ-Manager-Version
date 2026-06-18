package skyq.view;

import skyq.dao.AvionDAO;
import skyq.dao.PasajeroDAO;
import skyq.logic.ColaAbordaje;
import skyq.logic.DesembarqueManager;
import skyq.model.Avion;
import skyq.model.Pasajero;
import skyq.services.CheckInService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PanelCheckIn extends JPanel {
    private static final long serialVersionUID = 1L;

    private JComboBox<String> comboVuelosFlota;
    private JPanel panelCentralWorkspace;
    private CardLayout cardLayoutModos;

    // Campos del flujo Check-In
    private JTextField txtBuscarPNR;
    private JTextField txtNombrePasajero, txtPesoEquipaje;
    private JComboBox<String> comboPrioridad;
    private JLabel lblAsientoAsignado;
    private JPanel contenedorDinamicoCabina;
    private JButton btnEnviarCheckIn;

    private transient Pasajero pasajeroCargado = null;
    private transient List<Pasajero> pasajerosPNR = new ArrayList<>();

    // Componentes de la tabla de pasajeros del PNR
    private JTable tablaPasajerosPNR;
    private DefaultTableModel modeloTablaPasajerosPNR;

    // Componentes de las tablas de Abordaje/Desembarque
    private DefaultTableModel modeloTablaFlujo;
    private JTable tablaFlujoOperaciones;
    private JLabel lblTituloSeccionCambiante;

    private final transient AvionDAO avionDAO = new AvionDAO();
    private final transient PasajeroDAO pasajeroDAO = new PasajeroDAO();

    public PanelCheckIn() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(20, 20));
        initComponents();
        recargarVuelosDesdeFlota();
    }

    private void initComponents() {
        // ==========================================
        // 🛫 NORTE: BARRA SUPERIOR SELECCIÓN DE VUELO
        // ==========================================
        JPanel panelNorteSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panelNorteSeleccion.setBackground(EstiloUI.FONDO_TARJETA);
        panelNorteSeleccion.setBorder(EstiloUI.BORDE_COMPONENTE);

        JLabel lblSelector = new JLabel("OPERANDO FLOTA EN CONTEXTO:");
        lblSelector.setForeground(Color.CYAN);
        lblSelector.setFont(EstiloUI.FUENTE_SUBTITULO);

        comboVuelosFlota = new JComboBox<>();
        comboVuelosFlota.setPreferredSize(new Dimension(280, 32));
        comboVuelosFlota.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        comboVuelosFlota.setForeground(EstiloUI.TEXTO_BLANCO);

        panelNorteSeleccion.add(lblSelector);
        panelNorteSeleccion.add(comboVuelosFlota);
        add(panelNorteSeleccion, BorderLayout.NORTH);

        // ==========================================
        // 🎛️ OESTE: BARRA LATERAL SUB-NVEGACIÓN (FIGMA)
        // ==========================================
        JPanel sidebarModos = new JPanel(new GridLayout(3, 1, 0, 15));
        sidebarModos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        sidebarModos.setPreferredSize(new Dimension(180, 500));

        JButton btnModoCheckIn = crearBotonSidebar("1. Registro / Check-In", true);
        JButton btnModoAbordaje = crearBotonSidebar("2. Cola de Abordaje", false);
        JButton btnModoDesembarque = crearBotonSidebar("3. Desembarque", false);

        sidebarModos.add(btnModoCheckIn);
        sidebarModos.add(btnModoAbordaje);
        sidebarModos.add(btnModoDesembarque);
        add(sidebarModos, BorderLayout.WEST);

        // ==========================================
        // 🖥️ CENTRO: ESPACIO DE TRABAJO (CARDLAYOUT)
        // ==========================================
        cardLayoutModos = new CardLayout();
        panelCentralWorkspace = new JPanel(cardLayoutModos);
        panelCentralWorkspace.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        panelCentralWorkspace.add(construirFormularioCheckInVisual(), "VISTA_CHECKIN");
        panelCentralWorkspace.add(construirListadosTabularesDeVuelo(), "VISTA_TABLAS");

        add(panelCentralWorkspace, BorderLayout.CENTER);

        // Enlace interactivo de botones del menú lateral
        btnModoCheckIn.addActionListener(e -> {
            alternarEstiloMenu(btnModoCheckIn, btnModoAbordaje, btnModoDesembarque);
            cardLayoutModos.show(panelCentralWorkspace, "VISTA_CHECKIN");
        });

        btnModoAbordaje.addActionListener(e -> {
            alternarEstiloMenu(btnModoAbordaje, btnModoCheckIn, btnModoDesembarque);
            lblTituloSeccionCambiante.setText("COLA DE ABORDAJE ACTIVA - ENFOQUE: COMERCIAL + FIFO");
            cardLayoutModos.show(panelCentralWorkspace, "VISTA_TABLAS");
            calcularColaAbordajeDinamica();
        });

        btnModoDesembarque.addActionListener(e -> {
            alternarEstiloMenu(btnModoDesembarque, btnModoCheckIn, btnModoAbordaje);
            lblTituloSeccionCambiante.setText("SECUENCIA CRÍTICA DE DESEMBARQUE - PRIORIDAD: ADELANTE HACIA ATRÁS");
            cardLayoutModos.show(panelCentralWorkspace, "VISTA_TABLAS");
            calcularDesembarqueDinamico();
        });
    }

    private JPanel construirFormularioCheckInVisual() {
        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        // Tarjeta del Formulario
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(EstiloUI.FONDO_TARJETA);
        formCard.setPreferredSize(new Dimension(360, 500));
        formCard.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(20, 15, 20, 15)));

        txtBuscarPNR = crearFieldEstilizado();
        JButton btnBuscarPNR = new JButton("BUSCAR");
        btnBuscarPNR.setBackground(EstiloUI.AZUL_ACCENT); btnBuscarPNR.setForeground(EstiloUI.TEXTO_BLANCO);
        btnBuscarPNR.setBorderPainted(false); btnBuscarPNR.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnBuscarPNR.setFocusPainted(false);
        EstiloUI.aplicarHover(btnBuscarPNR, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());

        JPanel panelBuscar = new JPanel(new BorderLayout(5, 0));
        panelBuscar.setBackground(EstiloUI.FONDO_TARJETA);
        panelBuscar.add(txtBuscarPNR, BorderLayout.CENTER);
        panelBuscar.add(btnBuscarPNR, BorderLayout.EAST);

        txtNombrePasajero = crearFieldEstilizado();
        txtNombrePasajero.setEditable(false);
        txtPesoEquipaje = crearFieldEstilizado();
        txtPesoEquipaje.setEnabled(false);
        
        comboPrioridad = new JComboBox<>(new String[]{"1", "2", "3"});
        comboPrioridad.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); comboPrioridad.setForeground(EstiloUI.TEXTO_BLANCO);
        comboPrioridad.setEnabled(false);

        lblAsientoAsignado = new JLabel("[ Asiento ]");
        lblAsientoAsignado.setForeground(Color.CYAN); lblAsientoAsignado.setFont(EstiloUI.FUENTE_SUBTITULO);

        btnEnviarCheckIn = new JButton("EMITIR PASE DE ABORDAJE");
        btnEnviarCheckIn.setBackground(EstiloUI.AZUL_ACCENT); btnEnviarCheckIn.setForeground(EstiloUI.TEXTO_BLANCO); btnEnviarCheckIn.setBorderPainted(false); btnEnviarCheckIn.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnEnviarCheckIn.setEnabled(false);
        EstiloUI.aplicarHover(btnEnviarCheckIn, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5); g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel lblTitle = new JLabel("REGISTRO DE PASAJEROS"); lblTitle.setForeground(EstiloUI.TEXTO_BLANCO); lblTitle.setFont(EstiloUI.FUENTE_TITULO); formCard.add(lblTitle, g);

        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 1; formCard.add(new JLabel("Código PNR:"), g); g.gridx = 1; formCard.add(panelBuscar, g);
        g.gridx = 0; g.gridy = 2; formCard.add(new JLabel("Nombre Pasajero:"), g); g.gridx = 1; formCard.add(txtNombrePasajero, g);
        g.gridx = 0; g.gridy = 3; formCard.add(new JLabel("Categoría Vuelo:"), g); g.gridx = 1; formCard.add(comboPrioridad, g);
        g.gridx = 0; g.gridy = 4; formCard.add(new JLabel("Butaca Asignada:"), g); g.gridx = 1; formCard.add(lblAsientoAsignado, g);
        g.gridx = 0; g.gridy = 5; formCard.add(new JLabel("Peso Maleta (kg):"), g); g.gridx = 1; formCard.add(txtPesoEquipaje, g);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.insets = new Insets(20, 5, 5, 5); formCard.add(btnEnviarCheckIn, g);

        for (Component comp : formCard.getComponents()) {
            if (comp instanceof JLabel && comp != lblTitle && comp != lblAsientoAsignado) {
                comp.setForeground(EstiloUI.TEXTO_MUTED); comp.setFont(EstiloUI.FUENTE_LABEL);
            }
        }
        splitPanel.add(formCard, BorderLayout.WEST);

        // Tarjeta de la Lista de Pasajeros del PNR
        contenedorDinamicoCabina = new JPanel(new BorderLayout());
        contenedorDinamicoCabina.setBackground(EstiloUI.FONDO_TARJETA);
        contenedorDinamicoCabina.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        modeloTablaPasajerosPNR = new DefaultTableModel(new Object[]{"Nombre", "Asiento", "Clase", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPasajerosPNR = new JTable(modeloTablaPasajerosPNR);
        tablaPasajerosPNR.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tablaPasajerosPNR.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaPasajerosPNR.setGridColor(new Color(48, 54, 61));
        tablaPasajerosPNR.getTableHeader().setBackground(EstiloUI.FONDO_TARJETA);
        tablaPasajerosPNR.getTableHeader().setForeground(EstiloUI.TEXTO_MUTED);
        tablaPasajerosPNR.setRowHeight(24);
        tablaPasajerosPNR.setSelectionBackground(EstiloUI.AZUL_ACCENT);

        JScrollPane scrollPNR = new JScrollPane(tablaPasajerosPNR);
        scrollPNR.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollPNR.setBorder(BorderFactory.createEmptyBorder());
        
        contenedorDinamicoCabina.add(scrollPNR, BorderLayout.CENTER);
        splitPanel.add(contenedorDinamicoCabina, BorderLayout.CENTER);

        txtBuscarPNR.addActionListener(e -> buscarReservaPNR());
        btnBuscarPNR.addActionListener(e -> buscarReservaPNR());
        btnEnviarCheckIn.addActionListener(e -> procesarGuardadoCheckIn());

        // Evento de selección en la tabla de pasajeros
        tablaPasajerosPNR.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaPasajerosPNR.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < pasajerosPNR.size()) {
                    pasajeroCargado = pasajerosPNR.get(selectedRow);
                    txtNombrePasajero.setText(pasajeroCargado.getNombre());
                    comboPrioridad.setSelectedItem(String.valueOf(pasajeroCargado.getNivelPrioridad()));
                    lblAsientoAsignado.setText(pasajeroCargado.getNumAsiento());

                    if (pasajeroCargado.getTimestampLlegada() != null) {
                        btnEnviarCheckIn.setEnabled(false);
                        txtPesoEquipaje.setEnabled(false);
                        txtPesoEquipaje.setText("Check-in completado");
                    } else {
                        btnEnviarCheckIn.setEnabled(true);
                        txtPesoEquipaje.setEnabled(true);
                        txtPesoEquipaje.setText("");
                    }
                }
            }
        });

        return splitPanel;
    }

    private JPanel construirListadosTabularesDeVuelo() {
        JPanel panelContenedorTabla = new JPanel(new BorderLayout(10, 12));
        panelContenedorTabla.setBackground(EstiloUI.FONDO_TARJETA);
        panelContenedorTabla.setBorder(BorderFactory.createCompoundBorder(EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        lblTituloSeccionCambiante = new JLabel("MONITOR OPERATIVO DE CONTROL");
        lblTituloSeccionCambiante.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTituloSeccionCambiante.setFont(EstiloUI.FUENTE_SUBTITULO);
        panelContenedorTabla.add(lblTituloSeccionCambiante, BorderLayout.NORTH);

        modeloTablaFlujo = new DefaultTableModel(new Object[]{"ID PASAJERO", "NOMBRE DEL PASAJERO", "ASIENTO", "PRIORIDAD", "TIMESTAMP REGISTRO"}, 0);
        tablaFlujoOperaciones = new JTable(modeloTablaFlujo);
        tablaFlujoOperaciones.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tablaFlujoOperaciones.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaFlujoOperaciones.setGridColor(new Color(48, 54, 61));

        JScrollPane scrollPane = new JScrollPane(tablaFlujoOperaciones);
        scrollPane.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panelContenedorTabla.add(scrollPane, BorderLayout.CENTER);

        return panelContenedorTabla;
    }

    private void recargarVuelosDesdeFlota() {
        comboVuelosFlota.removeAllItems();
        List<Avion> flota = avionDAO.obtenerAvionesFlota();
        for (Avion a : flota) {
            comboVuelosFlota.addItem(a.getMatricula() + " - " + a.getModelo());
        }
        if (flota.isEmpty()) {
            comboVuelosFlota.addItem("HC-BXA - Configuración por Defecto");
        }
    }

    private String obtenerMatriculaActiva() {
        String item = (String) comboVuelosFlota.getSelectedItem();
        if (item == null || item.contains("Defecto")) {
            return "HC-BXA";
        }
        return item.split(" - ")[0];
    }

    private void buscarReservaPNR() {
        String pnr = txtBuscarPNR.getText().trim().toUpperCase();
        if (pnr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un código PNR para buscar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        pasajerosPNR = pasajeroDAO.obtenerPasajerosPorPNRMult(pnr);
        if (pasajerosPNR.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Reserva no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            limpiarCamposCheckIn();
            modeloTablaPasajerosPNR.setRowCount(0);
            return;
        }

        recargarTablaPasajerosPNR();
        limpiarCamposCheckIn();
    }

    private void recargarTablaPasajerosPNR() {
        modeloTablaPasajerosPNR.setRowCount(0);
        for (Pasajero p : pasajerosPNR) {
            String estado = p.getTimestampLlegada() != null 
                    ? "Registrado (" + p.getTimestampLlegada().format(DateTimeFormatter.ofPattern("HH:mm")) + ")" 
                    : "Pendiente";
            String clase = p.getNivelPrioridad() == 1 ? "VIP" : p.getNivelPrioridad() == 2 ? "Ejecutiva" : "Económica";
            modeloTablaPasajerosPNR.addRow(new Object[]{p.getNombre(), p.getNumAsiento(), clase, estado});
        }
    }

    private void procesarGuardadoCheckIn() {
        if (pasajeroCargado == null) {
            JOptionPane.showMessageDialog(this, "Debe buscar y seleccionar un pasajero de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String pesoTexto = txtPesoEquipaje.getText().trim();
        if (pesoTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete el peso de la maleta.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(pesoTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El peso de la maleta debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            CheckInService.realizarCheckIn(pasajeroCargado, peso);

            JOptionPane.showMessageDialog(this, "¡Pase de abordar emitido con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Refrescar tabla
            String pnr = txtBuscarPNR.getText().trim().toUpperCase();
            pasajerosPNR = pasajeroDAO.obtenerPasajerosPorPNRMult(pnr);
            recargarTablaPasajerosPNR();
            limpiarCamposCheckIn();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al realizar check-in", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCamposCheckIn() {
        pasajeroCargado = null;
        txtNombrePasajero.setText("");
        txtPesoEquipaje.setText("");
        lblAsientoAsignado.setText("[ Asiento ]");
        btnEnviarCheckIn.setEnabled(false);
        txtPesoEquipaje.setEnabled(false);
        tablaPasajerosPNR.clearSelection();
    }

    private void calcularColaAbordajeDinamica() {
        modeloTablaFlujo.setRowCount(0);
        List<Pasajero> base = pasajeroDAO.obtenerPasajerosPorVuelo(obtenerMatriculaActiva());
        List<Pasajero> ordenados = new ColaAbordaje().organizarPasajeros(base);

        for (Pasajero p : ordenados) {
            modeloTablaFlujo.addRow(new Object[]{p.getIdPasajero(), p.getNombre(), p.getNumAsiento(), p.getNivelPrioridad(), p.getTimestampLlegada()});
        }
    }

    private void calcularDesembarqueDinamico() {
        modeloTablaFlujo.setRowCount(0);
        List<Pasajero> base = pasajeroDAO.obtenerPasajerosPorVuelo(obtenerMatriculaActiva());
        List<Pasajero> ordenados = new DesembarqueManager().ordenarPorAsiento(base);

        for (Pasajero p : ordenados) {
            modeloTablaFlujo.addRow(new Object[]{p.getIdPasajero(), p.getNombre(), p.getNumAsiento(), p.getNivelPrioridad(), p.getTimestampLlegada()});
        }
    }

    private JTextField crearFieldEstilizado() {
        JTextField f = new JTextField(15);
        f.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL); f.setForeground(EstiloUI.TEXTO_BLANCO); f.setCaretColor(EstiloUI.TEXTO_BLANCO); f.setBorder(EstiloUI.BORDE_COMPONENTE);
        return f;
    }

    private JButton crearBotonSidebar(String texto, boolean activo) {
        JButton b = new JButton(texto);
        b.setPreferredSize(new Dimension(165, 42)); b.setFont(EstiloUI.FUENTE_COMPONENTE); b.setFocusPainted(false); b.setBorderPainted(false);
        if (activo) {
            b.setBackground(EstiloUI.AZUL_ACCENT); b.setForeground(EstiloUI.TEXTO_BLANCO);
        } else {
            b.setBackground(EstiloUI.GRIS_BOTON_PASIVO); b.setForeground(EstiloUI.TEXTO_MUTED);
        }
        return b;
    }

    private void alternarEstiloMenu(JButton sel, JButton b2, JButton b3) {
        sel.setBackground(EstiloUI.AZUL_ACCENT); sel.setForeground(EstiloUI.TEXTO_BLANCO);
        b2.setBackground(EstiloUI.GRIS_BOTON_PASIVO); b2.setForeground(EstiloUI.TEXTO_MUTED);
        b3.setBackground(EstiloUI.GRIS_BOTON_PASIVO); b3.setForeground(EstiloUI.TEXTO_MUTED);
    }
}