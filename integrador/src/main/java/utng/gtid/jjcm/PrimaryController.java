package utng.gtid.jjcm;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.repository.AuthenticationRepository;

/**
 * Controlador del inicio de sesión mostrado después de Cerrar sesión.
 */
public class PrimaryController {

    /** Repositorio que valida correo, estado y hash de contraseña. */
    private final AuthenticationRepository authenticationRepository =
            new AuthenticationRepository();

    /** Correo institucional escrito por el usuario. */
    @FXML private TextField txtCorreoLogin;

    /** Contraseña oculta escrita por el usuario. */
    @FXML private PasswordField txtContrasenaLogin;

    /** Mensaje visible de validación sin cuadros adicionales. */
    @FXML private Label lblErrorLogin;

    /** Coloca el correo del administrador para facilitar el primer acceso. */
    @FXML
    private void initialize() {
        txtCorreoLogin.setText("alejandro.herrera@utng.edu.mx");
    }

    /**
     * Comprueba las credenciales y abre Estadísticas cuando son válidas.
     */
    @FXML
    private void iniciarSesion() {
        lblErrorLogin.setText("");
        String email = txtCorreoLogin.getText() == null ? "" : txtCorreoLogin.getText().trim();
        String password = txtContrasenaLogin.getText() == null ? "" : txtContrasenaLogin.getText();
        if (email.isBlank()) {
            lblErrorLogin.setText("Escribe tu correo institucional.");
            return;
        }

        try {
            if (!authenticationRepository.authenticate(email, password)) {
                lblErrorLogin.setText("Correo, contraseña o estado de usuario incorrectos.");
                return;
            }
            App.setRoot("estadisticas");
        } catch (DatabaseException error) {
            lblErrorLogin.setText(error.getMessage());
        } catch (IOException error) {
            lblErrorLogin.setText("No se pudo abrir la pantalla de estadísticas.");
        }
    }
}
