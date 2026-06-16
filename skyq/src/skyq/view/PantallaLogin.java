package skyq.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import skyq.dao.UsuarioDAO;
import skyq.logic.SesionManager;
import skyq.model.Usuario;

public final class PantallaLogin extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JLabel lblEstado;

    public PantallaLogin() {
        initComponents();
    }

    private void initComponents() {
        setTitle("SkyQ - Autenticación");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        setExtendedState(JFrame.NORMAL);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);
        JLabel lblLogo = new JLabel("✈ SkyQ");
        lblLogo.setFont(EstiloUI.FUENTE_TITULO);
        lblLogo.setForeground(EstiloUI.AZUL_ACCENT);
        logoPanel.add(lblLogo);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(EstiloUI.FONDO_DARK_PRINCIPAL);

        // Usuario
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setForeground(EstiloUI.TEXTO_BLANCO);
        lblUsuario.setFont(EstiloUI.FUENTE_LABEL);
        txtUsuario = new JTextField(20);
        txtUsuario.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        txtUsuario.setForeground(EstiloUI.TEXTO_BLANCO);
        txtUsuario.setFont(EstiloUI.FUENTE_LABEL);
        txtUsuario.setBorder(EstiloUI.BORDE_COMPONENTE);
        formPanel.add(lblUsuario);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(txtUsuario);
        formPanel.add(Box.createVerticalStrut(20));

        // Contraseña
        JLabel lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setForeground(EstiloUI.TEXTO_BLANCO);
        lblContrasena.setFont(EstiloUI.FUENTE_LABEL);
        txtContrasena = new JPasswordField(20);
        txtContrasena.setBackground(EstiloUI.GRIS_BOTON_PASIVO);
        txtContrasena.setForeground(EstiloUI.TEXTO_BLANCO);
        txtContrasena.setFont(EstiloUI.FUENTE_LABEL);
        txtContrasena.setBorder(EstiloUI.BORDE_COMPONENTE);
        formPanel.add(lblContrasena);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(txtContrasena);
        formPanel.add(Box.createVerticalStrut(30));

        // Botón
        btnIngresar = new JButton("INGRESAR");
        btnIngresar.setBackground(EstiloUI.AZUL_ACCENT);
        btnIngresar.setForeground(EstiloUI.TEXTO_BLANCO);
        btnIngresar.setFont(EstiloUI.FUENTE_COMPONENTE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorder(EstiloUI.BORDE_COMPONENTE);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.addActionListener(e -> autenticar());
        formPanel.add(btnIngresar);
        formPanel.add(Box.createVerticalStrut(15));

        // Estado
        lblEstado = new JLabel("");
        lblEstado.setForeground(EstiloUI.ROJO_ALERTA);
        lblEstado.setFont(EstiloUI.FUENTE_LABEL);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(lblEstado);

        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void autenticar() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            lblEstado.setText("Ingrese usuario y contraseña");
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuarioAutenticado = dao.autenticar(usuario, contrasena);

        if (usuarioAutenticado != null) {
            SesionManager.getInstance().setUsuarioActual(usuarioAutenticado);
            new VentanaPrincipal().setVisible(true);
            dispose();
        } else {
            lblEstado.setText("Credenciales inválidas");
            txtContrasena.setText("");
        }
    }
}
