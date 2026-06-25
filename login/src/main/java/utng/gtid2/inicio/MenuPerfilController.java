package utng.gtid232.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import utng.gtid232.jjcm.App;

/**
 * Controlador para MenuPerfil.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/MenuPerfilController.java
 *
 * Botones disponibles:
 *   - Ver perfil         → VerPerfil.fxml
 *   - Editar perfil      → EditarPerfil.fxml
 *   - Cambiar contraseña → Alert informativo
 *   - Cerrar sesión      → CerrarSesion.fxml
 */
public class MenuPerfilController {

    // ----------------------------------------------------------------
    // Navegación
    // ----------------------------------------------------------------

    @FXML
    private void onVerPerfil(ActionEvent event) {
        App.navigateTo("VerPerfil.fxml");
    }

    @FXML
    private void onEditarPerfil(ActionEvent event) {
        App.navigateTo("EditarPerfil.fxml");
    }

    @FXML
    private void onCambiarPassword(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Cambiar contraseña");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad de cambio de contraseña próximamente disponible.");
        alert.showAndWait();
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        App.navigateTo("CerrarSesion.fxml");
    }
}
