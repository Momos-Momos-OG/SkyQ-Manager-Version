package skyq.view;

import skyq.dao.AvionDAO;
import skyq.model.Avion;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class PanelGerente extends JPanel {

    private JTextField txtMatricula;
    private JTextField txtModelo;
    private JTextField txtCapacidad;
    private JComboBox<String> comboEstado;
    private JButton btnGuardar;

    public PanelGerente() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        JLabel lblMatricula = new JLabel("Matrícula:");
        JLabel lblModelo = new JLabel("Modelo:");
        JLabel lblCapacidad = new JLabel("Capacidad:");
        JLabel lblEstado = new JLabel("Estado:");

        txtMatricula = new JTextField(18);
        txtModelo = new JTextField(18);
        txtCapacidad = new JTextField(18);
        comboEstado = new JComboBox<>(new String[]{"Disponible", "En mantenimiento", "Fuera de servicio"});
        btnGuardar = new JButton("Guardar avión");

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        add(lblMatricula, constraints);

        constraints.gridx = 1;
        add(txtMatricula, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        add(lblModelo, constraints);

        constraints.gridx = 1;
        add(txtModelo, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        add(lblCapacidad, constraints);

        constraints.gridx = 1;
        add(txtCapacidad, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        add(lblEstado, constraints);

        constraints.gridx = 1;
        add(comboEstado, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        add(btnGuardar, constraints);

        btnGuardar.addActionListener(e -> guardarAvion());
    }

    private void guardarAvion() {
        String matricula = txtMatricula.getText().trim();
        String modelo = txtModelo.getText().trim();
        String capacidadTexto = txtCapacidad.getText().trim();
        String estado = (String) comboEstado.getSelectedItem();

        if (matricula.isEmpty() || modelo.isEmpty() || capacidadTexto.isEmpty() || estado == null || estado.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La capacidad debe ser un número entero.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AvionDAO avionDAO = new AvionDAO();

        if (avionDAO.verificarMatriculaRegistrada(matricula)) {
            JOptionPane.showMessageDialog(this, "La matrícula ya está registrada.", "Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Avion avion = new Avion(matricula, modelo, capacidad, estado);

        if (avionDAO.guardarAvion(avion)) {
            JOptionPane.showMessageDialog(this, "Avión guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el avión.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtMatricula.setText("");
        txtModelo.setText("");
        txtCapacidad.setText("");
        comboEstado.setSelectedIndex(0);
    }
}