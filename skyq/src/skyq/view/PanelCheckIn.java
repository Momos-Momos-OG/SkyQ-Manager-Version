package skyq.view;

import skyq.dao.EquipajeDAO;
import skyq.dao.PasajeroDAO;
import skyq.model.Equipaje;
import skyq.model.Pasajero;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;

public class PanelCheckIn extends JPanel {

    private JTextField txtNombre;
    private JTextField txtAsiento;
    private JComboBox<String> comboPrioridad;
    private JTextField txtPesoEquipaje;
    private JButton btnRegistrar;

    public PanelCheckIn() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        JLabel lblNombre = new JLabel("Nombre del pasajero:");
        JLabel lblAsiento = new JLabel("Número de asiento:");
        JLabel lblPrioridad = new JLabel("Prioridad:");
        JLabel lblPeso = new JLabel("Peso del equipaje:");

        txtNombre = new JTextField(18);
        txtAsiento = new JTextField(18);
        comboPrioridad = new JComboBox<>(new String[]{"1", "2", "3"});
        txtPesoEquipaje = new JTextField(18);
        btnRegistrar = new JButton("Registrar Check-In");

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        add(lblNombre, constraints);

        constraints.gridx = 1;
        add(txtNombre, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        add(lblAsiento, constraints);

        constraints.gridx = 1;
        add(txtAsiento, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        add(lblPrioridad, constraints);

        constraints.gridx = 1;
        add(comboPrioridad, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        add(lblPeso, constraints);

        constraints.gridx = 1;
        add(txtPesoEquipaje, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        add(btnRegistrar, constraints);

        btnRegistrar.addActionListener(e -> registrarCheckIn());
    }

    private void registrarCheckIn() {
        String nombre = txtNombre.getText().trim();
        String asiento = txtAsiento.getText().trim();
        String prioridadTexto = (String) comboPrioridad.getSelectedItem();
        String pesoTexto = txtPesoEquipaje.getText().trim();

        if (nombre.isEmpty() || asiento.isEmpty() || prioridadTexto == null || prioridadTexto.trim().isEmpty() || pesoTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int prioridad;
        double peso;

        try {
            prioridad = Integer.parseInt(prioridadTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La prioridad no es válida.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            peso = Double.parseDouble(pesoTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El peso debe ser un número.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PasajeroDAO pasajeroDAO = new PasajeroDAO();
        EquipajeDAO equipajeDAO = new EquipajeDAO();

        if (pasajeroDAO.verificarAsientoOcupado(asiento)) {
            JOptionPane.showMessageDialog(this, "El asiento ya está ocupado.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double pesoMaximo;
        if (prioridad == 1) {
            pesoMaximo = 32.0;
        } else if (prioridad == 2) {
            pesoMaximo = 23.0;
        } else {
            pesoMaximo = 15.0;
        }

        if (peso > pesoMaximo) {
            JOptionPane.showMessageDialog(this, "El equipaje excede el peso permitido para esta prioridad.", "Sobrepeso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pasajero pasajero = new Pasajero();
        pasajero.setNombre(nombre);
        pasajero.setNumAsiento(asiento);
        pasajero.setNivelPrioridad(prioridad);
        pasajero.setTimestampLlegada(LocalDateTime.now());

        int idPasajero = pasajeroDAO.insertarPasajeroYObtenerId(pasajero);
        if (idPasajero <= 0) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el pasajero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Equipaje equipaje = new Equipaje();
        equipaje.setIdPasajero(idPasajero);
        equipaje.setPeso(peso);
        equipaje.setEstado("Aceptado");

        if (equipajeDAO.registrarEquipaje(equipaje)) {
            JOptionPane.showMessageDialog(this, "Check-In registrado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "El pasajero se guardó, pero no fue posible registrar el equipaje.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtAsiento.setText("");
        txtPesoEquipaje.setText("");
        comboPrioridad.setSelectedIndex(0);
    }
}