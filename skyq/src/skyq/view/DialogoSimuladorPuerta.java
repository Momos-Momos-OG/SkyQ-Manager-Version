package skyq.view;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.PriorityQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import skyq.dao.EquipajeDAO;
import skyq.dao.PasajeroDAO;
import skyq.dao.VueloDAO;
import skyq.model.Equipaje;
import skyq.model.Pasajero;
import skyq.model.Vuelo;
import skyq.services.AbordajeService;
import skyq.services.EquipajeService;
import skyq.services.DesembarqueService;

public final class DialogoSimuladorPuerta extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JComboBox<String> comboVuelos;
    private transient List<Vuelo> vuelosCargados;

    // Columna 1: Abordaje
    private DefaultListModel<String> modeloAbordaje;
    private JList<String> listAbordaje;

    // Columna 2: Bodega
    private DefaultListModel<String> modeloBodega;
    private JList<String> listBodega;
    private DefaultListModel<String> modeloDescargados;
    private JList<String> listDescargados;
    private transient Stack<Equipaje> bodegaStack;

    // Columna 3: Desembarque
    private DefaultListModel<String> modeloDesembarque;
    private JList<String> listDesembarque;

    private final transient VueloDAO vueloDAO = new VueloDAO();
    private final transient PasajeroDAO pasajeroDAO = new PasajeroDAO();
    private final transient EquipajeDAO equipajeDAO = new EquipajeDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public DialogoSimuladorPuerta(Frame padre) {
        super(padre, "Simulador de Puerta de Embarque y Bodega", true);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(padre);
        getContentPane().setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        setLayout(new BorderLayout());

        // Barra de título personalizada
        add(new PanelBarraTitulo(this, "🚀  Simulador de Estructuras de Datos (SRS) — Puerta & Bodega", false), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // ==========================================
        // 🛫 SECTOR NORTE: SELECCIÓN DE VUELO
        // ==========================================
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        selectorPanel.setBackground(EstiloUI.FONDO_TARJETA);
        selectorPanel.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(5, 10, 5, 10)));

        JLabel lblVuelo = new JLabel("SELECCIONE VUELO PARA SIMULACIÓN:");
        lblVuelo.setForeground(Color.CYAN);
        lblVuelo.setFont(EstiloUI.FUENTE_SUBTITULO);

        comboVuelos = new JComboBox<>();
        comboVuelos.setPreferredSize(new Dimension(450, 32));
        comboVuelos.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        comboVuelos.setForeground(EstiloUI.TEXTO_BLANCO);
        comboVuelos.addActionListener(e -> alCambiarVuelo());

        selectorPanel.add(lblVuelo);
        selectorPanel.add(comboVuelos);
        mainPanel.add(selectorPanel, BorderLayout.NORTH);

        // ==========================================
        // 🎛️ SECTOR CENTRAL: COLUMNAS DE SIMULACIÓN
        // ==========================================
        JPanel gridPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        gridPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        // --- COLUMNA 1: ABORDAJE (PriorityQueue) ---
        gridPanel.add(crearColumnaAbordaje());

        // --- COLUMNA 2: BODEGA (Stack / LIFO) ---
        gridPanel.add(crearColumnaBodega());

        // --- COLUMNA 3: DESEMBARQUE (Bubble Sort) ---
        gridPanel.add(crearColumnaDesembarque());

        mainPanel.add(gridPanel, BorderLayout.CENTER);

        // ==========================================
        // 🚨 SECTOR SUR: BOTÓN CERRAR
        // ==========================================
        JPanel surPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        surPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        JButton btnCerrar = new JButton("✕  CERRAR SIMULADOR");
        btnCerrar.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        btnCerrar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnCerrar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setPreferredSize(new Dimension(180, 38));
        EstiloUI.aplicarHover(btnCerrar, EstiloUI.GRIS_BOTON_PASIVO, new Color(55, 62, 71));
        btnCerrar.addActionListener(e -> dispose());
        surPanel.add(btnCerrar);
        mainPanel.add(surPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        cargarVuelos();
    }

    private JPanel crearColumnaAbordaje() {
        JPanel col = new JPanel(new BorderLayout(10, 10));
        col.setBackground(EstiloUI.FONDO_TARJETA);
        col.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(12, 12, 12, 12)));

        JLabel lblTitulo = new JLabel("1. ABORDAJE (PriorityQueue)", SwingConstants.CENTER);
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);

        modeloAbordaje = new DefaultListModel<>();
        listAbordaje = new JList<>(modeloAbordaje);
        listAbordaje.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        listAbordaje.setForeground(EstiloUI.TEXTO_BLANCO);
        listAbordaje.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(listAbordaje);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(48, 54, 61)));

        JButton btnAbordar = new JButton("🛫 INICIAR ABORDAJE");
        btnAbordar.setBackground(EstiloUI.AZUL_ACCENT);
        btnAbordar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnAbordar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnAbordar.setBorderPainted(false);
        EstiloUI.aplicarHover(btnAbordar, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());
        btnAbordar.addActionListener(e -> simularAbordaje());

        col.add(lblTitulo, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        col.add(btnAbordar, BorderLayout.SOUTH);

        return col;
    }

    private JPanel crearColumnaBodega() {
        JPanel col = new JPanel(new BorderLayout(10, 10));
        col.setBackground(EstiloUI.FONDO_TARJETA);
        col.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(12, 12, 12, 12)));

        JLabel lblTitulo = new JLabel("2. BODEGA (Stack LIFO)", SwingConstants.CENTER);
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);

        // Panel dividido para la Bodega (Pila) y Maletas Descargadas
        JPanel splitPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        splitPanel.setBackground(EstiloUI.FONDO_TARJETA);

        // Bodega (Pila)
        JPanel pilaPanel = new JPanel(new BorderLayout(5, 5));
        pilaPanel.setBackground(EstiloUI.FONDO_TARJETA);
        pilaPanel.add(new JLabel("PILA EN BODEGA (Tope arriba):", SwingConstants.LEFT) {{
            setForeground(EstiloUI.TEXTO_MUTED);
            setFont(new Font("SansSerif", Font.BOLD, 10));
        }}, BorderLayout.NORTH);

        modeloBodega = new DefaultListModel<>();
        listBodega = new JList<>(modeloBodega);
        listBodega.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        listBodega.setForeground(Color.YELLOW);
        listBodega.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollPila = new JScrollPane(listBodega);
        scrollPila.setBorder(BorderFactory.createLineBorder(new Color(48, 54, 61)));
        pilaPanel.add(scrollPila, BorderLayout.CENTER);

        // Descargadas
        JPanel descargadosPanel = new JPanel(new BorderLayout(5, 5));
        descargadosPanel.setBackground(EstiloUI.FONDO_TARJETA);
        descargadosPanel.add(new JLabel("DESCARGADOS (LIFO - Primero en Salir):", SwingConstants.LEFT) {{
            setForeground(EstiloUI.TEXTO_MUTED);
            setFont(new Font("SansSerif", Font.BOLD, 10));
        }}, BorderLayout.NORTH);

        modeloDescargados = new DefaultListModel<>();
        listDescargados = new JList<>(modeloDescargados);
        listDescargados.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        listDescargados.setForeground(Color.GREEN);
        listDescargados.setFont(new Font("Monospaced", Font.PLAIN, 10));
        JScrollPane scrollDesc = new JScrollPane(listDescargados);
        scrollDesc.setBorder(BorderFactory.createLineBorder(new Color(48, 54, 61)));
        descargadosPanel.add(scrollDesc, BorderLayout.CENTER);

        splitPanel.add(pilaPanel);
        splitPanel.add(descargadosPanel);

        // Panel de botones
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setBackground(EstiloUI.FONDO_TARJETA);

        JButton btnCargar = new JButton("📥 CARGAR");
        btnCargar.setBackground(EstiloUI.AZUL_ACCENT);
        btnCargar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnCargar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnCargar.setBorderPainted(false);
        EstiloUI.aplicarHover(btnCargar, EstiloUI.AZUL_ACCENT, EstiloUI.AZUL_ACCENT.brighter());
        btnCargar.addActionListener(e -> simularCargaBodega());

        JButton btnDescargar = new JButton("📤 DESCARGAR");
        btnDescargar.setBackground(EstiloUI.VERDE_NEON);
        btnDescargar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnDescargar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnDescargar.setBorderPainted(false);
        EstiloUI.aplicarHover(btnDescargar, EstiloUI.VERDE_NEON, EstiloUI.VERDE_NEON.brighter());
        btnDescargar.addActionListener(e -> simularDescargaBodega());

        btns.add(btnCargar);
        btns.add(btnDescargar);

        col.add(lblTitulo, BorderLayout.NORTH);
        col.add(splitPanel, BorderLayout.CENTER);
        col.add(btns, BorderLayout.SOUTH);

        return col;
    }

    private JPanel crearColumnaDesembarque() {
        JPanel col = new JPanel(new BorderLayout(10, 10));
        col.setBackground(EstiloUI.FONDO_TARJETA);
        col.setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.BORDE_COMPONENTE, new EmptyBorder(12, 12, 12, 12)));

        JLabel lblTitulo = new JLabel("3. DESEMBARQUE (Bubble Sort)", SwingConstants.CENTER);
        lblTitulo.setForeground(EstiloUI.TEXTO_BLANCO);
        lblTitulo.setFont(EstiloUI.FUENTE_SUBTITULO);

        modeloDesembarque = new DefaultListModel<>();
        listDesembarque = new JList<>(modeloDesembarque);
        listDesembarque.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        listDesembarque.setForeground(EstiloUI.TEXTO_BLANCO);
        listDesembarque.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(listDesembarque);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(48, 54, 61)));

        JButton btnDesembarcar = new JButton("🛬 SIMULAR DESEMBARQUE");
        btnDesembarcar.setBackground(EstiloUI.VERDE_NEON);
        btnDesembarcar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnDesembarcar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnDesembarcar.setBorderPainted(false);
        EstiloUI.aplicarHover(btnDesembarcar, EstiloUI.VERDE_NEON, EstiloUI.VERDE_NEON.brighter());
        btnDesembarcar.addActionListener(e -> simularDesembarque());

        col.add(lblTitulo, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        col.add(btnDesembarcar, BorderLayout.SOUTH);

        return col;
    }

    private void cargarVuelos() {
        comboVuelos.removeAllItems();
        vuelosCargados = vueloDAO.obtenerTodosLosVuelos();
        for (Vuelo v : vuelosCargados) {
            comboVuelos.addItem("Vuelo #" + v.getIdVuelo() + " \u2014 " + v.getMatricula()
                    + " (" + (v.getModeloAvion() != null ? v.getModeloAvion() : "Avión") + ") \u2014 "
                    + (v.getFechaSalida() != null ? v.getFechaSalida().format(FMT) : "Sin fecha"));
        }
        if (vuelosCargados.isEmpty()) {
            comboVuelos.addItem("No hay vuelos programados");
        }
    }

    private void alCambiarVuelo() {
        modeloAbordaje.clear();
        modeloBodega.clear();
        modeloDescargados.clear();
        modeloDesembarque.clear();
        bodegaStack = null;
    }

    private void simularAbordaje() {
        modeloAbordaje.clear();
        int sel = comboVuelos.getSelectedIndex();
        if (sel < 0 || vuelosCargados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un vuelo válido.", "Simulación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vuelo v = vuelosCargados.get(sel);
        List<Pasajero> todos = pasajeroDAO.obtenerPasajerosPorVuelo(v.getMatricula());

        // Filtrar solo pasajeros que tienen check-in realizado (timestampLlegada != null)
        List<Pasajero> conCheckIn = new ArrayList<>();
        for (Pasajero p : todos) {
            if (p.getTimestampLlegada() != null) {
                conCheckIn.add(p);
            }
        }

        if (conCheckIn.isEmpty()) {
            modeloAbordaje.addElement("  Sin pasajeros registrados");
            modeloAbordaje.addElement("  en Check-in para este vuelo.");
            return;
        }

        // Crear Cola de Prioridad
        PriorityQueue<Pasajero> cola = AbordajeService.crearColaAbordaje(conCheckIn);
        int num = 1;
        while (!cola.isEmpty()) {
            Pasajero p = cola.poll();
            String clase = (p.getNivelPrioridad() == 1 || p.isUpgrade()) ? "VIP/Up" : (p.getNivelPrioridad() == 2 ? "Ejecutiva" : "Económica");
            String silla = p.isSillaRuedas() ? "♿ Silla" : "Estándar";
            String item = String.format("%02d. [%-7s | %-8s] Seat:%-3s %s",
                    num++, clase, silla, p.getNumAsiento(), p.getNombre());
            modeloAbordaje.addElement(item);
        }
    }

    private void simularCargaBodega() {
        modeloBodega.clear();
        modeloDescargados.clear();
        int sel = comboVuelos.getSelectedIndex();
        if (sel < 0 || vuelosCargados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un vuelo válido.", "Simulación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vuelo v = vuelosCargados.get(sel);
        List<Equipaje> maletas = equipajeDAO.obtenerEquipajePorVuelo(v.getMatricula());

        if (maletas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se registraron equipajes para los pasajeros de este vuelo.", "Pila Vacía", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Cargar en la pila (LIFO)
        bodegaStack = EquipajeService.cargarBodega(maletas);

        // Mostrar en la lista visual (desde el tope hacia abajo)
        // Para previsualizar el Stack en orden LIFO, vaciamos una copia
        Stack<Equipaje> copia = new Stack<>();
        copia.addAll(bodegaStack);

        int pos = 1;
        while (!copia.isEmpty()) {
            Equipaje eq = copia.pop();
            String item = String.format("[%02d] Maleta #%-4d (Peso: %-4.1f kg)",
                    pos++, eq.getIdMaleta(), eq.getPeso());
            modeloBodega.addElement(item);
        }
        JOptionPane.showMessageDialog(this, "¡Se cargaron " + maletas.size() + " maletas en la pila de la bodega!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void simularDescargaBodega() {
        if (bodegaStack == null || bodegaStack.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La bodega está vacía o ya fue descargada por completo.", "Simulación Stack", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Simular descarga de 1 maleta interactiva por clic (demostrando el LIFO en tiempo real)
        Equipaje eq = bodegaStack.pop();

        // Remover tope del modelo visual de Bodega (primer elemento)
        if (!modeloBodega.isEmpty()) {
            modeloBodega.remove(0);
        }

        // Añadir al modelo de descargados en la primera posición para mostrar el último en entrar como el primero en salir
        String item = String.format("Maleta #%-4d (Peso: %-4.1f kg) <- Salida", eq.getIdMaleta(), eq.getPeso());
        modeloDescargados.insertElementAt(item, 0);
    }

    private void simularDesembarque() {
        modeloDesembarque.clear();
        int sel = comboVuelos.getSelectedIndex();
        if (sel < 0 || vuelosCargados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un vuelo válido.", "Simulación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vuelo v = vuelosCargados.get(sel);
        List<Pasajero> todos = pasajeroDAO.obtenerPasajerosPorVuelo(v.getMatricula());

        if (todos.isEmpty()) {
            modeloDesembarque.addElement("  No hay pasajeros en este vuelo.");
            return;
        }

        // Ordenamiento por Bubble Sort manual
        List<Pasajero> ordenados = DesembarqueService.ordenarDesembarque(todos);

        int pos = 1;
        for (Pasajero p : ordenados) {
            String clase = (p.getNivelPrioridad() == 1 || p.isUpgrade()) ? "VIP" : (p.getNivelPrioridad() == 2 ? "Ejec" : "Econ");
            String item = String.format("%02d. [Seat: %-3s | Class: %-4s] %s",
                    pos++, p.getNumAsiento(), clase, p.getNombre());
            modeloDesembarque.addElement(item);
        }
    }
}
