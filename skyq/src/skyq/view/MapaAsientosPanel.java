package skyq.view;

import skyq.dao.ConfiguracionDAO;
import skyq.dao.PasajeroDAO;
import skyq.dao.AvionDAO;
import skyq.model.Avion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class MapaAsientosPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final String matriculaAvion;
    private final transient AsientoSeleccionadoListener listener;
    private final transient PasajeroDAO pasajeroDAO;
    
    private boolean multiSelect = false;
    private int limiteSeleccion = 0;
    private final List<String> asientosSeleccionados = new ArrayList<>();
    private JToggleButton asientoSeleccionadoActual = null;

    private static final Color COLOR_OCUPADO = new Color(110, 80, 80); // Gris/Rojo tenue

    public interface AsientoSeleccionadoListener {
        void onAsientoSeleccionado(String codigoAsiento);
    }

    public MapaAsientosPanel(String matriculaAvion, AsientoSeleccionadoListener listener) {
        this(matriculaAvion, false, 0, listener);
    }

    public MapaAsientosPanel(String matriculaAvion, boolean multiSelect, int limiteSeleccion, AsientoSeleccionadoListener listener) {
        this.matriculaAvion = matriculaAvion;
        this.multiSelect = multiSelect;
        this.limiteSeleccion = limiteSeleccion;
        this.listener = listener;
        this.pasajeroDAO = new PasajeroDAO();

        setBackground(EstiloUI.FONDO_TARJETA);
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Contenedor interno que tendrá todas las filas apiladas verticalmente
        JPanel contenidoPanel = new JPanel();
        contenidoPanel.setLayout(new BoxLayout(contenidoPanel, BoxLayout.Y_AXIS));
        contenidoPanel.setBackground(EstiloUI.FONDO_TARJETA);
        contenidoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Obtener configuración de distribución
        ConfiguracionDAO confDAO = new ConfiguracionDAO();
        String distribucion = confDAO.obtenerDistribucion(matriculaAvion);
        if (distribucion == null) {
            // Fallback: calcular distribución en base a la capacidad real de la aeronave
            AvionDAO avionDAO = new AvionDAO();
            int capacidad = 150; // valor por defecto
            List<Avion> flota = avionDAO.obtenerAvionesFlota();
            for (Avion a : flota) {
                if (a.getMatricula().equals(matriculaAvion)) {
                    capacidad = a.getCapacidad();
                    break;
                }
            }
            distribucion = skyq.logic.AutoCalculadorCabina.calcularDistribucion(capacidad);
        }

        String[] clases = distribucion.split("\\|");
        String[] letrasAsientos = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M"};
        int filaActual = 1;

        ButtonGroup grupoAsientos = new ButtonGroup();

        for (String clase : clases) {
            String[] partes = clase.split(":");
            if (partes.length != 3) {
                continue;
            }

            String nombreClase = partes[0];
            String distribucionAsientos = partes[1];
            int cantidadFilas = Integer.parseInt(partes[2]);

            String[] columnasBloques = distribucionAsientos.split("-");

            // Cabecera de sección (ej: "--- SECCIÓN VIP ---")
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            headerPanel.setBackground(EstiloUI.FONDO_TARJETA);
            JLabel lblHeader = new JLabel("─── CLASE " + nombreClase + " ───");
            lblHeader.setForeground(EstiloUI.TEXTO_MUTED);
            lblHeader.setFont(new Font("SansSerif", Font.BOLD, 10));
            headerPanel.add(lblHeader);
            contenidoPanel.add(headerPanel);
            contenidoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            for (int f = 0; f < cantidadFilas; f++) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
                rowPanel.setBackground(EstiloUI.FONDO_TARJETA);

                int indiceLetra = 0;

                for (int b = 0; b < columnasBloques.length; b++) {
                    int cols = Integer.parseInt(columnasBloques[b]);

                    for (int c = 0; c < cols; c++) {
                        String codigoAsiento = filaActual + letrasAsientos[indiceLetra % letrasAsientos.length];
                        indiceLetra++;

                        boolean estaOcupado = pasajeroDAO.verificarAsientoOcupadoEnVuelo(codigoAsiento, matriculaAvion);

                        JToggleButton btnAsiento = new JToggleButton(codigoAsiento);
                        btnAsiento.setOpaque(true);
                        btnAsiento.setBorder(EstiloUI.BORDE_COMPONENTE);
                        btnAsiento.setFont(new Font("SansSerif", Font.BOLD, 9));
                        btnAsiento.setPreferredSize(new Dimension(48, 26));

                        if (estaOcupado) {
                            btnAsiento.setBackground(COLOR_OCUPADO);
                            btnAsiento.setForeground(EstiloUI.TEXTO_MUTED);
                            btnAsiento.setEnabled(false);
                        } else {
                            btnAsiento.setBackground(EstiloUI.VERDE_ESMERALDA);
                            btnAsiento.setForeground(EstiloUI.TEXTO_BLANCO);
                            
                            if (!multiSelect) {
                                grupoAsientos.add(btnAsiento);
                            }

                            btnAsiento.addActionListener(e -> {
                                if (multiSelect) {
                                    if (btnAsiento.isSelected()) {
                                        if (limiteSeleccion > 0 && asientosSeleccionados.size() >= limiteSeleccion) {
                                            btnAsiento.setSelected(false);
                                            JOptionPane.showMessageDialog(this, 
                                                    "Límite de selección alcanzado (" + limiteSeleccion + " asientos).", 
                                                    "Información", JOptionPane.WARNING_MESSAGE);
                                        } else {
                                            asientosSeleccionados.add(codigoAsiento);
                                            btnAsiento.setBackground(EstiloUI.AZUL_BRILLANTE);
                                            listener.onAsientoSeleccionado(codigoAsiento);
                                        }
                                    } else {
                                        asientosSeleccionados.remove(codigoAsiento);
                                        btnAsiento.setBackground(EstiloUI.VERDE_ESMERALDA);
                                        listener.onAsientoSeleccionado(codigoAsiento);
                                    }
                                } else {
                                    if (asientoSeleccionadoActual != null) {
                                        asientoSeleccionadoActual.setBackground(EstiloUI.VERDE_ESMERALDA);
                                    }
                                    btnAsiento.setBackground(EstiloUI.AZUL_BRILLANTE);
                                    asientoSeleccionadoActual = btnAsiento;
                                    listener.onAsientoSeleccionado(codigoAsiento);
                                }
                            });
                        }
                        rowPanel.add(btnAsiento);
                    }

                    // Insertar pasillo indicador (con el número de fila) si no es el último bloque
                    if (b < columnasBloques.length - 1) {
                        JLabel lblPasillo = new JLabel(String.valueOf(filaActual), SwingConstants.CENTER);
                        lblPasillo.setForeground(EstiloUI.TEXTO_MUTED);
                        lblPasillo.setFont(new Font("SansSerif", Font.BOLD, 10));
                        lblPasillo.setPreferredSize(new Dimension(28, 26));
                        rowPanel.add(lblPasillo);
                    }
                }

                contenidoPanel.add(rowPanel);
                contenidoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
                filaActual++;
            }
            contenidoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Envolver el contenido en un JScrollPane con scrollbars automáticos
        JScrollPane scroll = new JScrollPane(contenidoPanel);
        scroll.getViewport().setBackground(EstiloUI.FONDO_TARJETA);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll, BorderLayout.CENTER);
    }

    public void setLimiteSeleccion(int limite) {
        this.limiteSeleccion = limite;
    }

    public List<String> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }

    public void limpiarSeleccion() {
        asientosSeleccionados.clear();
        asientoSeleccionadoActual = null;
        deseleccionarBotonesRecursivo(this);
    }

    private void deseleccionarBotonesRecursivo(Container container) {
        for (Component comp : container.getComponents()) {
            switch (comp) {
                case JToggleButton btn -> {
                    if (btn.isEnabled()) {
                        btn.setSelected(false);
                        btn.setBackground(EstiloUI.VERDE_ESMERALDA);
                    }
                }
                case Container childContainer -> deseleccionarBotonesRecursivo(childContainer);
                default -> {}
            }
        }
    }

    public void setSeleccionBloqueada(boolean bloqueada) {
        deshabilitarBotonesRecursivo(this, !bloqueada);
    }

    private void deshabilitarBotonesRecursivo(Container container, boolean enabled) {
        for (Component comp : container.getComponents()) {
            switch (comp) {
                case JToggleButton btn -> {
                    if (btn.getBackground() != COLOR_OCUPADO) {
                        btn.setEnabled(enabled);
                    }
                }
                case Container childContainer -> deshabilitarBotonesRecursivo(childContainer, enabled);
                default -> {}
            }
        }
    }
}