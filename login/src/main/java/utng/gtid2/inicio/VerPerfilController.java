package utng.gtid232.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import utng.gtid232.jjcm.App;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para VerPerfil.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/VerPerfilController.java
 *
 * fx:id disponibles: lblNombre, lblCorreo, lblTelefono, lblRol, lblFecha
 * Botones: Volver → MenuPerfil | Editar perfil → EditarPerfil
 */
public class VerPerfilController implements Initializable {

    // ----------------------------------------------------------------
    // fx:id — deben coincidir exactamente con los del FXML
    // ----------------------------------------------------------------

    @FXML private Label lblNombre;
    @FXML private Label lblCorreo;
    @FXML private Label lblTelefono;
    @FXML private Label lblRol;
    @FXML private Label lblFecha;

    // ----------------------------------------------------------------
    // Inicialización — aquí podrías cargar datos desde un modelo/BD
    // ----------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Los valores por defecto ya están en el FXML.
        // Cuando integres tu capa de datos, sustituye aquí:
        // Usuario u = SessionManager.getUsuarioActual();
        // lblNombre.setText(u.getNombre());
        // lblCorreo.setText(u.getCorreo());
        // etc.
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onBack(ActionEvent event) {
        App.navigateTo("MenuPerfil.fxml");
    }

    @FXML
    private void onEditarPerfil(ActionEvent event) {
        App.navigateTo("EditarPerfil.fxml");
    }
}
