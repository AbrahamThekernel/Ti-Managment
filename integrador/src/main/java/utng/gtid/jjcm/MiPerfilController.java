package utng.gtid.jjcm;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utng.gtid.jjcm.database.DatabaseException;
import utng.gtid.jjcm.model.UserView;
import utng.gtid.jjcm.repository.UserRepository;
import utng.gtid.jjcm.security.PasswordHasher;
import utng.gtid.jjcm.session.SessionContext;

/**
 * Controlador del perfil del administrador utilizado por la sesión actual.
 */
public class MiPerfilController extends NavigationController {

    /** Repositorio que lee y actualiza la tabla usuarios. */
    private final UserRepository userRepository = new UserRepository();

    /** Usuario cargado; conserva su id y rol al guardar cambios. */
    private UserView currentUser;

    /** Campos de información personal definidos en mi-perfil.fxml. */
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtPuesto;
    @FXML private TextField txtDepartamento;

    /** Datos visibles en la tarjeta izquierda del perfil. */
    @FXML private Label lblNombrePerfil;
    @FXML private Label lblRolPerfil;
    @FXML private Label lblCorreoPerfil;

    /** Campos de contraseña; PasswordField oculta los caracteres escritos. */
    @FXML private PasswordField txtContrasenaActual;
    @FXML private PasswordField txtNuevaContrasena;
    @FXML private PasswordField txtConfirmarContrasena;

    /** Botones normales de edición, guardado y cancelación. */
    @FXML private Button btnEditarPerfil;
    @FXML private Button btnGuardarPerfil;
    @FXML private Button btnCancelarPerfil;
    @FXML private Button btnCambiarContrasena;
    @FXML private Button btnAlternarNotificacionesCorreo;
    @FXML private Button btnAlternarResumenSemanal;
    @FXML private Button btnCerrarOtrasSesiones;
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnEliminarFoto;

    /** Configura todos los botones y recupera el perfil desde PostgreSQL. */
    @FXML
    private void initialize() {
        btnEditarPerfil.setOnAction(event -> setEditing(true));
        btnGuardarPerfil.setOnAction(event -> saveProfile());
        btnCancelarPerfil.setOnAction(event -> loadProfile());
        btnCambiarContrasena.setOnAction(event -> changePassword());

        // Estas preferencias visuales cambian entre activado y desactivado.
        btnAlternarNotificacionesCorreo.setOnAction(event -> toggleButton(btnAlternarNotificacionesCorreo));
        btnAlternarResumenSemanal.setOnAction(event -> toggleButton(btnAlternarResumenSemanal));
        btnCerrarOtrasSesiones.setOnAction(event -> showInformation(
                "Sesiones cerradas", "No existen otras sesiones activas."
        ));

        // La maqueta no incluye una columna para fotografías; se informa con claridad.
        btnCambiarFoto.setOnAction(event -> showInformation(
                "Fotografía de perfil",
                "La información personal sí se guarda en PostgreSQL; la fotografía requiere almacenamiento de archivos."
        ));
        btnEliminarFoto.setOnAction(event -> showInformation(
                "Fotografía de perfil", "No hay una fotografía almacenada para eliminar."
        ));

        loadProfile();
    }

    /** Consulta la cuenta autenticada y llena todos los campos. */
    private void loadProfile() {
        try {
            Long sessionUserId = SessionContext.getUserId();
            if (sessionUserId == null) {
                throw new DatabaseException("No existe una sesión activa.");
            }

            currentUser = userRepository.findById(sessionUserId);
            txtNombre.setText(currentUser.getName());
            txtApellidos.setText(currentUser.getLastName());
            txtCorreo.setText(currentUser.getEmail());
            txtTelefono.setText(currentUser.getPhone());
            txtPuesto.setText(currentUser.getPosition());
            txtDepartamento.setText(currentUser.getDepartment());

            // La tarjeta y el pie del menú muestran la misma cuenta autenticada.
            lblNombrePerfil.setText(currentUser.getFullName());
            lblRolPerfil.setText(currentUser.getRole());
            lblCorreoPerfil.setText(currentUser.getEmail());
            SessionContext.updateIdentity(
                    currentUser.getName(),
                    currentUser.getLastName(),
                    currentUser.getRole()
            );
            actualizarIdentidadSesion();
            setEditing(false);
        } catch (DatabaseException error) {
            showError("No se pudo cargar el perfil", error.getMessage());
        }
    }

    /** Habilita o bloquea únicamente los campos editables. */
    private void setEditing(boolean editing) {
        txtNombre.setDisable(!editing);
        txtApellidos.setDisable(!editing);
        txtCorreo.setDisable(!editing);
        txtTelefono.setDisable(!editing);
        txtPuesto.setDisable(!editing);
        txtDepartamento.setDisable(!editing);
        btnGuardarPerfil.setDisable(!editing);
        btnCancelarPerfil.setDisable(!editing);
        btnEditarPerfil.setDisable(editing);
    }

    /** Valida y guarda la información personal conservando el mismo rol. */
    private void saveProfile() {
        if (currentUser == null) {
            return;
        }
        if (txtNombre.getText().isBlank() || txtApellidos.getText().isBlank()) {
            showError("Datos incorrectos", "Nombre y apellidos son obligatorios.");
            return;
        }
        if (!txtCorreo.getText().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showError("Datos incorrectos", "Escribe un correo válido.");
            return;
        }

        try {
            userRepository.update(
                    currentUser.getId(),
                    currentUser.getRoleId(),
                    txtNombre.getText().trim(),
                    txtApellidos.getText().trim(),
                    txtCorreo.getText().trim().toLowerCase(),
                    txtTelefono.getText(),
                    txtPuesto.getText(),
                    txtDepartamento.getText()
            );
            loadProfile();
            showInformation("Perfil actualizado", "Los cambios se guardaron en PostgreSQL.");
        } catch (DatabaseException error) {
            showError("No se pudo guardar el perfil", error.getMessage());
        }
    }

    /**
     * Comprueba la contraseña actual y guarda un hash PBKDF2 de la nueva.
     */
    private void changePassword() {
        if (currentUser == null) {
            return;
        }
        String newPassword = txtNuevaContrasena.getText();
        if (newPassword == null || newPassword.length() < 8) {
            showError("Contraseña no válida", "La nueva contraseña debe tener al menos 8 caracteres.");
            return;
        }
        if (!newPassword.equals(txtConfirmarContrasena.getText())) {
            showError("Contraseña no válida", "La confirmación no coincide.");
            return;
        }

        try {
            String storedHash = userRepository.findPasswordHash(currentUser.getId());
            boolean hasPassword = storedHash != null && !storedHash.isBlank();

            // En la primera configuración no existe hash y se permite crear la clave.
            if (hasPassword && !PasswordHasher.verify(txtContrasenaActual.getText(), storedHash)) {
                showError("Contraseña incorrecta", "La contraseña actual no es correcta.");
                return;
            }

            userRepository.updatePassword(currentUser.getId(), PasswordHasher.hash(newPassword));
            clearPasswordFields();
            showInformation("Contraseña actualizada", "La nueva contraseña se guardó de forma segura.");
        } catch (DatabaseException error) {
            showError("No se pudo cambiar la contraseña", error.getMessage());
        }
    }

    /** Borra los PasswordField después de una operación correcta. */
    private void clearPasswordFields() {
        txtContrasenaActual.clear();
        txtNuevaContrasena.clear();
        txtConfirmarContrasena.clear();
    }

    /** Alterna el texto de una preferencia visual. */
    private void toggleButton(Button button) {
        boolean enabled = button.getText().startsWith("Activad");
        button.setText(enabled ? "Desactivado" : "Activado");
    }

    /** Muestra confirmaciones de operaciones correctas. */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Muestra errores de validación o persistencia. */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
