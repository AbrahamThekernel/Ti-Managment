package utng.gtid232.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utng.gtid232.jjcm.App;

/**
 * Controlador para CerrarSesion.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/CerrarSesionController.java
 *
 * Botones:
 *   - Cancelar              → MenuPerfil.fxml
 *   - Sí, cerrar sesión     → cierra la aplicación
 */
public class CerrarSesionController {

    @FXML
    private void onCancelar(ActionEvent event) {
        App.navigateTo("MenuPerfil.fxml");
    }

    @FXML
    private void onConfirmarCerrarSesion(ActionEvent event) {
        App.exit();
    }
}
