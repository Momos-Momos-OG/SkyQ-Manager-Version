package skyq.view;

import skyq.dao.AuditoriaDAO;
import skyq.dao.MantenimientoDAO;
import skyq.dao.PasajeroDAO;
import skyq.dao.VueloDAO;
import skyq.logic.LoggerManager;
import skyq.logic.SesionManager;
import skyq.model.Pasajero;
import skyq.model.Vuelo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PanelVentas extends JPanel {
    private static final long serialVersionUID = 1L;

    private JComboBox<String> comboVuelos;
    private JTextField txtNombrePasajero;
    private JComboBox<String> comboPrioridad;
    private JButton btnProcesarVenta;

    private transient List<Vuelo> vuelosCargados;
    private final transient VueloDAO vueloDAO = new VueloDAO();
    private final transient MantenimientoDAO mantenimientoDAO = new MantenimientoDAO();
    private final transient PasajeroDAO pasajeroDAO = new PasajeroDAO();
    private final transient AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelVentas() {
        setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setLayout(new GridBagLayout());
        initComponents();
        recargarVuelos();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tarjeta Central
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(EstiloUI.FONDO_TARJETA);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_TARJETA, new EmptyBorder(30, 40, 30, 40)));
        cardPanel.setPreferredSize(new Dimension(500, 420));

        JLabel lblTitulo = new JLabel("🛒 VENTAS Y RESERVAS (BOOKING)");
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_TITULO);

        JLabel lblVuelo = crearLabel("Vuelo Destinado:");
        comboVuelos = new JComboBox<>();
        estilizarCombo(comboVuelos);

        JLabel lblNombre = crearLabel("Nombre Pasajero:");
        txtNombrePasajero = crearCampoTexto();

        JLabel lblPrioridad = crearLabel("Categoría Prioridad:");
        comboPrioridad = new JComboBox<>(new String[]{"VIP (1)", "Normal (2)", "Básico (3)"});
        estilizarCombo(comboPrioridad);

        btnProcesarVenta = new JButton("PROCESAR RESERVA / GENERAR PNR");
        btnProcesarVenta.setBackground(EstiloUI.VERDE_NEON);
        btnProcesarVenta.setForeground(EstiloUI.TEXTO_BLANCO);
        btnProcesarVenta.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnProcesarVenta.setBorderPainted(false);
        btnProcesarVenta.setFocusPainted(false);
        PanelRadarView.aplicarHover(btnProcesarVenta, EstiloUI.VERDE_NEON, EstiloUI.VERDE_NEON.brighter());

        JButton btnActualizarVuelos = new JButton("🔄 Actualizar Vuelos");
        btnActualizarVuelos.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        btnActualizarVuelos.setForeground(EstiloUI.TEXTO_BLANCO);
        btnActualizarVuelos.setFont(EstiloUI.FUENTE_LABEL);
        btnActualizarVuelos.setBorderPainted(false);
        btnActualizarVuelos.setFocusPainted(false);
        PanelRadarView.aplicarHover(btnActualizarVuelos, EstiloUI.GRIS_BOTON_PASIVO, new Color(55, 62, 71));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        cardPanel.add(lblTitulo, c);

        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1; cardPanel.add(lblVuelo, c);
        c.gridx = 1; cardPanel.add(comboVuelos, c);

        c.gridx = 0; c.gridy = 2; cardPanel.add(lblNombre, c);
        c.gridx = 1; cardPanel.add(txtNombrePasajero, c);

        c.gridx = 0; c.gridy = 3; cardPanel.add(lblPrioridad, c);
        c.gridx = 1; cardPanel.add(comboPrioridad, c);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        cardPanel.add(btnProcesarVenta, c);

        c.gridy = 5;
        cardPanel.add(btnActualizarVuelos, c);

        gbc.gridx = 0; gbc.gridy = 0;
        add(cardPanel, gbc);

        // Listeners
        btnProcesarVenta.addActionListener(e -> procesarVenta());
        btnActualizarVuelos.addActionListener(e -> recargarVuelos());
    }

    private void recargarVuelos() {
        comboVuelos.removeAllItems();
        vuelosCargados = vueloDAO.obtenerTodosLosVuelos();
        for (Vuelo v : vuelosCargados) {
            String modeloAvion;
            if (v.getModeloAvion() != null) {
                modeloAvion = v.getModeloAvion();
            } else {
                modeloAvion = "Avión";
            }
            String fechaSalidaStr;
            if (v.getFechaSalida() != null) {
                fechaSalidaStr = v.getFechaSalida().format(FMT);
            } else {
                fechaSalidaStr = "Sin fecha";
            }
            String etiqueta = String.format("Vuelo #%d - %s (%s) - Salida: %s",
                    v.getIdVuelo(),
                    v.getMatricula(),
                    modeloAvion,
                    fechaSalidaStr);
            comboVuelos.addItem(etiqueta);
        }
        if (vuelosCargados.isEmpty()) {
            comboVuelos.addItem("No hay vuelos programados en el sistema");
        }
    }

    private void procesarVenta() {
        if (vuelosCargados == null || vuelosCargados.isEmpty() || comboVuelos.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un vuelo válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vuelo vuelo = vuelosCargados.get(comboVuelos.getSelectedIndex());
        String nombre = txtNombrePasajero.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del pasajero.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener prioridad (VIP = 1, Normal = 2, Básico = 3)
        int indexPrioridad = comboPrioridad.getSelectedIndex();
        int prioridad = indexPrioridad + 1;

        // Validación crítica de Mantenimiento
        java.util.Date fechaVuelo = java.sql.Timestamp.valueOf(vuelo.getFechaSalida());
        boolean enManto = mantenimientoDAO.estaEnMantenimiento(vuelo.getMatricula(), fechaVuelo);

        if (enManto) {
            JOptionPane.showMessageDialog(this,
                    "Venta denegada. La aeronave asignada a este vuelo se encontrará en mantenimiento durante esa fecha.",
                    "Error Crítico",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generar código PNR
        String pnr = generarPNR();

        // Crear Pasajero (con asiento vacío y sin timestamp de check-in)
        Pasajero pasajero = new Pasajero(0, nombre, "", prioridad, null, vuelo.getMatricula(), pnr);

        int idPasajero = pasajeroDAO.insertarPasajeroYObtenerId(pasajero);

        if (idPasajero > 0) {
            // Auditoría y Log
            String userActual = "Desconocido";
            if (SesionManager.getInstance().getUsuarioActual() != null) {
                userActual = SesionManager.getInstance().getUsuarioActual().getUsername();
            }
            auditoriaDAO.registrarAccion(userActual, "VENTA_BOLETO",
                    "Pasajero: " + nombre + ", PNR: " + pnr + ", Vuelo: #" + vuelo.getIdVuelo() + ", Avión: " + vuelo.getMatricula());

            LoggerManager.getInstance().logInfo("Venta procesada - PNR: " + pnr + " para pasajero: " + nombre);

            JOptionPane.showMessageDialog(this,
                    "¡Reserva completada con éxito!\nCódigo PNR: " + pnr + "\nPor favor, recuerde este código para su Check-In.",
                    "Venta Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            txtNombrePasajero.setText("");
            comboPrioridad.setSelectedIndex(1); // Restablecer a Normal
        } else {
            JOptionPane.showMessageDialog(this, "Error de base de datos al guardar la reserva.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generarPNR() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("SQ-");
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 4; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(EstiloUI.TEXTO_MUTED);
        l.setFont(EstiloUI.FUENTE_LABEL);
        return l;
    }

    private JTextField crearCampoTexto() {
        JTextField f = new JTextField(18);
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
