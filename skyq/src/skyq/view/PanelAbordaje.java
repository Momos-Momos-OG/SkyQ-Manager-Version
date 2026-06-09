package skyq.view;

import skyq.dao.PasajeroDAO;
import skyq.logic.ColaAbordaje;
import skyq.model.Pasajero;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class PanelAbordaje extends JPanel {

    private JTable tablaPasajeros;
    private DefaultTableModel modeloTabla;
    private JButton btnCargarCola;

    public PanelAbordaje() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        btnCargarCola = new JButton("Cargar Cola de Abordaje");
        add(btnCargarCola, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Asiento", "Prioridad", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaPasajeros = new JTable(modeloTabla);
        add(new JScrollPane(tablaPasajeros), BorderLayout.CENTER);

        btnCargarCola.addActionListener(e -> cargarColaAbordaje());
    }

    private void cargarColaAbordaje() {
        PasajeroDAO pasajeroDAO = new PasajeroDAO();
        ColaAbordaje colaAbordaje = new ColaAbordaje();

        List<Pasajero> pasajeros = pasajeroDAO.obtenerPasajerosVuelo();
        List<Pasajero> pasajerosOrdenados = colaAbordaje.organizarPasajeros(pasajeros);

        modeloTabla.setRowCount(0);

        for (Pasajero pasajero : pasajerosOrdenados) {
            modeloTabla.addRow(new Object[]{
                    pasajero.getIdPasajero(),
                    pasajero.getNombre(),
                    pasajero.getNumAsiento(),
                    pasajero.getNivelPrioridad(),
                    pasajero.getTimestampLlegada()
            });
        }
    }
}