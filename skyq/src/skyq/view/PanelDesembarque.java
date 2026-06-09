package skyq.view;

import skyq.dao.PasajeroDAO;
import skyq.logic.DesembarqueManager;
import skyq.model.Pasajero;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class PanelDesembarque extends JPanel {

    private JTable tablaDesembarque;
    private DefaultTableModel modeloTabla;
    private JButton btnIniciarDesembarque;

    public PanelDesembarque() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        btnIniciarDesembarque = new JButton("Iniciar Desembarque");
        add(btnIniciarDesembarque, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "Asiento", "Prioridad", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaDesembarque = new JTable(modeloTabla);
        add(new JScrollPane(tablaDesembarque), BorderLayout.CENTER);

        btnIniciarDesembarque.addActionListener(e -> iniciarDesembarque());
    }

    private void iniciarDesembarque() {
        PasajeroDAO pasajeroDAO = new PasajeroDAO();
        DesembarqueManager desembarqueManager = new DesembarqueManager();

        List<Pasajero> pasajeros = pasajeroDAO.obtenerPasajerosVuelo();
        List<Pasajero> pasajerosOrdenados = desembarqueManager.ordenarPorAsiento(pasajeros);

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