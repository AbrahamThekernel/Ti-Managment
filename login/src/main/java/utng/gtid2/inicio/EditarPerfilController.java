package utng.gtid232.inicio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import utng.gtid232.jjcm.App;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para EditarPerfil.fxml
 *
 * Ubicación: src/main/java/utng/gtid232/jjcm/EditarPerfilController.java
 *
 * fx:id disponibles: txtNombre, txtCorreo, txtTelefono, txtRol
 * Botones: Cancelar → MenuPerfil | Guardar → Alert de éxito
 */
public class EditarPerfilController implements Initializable {

    // ----------------------------------------------------------------
    // fx:id — deben coincidir exactamente con los del FXML
    // ----------------------------------------------------------------

    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtRol;

    // ----------------------------------------------------------------
    // Inicialización
    // ----------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Los valores por defecto ya están en el FXML.
        // Cuando integres tu capa de datos, carga aquí los valores reales:
        // Usuario u = SessionManager.getUsuarioActual();
        // txtNombre.setText(u.getNombre());
        // txtCorreo.setText(u.getCorreo());
        // etc.
    }

    // ----------------------------------------------------------------
    // Eventos de botones
    // ----------------------------------------------------------------

    @FXML
    private void onGuardar(ActionEvent event) {
        // Validación básica de campos vacíos
        if (txtNombre.getText().isBlank()
                || txtCorreo.getText().isBlank()
                || txtTelefono.getText().isBlank()) {
            Alert error = new Alert(AlertType.WARNING);
            error.setTitle("Campos incompletos");
            error.setHeaderText(null);
            error.setContentText("Por favor completa todos los campos antes de guardar.");
            error.showAndWait();
            return;
        }

        // Aquí iría la lógica para persistir los cambios:
        // SessionManager.getUsuarioActual().setNombre(txtNombre.getText());
        // UsuarioDAO.actualizar(...);

        Alert ok = new Alert(AlertType.INFORMATION);
        ok.setTitle("Perfil actualizado");
        ok.setHeaderText(null);
        ok.setContentText("Los cambios se guardaron correctamente.");
        ok.showAndWait();

        App.navigateTo("MenuPerfil.fxml");
    }

    @FXML
    private void onBack(ActionEvent event) {
        App.navigateTo("MenuPerfil.fxml");
    }
}
