package skyq.view;

import skyq.dao.AuditoriaDAO;
import skyq.dao.AvionDAO;
import skyq.dao.MantenimientoDAO;
import skyq.dao.PilotoDAO;
import skyq.dao.VueloDAO;
import skyq.dao.UsuarioDAO;
import skyq.logic.LoggerManager;
import skyq.logic.SesionManager;
import skyq.model.Avion;
import skyq.model.Piloto;
import skyq.model.Vuelo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PanelVuelos extends JPanel {

    private JTextField txtOrigen, txtDestino, txtSalida, txtLlegada;
    private JComboBox<String> comboAvion, comboPiloto, comboEstado;
    private JTable tablaVuelos;
    private DefaultTableModel modeloTabla;

    private List<Avion> avionesFlota;
    private List<Piloto> pilotosFlota;
    private List<Vuelo> vuelosProgramados;

    private final VueloDAO vueloDAO = new VueloDAO();
    private final AvionDAO avionDAO = new AvionDAO();
    private final PilotoDAO pilotoDAO = new PilotoDAO();
    private final MantenimientoDAO mantenimientoDAO = new MantenimientoDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String DATE_PLACEHOLDER = "dd/MM/yyyy HH:mm";

    private Vuelo vueloSeleccionado = null;

    public PanelVuelos() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(15, 15));
        initComponents();
        cargarDatosFormulario();
        recargarTablaVuelos();
    }

    private void initComponents() {
        // ==========================================
        // 🛫 PANEL SUPERIOR/IZQUIERDO: FORMULARIO
        // ==========================================
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(EstiloUI.FONDO_TARJETA);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));
        formCard.setPreferredSize(new Dimension(380, 500));

        JLabel lblTitle = new JLabel("PROGRAMACIÓN DE VUELOS");
        lblTitle.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitle.setFont(EstiloUI.FUENTE_TITULO);

        txtOrigen = crearFieldEstilizado();
        txtDestino = crearFieldEstilizado();
        
        txtSalida = crearFieldEstilizado();
        txtSalida.setText(DATE_PLACEHOLDER);
        txtSalida.setForeground(EstiloUI.TEXTO_MUTED);
        limpiarPlaceholder(txtSalida, DATE_PLACEHOLDER);

        txtLlegada = crearFieldEstilizado();
        txtLlegada.setText(DATE_PLACEHOLDER);
        txtLlegada.setForeground(EstiloUI.TEXTO_MUTED);
        limpiarPlaceholder(txtLlegada, DATE_PLACEHOLDER);

        comboAvion = new JComboBox<>();
        estilizarCombo(comboAvion);

        comboPiloto = new JComboBox<>();
        estilizarCombo(comboPiloto);

        comboEstado = new JComboBox<>(new String[]{"Programado", "En Vuelo", "Completado", "Cancelado"});
        estilizarCombo(comboEstado);

        // Botones
        JButton btnRegistrar = new JButton("REGISTRAR VUELO");
        btnRegistrar.setBackground(EstiloUI.VERDE_NEON);
        btnRegistrar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnRegistrar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setFocusPainted(false);
        PanelRadarView.aplicarHover(btnRegistrar, EstiloUI.VERDE_NEON, EstiloUI.VERDE_NEON.brighter());

        JButton btnEditar = new JButton("EDITAR VUELO");
        btnEditar.setBackground(EstiloUI.AZUL_ACCENT);
        btnEditar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnEditar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnEditar.setBorderPainted(false);
        btnEditar.setFocusPainted(false);
        PanelRadarView.aplicarHover(btnEditar, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());

        JButton btnEliminar = new JButton("ELIMINAR VUELO");
        btnEliminar.setBackground(EstiloUI.ROJO_ALERTA);
        btnEliminar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnEliminar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnEliminar.setBorderPainted(false);
        btnEliminar.setFocusPainted(false);
        PanelRadarView.aplicarHover(btnEliminar, EstiloUI.ROJO_ALERTA, EstiloUI.ROJO_ALERTA.brighter());

        JButton btnLimpiar = new JButton("LIMPIAR");
        btnLimpiar.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        btnLimpiar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnLimpiar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setFocusPainted(false);
        PanelRadarView.aplicarHover(btnLimpiar, EstiloUI.GRIS_BOTON_PASIVO, new Color(55, 62, 71));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 5, 6, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        formCard.add(lblTitle, g);

        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 1; formCard.add(crearLabel("Origen:"), g);
        g.gridx = 1; formCard.add(txtOrigen, g);

        g.gridx = 0; g.gridy = 2; formCard.add(crearLabel("Destino:"), g);
        g.gridx = 1; formCard.add(txtDestino, g);

        g.gridx = 0; g.gridy = 3; formCard.add(crearLabel("Fecha Salida:"), g);
        g.gridx = 1; formCard.add(txtSalida, g);

        g.gridx = 0; g.gridy = 4; formCard.add(crearLabel("Fecha Llegada:"), g);
        g.gridx = 1; formCard.add(txtLlegada, g);

        g.gridx = 0; g.gridy = 5; formCard.add(crearLabel("Aeronave:"), g);
        g.gridx = 1; formCard.add(comboAvion, g);

        g.gridx = 0; g.gridy = 6; formCard.add(crearLabel("Piloto:"), g);
        g.gridx = 1; formCard.add(comboPiloto, g);

        g.gridx = 0; g.gridy = 7; formCard.add(crearLabel("Estado:"), g);
        g.gridx = 1; formCard.add(comboEstado, g);

        // Sub-panel de botones organizados
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 8, 8));
        panelBotones.setBackground(EstiloUI.FONDO_TARJETA);
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
        g.insets = new Insets(15, 5, 5, 5);
        formCard.add(panelBotones, g);

        for (Component comp : formCard.getComponents()) {
            if (comp instanceof JLabel && comp != lblTitle) {
                comp.setForeground(EstiloUI.TEXTO_MUTED);
                comp.setFont(EstiloUI.FUENTE_LABEL);
            }
        }
        add(formCard, BorderLayout.WEST);

        // ==========================================
        // 🖥️ PANEL INFERIOR/DERECHO: TABLA
        // ==========================================
        JPanel tableCard = new JPanel(new BorderLayout(10, 10));
        tableCard.setBackground(EstiloUI.FONDO_TARJETA);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTableTitle = new JLabel("📅 HISTORIAL Y VUELOS PROGRAMADOS");
        lblTableTitle.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTableTitle.setFont(EstiloUI.FUENTE_SUBTITULO);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] columnas = {"ID VUELO", "ORIGEN", "DESTINO", "AERONAVE", "PILOTO", "SALIDA", "LLEGADA", "ESTADO"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaVuelos = new JTable(modeloTabla);
        tablaVuelos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        tablaVuelos.setForeground(EstiloUI.TEXTO_BLANCO);
        tablaVuelos.setGridColor(new Color(48, 54, 61));
        tablaVuelos.getTableHeader().setBackground(EstiloUI.FONDO_TARJETA);
        tablaVuelos.getTableHeader().setForeground(EstiloUI.TEXTO_MUTED);
        tablaVuelos.setRowHeight(26);
        tablaVuelos.setSelectionBackground(EstiloUI.AZUL_ACCENT);

        JScrollPane scrollTable = new JScrollPane(tablaVuelos);
        scrollTable.getViewport().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollTable, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // Listeners de los botones
        btnRegistrar.addActionListener(e -> procesarGuardarVuelo());
        btnEditar.addActionListener(e -> procesarEditarVuelo());
        btnEliminar.addActionListener(e -> procesarEliminarVuelo());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        // Listener de selección en tabla
        tablaVuelos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSel = tablaVuelos.getSelectedRow();
                if (filaSel >= 0) {
                    vueloSeleccionado = vuelosProgramados.get(filaSel);
                    cargarVueloEnFormulario(vueloSeleccionado);
                }
            }
        });
    }

    private void cargarDatosFormulario() {
        comboAvion.removeAllItems();
        avionesFlota = avionDAO.obtenerAvionesFlota();
        for (Avion a : avionesFlota) {
            // Mostrar todos los aviones pero identificar su estado
            comboAvion.addItem(a.getMatricula() + " - " + a.getModelo() + " (" + a.getEstado() + ")");
        }

        comboPiloto.removeAllItems();
        pilotosFlota = pilotoDAO.obtenerPilotos();
        for (Piloto p : pilotosFlota) {
            comboPiloto.addItem(p.getNombre() + " (" + p.getRango() + ") [" + p.getEstado() + "]");
        }
    }

    private void recargarTablaVuelos() {
        modeloTabla.setRowCount(0);
        vuelosProgramados = vueloDAO.obtenerTodosLosVuelos();

        for (Vuelo v : vuelosProgramados) {
            modeloTabla.addRow(new Object[]{
                    v.getIdVuelo(),
                    v.getOrigen() != null ? v.getOrigen() : "—",
                    v.getDestino() != null ? v.getDestino() : "—",
                    v.getMatricula(),
                    v.getNombrePiloto() != null ? v.getNombrePiloto() : "ID: " + v.getIdPiloto(),
                    v.getFechaSalida() != null ? v.getFechaSalida().format(FMT) : "—",
                    v.getFechaRegreso() != null ? v.getFechaRegreso().format(FMT) : "—",
                    v.getEstado()
            });
        }
    }

    private void cargarVueloEnFormulario(Vuelo v) {
        txtOrigen.setText(v.getOrigen());
        txtDestino.setText(v.getDestino());
        
        txtSalida.setText(v.getFechaSalida() != null ? v.getFechaSalida().format(FMT) : "");
        txtSalida.setForeground(EstiloUI.TEXTO_BLANCO);
        
        txtLlegada.setText(v.getFechaRegreso() != null ? v.getFechaRegreso().format(FMT) : "");
        txtLlegada.setForeground(EstiloUI.TEXTO_BLANCO);

        // Seleccionar avión correspondiente
        for (int i = 0; i < avionesFlota.size(); i++) {
            if (avionesFlota.get(i).getMatricula().equals(v.getMatricula())) {
                comboAvion.setSelectedIndex(i);
                break;
            }
        }

        // Seleccionar piloto correspondiente
        for (int i = 0; i < pilotosFlota.size(); i++) {
            if (pilotosFlota.get(i).getIdPiloto() == v.getIdPiloto()) {
                comboPiloto.setSelectedIndex(i);
                break;
            }
        }

        comboEstado.setSelectedItem(v.getEstado());
    }

    private void procesarGuardarVuelo() {
        String origen = txtOrigen.getText().trim();
        String destino = txtDestino.getText().trim();
        String salidaStr = txtSalida.getText().trim();
        String llegadaStr = txtLlegada.getText().trim();

        if (origen.isEmpty() || destino.isEmpty() || salidaStr.equals(DATE_PLACEHOLDER) || llegadaStr.equals(DATE_PLACEHOLDER)) {
            JOptionPane.showMessageDialog(this, "Rellene todos los campos del formulario.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime fechaSalida, fechaLlegada;
        try {
            fechaSalida = LocalDateTime.parse(salidaStr, FMT);
            fechaLlegada = LocalDateTime.parse(llegadaStr, FMT);
            if (!fechaLlegada.isAfter(fechaSalida)) {
                JOptionPane.showMessageDialog(this, "La llegada debe ser posterior a la salida.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fechas incorrecto. Use: dd/MM/yyyy HH:mm", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (comboAvion.getSelectedIndex() < 0 || comboPiloto.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Asegúrese de seleccionar avión y piloto.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String matricula = avionesFlota.get(comboAvion.getSelectedIndex()).getMatricula();
        int idPiloto = pilotosFlota.get(comboPiloto.getSelectedIndex()).getIdPiloto();
        String estado = (String) comboEstado.getSelectedItem();

        // Validar mantenimiento en fecha de salida
        java.util.Date dateSalida = java.sql.Timestamp.valueOf(fechaSalida);
        boolean enManto = mantenimientoDAO.estaEnMantenimiento(matricula, dateSalida);
        if (enManto) {
            JOptionPane.showMessageDialog(this,
                    "Registro denegado. La aeronave seleccionada se encuentra en mantenimiento durante la fecha de salida.",
                    "Conflicto Operativo",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vuelo nuevoVuelo = new Vuelo(0, matricula, idPiloto, fechaSalida, fechaLlegada, estado, origen, destino);

        if (vueloDAO.insertarVuelo(nuevoVuelo)) {
            String user = SesionManager.getInstance().getUsuarioActual() != null ? SesionManager.getInstance().getUsuarioActual().getUsername() : "Sistema";
            auditoriaDAO.registrarAccion(user, "REGISTRAR_VUELO", "Matrícula: " + matricula + ", Ruta: " + origen + " - " + destino);
            LoggerManager.getInstance().logInfo("Vuelo registrado exitosamente para avión: " + matricula);

            JOptionPane.showMessageDialog(this, "Vuelo programado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            recargarTablaVuelos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar el vuelo en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void procesarEditarVuelo() {
        if (vueloSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un vuelo de la tabla para editar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Solicitud de autorización Manager Override
        String passwordGerente = solicitarPasswordAutorizacion();
        if (passwordGerente == null) return; // Cancelado

        if (!UsuarioDAO.verificarPasswordGerente(passwordGerente)) {
            JOptionPane.showMessageDialog(this, "Autorización denegada. Contraseña incorrecta.", "Seguridad", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar formulario
        String origen = txtOrigen.getText().trim();
        String destino = txtDestino.getText().trim();
        String salidaStr = txtSalida.getText().trim();
        String llegadaStr = txtLlegada.getText().trim();

        if (origen.isEmpty() || destino.isEmpty() || salidaStr.isEmpty() || llegadaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Los campos no pueden estar vacíos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime fechaSalida, fechaLlegada;
        try {
            fechaSalida = LocalDateTime.parse(salidaStr, FMT);
            fechaLlegada = LocalDateTime.parse(llegadaStr, FMT);
            if (!fechaLlegada.isAfter(fechaSalida)) {
                JOptionPane.showMessageDialog(this, "La llegada debe ser posterior a la salida.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fechas incorrecto. Use: dd/MM/yyyy HH:mm", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String matricula = avionesFlota.get(comboAvion.getSelectedIndex()).getMatricula();
        int idPiloto = pilotosFlota.get(comboPiloto.getSelectedIndex()).getIdPiloto();
        String estado = (String) comboEstado.getSelectedItem();

        // Validar mantenimiento
        java.util.Date dateSalida = java.sql.Timestamp.valueOf(fechaSalida);
        boolean enManto = mantenimientoDAO.estaEnMantenimiento(matricula, dateSalida);
        if (enManto) {
            JOptionPane.showMessageDialog(this,
                    "Modificación denegada. La aeronave seleccionada se encuentra en mantenimiento durante la fecha de salida.",
                    "Conflicto Operativo",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        vueloSeleccionado.setMatricula(matricula);
        vueloSeleccionado.setIdPiloto(idPiloto);
        vueloSeleccionado.setFechaSalida(fechaSalida);
        vueloSeleccionado.setFechaRegreso(fechaLlegada);
        vueloSeleccionado.setEstado(estado);
        vueloSeleccionado.setOrigen(origen);
        vueloSeleccionado.setDestino(destino);

        if (vueloDAO.actualizarVuelo(vueloSeleccionado)) {
            String user = SesionManager.getInstance().getUsuarioActual() != null ? SesionManager.getInstance().getUsuarioActual().getUsername() : "Operario";
            auditoriaDAO.registrarAccion(user, "EDITAR_VUELO", "Autorizó Gerente. Vuelo ID: " + vueloSeleccionado.getIdVuelo() + ", Ruta: " + origen + " - " + destino);
            LoggerManager.getInstance().logInfo("Vuelo ID: " + vueloSeleccionado.getIdVuelo() + " modificado bajo Manager Override.");

            JOptionPane.showMessageDialog(this, "Vuelo modificado exitosamente bajo supervisión.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            recargarTablaVuelos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al modificar el vuelo en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void procesarEliminarVuelo() {
        if (vueloSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un vuelo de la tabla para eliminar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Solicitud de autorización Manager Override
        String passwordGerente = solicitarPasswordAutorizacion();
        if (passwordGerente == null) return; // Cancelado

        if (!UsuarioDAO.verificarPasswordGerente(passwordGerente)) {
            JOptionPane.showMessageDialog(this, "Autorización denegada. Contraseña incorrecta.", "Seguridad", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar de forma permanente el vuelo #" + vueloSeleccionado.getIdVuelo() + " (" + vueloSeleccionado.getOrigen() + " - " + vueloSeleccionado.getDestino() + ")?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (vueloDAO.eliminarVuelo(vueloSeleccionado.getIdVuelo())) {
                String user = SesionManager.getInstance().getUsuarioActual() != null ? SesionManager.getInstance().getUsuarioActual().getUsername() : "Operario";
                auditoriaDAO.registrarAccion(user, "ELIMINAR_VUELO", "Autorizó Gerente. Vuelo ID: " + vueloSeleccionado.getIdVuelo());
                LoggerManager.getInstance().logInfo("Vuelo ID: " + vueloSeleccionado.getIdVuelo() + " eliminado bajo Manager Override.");

                JOptionPane.showMessageDialog(this, "Vuelo eliminado del sistema.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                recargarTablaVuelos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el vuelo. El vuelo puede estar asociado a pasajeros activos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String solicitarPasswordAutorizacion() {
        JPasswordField pf = new JPasswordField(15);
        pf.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        pf.setForeground(EstiloUI.TEXTO_BLANCO);
        pf.setCaretColor(EstiloUI.TEXTO_BLANCO);
        pf.setBorder(EstiloUI.BORDE_COMPONENTE);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(EstiloUI.FONDO_TARJETA);
        JLabel lbl = new JLabel("Acción restringida. Ingrese contraseña de autorización de Gerente:");
        lbl.setForeground(EstiloUI.TEXTO_BLANCO);
        lbl.setFont(EstiloUI.FUENTE_LABEL);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(pf, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(this, panel, "Manager Override", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            return new String(pf.getPassword());
        }
        return null;
    }

    private void limpiarCampos() {
        txtOrigen.setText("");
        txtDestino.setText("");
        txtSalida.setText(DATE_PLACEHOLDER);
        txtSalida.setForeground(EstiloUI.TEXTO_MUTED);
        txtLlegada.setText(DATE_PLACEHOLDER);
        txtLlegada.setForeground(EstiloUI.TEXTO_MUTED);
        if (comboAvion.getItemCount() > 0) comboAvion.setSelectedIndex(0);
        if (comboPiloto.getItemCount() > 0) comboPiloto.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);
        tablaVuelos.clearSelection();
        vueloSeleccionado = null;
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloUI.TEXTO_MUTED);
        l.setFont(EstiloUI.FUENTE_LABEL);
        return l;
    }

    private JTextField crearFieldEstilizado() {
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
}
